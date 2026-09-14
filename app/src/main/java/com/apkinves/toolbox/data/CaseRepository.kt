package com.apkinves.toolbox.data

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.SecureRandom
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
 * El fichero se cifra en reposo (Jetpack Security / AES256-GCM) porque puede
 * contener objetivos y resultados sensibles de una investigación en curso.
 */
class CaseRepository(private val context: Context) {

    companion object {
        // Límite de entradas guardadas: sin esto, el historial crece sin fin y
        // cada consulta nueva reescribe un fichero cada vez más grande.
        private const val MAX_ENTRIES = 200
    }

    private val file = File(context.filesDir, "case_history.enc")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val masterKey by lazy {
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    }

    private fun encryptedFile(target: File): EncryptedFile =
        EncryptedFile.Builder(context, target, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build()

    private val _entries = MutableStateFlow<List<CaseEntry>>(loadFromDisk())
    val entries = _entries.asStateFlow()

    private fun loadFromDisk(): List<CaseEntry> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val bytes = encryptedFile(file).openFileInput().use { it.readBytes() }
            json.decodeFromString<List<CaseEntry>>(String(bytes, Charsets.UTF_8))
        }.getOrDefault(emptyList())
    }

    private fun writeToDisk(list: List<CaseEntry>) {
        if (file.exists()) file.delete()
        encryptedFile(file).openFileOutput().use { it.write(json.encodeToString(list).toByteArray(Charsets.UTF_8)) }
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
            writeToDisk(updated)
        }
    }

    /** Borrado seguro: sobrescribe el fichero con datos aleatorios antes de eliminarlo, para que no quede recuperable. */
    suspend fun clear() {
        withContext(Dispatchers.IO) {
            _entries.value = emptyList()
            if (file.exists()) {
                val size = file.length()
                if (size > 0) {
                    val randomBytes = ByteArray(size.toInt())
                    SecureRandom().nextBytes(randomBytes)
                    runCatching { file.writeBytes(randomBytes) }
                }
                file.delete()
            }
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
