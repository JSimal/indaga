package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object MetaExtractor {

    private val metaTagRegex = Regex("(?i)<meta[^>]+(?:property|name)=[\"']([^\"']+)[\"'][^>]+content=[\"']([^\"']*)[\"'][^>]*>")
    private val titleRegex = Regex("(?i)<title[^>]*>([^<]*)</title>")

    data class MetaReport(val title: String?, val tags: Map<String, String>)

    suspend fun extract(url: String): Result<MetaReport> = withContext(Dispatchers.IO) {
        runCatching {
            val target = if (url.startsWith("http")) url else "https://$url"
            val conn = URL(target).openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Indaga-App)")
            val body = try {
                conn.inputStream.bufferedReader().readText().take(200_000)
            } finally {
                conn.disconnect()
            }

            val title = titleRegex.find(body)?.groupValues?.get(1)?.trim()
            val tags = metaTagRegex.findAll(body)
                .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                .filterKeys { it.startsWith("og:") || it.startsWith("twitter:") || it == "description" }

            MetaReport(title, tags)
        }
    }
}
