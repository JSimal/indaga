package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Comprueba un dominio contra el feed público y gratuito de OpenPhish (URLs
 * de phishing activas confirmadas, sin necesidad de registro ni API key).
 * El feed gratuito tiene un pequeño retraso respecto al de pago, pero sigue
 * siendo una señal real de "esto se ha reportado como phishing".
 *
 * El feed se cachea en memoria un par de minutos: en una Consulta por Lotes
 * con varios dominios no tiene sentido volver a descargar los mismos ~15KB
 * para cada uno.
 */
object PhishingFeedClient {

    private const val FEED_URL = "https://openphish.com/feed.txt"
    private const val CACHE_TTL_MS = 2 * 60 * 1000L

    private val mutex = Mutex()
    private var cachedBody: String? = null
    private var cachedAt: Long = 0

    suspend fun checkDomain(domain: String): Result<Boolean> = runCatching {
        val body = fetchFeed()
        val target = domain.trim().lowercase().removePrefix("www.")
        body.lineSequence().any { it.contains(target, ignoreCase = true) }
    }

    private suspend fun fetchFeed(): String = mutex.withLock {
        val now = System.currentTimeMillis()
        cachedBody?.let { if (now - cachedAt < CACHE_TTL_MS) return it }

        val fresh = withContext(Dispatchers.IO) {
            val conn = URL(FEED_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 10000
            conn.instanceFollowRedirects = true
            try {
                if (conn.responseCode !in 200..299) error("Feed OpenPhish respondió ${conn.responseCode}")
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
        }
        cachedBody = fresh
        cachedAt = now
        fresh
    }
}
