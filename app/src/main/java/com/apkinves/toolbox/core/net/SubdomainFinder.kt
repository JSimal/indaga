package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class CrtShEntry(val name_value: String = "")

@Serializable
private data class CertSpotterEntry(val dns_names: List<String> = emptyList())

/**
 * Busca subdominios a partir de los certificados públicos emitidos
 * (Certificate Transparency). crt.sh es la fuente habitual, pero tiene fama
 * de caerse a menudo (502/503 con frecuencia); si falla, se usa CertSpotter
 * como respaldo. Ambas gratis, sin registro ni API key.
 */
object SubdomainFinder {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun find(domain: String): List<String> = withContext(Dispatchers.IO) {
        val fromCrtSh = runCatching { findViaCrtSh(domain) }.getOrNull()
        val result = if (!fromCrtSh.isNullOrEmpty()) fromCrtSh else findViaCertSpotter(domain)
        result.distinct().sorted()
    }

    private fun findViaCrtSh(domain: String): List<String> {
        val url = URL("https://crt.sh/?q=%25.${domain.trim()}&output=json")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", "Indaga-App")
        try {
            if (conn.responseCode !in 200..299) error("crt.sh respondió ${conn.responseCode}")
            val body = conn.inputStream.bufferedReader().readText()
            val entries = json.decodeFromString<List<CrtShEntry>>(body)
            return entries.flatMap { it.name_value.split("\n") }
                .map { it.trim().removePrefix("*.") }
                .filter { it.isNotBlank() }
        } finally {
            conn.disconnect()
        }
    }

    private fun findViaCertSpotter(domain: String): List<String> {
        val url = URL("https://api.certspotter.com/v1/issuances?domain=${domain.trim()}&include_subdomains=true&expand=dns_names")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", "Indaga-App")
        try {
            if (conn.responseCode !in 200..299) return emptyList()
            val body = conn.inputStream.bufferedReader().readText()
            val entries = json.decodeFromString<List<CertSpotterEntry>>(body)
            return entries.flatMap { it.dns_names }
                .map { it.trim().removePrefix("*.") }
                .filter { it.isNotBlank() }
        } finally {
            conn.disconnect()
        }
    }
}
