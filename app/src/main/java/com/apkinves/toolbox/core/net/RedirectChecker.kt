package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object RedirectChecker {

    data class Hop(val url: String, val statusCode: Int)

    suspend fun trace(startUrl: String, maxHops: Int = 10): List<Hop> = withContext(Dispatchers.IO) {
        val hops = mutableListOf<Hop>()
        var current = if (startUrl.startsWith("http")) startUrl else "https://$startUrl"

        repeat(maxHops) {
            val conn = URL(current).openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.instanceFollowRedirects = false
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Indaga-App)")

            val code = try { conn.responseCode } catch (e: Exception) { -1 }
            hops.add(Hop(current, code))

            val location = conn.getHeaderField("Location")
            conn.disconnect()

            if (code !in 300..399 || location == null) return@withContext hops
            current = if (location.startsWith("http")) location else URL(URL(current), location).toString()
        }
        hops
    }
}
