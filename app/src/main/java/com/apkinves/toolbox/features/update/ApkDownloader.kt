package com.apkinves.toolbox.features.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    data class Progress(val bytesRead: Long, val totalBytes: Long) : DownloadState()
    data class Done(val file: File) : DownloadState()
    data class Failed(val message: String) : DownloadState()
}

/** Descarga el APK de la Release a la caché de la app, emitiendo progreso real en cada trozo leído. */
object ApkDownloader {

    fun download(context: Context, url: String, fileName: String): Flow<DownloadState> = flow {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        conn.instanceFollowRedirects = true
        conn.connect()

        val totalBytes = conn.contentLengthLong
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val outFile = File(dir, fileName)

        conn.inputStream.use { input ->
            outFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    bytesRead += read
                    emit(DownloadState.Progress(bytesRead, totalBytes))
                }
            }
        }
        conn.disconnect()
        emit(DownloadState.Done(outFile))
    }.flowOn(Dispatchers.IO)
        .catch { e -> emit(DownloadState.Failed(e.message ?: "Error de descarga")) }
}
