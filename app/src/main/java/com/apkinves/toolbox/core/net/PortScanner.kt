package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PortScanner {

    val COMMON_PORTS = listOf(
        21 to "FTP", 22 to "SSH", 23 to "Telnet", 25 to "SMTP", 53 to "DNS",
        80 to "HTTP", 110 to "POP3", 143 to "IMAP", 443 to "HTTPS", 445 to "SMB",
        993 to "IMAPS", 995 to "POP3S", 3306 to "MySQL", 3389 to "RDP", 5432 to "PostgreSQL",
        8080 to "HTTP-alt", 8443 to "HTTPS-alt",
    )

    data class PortResult(val port: Int, val service: String, val open: Boolean)

    suspend fun scan(host: String, ports: List<Pair<Int, String>> = COMMON_PORTS, timeoutMs: Int = 1500): List<PortResult> =
        withContext(Dispatchers.IO) {
            ports.map { (port, service) ->
                async {
                    val open = try {
                        Socket().use { it.connect(InetSocketAddress(host, port), timeoutMs); true }
                    } catch (e: Exception) {
                        false
                    }
                    PortResult(port, service, open)
                }
            }.awaitAll()
        }
}
