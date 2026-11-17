package com.apkinves.toolbox.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class DirectoryEntry(
    val id: String,
    val nombre: String,
    val tipo: String,
    val alias: List<String> = emptyList(),
    val entidad: String = "",
    val portal: String = "",
    val email: String = "",
    val guia: String = "",
    val conservacion: String = "",
    val requisitos: String = "",
    val emergencias: String = "",
    val notas: String = "",
    /** AAAA-MM-DD en que alguien comprobó la ficha contra la fuente oficial; vacío = sin comprobar. */
    val verificado: String = "",
) {
    fun coincide(texto: String): Boolean {
        val q = texto.trim().lowercase()
        if (q.isEmpty()) return true
        return nombre.lowercase().contains(q) || tipo.lowercase().contains(q) ||
            alias.any { it.lowercase().contains(q) } || notas.lowercase().contains(q)
    }
}

@Serializable
data class Directory(
    val actualizado: String = "",
    val aviso: String = "",
    val entradas: List<DirectoryEntry> = emptyList(),
)

/**
 * Directorio «¿A quién requerir?». El contenido vive en un único JSON
 * (`assets/directorio.json`) que viaja dentro del APK, de modo que funciona
 * sin cobertura. Al abrir la pantalla se intenta además bajar la versión que
 * hay en `main` del repo (mismo fichero, servido por raw.githubusercontent,
 * como ya hace [com.apkinves.toolbox.core.remote.AppStatusClient]): así una
 * ficha corregida llega a todos sin tener que publicar una versión nueva de
 * la app. Se queda la más reciente según el campo `actualizado`; si la red
 * falla, se usa la copia guardada o la del APK, sin errores visibles.
 */
class DirectoryRepository(context: Context) {

    private val appContext = context.applicationContext
    private val cache = File(appContext.filesDir, "directorio.json")
    private val json = Json { ignoreUnknownKeys = true }

    fun cargarLocal(): Directory {
        val delApk = runCatching {
            appContext.assets.open(ASSET).bufferedReader().use { json.decodeFromString(Directory.serializer(), it.readText()) }
        }.getOrDefault(Directory())
        val guardado = runCatching {
            if (cache.exists()) json.decodeFromString(Directory.serializer(), cache.readText()) else null
        }.getOrNull()
        return if (guardado != null && guardado.actualizado > delApk.actualizado) guardado else delApk
    }

    /** Devuelve el directorio remoto solo si es más nuevo que [actual]; si no, null. */
    suspend fun actualizarDesdeRed(actual: Directory): Directory? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(REMOTE_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            val body = try {
                if (conn.responseCode !in 200..299) error("status ${conn.responseCode}")
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
            val remoto = json.decodeFromString(Directory.serializer(), body)
            if (remoto.entradas.isNotEmpty() && remoto.actualizado > actual.actualizado) {
                cache.writeText(body)
                remoto
            } else {
                null
            }
        }.getOrNull()
    }

    private companion object {
        const val ASSET = "directorio.json"
        const val REMOTE_URL = "https://raw.githubusercontent.com/JSimal/indaga/main/app/src/main/assets/directorio.json"
    }
}

/** Datos de quien firma las peticiones: se rellenan una vez y se recuerdan en el móvil. */
data class Firmante(val unidad: String, val nombre: String, val email: String, val telefono: String)

class FirmanteRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("directorio_firmante", Context.MODE_PRIVATE)

    fun cargar() = Firmante(
        unidad = prefs.getString("unidad", "") ?: "",
        nombre = prefs.getString("nombre", "") ?: "",
        email = prefs.getString("email", "") ?: "",
        telefono = prefs.getString("telefono", "") ?: "",
    )

    fun guardar(f: Firmante) {
        prefs.edit()
            .putString("unidad", f.unidad)
            .putString("nombre", f.nombre)
            .putString("email", f.email)
            .putString("telefono", f.telefono)
            .apply()
    }
}
