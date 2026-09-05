package com.apkinves.toolbox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.R
import com.apkinves.toolbox.ui.theme.CyberColors
import kotlinx.coroutines.delay

private val SPLASH_LINES = listOf(
    "Iniciando Indaga...",
    "Cargando módulos OSINT...",
    "Listo.",
)

@Composable
fun SplashScreen() {
    var visibleLines by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        for (i in SPLASH_LINES.indices) {
            visibleLines = i + 1
            delay(1250)
        }
    }

    val infinite = rememberInfiniteTransition(label = "splash")
    val sweepAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "sweep",
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val ringColor = CyberColors.NeonCyan
                    val radius = size.minDimension / 2 * pulse
                    val center = Offset(size.width / 2, size.height / 2)

                    drawCircle(color = ringColor.copy(alpha = 0.18f), radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))
                    drawCircle(color = ringColor.copy(alpha = 0.35f), radius = radius * 0.72f, center = center, style = Stroke(width = 2.dp.toPx()))

                    rotate(sweepAngle, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(ringColor.copy(alpha = 0f), ringColor.copy(alpha = 0.9f))),
                            startAngle = 0f,
                            sweepAngle = 90f,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        )
                    }
                }
                Box(modifier = Modifier.size(84.dp)) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.height(72.dp).padding(top = 8.dp)) {
                for (i in SPLASH_LINES.indices) {
                    AnimatedVisibility(visible = i < visibleLines, enter = fadeIn(tween(300))) {
                        Text(
                            SPLASH_LINES[i],
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (i == SPLASH_LINES.lastIndex) CyberColors.NeonGreen else MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
