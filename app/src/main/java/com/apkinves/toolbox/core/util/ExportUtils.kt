package com.apkinves.toolbox.core.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.apkinves.toolbox.data.CaseEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exportación de casos a PDF/JSON/CSV, más el hash SHA-256 del fichero
 * exportado ("cadena de custodia" mínima: hash + fecha para poder demostrar
 * después que el fichero compartido no se ha alterado). El hash se calcula
 * sobre el fichero ya escrito, nunca se incrusta dentro de sí mismo, porque
 * eso cambiaría el propio hash.
 */
object ExportUtils {

    data class ExportedFile(val file: File, val sha256: String, val timestamp: Long)

    private val json = Json { prettyPrint = true }
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun exportsDir(context: Context): File =
        File(context.cacheDir, "exports").apply { mkdirs() }

    private fun write(context: Context, filename: String, bytes: ByteArray): ExportedFile {
        val file = File(exportsDir(context), filename)
        file.writeBytes(bytes)
        return ExportedFile(file, sha256Hex(bytes), System.currentTimeMillis())
    }

    fun uriFor(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun exportJson(context: Context, entries: List<CaseEntry>): ExportedFile {
        val bytes = json.encodeToString(entries).toByteArray(Charsets.UTF_8)
        return write(context, "indaga-caso.json", bytes)
    }

    fun exportCsv(context: Context, entries: List<CaseEntry>): ExportedFile {
        fun escape(s: String) = "\"${s.replace("\"", "\"\"")}\""
        val sb = StringBuilder("fecha,herramienta,objetivo,resumen\n")
        entries.forEach { e ->
            sb.append(dateFormat.format(Date(e.timestamp * 1000))).append(',')
                .append(escape(e.tool)).append(',')
                .append(escape(e.target)).append(',')
                .append(escape(e.summary)).append('\n')
        }
        return write(context, "indaga-caso.csv", sb.toString().toByteArray(Charsets.UTF_8))
    }

    fun exportPdf(context: Context, entries: List<CaseEntry>): ExportedFile {
        val document = PdfDocument()
        val pageWidth = 595 // A4 a 72dpi aprox.
        val pageHeight = 842
        val margin = 36f
        val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 10f }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin

        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = margin
        }

        fun ensureSpace(lines: Int, lineHeight: Float) {
            if (y + lines * lineHeight > pageHeight - margin) newPage()
        }

        canvas.drawText("Informe de investigación — Indaga", margin, y, titlePaint)
        y += 22f
        canvas.drawText("Generado: ${dateFormat.format(Date())}", margin, y, bodyPaint)
        y += 20f

        entries.groupBy { it.target }.forEach { (target, items) ->
            ensureSpace(2, 18f)
            canvas.drawText(target, margin, y, headerPaint)
            y += 18f
            items.forEach { entry ->
                ensureSpace(1, 14f)
                canvas.drawText("· ${entry.tool} — ${dateFormat.format(Date(entry.timestamp * 1000))}", margin + 10f, y, bodyPaint)
                y += 14f
                entry.fullResult.lineSequence().forEach { rawLine ->
                    // Envuelve líneas largas a mano (Paint no hace wrap automático).
                    var line = rawLine
                    while (line.length > 100) {
                        ensureSpace(1, 12f)
                        canvas.drawText(line.take(100), margin + 16f, y, bodyPaint)
                        y += 12f
                        line = line.substring(100)
                    }
                    ensureSpace(1, 12f)
                    canvas.drawText(line, margin + 16f, y, bodyPaint)
                    y += 12f
                }
                y += 6f
            }
        }
        document.finishPage(page)

        val file = File(exportsDir(context), "indaga-informe.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        val bytes = file.readBytes()
        return ExportedFile(file, sha256Hex(bytes), System.currentTimeMillis())
    }
}
