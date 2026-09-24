package com.apkinves.toolbox.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Rutas de las últimas herramientas abiertas, más reciente primero, para la
 * sección "Usadas recientemente" de Inicio. Persistido como una lista
 * delimitada por comas en SharedPreferences (un Set normal no conserva el
 * orden, que aquí es lo importante).
 */
class RecentToolsRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("recent_tools", Context.MODE_PRIVATE)
    private val _recent = MutableStateFlow(load())
    val recent = _recent.asStateFlow()

    private fun load(): List<String> =
        prefs.getString(KEY, null)?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    fun recordVisit(route: String) {
        val updated = (listOf(route) + _recent.value.filter { it != route }).take(MAX_ENTRIES)
        _recent.value = updated
        prefs.edit().putString(KEY, updated.joinToString(",")).apply()
    }

    companion object {
        private const val KEY = "recent_routes"
        private const val MAX_ENTRIES = 6
    }
}
