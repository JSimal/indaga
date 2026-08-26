package com.apkinves.toolbox.core.util

import kotlin.math.log2
import kotlin.random.Random

object PasswordUtils {

    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#\$%^&*()-_=+[]{}<>?/"

    fun generate(length: Int, useUpper: Boolean, useDigits: Boolean, useSymbols: Boolean): String {
        var pool = LOWER
        if (useUpper) pool += UPPER
        if (useDigits) pool += DIGITS
        if (useSymbols) pool += SYMBOLS
        return (1..length).map { pool[Random.nextInt(pool.length)] }.joinToString("")
    }

    data class Strength(val bits: Double, val label: String, val crackTimeLabel: String)

    /** Estimación tipo entropía (no sustituye a zxcvbn, pero da una idea razonable). */
    fun estimateStrength(password: String): Strength {
        if (password.isEmpty()) return Strength(0.0, "Vacía", "instantáneo")

        var poolSize = 0
        if (password.any { it in LOWER }) poolSize += LOWER.length
        if (password.any { it in UPPER }) poolSize += UPPER.length
        if (password.any { it in DIGITS }) poolSize += DIGITS.length
        if (password.any { it in SYMBOLS || !it.isLetterOrDigit() }) poolSize += SYMBOLS.length
        if (poolSize == 0) poolSize = 26

        val bits = password.length * log2(poolSize.toDouble())

        // Asumiendo un atacante offline rápido: ~10^10 intentos/seg (hash rápido sin salt fuerte)
        val guessesPerSecond = 1e10
        val seconds = Math.pow(2.0, bits) / guessesPerSecond

        val crackTimeLabel = formatDuration(seconds)
        val label = when {
            bits < 28 -> "Muy débil"
            bits < 36 -> "Débil"
            bits < 60 -> "Razonable"
            bits < 80 -> "Fuerte"
            else -> "Muy fuerte"
        }
        return Strength(bits, label, crackTimeLabel)
    }

    private fun formatDuration(seconds: Double): String {
        if (seconds < 1) return "instantáneo"
        val units = listOf(
            "años" to 31_536_000.0,
            "días" to 86_400.0,
            "horas" to 3_600.0,
            "minutos" to 60.0,
            "segundos" to 1.0,
        )
        for ((name, unitSeconds) in units) {
            if (seconds >= unitSeconds) {
                val value = seconds / unitSeconds
                return if (value > 1e6) "más de un millón de $name" else "%.1f %s".format(value, name)
            }
        }
        return "instantáneo"
    }
}
