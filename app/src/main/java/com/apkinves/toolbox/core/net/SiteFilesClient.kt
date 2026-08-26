package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object SiteFilesClient {

    data class FetchResult(val found: Boolean, val statusCode: Int, val content: String)

    suspend fun fetch(domain: String, path: String): FetchResult = withContext(Dispatchers.IO) {
        val base = if (domain.startsWith("http")) domain.trimEnd('/') else "https://${domain.trim().trimEnd('/')}"
        val conn = URL("$base/$path").openConnection() as HttpURLConnection
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.instanceFollowRedirects = true
        try {
            val code = conn.responseCode
            val body = if (code in 200..299) {
                conn.inputStream.bufferedReader().readText().take(50_000)
            } else ""
            FetchResult(code in 200..299, code, body)
        } catch (e: Exception) {
            FetchResult(false, -1, "Error: ${e.message}")
        } finally {
            conn.disconnect()
        }
    }
}
