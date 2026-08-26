package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class CrtShEntry(val name_value: String = "")

/**
 * Busca subdominios a partir de los certificados públicos emitidos (Certificate
 * Transparency), vía crt.sh. Gratis, sin registro ni API key.
 */
object SubdomainFinder {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun find(domain: String): List<String> = withContext(Dispatchers.IO) {
        val url = URL("https://crt.sh/?q=%25.${domain.trim()}&output=json")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", "Indaga-App")
        try {
            val body = conn.inputStream.bufferedReader().readText()
            val entries = json.decodeFromString<List<CrtShEntry>>(body)
            entries.flatMap { it.name_value.split("\n") }
                .map { it.trim().removePrefix("*.") }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        } finally {
            conn.disconnect()
        }
    }
}
