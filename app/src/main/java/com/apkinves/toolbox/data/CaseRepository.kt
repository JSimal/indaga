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
import java.time.Instant

@Serializable
data class CaseEntry(
    val id: String,
    val tool: String,
    val target: String,
    val summary: String,
    val fullResult: String,
    val timestamp: Long,
)

/**
 * Historial/"caso" de investigación: cada consulta de cualquier herramienta
 * se guarda aquí para poder revisarla o exportarla más tarde.
 * Persistencia sencilla en un fichero JSON local, sin base de datos.
 */
class CaseRepository(context: Context) {

    companion object {
        // Límite de entradas guardadas: sin esto, el historial crece sin fin y
        // cada consulta nueva reescribe un fichero cada vez más grande.
        private const val MAX_ENTRIES = 200
    }

    private val file = File(context.filesDir, "case_history.json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val _entries = MutableStateFlow<List<CaseEntry>>(loadFromDisk())
    val entries = _entries.asStateFlow()

    private fun loadFromDisk(): List<CaseEntry> {
        if (!file.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<CaseEntry>>(file.readText())
        }.getOrDefault(emptyList())
    }

    suspend fun add(tool: String, target: String, summary: String, fullResult: String) {
        withContext(Dispatchers.IO) {
            val entry = CaseEntry(
                id = "${System.currentTimeMillis()}-${(0..9999).random()}",
                tool = tool,
                target = target,
                summary = summary,
                fullResult = fullResult,
                timestamp = Instant.now().epochSecond,
            )
            val updated = (listOf(entry) + _entries.value).take(MAX_ENTRIES)
            _entries.value = updated
            file.writeText(json.encodeToString(updated))
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            _entries.value = emptyList()
            if (file.exists()) file.delete()
        }
    }

    fun exportAsMarkdown(): String {
        val sb = StringBuilder("# Informe de investigación\n\n")
        _entries.value.groupBy { it.target }.forEach { (target, items) ->
            sb.append("## $target\n\n")
            items.forEach { entry ->
                sb.append("### ${entry.tool}\n")
                sb.append("```\n${entry.fullResult}\n```\n\n")
            }
        }
        return sb.toString()
    }
}
