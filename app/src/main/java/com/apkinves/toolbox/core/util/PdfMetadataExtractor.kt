package com.apkinves.toolbox.core.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extractor de metadatos de PDF "best effort": los PDF no comprimidos (la
 * mayoría de los generados por herramientas de oficina) incluyen el
 * diccionario /Info como texto plano cerca del final del archivo. Se busca
 * ahí en vez de añadir una librería de parseo de PDF completa.
 */
object PdfMetadataExtractor {

    private val FIELDS = listOf("Title", "Author", "Subject", "Creator", "Producer", "CreationDate", "ModDate")

    data class PdfReport(val fields: Map<String, String>, val fullyParsed: Boolean)

    suspend fun extract(context: Context, uri: Uri): Result<PdfReport> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("No se pudo abrir el archivo")

            require(bytes.size >= 4 && String(bytes, 0, 4, Charsets.US_ASCII) == "%PDF") {
                "El archivo no parece un PDF válido"
            }

            // Buscamos en los últimos 200 KB, donde suele estar el trailer/Info.
            val tailStart = maxOf(0, bytes.size - 200_000)
            val tail = String(bytes, tailStart, bytes.size - tailStart, Charsets.ISO_8859_1)

            val fields = mutableMapOf<String, String>()
            FIELDS.forEach { field ->
                val regex = Regex("/$field\\s*\\(([^)]*)\\)")
                regex.find(tail)?.let { fields[field] = it.groupValues[1].trim() }
            }

            PdfReport(fields, fullyParsed = fields.isNotEmpty())
        }
    }
}
