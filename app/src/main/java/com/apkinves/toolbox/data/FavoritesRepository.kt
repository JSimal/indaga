package com.apkinves.toolbox.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Rutas de herramientas marcadas como favoritas, persistidas en SharedPreferences (dato mínimo, sin JSON). */
class FavoritesRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val _favorites = MutableStateFlow(prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet())
    val favorites = _favorites.asStateFlow()

    fun toggle(route: String) {
        val updated = if (route in _favorites.value) _favorites.value - route else _favorites.value + route
        _favorites.value = updated
        prefs.edit().putStringSet(KEY, updated).apply()
    }

    fun isFavorite(route: String): Boolean = route in _favorites.value

    companion object {
        private const val KEY = "favorite_routes"
    }
}
