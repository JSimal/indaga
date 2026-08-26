package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class BinCountry(val name: String = "", val emoji: String = "")

@Serializable
data class BinBank(val name: String = "")

@Serializable
data class BinInfo(
    val scheme: String = "",
    val type: String = "",
    val brand: String = "",
    val country: BinCountry = BinCountry(),
    val bank: BinBank = BinBank(),
)

/**
 * binlist.net: API HTTPS gratuita, sin registro. Límite generoso para uso
 * personal (unas pocas peticiones por segundo).
 */
object BinLookupClient {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun lookup(bin: String): Result<BinInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL("https://lookup.binlist.net/${bin.trim()}").openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("Accept-Version", "3")
            try {
                if (conn.responseCode != 200) error("BIN no encontrado (código ${conn.responseCode})")
                val body = conn.inputStream.bufferedReader().readText()
                json.decodeFromString(BinInfo.serializer(), body)
            } finally {
                conn.disconnect()
            }
        }
    }
}
