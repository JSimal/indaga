package com.apkinves.toolbox.features.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class GithubRelease(
    val tag_name: String = "",
    val name: String = "",
    val html_url: String = "",
    val body: String = "",
    val assets: List<GithubAsset> = emptyList(),
)

@Serializable
data class GithubAsset(
    val name: String = "",
    val browser_download_url: String = "",
)

sealed class UpdateResult {
    data class UpdateAvailable(val release: GithubRelease, val apkUrl: String) : UpdateResult()
    object UpToDate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

/**
 * Comprueba la última Release publicada en GitHub y compara con la versión
 * instalada. No requiere backend propio: usa directamente la API pública de
 * GitHub (sin autenticación, con límite de 60 peticiones/hora por IP, de sobra
 * para un chequeo puntual al abrir la app).
 *
 * Sustituir GITHUB_REPO por "usuario/repositorio" antes de publicar.
 */
object UpdateChecker {

    private const val GITHUB_REPO = "TU_USUARIO/TU_REPO"
    private val json = Json { ignoreUnknownKeys = true }

    // Se comprueba como mucho una vez por sesión de la app: evita relanzar la
    // petición de red cada vez que el usuario vuelve al menú principal.
    private var cachedResult: UpdateResult? = null

    suspend fun checkForUpdate(currentVersionName: String): UpdateResult {
        cachedResult?.let { return it }
        val result = fetchUpdate(currentVersionName)
        cachedResult = result
        return result
    }

    private suspend fun fetchUpdate(currentVersionName: String): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$GITHUB_REPO/releases/latest")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val release = json.decodeFromString(GithubRelease.serializer(), body)
            val latestVersion = release.tag_name.removePrefix("v")

            if (isNewer(latestVersion, currentVersionName)) {
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                if (apkAsset != null) {
                    UpdateResult.UpdateAvailable(release, apkAsset.browser_download_url)
                } else {
                    UpdateResult.Error("Hay una release nueva pero no incluye un APK adjunto")
                }
            } else {
                UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Error desconocido comprobando actualizaciones")
        }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        fun parts(v: String) = v.split(".").mapNotNull { it.toIntOrNull() }
        val l = parts(latest)
        val c = parts(current)
        for (i in 0 until maxOf(l.size, c.size)) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv != cv) return lv > cv
        }
        return false
    }
}
