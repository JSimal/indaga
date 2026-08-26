package com.apkinves.toolbox.core.util

import java.security.MessageDigest

object HashUtils {

    val ALGORITHMS = listOf("MD5", "SHA-1", "SHA-256", "SHA-512")

    fun hash(text: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashAll(text: String): Map<String, String> = ALGORITHMS.associateWith { hash(text, it) }
}
