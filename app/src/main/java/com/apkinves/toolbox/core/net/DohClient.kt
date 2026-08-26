package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class DohAnswer(val name: String = "", val type: Int = 0, val TTL: Long = 0, val data: String = "")

@Serializable
private data class DohResponse(val Status: Int = -1, val Answer: List<DohAnswer> = emptyList())

/**
 * DNS-over-HTTPS (Google), respaldo para cuando el DNS por UDP/53 en crudo
 * está bloqueado (muy habitual en redes móviles). Va sobre HTTPS/443, que
 * prácticamente nunca se bloquea.
 */
object DohClient {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun query(domain: String, type: DnsClient.RecordType): List<DnsClient.DnsRecord> = withContext(Dispatchers.IO) {
        val conn = URL("https://dns.google/resolve?name=${domain.trim()}&type=${type.name}").openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("Accept", "application/dns-json")
        try {
            val body = conn.inputStream.bufferedReader().readText()
            val parsed = json.decodeFromString(DohResponse.serializer(), body)
            parsed.Answer
                .filter { it.type == type.code }
                .map { DnsClient.DnsRecord(type.name, it.data.trim('"'), it.TTL) }
        } finally {
            conn.disconnect()
        }
    }
}
