package com.apkinves.toolbox.core.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class AppStatus(
    val enabled: Boolean = true,
    val message: String = "",
    val minVersionCode: Int = 0,
    // Reservado para una futura capa de licencia (ej. "1€ simbólico"): de
    // momento no se usa para cobrar nada, solo queda el campo preparado.
    val requiresLicense: Boolean = false,
)

/**
 * Interruptor remoto de emergencia: consulta un JSON público en el propio
 * repo de GitHub (servido directo, sin límite de peticiones de la API) para
 * saber si la app debe seguir funcionando. Pensado para casos extremos (bug
 * de seguridad grave, abuso...), no es DRM ni control de piratería.
 *
 * Si la consulta falla (sin internet, GitHub caído...) se asume "todo bien"
 * (fail-open): un interruptor de emergencia nunca debe bloquear a alguien
 * solo porque no tiene cobertura en ese momento.
 */
object AppStatusClient {

    private const val STATUS_URL = "https://raw.githubusercontent.com/JSimal/indaga/main/app-status.json"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(): AppStatus = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(STATUS_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val body = try {
                if (conn.responseCode !in 200..299) error("status ${conn.responseCode}")
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }
            json.decodeFromString(AppStatus.serializer(), body)
        }.getOrDefault(AppStatus(enabled = true))
    }
}
