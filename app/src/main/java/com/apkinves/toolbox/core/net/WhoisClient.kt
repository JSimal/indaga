package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Cliente WHOIS puro sobre TCP/43 (RFC 3912). Empieza en IANA y sigue el
 * servidor "refer:" que devuelva cada TLD, sin depender de ninguna API externa.
 */
object WhoisClient {

    private const val IANA_WHOIS = "whois.iana.org"
    private const val PORT = 43
    private const val TIMEOUT_MS = 6000
    private const val MAX_HOPS = 5

    /**
     * Intenta primero WHOIS "de verdad" por TCP/43. Muchas redes móviles y
     * corporativas bloquean ese puerto (no es tráfico web habitual); si
     * falla o no devuelve nada útil, se usa como respaldo RDAP sobre HTTPS.
     */
    suspend fun lookupDomain(domain: String): String {
        val raw = runCatching { lookupDomainRaw(domain) }.getOrNull()
        if (!raw.isNullOrBlank()) return raw
        return RdapClient.lookupDomain(domain).getOrElse { "No se pudo consultar WHOIS ni RDAP: ${it.message}" }
    }

    suspend fun lookupIp(ip: String): String {
        val raw = runCatching { lookupIpRaw(ip) }.getOrNull()
        if (!raw.isNullOrBlank()) return raw
        return RdapClient.lookupIp(ip).getOrElse { "No se pudo consultar WHOIS ni RDAP: ${it.message}" }
    }

    private suspend fun lookupDomainRaw(domain: String): String = withContext(Dispatchers.IO) {
        var server = IANA_WHOIS
        var lastResponse = ""
        val visited = LinkedHashSet<String>()

        repeat(MAX_HOPS) {
            if (!visited.add(server)) return@repeat
            val response = query(server, domain)
            lastResponse = response
            val referral = extractReferral(response)
            if (referral == null || referral.equals(server, ignoreCase = true)) {
                return@withContext response
            }
            server = referral
        }
        lastResponse
    }

    private suspend fun lookupIpRaw(ip: String): String = withContext(Dispatchers.IO) {
        // Para IPs, los RIRs regionales (ARIN/RIPE/APNIC...) resuelven bien
        // consultando directamente a whois.arin.org, que reenvía (refer) al RIR correcto.
        var server = "whois.arin.org"
        var lastResponse = ""
        val visited = LinkedHashSet<String>()

        repeat(MAX_HOPS) {
            if (!visited.add(server)) return@repeat
            val response = query(server, ip)
            lastResponse = response
            val referral = extractReferral(response)
            if (referral == null || referral.equals(server, ignoreCase = true)) {
                return@withContext response
            }
            server = referral
        }
        lastResponse
    }

    private fun query(server: String, target: String): String {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(server, PORT), TIMEOUT_MS)
            socket.soTimeout = TIMEOUT_MS
            OutputStreamWriter(socket.getOutputStream()).apply {
                write("$target\r\n")
                flush()
            }
            return BufferedReader(InputStreamReader(socket.getInputStream())).readText()
        }
    }

    private fun extractReferral(response: String): String? {
        val patterns = listOf(
            Regex("(?i)refer:\\s*(\\S+)"),
            Regex("(?i)whois:\\s*(\\S+)"),
            Regex("(?i)ReferralServer:\\s*whois://(\\S+)"),
        )
        for (p in patterns) {
            val match = p.find(response)
            if (match != null) return match.groupValues[1].trim()
        }
        return null
    }
}
