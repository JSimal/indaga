package com.apkinves.toolbox.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

/**
 * Análisis de nivel de error (ELA): recomprime la imagen en JPEG y compara con el
 * original — las zonas editadas o pegadas suelen recomprimirse de forma distinta al
 * resto y quedan resaltadas. Solo es significativo sobre imágenes que ya eran JPEG.
 */
object ImageForensics {
    private const val MAX_PIXELS = 4_000_000L
    private const val RECOMPRESS_QUALITY = 90
    private const val AMPLIFICATION = 12

    data class ElaReport(val heatmap: Bitmap, val averageDifference: Double, val maxDifference: Int, val likelyJpeg: Boolean)

    suspend fun analyze(context: Context, uri: Uri): Result<ElaReport> = withContext(Dispatchers.IO) {
        runCatching {
            val mime = context.contentResolver.getType(uri).orEmpty()
            val likelyJpeg = mime.contains("jpeg") || mime.contains("jpg")

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val pixelCount = bounds.outWidth.toLong() * bounds.outHeight.toLong()
            require(pixelCount in 1..MAX_PIXELS) { "Imagen demasiado grande o formato no reconocido para este análisis" }

            val original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?.copy(Bitmap.Config.ARGB_8888, false)
                ?: error("No se pudo decodificar la imagen")

            val recompressedBytes = ByteArrayOutputStream().apply {
                original.compress(Bitmap.CompressFormat.JPEG, RECOMPRESS_QUALITY, this)
            }.toByteArray()
            val recompressed = BitmapFactory.decodeByteArray(recompressedBytes, 0, recompressedBytes.size)

            val width = original.width
            val height = original.height
            val heatmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val origPixels = IntArray(width * height)
            val recPixels = IntArray(width * height)
            original.getPixels(origPixels, 0, width, 0, 0, width, height)
            recompressed.getPixels(recPixels, 0, width, 0, 0, width, height)

            var sum = 0.0
            var max = 0
            val outPixels = IntArray(width * height)
            for (i in origPixels.indices) {
                val a = origPixels[i]
                val b = recPixels[i]
                val dr = ((a shr 16 and 0xFF) - (b shr 16 and 0xFF))
                val dg = ((a shr 8 and 0xFF) - (b shr 8 and 0xFF))
                val db = ((a and 0xFF) - (b and 0xFF))
                val diff = (kotlin.math.abs(dr) + kotlin.math.abs(dg) + kotlin.math.abs(db)) / 3
                sum += diff
                if (diff > max) max = diff
                val amplified = min(255, diff * AMPLIFICATION)
                outPixels[i] = (0xFF shl 24) or (amplified shl 16) or (amplified shl 8) or amplified
            }
            heatmap.setPixels(outPixels, 0, width, 0, 0, width, height)

            original.recycle()
            recompressed.recycle()

            ElaReport(
                heatmap = heatmap,
                averageDifference = sum / origPixels.size,
                maxDifference = max,
                likelyJpeg = likelyJpeg,
            )
        }
    }
}
