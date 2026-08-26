package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Cliente de conexión TCP en crudo (lo que tradicionalmente se hacía con
 * telnet para probar un puerto): conecta, opcionalmente envía una línea de
 * texto, y muestra lo primero que responde el servicio. Útil para servicios
 * de texto plano (SMTP, HTTP crudo, etc.), no implementa el protocolo Telnet
 * con negociación de opciones.
 */
object TelnetClient {

    data class TelnetResult(val connected: Boolean, val banner: String, val error: String?)

    suspend fun connect(host: String, port: Int, sendLine: String?, timeoutMs: Int = 5000): TelnetResult =
        withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeoutMs)
                    socket.soTimeout = timeoutMs

                    if (!sendLine.isNullOrBlank()) {
                        socket.getOutputStream().write("$sendLine\r\n".toByteArray())
                        socket.getOutputStream().flush()
                    }

                    val buffer = ByteArray(4096)
                    val read = runCatching { socket.getInputStream().read(buffer) }.getOrDefault(-1)
                    val banner = if (read > 0) String(buffer, 0, read).trim() else "(sin respuesta inmediata, pero la conexión TCP se estableció)"
                    TelnetResult(true, banner, null)
                }
            } catch (e: Exception) {
                TelnetResult(false, "", e.message ?: "Error de conexión")
            }
        }
}
