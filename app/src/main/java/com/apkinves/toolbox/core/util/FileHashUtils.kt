package com.apkinves.toolbox.core.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

object FileHashUtils {

    suspend fun hashFile(context: Context, uri: Uri, algorithm: String = "SHA-256"): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val digest = MessageDigest.getInstance(algorithm)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        digest.update(buffer, 0, read)
                    }
                } ?: error("No se pudo abrir el archivo")
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        }
}
