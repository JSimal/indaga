package com.apkinves.toolbox.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { SYSTEM, DARK, LIGHT }

/** Preferencia de tema persistida en SharedPreferences, con estado observable por Compose. */
object ThemePreference {
    private const val PREFS = "theme_prefs"
    private const val KEY_MODE = "mode"

    var mode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        mode = runCatching { ThemeMode.valueOf(prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name)!!) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun cycle(context: Context) {
        mode = when (mode) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_MODE, mode.name).apply()
    }

    fun icon(): String = when (mode) {
        ThemeMode.SYSTEM -> "🔄"
        ThemeMode.DARK -> "🌙"
        ThemeMode.LIGHT -> "☀️"
    }

    fun label(): String = when (mode) {
        ThemeMode.SYSTEM -> "Tema: automático (sigue al sistema)"
        ThemeMode.DARK -> "Tema: oscuro"
        ThemeMode.LIGHT -> "Tema: claro"
    }
}
