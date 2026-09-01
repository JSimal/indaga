package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class MarketChartResponse(val prices: List<List<Double>> = emptyList())

/** Cotización en tiempo real + histórico de 7 días de BTC/ETH, vía CoinGecko (gratis, sin key). */
object CryptoTickerClient {

    data class CoinTicker(val name: String, val symbol: String, val priceEur: Double, val last7Days: List<Double>)

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(): Result<List<CoinTicker>> = coroutineScope {
        runCatching {
            val btcJob = async { fetchOne("bitcoin", "BTC") }
            val ethJob = async { fetchOne("ethereum", "ETH") }
            listOfNotNull(btcJob.await(), ethJob.await())
        }
    }

    private suspend fun fetchOne(id: String, symbol: String): CoinTicker? = withContext(Dispatchers.IO) {
        runCatching {
            val priceConn = URL("https://api.coingecko.com/api/v3/simple/price?ids=$id&vs_currencies=eur").openConnection() as HttpURLConnection
            priceConn.connectTimeout = 8000
            priceConn.readTimeout = 8000
            val priceBody = try {
                priceConn.inputStream.bufferedReader().readText()
            } finally {
                priceConn.disconnect()
            }
            val price = (Json.parseToJsonElement(priceBody).jsonObject[id] as? JsonObject)
                ?.get("eur")?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@withContext null

            val chartConn = URL("https://api.coingecko.com/api/v3/coins/$id/market_chart?vs_currency=eur&days=7").openConnection() as HttpURLConnection
            chartConn.connectTimeout = 8000
            chartConn.readTimeout = 8000
            val chartBody = try {
                chartConn.inputStream.bufferedReader().readText()
            } finally {
                chartConn.disconnect()
            }
            val prices = json.decodeFromString(MarketChartResponse.serializer(), chartBody).prices.map { it[1] }

            CoinTicker(name = id.replaceFirstChar { it.uppercase() }, symbol = symbol, priceEur = price, last7Days = prices)
        }.getOrNull()
    }
}
