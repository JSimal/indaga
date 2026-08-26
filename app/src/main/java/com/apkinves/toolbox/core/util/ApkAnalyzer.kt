package com.apkinves.toolbox.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Analiza un APK que el propio usuario elige con el selector de archivos.
 * A propósito NO usa QUERY_ALL_PACKAGES ni enumera apps instaladas: solo
 * inspecciona el fichero .apk indicado, igual que si lo abrieras con
 * cualquier gestor de archivos.
 */
object ApkAnalyzer {

    data class ApkReport(
        val packageName: String,
        val versionName: String?,
        val versionCode: Long,
        val minSdk: Int,
        val targetSdk: Int,
        val permissions: List<String>,
        val signers: List<String>,
    )

    suspend fun analyze(context: Context, uri: Uri): Result<ApkReport> = withContext(Dispatchers.IO) {
        runCatching {
            // PackageManager necesita una ruta de fichero real, así que copiamos el
            // contenido a un temporal en la caché de la app (se borra al salir).
            val tempFile = File.createTempFile("analizado_", ".apk", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: error("No se pudo leer el archivo")

            try {
                val pm = context.packageManager
                val flags = PackageManager.GET_PERMISSIONS or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES

                val info = pm.getPackageArchiveInfo(tempFile.absolutePath, flags)
                    ?: error("El archivo no es un APK válido o está dañado")

                val signers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    info.signingInfo?.apkContentsSigners?.map { it.toCharsString().take(16) + "…" } ?: emptyList()
                } else {
                    @Suppress("DEPRECATION")
                    info.signatures?.map { it.toCharsString().take(16) + "…" } ?: emptyList()
                }

                ApkReport(
                    packageName = info.packageName,
                    versionName = info.versionName,
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode.toLong(),
                    minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) info.applicationInfo?.minSdkVersion ?: -1 else -1,
                    targetSdk = info.applicationInfo?.targetSdkVersion ?: -1,
                    permissions = info.requestedPermissions?.toList() ?: emptyList(),
                    signers = signers,
                )
            } finally {
                tempFile.delete()
            }
        }
    }
}
