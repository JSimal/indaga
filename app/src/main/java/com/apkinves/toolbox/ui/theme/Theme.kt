package com.apkinves.toolbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Paleta "ciberseguridad": fondo casi negro azulado, acentos neón cian/verde,
// como una terminal moderna. Colores de estado reutilizables por las pantallas.
object CyberColors {
    val NeonCyan = Color(0xFF22D3EE)
    val NeonGreen = Color(0xFF4ADE80)
    val NeonAmber = Color(0xFFFBBF24)
    val NeonRed = Color(0xFFF87171)
    val NeonPurple = Color(0xFFA78BFA)
    val NeonPink = Color(0xFFF472B6)
    val NeonOrange = Color(0xFFFB923C)
    val NeonTeal = Color(0xFF2DD4BF)
}

private val DarkBackground = Color(0xFF090C10)
private val DarkSurface = Color(0xFF11151C)
private val DarkSurfaceVariant = Color(0xFF1A2029)

private val DarkColors = darkColorScheme(
    primary = CyberColors.NeonCyan,
    onPrimary = Color(0xFF00272E),
    primaryContainer = Color(0xFF0E3A44),
    onPrimaryContainer = CyberColors.NeonCyan,
    secondary = CyberColors.NeonGreen,
    onSecondary = Color(0xFF04310F),
    tertiary = CyberColors.NeonPurple,
    background = DarkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = CyberColors.NeonRed,
    onError = Color(0xFF350A0A),
    outline = Color(0xFF2A3441),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0891B2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF083344),
    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    tertiary = Color(0xFF7C3AED),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEFF2F6),
    onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFDC2626),
    outline = Color(0xFFCBD5E1),
)

private val CyberShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun ToolboxTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, shapes = CyberShapes, content = content)
}
