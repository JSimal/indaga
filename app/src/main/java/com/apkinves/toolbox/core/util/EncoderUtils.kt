package com.apkinves.toolbox.core.util

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder

object EncoderUtils {

    fun base64Encode(text: String): String = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    fun base64Decode(text: String): Result<String> = runCatching {
        String(Base64.decode(text, Base64.DEFAULT), Charsets.UTF_8)
    }

    fun hexEncode(text: String): String = text.toByteArray(Charsets.UTF_8).joinToString("") { "%02x".format(it) }
    fun hexDecode(text: String): Result<String> = runCatching {
        val clean = text.trim().replace(" ", "")
        require(clean.length % 2 == 0) { "Longitud hex inválida" }
        val bytes = ByteArray(clean.length / 2) { i -> clean.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        String(bytes, Charsets.UTF_8)
    }

    fun urlEncode(text: String): String = URLEncoder.encode(text, "UTF-8")
    fun urlDecode(text: String): Result<String> = runCatching { URLDecoder.decode(text, "UTF-8") }

    data class JwtParts(val header: String, val payload: String, val signature: String)

    fun jwtDecode(token: String): Result<JwtParts> = runCatching {
        val parts = token.trim().split(".")
        require(parts.size == 3) { "Un JWT tiene 3 partes separadas por '.'" }
        fun decodePart(p: String): String {
            val padded = p.replace('-', '+').replace('_', '/')
            val padding = (4 - padded.length % 4) % 4
            val bytes = Base64.decode(padded + "=".repeat(padding), Base64.DEFAULT)
            return String(bytes, Charsets.UTF_8)
        }
        JwtParts(decodePart(parts[0]), decodePart(parts[1]), parts[2])
    }
}
