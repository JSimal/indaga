package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Serializable
data class WaybackSnapshot(val url: String = "", val timestamp: String = "", val status: String = "")

@Serializable
data class WaybackArchivedSnapshots(val closest: WaybackSnapshot? = null)

@Serializable
data class WaybackResponse(val url: String = "", val archived_snapshots: WaybackArchivedSnapshots = WaybackArchivedSnapshots())

object WaybackClient {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun closestSnapshot(url: String): Result<WaybackSnapshot?> = withContext(Dispatchers.IO) {
        runCatching {
            val target = if (url.startsWith("http")) url else "https://$url"
            val encoded = URLEncoder.encode(target, "UTF-8")
            val conn = URL("https://archive.org/wayback/available?url=$encoded").openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            try {
                val body = conn.inputStream.bufferedReader().readText()
                json.decodeFromString(WaybackResponse.serializer(), body).archived_snapshots.closest
            } finally {
                conn.disconnect()
            }
        }
    }
}
