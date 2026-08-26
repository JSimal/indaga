package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Heurística ligera de detección de tecnologías a partir de cabeceras HTTP y
 * patrones comunes en el HTML. No pretende ser exhaustiva como Wappalyzer,
 * pero cubre los casos más frecuentes sin dependencias externas.
 */
object TechDetector {

    data class TechReport(
        val server: String?,
        val poweredBy: String?,
        val detected: List<String>,
        val headers: Map<String, String>,
    )

    private val SIGNATURES = listOf(
        "WordPress" to Regex("(?i)wp-content|wp-includes|generator\"\\s+content=\"WordPress"),
        "Joomla" to Regex("(?i)/media/jui/|Joomla!"),
        "Drupal" to Regex("(?i)Drupal.settings|/sites/default/files"),
        "Shopify" to Regex("(?i)cdn.shopify.com|Shopify.theme"),
        "React" to Regex("(?i)__NEXT_DATA__|react-root|data-reactroot"),
        "Next.js" to Regex("(?i)__NEXT_DATA__|/_next/static"),
        "Vue.js" to Regex("(?i)data-v-|__VUE__"),
        "Angular" to Regex("(?i)ng-version|ng-app"),
        "jQuery" to Regex("(?i)jquery(\\.min)?\\.js"),
        "Bootstrap" to Regex("(?i)bootstrap(\\.min)?\\.css"),
        "Cloudflare" to Regex("(?i)cloudflare"),
        "Google Analytics" to Regex("(?i)www\\.google-analytics\\.com|gtag\\("),
        "Google Tag Manager" to Regex("(?i)googletagmanager\\.com"),
    )

    suspend fun detect(url: String): Result<TechReport> = withContext(Dispatchers.IO) {
        runCatching {
            val target = if (url.startsWith("http")) url else "https://$url"
            val conn = URL(target).openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Indaga-App)")

            val headers = conn.headerFields
                .filterKeys { it != null }
                .mapKeys { it.key!! }
                .mapValues { it.value.joinToString(", ") }

            val body = try {
                conn.inputStream.bufferedReader().readText().take(200_000)
            } catch (e: Exception) {
                ""
            }
            conn.disconnect()

            val detected = SIGNATURES.filter { (_, regex) -> regex.containsMatchIn(body) || headers.values.any { regex.containsMatchIn(it) } }
                .map { it.first }

            TechReport(
                server = headers.entries.firstOrNull { it.key.equals("Server", ignoreCase = true) }?.value,
                poweredBy = headers.entries.firstOrNull { it.key.equals("X-Powered-By", ignoreCase = true) }?.value,
                detected = detected,
                headers = headers,
            )
        }
    }
}
