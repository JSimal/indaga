package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object UptimeChecker {

    data class UptimeResult(val up: Boolean, val statusCode: Int, val rttMs: Long, val error: String?)

    suspend fun check(url: String): UptimeResult = withContext(Dispatchers.IO) {
        val target = if (url.startsWith("http")) url else "https://$url"
        val start = System.currentTimeMillis()
        try {
            val conn = URL(target).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "HEAD"
            conn.instanceFollowRedirects = true
            val code = conn.responseCode
            conn.disconnect()
            UptimeResult(code in 200..399, code, System.currentTimeMillis() - start, null)
        } catch (e: Exception) {
            UptimeResult(false, -1, System.currentTimeMillis() - start, e.message)
        }
    }
}
