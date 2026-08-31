package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Comprueba un dominio contra el feed público y gratuito de OpenPhish (URLs
 * de phishing activas confirmadas, sin necesidad de registro ni API key).
 * El feed gratuito tiene un pequeño retraso respecto al de pago, pero sigue
 * siendo una señal real de "esto se ha reportado como phishing".
 */
object PhishingFeedClient {

    private const val FEED_URL = "https://openphish.com/feed.txt"

    suspend fun checkDomain(domain: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(FEED_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 10000
            conn.instanceFollowRedirects = true
            val body = try {
                if (conn.responseCode !in 200..299) error("Feed OpenPhish respondió ${conn.responseCode}")
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
            val target = domain.trim().lowercase().removePrefix("www.")
            body.lineSequence().any { it.contains(target, ignoreCase = true) }
        }
    }
}
