package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Cotizaciones de índices, ETFs y materias primas vía el endpoint de gráficos
 * de Yahoo Finance (no oficial pero de acceso libre, sin registro ni clave;
 * como cualquier API no documentada puede cambiar sin aviso).
 */
object MarketQuoteClient {
    data class Quote(
        val label: String,
        val symbol: String,
        val price: Double,
        val currency: String,
        val changePercent: Double?,
        val last5Days: List<Double>,
    )

    private val INSTRUMENTS = listOf(
        "BTC-USD" to "BTC/USD",
        "ETH-USD" to "ETH/USD",
        "GC=F" to "Oro (XAU/USD aprox.)",
        "^NDX" to "Nasdaq 100",
        "IWDA.AS" to "IWDA",
        "EIMI.L" to "EIMI",
        "IS3S.DE" to "IS3S",
        "PPFB.DE" to "PPFB",
        "2B7A.DE" to "2B7A",
    )

    suspend fun fetchAll(): List<Quote> = coroutineScope {
        INSTRUMENTS.map { (symbol, label) -> async { fetchOne(symbol, label) } }.awaitAll().filterNotNull()
    }

    private suspend fun fetchOne(symbol: String, label: String): Quote? = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = URLEncoder.encode(symbol, "UTF-8")
            val conn = URL("https://query1.finance.yahoo.com/v8/finance/chart/$encoded?range=5d&interval=1d").openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            val body = try {
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
            val result = Json.parseToJsonElement(body).jsonObject["chart"]?.jsonObject
                ?.get("result")?.jsonArray?.firstOrNull()?.jsonObject ?: return@withContext null
            val meta = result["meta"]?.jsonObject ?: return@withContext null
            val price = meta["regularMarketPrice"]?.jsonPrimitive?.doubleOrNull ?: return@withContext null
            val currency = meta["currency"]?.jsonPrimitive?.content ?: ""
            val changePercent = meta["regularMarketChangePercent"]?.jsonPrimitive?.doubleOrNull
            val closes = result["indicators"]?.jsonObject?.get("quote")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("close")?.jsonArray?.mapNotNull { it.jsonPrimitive.doubleOrNull } ?: emptyList()

            Quote(label = label, symbol = symbol, price = price, currency = currency, changePercent = changePercent, last5Days = closes)
        }.getOrNull()
    }
}
