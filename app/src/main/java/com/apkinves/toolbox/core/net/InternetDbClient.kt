package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Shodan InternetDB: puertos, hostnames, CPEs y CVEs conocidas para una IP,
 * a partir de escaneos pasivos de Shodan. Gratuito, sin API key ni límite
 * publicado. Devuelve 404 cuando Shodan no tiene datos para esa IP (no es
 * un error, simplemente no hay información).
 */
object InternetDbClient {

    @Serializable
    data class InternetDbInfo(
        val ip: String,
        val ports: List<Int> = emptyList(),
        val hostnames: List<String> = emptyList(),
        val cpes: List<String> = emptyList(),
        val vulns: List<String> = emptyList(),
        val tags: List<String> = emptyList(),
    )

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = TtlCache<String, InternetDbInfo?>(5 * 60 * 1000L)

    suspend fun lookup(ip: String): InternetDbInfo? = cache.getOrFetch(ip) {
        withContext(Dispatchers.IO) {
            val conn = URL("https://internetdb.shodan.io/$ip").openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 8000
            try {
                when (conn.responseCode) {
                    404 -> null
                    in 200..299 -> json.decodeFromString(InternetDbInfo.serializer(), conn.inputStream.bufferedReader().readText())
                    else -> error("InternetDB respondió ${conn.responseCode}")
                }
            } finally {
                conn.disconnect()
            }
        }
    }
}
