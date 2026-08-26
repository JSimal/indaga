package com.apkinves.toolbox.core.util

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ExifExtractor {

    data class ExifReport(
        val make: String?,
        val model: String?,
        val dateTime: String?,
        val latitude: Double?,
        val longitude: Double?,
        val width: String?,
        val height: String?,
        val software: String?,
    )

    suspend fun extract(context: Context, uri: Uri): Result<ExifReport> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val latLong = exif.latLong

                ExifReport(
                    make = exif.getAttribute(ExifInterface.TAG_MAKE),
                    model = exif.getAttribute(ExifInterface.TAG_MODEL),
                    dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME),
                    latitude = latLong?.get(0),
                    longitude = latLong?.get(1),
                    width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH),
                    height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH),
                    software = exif.getAttribute(ExifInterface.TAG_SOFTWARE),
                )
            } ?: error("No se pudo abrir la imagen")
        }
    }
}
