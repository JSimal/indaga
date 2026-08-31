package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Comprobador de presencia de un username en varias plataformas, sin API ni
 * key: para cada una se pide su URL de perfil y se mira el código HTTP (404
 * = no existe). Solo se incluyen plataformas donde esa comprobación es
 * fiable — muchas redes (Instagram, X/Twitter, TikTok...) son aplicaciones
 * de una sola página que devuelven 200 siempre, con o sin usuario real, así
 * que darían falsos positivos y se han dejado fuera a propósito.
 */
object UsernameCheckClient {

    data class PlatformResult(val platform: String, val url: String, val exists: Boolean?)

    private val PLATFORMS = listOf(
        "GitHub" to "https://github.com/%s",
        "GitLab" to "https://gitlab.com/%s",
        "Codeberg" to "https://codeberg.org/%s",
        "Reddit" to "https://www.reddit.com/user/%s/",
        "Twitch" to "https://www.twitch.tv/%s",
        "PyPI" to "https://pypi.org/user/%s/",
        "npm" to "https://www.npmjs.com/~%s",
        "Keybase" to "https://keybase.io/%s",
        "Medium" to "https://medium.com/@%s",
        "Pinterest" to "https://www.pinterest.com/%s/",
        "SoundCloud" to "https://soundcloud.com/%s",
        "Vimeo" to "https://vimeo.com/%s",
        "Dev.to" to "https://dev.to/%s",
        "Dockerhub" to "https://hub.docker.com/u/%s",
    )

    suspend fun check(username: String): List<PlatformResult> = withContext(Dispatchers.IO) {
        val clean = username.trim()
        PLATFORMS.map { (name, template) ->
            async {
                val url = template.format(clean)
                val exists = runCatching { checkExists(url) }.getOrNull()
                PlatformResult(name, url, exists)
            }
        }.awaitAll()
    }

    private fun checkExists(urlStr: String): Boolean {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Indaga-App)")
        return try {
            conn.responseCode != 404
        } finally {
            conn.disconnect()
        }
    }
}
