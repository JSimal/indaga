package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Lee el "banner" que muchos servicios envían nada más conectar (SSH, FTP,
 * SMTP...), o hace una petición HTTP mínima en los puertos web en claro.
 * Los puertos con TLS (443, 8443...) se omiten: sin hacer el handshake TLS
 * primero, lo que se lee es ruido binario, no un banner legible; esa
 * información ya la da la tarjeta de certificado SSL por separado.
 */
object BannerGrabber {

    private val TLS_PORTS = setOf(443, 8443, 993, 995, 465)
    private val PLAINTEXT_HTTP_PORTS = setOf(80, 8080)

    suspend fun grab(host: String, port: Int, timeoutMs: Int = 2500): String? {
        if (port in TLS_PORTS) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeoutMs)
                    socket.soTimeout = timeoutMs

                    if (port in PLAINTEXT_HTTP_PORTS) {
                        socket.getOutputStream().write("HEAD / HTTP/1.0\r\nHost: $host\r\n\r\n".toByteArray())
                        socket.getOutputStream().flush()
                    }

                    val buffer = ByteArray(512)
                    val read = socket.getInputStream().read(buffer)
                    if (read <= 0) null
                    else String(buffer, 0, read, Charsets.ISO_8859_1).lineSequence().firstOrNull()?.trim()?.take(120)
                }
            }.getOrNull()
        }
    }
}
