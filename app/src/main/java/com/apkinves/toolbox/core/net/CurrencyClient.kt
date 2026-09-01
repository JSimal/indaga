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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
private data class FrankfurterResponse(val rates: Map<String, Double> = emptyMap())

/**
 * Conversión EUR -> USD (Frankfurter, tipos del BCE, gratis y sin key) y
 * EUR -> USDT/USDC (CoinGecko, gratis y sin key). Si se pide una fecha
 * pasada, USD tiene histórico completo; USDT/USDC en el plan gratuito de
 * CoinGecko solo tienen histórico de los últimos 365 días.
 */
object CurrencyClient {

    data class ConversionResult(
        val amountEur: Double,
        val usd: Double?,
        val usdt: Double?,
        val usdc: Double?,
        val dateUsed: String,
        val note: String?,
    )

    private val json = Json { ignoreUnknownKeys = true }
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd
    private val coinGeckoDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    suspend fun convert(amountEur: Double, date: LocalDate?): ConversionResult = coroutineScope {
        val moreThanAYearAgo = date != null && date.isBefore(LocalDate.now().minusDays(365))

        val usdJob = async { fetchUsdRate(date) }
        val usdtJob = async { if (moreThanAYearAgo) null else fetchCoinPrice("tether", date) }
        val usdcJob = async { if (moreThanAYearAgo) null else fetchCoinPrice("usd-coin", date) }

        val usdRate = usdJob.await()
        val usdtRate = usdtJob.await()
        val usdcRate = usdcJob.await()

        ConversionResult(
            amountEur = amountEur,
            usd = usdRate?.let { amountEur * it },
            usdt = usdtRate?.let { amountEur / it },
            usdc = usdcRate?.let { amountEur / it },
            dateUsed = date?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter),
            note = if (moreThanAYearAgo) "USDT/USDC no disponibles: CoinGecko (gratis) solo da histórico de los últimos 365 días." else null,
        )
    }

    private suspend fun fetchUsdRate(date: LocalDate?): Double? = withContext(Dispatchers.IO) {
        runCatching {
            val path = date?.format(dateFormatter) ?: "latest"
            val conn = URL("https://api.frankfurter.app/$path?from=EUR&to=USD").openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.instanceFollowRedirects = true
            val body = try {
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
            json.decodeFromString(FrankfurterResponse.serializer(), body).rates["USD"]
        }.getOrNull()
    }

    /** Precio de 1 unidad de la criptomoneda en EUR, a fecha actual o pasada. */
    private suspend fun fetchCoinPrice(coinId: String, date: LocalDate?): Double? = withContext(Dispatchers.IO) {
        runCatching {
            val url = if (date == null) {
                "https://api.coingecko.com/api/v3/simple/price?ids=$coinId&vs_currencies=eur"
            } else {
                "https://api.coingecko.com/api/v3/coins/$coinId/history?date=${date.format(coinGeckoDateFormatter)}&localization=false"
            }
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            val body = try {
                if (conn.responseCode !in 200..299) return@withContext null
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
            val root = Json.parseToJsonElement(body).jsonObject
            if (date == null) {
                (root[coinId] as? JsonObject)?.get("eur")?.jsonPrimitive?.content?.toDoubleOrNull()
            } else {
                val marketData = root["market_data"]?.jsonObject
                marketData?.get("current_price")?.jsonObject?.get("eur")?.jsonPrimitive?.content?.toDoubleOrNull()
            }
        }.getOrNull()
    }
}
