package com.apkinves.toolbox.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Gráfica de línea minimalista (sin ejes ni etiquetas) para series de precios cortas. */
@Composable
fun Sparkline(values: List<Double>, color: Color, modifier: Modifier = Modifier.width(100.dp).height(36.dp)) {
    if (values.size < 2) return
    Canvas(modifier = modifier) {
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val stepX = size.width / (values.size - 1)

        val path = androidx.compose.ui.graphics.Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - ((v - min) / range * size.height).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = color, style = Stroke(width = 3f))
    }
}
