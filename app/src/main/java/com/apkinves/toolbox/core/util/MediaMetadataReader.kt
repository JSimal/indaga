package com.apkinves.toolbox.core.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Metadatos de audio/vídeo (ID3, atados MP4...) vía MediaMetadataRetriever, sin librerías externas. */
object MediaMetadataReader {
    data class MediaReport(
        val title: String?,
        val artist: String?,
        val album: String?,
        val date: String?,
        val durationMs: Long?,
        val bitrateKbps: Long?,
        val mimeType: String?,
        val hasVideo: Boolean,
        val videoWidth: String?,
        val videoHeight: String?,
        val rotationDegrees: String?,
        val location: String?,
    )

    suspend fun read(context: Context, uri: Uri): Result<MediaReport> = withContext(Dispatchers.IO) {
        runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                fun key(k: Int) = retriever.extractMetadata(k)
                MediaReport(
                    title = key(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    artist = key(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: key(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                    album = key(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    date = key(MediaMetadataRetriever.METADATA_KEY_DATE),
                    durationMs = key(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull(),
                    bitrateKbps = key(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()?.div(1000),
                    mimeType = key(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                    hasVideo = key(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == "yes",
                    videoWidth = key(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
                    videoHeight = key(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
                    rotationDegrees = key(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION),
                    location = key(MediaMetadataRetriever.METADATA_KEY_LOCATION),
                )
            } finally {
                retriever.release()
            }
        }
    }
}
