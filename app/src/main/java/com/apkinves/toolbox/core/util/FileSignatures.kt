package com.apkinves.toolbox.core.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FileSignatures {

    private data class Signature(val name: String, val bytes: List<Int>, val offset: Int = 0)

    private val SIGNATURES = listOf(
        Signature("PDF", listOf(0x25, 0x50, 0x44, 0x46)),
        Signature("PNG", listOf(0x89, 0x50, 0x4E, 0x47)),
        Signature("JPEG", listOf(0xFF, 0xD8, 0xFF)),
        Signature("GIF", listOf(0x47, 0x49, 0x46, 0x38)),
        Signature("ZIP / APK / DOCX / XLSX", listOf(0x50, 0x4B, 0x03, 0x04)),
        Signature("RAR", listOf(0x52, 0x61, 0x72, 0x21)),
        Signature("7-Zip", listOf(0x37, 0x7A, 0xBC, 0xAF)),
        Signature("GZIP", listOf(0x1F, 0x8B)),
        Signature("ELF (ejecutable Linux/Android nativo)", listOf(0x7F, 0x45, 0x4C, 0x46)),
        Signature("Windows EXE/DLL", listOf(0x4D, 0x5A)),
        Signature("BMP", listOf(0x42, 0x4D)),
        Signature("WEBP", listOf(0x52, 0x49, 0x46, 0x46)),
        Signature("SQLite DB", listOf(0x53, 0x51, 0x4C, 0x69, 0x74, 0x65)),
        Signature("MP4/MOV (contenedor ISO-BMFF)", listOf(0x66, 0x74, 0x79, 0x70), offset = 4),
        Signature("MP3 (con ID3)", listOf(0x49, 0x44, 0x33)),
    )

    data class DetectionResult(val declaredExtension: String, val detectedType: String, val matches: Boolean, val headerHex: String)

    suspend fun detect(context: Context, uri: Uri): Result<DetectionResult> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readHeaderBytes(64) }
                ?: error("No se pudo leer el archivo")

            val detected = SIGNATURES.firstOrNull { sig ->
                bytes.size >= sig.offset + sig.bytes.size &&
                    sig.bytes.indices.all { i -> (bytes[sig.offset + i].toInt() and 0xFF) == sig.bytes[i] }
            }?.name ?: "Desconocido"

            val declaredExt = uri.lastPathSegment?.substringAfterLast('.', "")?.uppercase().orEmpty()
            val headerHex = bytes.take(16).joinToString(" ") { "%02X".format(it) }

            val matchesDeclared = declaredExt.isBlank() || detected.contains(declaredExt, ignoreCase = true) ||
                (declaredExt in listOf("APK", "DOCX", "XLSX", "PPTX", "JAR") && detected.startsWith("ZIP"))

            DetectionResult(declaredExt.ifBlank { "(sin extensión)" }, detected, matchesDeclared, headerHex)
        }
    }

    private fun java.io.InputStream.readHeaderBytes(max: Int): ByteArray {
        val buffer = ByteArray(max)
        val read = this.read(buffer)
        return if (read <= 0) ByteArray(0) else buffer.copyOf(read)
    }
}
