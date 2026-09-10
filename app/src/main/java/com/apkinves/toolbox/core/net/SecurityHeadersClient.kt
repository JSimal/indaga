package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** Comprueba las cabeceras de seguridad HTTP habituales de un sitio (estilo securityheaders.com, sin servicio externo). */
object SecurityHeadersClient {
    data class Report(
        val hsts: String?,
        val csp: String?,
        val xContentTypeOptions: String?,
        val xFrameOptions: String?,
        val referrerPolicy: String?,
        val permissionsPolicy: String?,
    ) {
        val presentCount: Int get() = listOfNotNull(hsts, csp, xContentTypeOptions, xFrameOptions, referrerPolicy, permissionsPolicy).size
    }

    suspend fun check(domain: String): Result<Report> = withContext(Dispatchers.IO) {
        runCatching {
            val target = if (domain.startsWith("http")) domain else "https://${domain.trim()}"
            val conn = URL(target).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "HEAD"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.instanceFollowRedirects = true
            try {
                fun header(name: String) = conn.getHeaderField(name)
                Report(
                    hsts = header("Strict-Transport-Security"),
                    csp = header("Content-Security-Policy"),
                    xContentTypeOptions = header("X-Content-Type-Options"),
                    xFrameOptions = header("X-Frame-Options"),
                    referrerPolicy = header("Referrer-Policy"),
                    permissionsPolicy = header("Permissions-Policy"),
                )
            } finally {
                conn.disconnect()
            }
        }
    }
}
