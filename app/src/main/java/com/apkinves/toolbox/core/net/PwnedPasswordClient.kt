package com.apkinves.toolbox.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Comprobación de contraseñas filtradas contra Have I Been Pwned, usando su
 * modelo de "k-anonimato": solo se envían los 5 primeros caracteres del hash
 * SHA-1 de la contraseña, nunca la contraseña ni el hash completo. El
 * servidor devuelve todos los sufijos que empiezan por esos 5 caracteres (miles
 * de ellos) y la comparación del resto se hace localmente en el móvil. API
 * gratuita, sin registro ni key.
 */
object PwnedPasswordClient {

    data class PwnedResult(val timesSeen: Int)

    suspend fun check(password: String): Result<PwnedResult> = withContext(Dispatchers.IO) {
        runCatching {
            val sha1 = MessageDigest.getInstance("SHA-1").digest(password.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02X".format(it) }
            val prefix = sha1.take(5)
            val suffix = sha1.substring(5)

            val conn = URL("https://api.pwnedpasswords.com/range/$prefix").openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Add-Padding", "true")
            val body = try {
                conn.inputStream.bufferedReader().readText()
            } finally {
                conn.disconnect()
            }

            val match = body.lineSequence()
                .map { it.trim().split(":") }
                .firstOrNull { it.size == 2 && it[0].equals(suffix, ignoreCase = true) }

            PwnedResult(timesSeen = match?.get(1)?.toIntOrNull() ?: 0)
        }
    }
}
