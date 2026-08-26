package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class IpInfo(
    val status: String = "",
    val message: String = "",
    val query: String = "",
    val country: String = "",
    val countryCode: String = "",
    val regionName: String = "",
    val city: String = "",
    val zip: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val timezone: String = "",
    val isp: String = "",
    val org: String = "",
    val `as`: String = "",
    val proxy: Boolean = false,
    val hosting: Boolean = false,
    val mobile: Boolean = false,
)

/**
 * ip-api.com: API HTTP gratuita, sin registro ni API key, ~45 req/min.
 */
object IpInfoClient {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun lookup(ip: String): IpInfo = withContext(Dispatchers.IO) {
        val fields = "status,message,query,country,countryCode,regionName,city,zip,lat,lon," +
            "timezone,isp,org,as,proxy,hosting,mobile"
        val url = URL("http://ip-api.com/json/$ip?fields=$fields")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        try {
            val body = conn.inputStream.bufferedReader().readText()
            json.decodeFromString(IpInfo.serializer(), body)
        } finally {
            conn.disconnect()
        }
    }
}
