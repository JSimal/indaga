package com.apkinves.toolbox.core.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Detección "best effort" de esteganografía LSB: en una imagen normal el bit
 * menos significativo de cada canal de color NO es perfectamente aleatorio
 * (hay ligera correlación por gradientes/compresión). Si alguien ha ocultado
 * ahí datos cifrados o comprimidos, esos bits sí que se comportan como
 * ruido puro, y la proporción de unos se acerca mucho más a exactamente el
 * 50%. Por eso un valor MUY cercano a 0.5 es la señal de alerta (heurística
 * simplificada de esteganálisis por chi-cuadrado, no una prueba definitiva).
 */
object StegoDetector {

    // El análisis LSB necesita cada píxel a resolución completa (no se puede
    // reducir sin alterar los propios bits que se están midiendo), así que
    // en vez de arriesgarse a un OutOfMemoryError con una imagen enorme,
    // se comprueba antes el tamaño y se rechaza con un mensaje claro.
    private const val MAX_PIXELS = 30_000_000 // ~30 megapíxeles

    data class StegoReport(val sampledPixels: Int, val lsbOnesRatio: Double, val suspicious: Boolean)

    suspend fun analyze(context: Context, uri: Uri): Result<StegoReport> = withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val pixelCount = bounds.outWidth.toLong() * bounds.outHeight.toLong()
            require(pixelCount in 1..MAX_PIXELS) { "Imagen demasiado grande para analizar (${bounds.outWidth}x${bounds.outHeight}); prueba con una más pequeña" }

            val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: error("No se pudo decodificar la imagen")

            var onesCount = 0L
            var total = 0L
            val stepX = maxOf(1, bitmap.width / 200)
            val stepY = maxOf(1, bitmap.height / 200)

            var y = 0
            while (y < bitmap.height) {
                var x = 0
                while (x < bitmap.width) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    onesCount += (r and 1) + (g and 1) + (b and 1)
                    total += 3
                    x += stepX
                }
                y += stepY
            }
            bitmap.recycle()

            val ratio = if (total == 0L) 0.0 else onesCount.toDouble() / total
            // Una imagen "limpia" suele rondar 0.5; nos alejamos si hay >8 puntos de desviación.
            val suspicious = abs(ratio - 0.5) < 0.02 && total > 1000

            StegoReport(total.toInt(), ratio, suspicious)
        }
    }
}
