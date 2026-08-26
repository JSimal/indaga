package com.apkinves.toolbox.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

enum class WatchType { CONTENT, SUBDOMAINS }

@Serializable
data class WatchItem(
    val id: String,
    val type: String, // WatchType.name
    val target: String,
    val lastSignature: String? = null,
    val lastCheckedAt: Long = 0,
    val lastChangedAt: Long? = null,
)

/** Lista de objetivos a vigilar periódicamente. Persistencia local en JSON, igual que el historial de casos. */
class WatchRepository(context: Context) {

    private val file = File(context.filesDir, "watchlist.json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val _items = MutableStateFlow(loadFromDisk())
    val items = _items.asStateFlow()

    private fun loadFromDisk(): List<WatchItem> {
        if (!file.exists()) return emptyList()
        return runCatching { json.decodeFromString<List<WatchItem>>(file.readText()) }.getOrDefault(emptyList())
    }

    private fun persist(list: List<WatchItem>) {
        _items.value = list
        runCatching { file.writeText(json.encodeToString(list)) }
    }

    suspend fun add(type: WatchType, target: String) = withContext(Dispatchers.IO) {
        val item = WatchItem(id = "${System.currentTimeMillis()}-${(0..9999).random()}", type = type.name, target = target)
        persist(listOf(item) + _items.value)
    }

    suspend fun remove(id: String) = withContext(Dispatchers.IO) {
        persist(_items.value.filterNot { it.id == id })
    }

    suspend fun updateResult(id: String, newSignature: String, changed: Boolean) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        persist(
            _items.value.map {
                if (it.id == id) it.copy(
                    lastSignature = newSignature,
                    lastCheckedAt = now,
                    lastChangedAt = if (changed) now else it.lastChangedAt,
                ) else it
            },
        )
    }

    fun snapshot(): List<WatchItem> = _items.value
}
