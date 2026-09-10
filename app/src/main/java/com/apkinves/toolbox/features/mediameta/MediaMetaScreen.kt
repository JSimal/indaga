package com.apkinves.toolbox.features.mediameta

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.MediaMetadataReader
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun MediaMetaScreen() {
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        loading = true
        result = ""
        scope.launch {
            MediaMetadataReader.read(context, uri).fold(
                onSuccess = { r ->
                    result = buildString {
                        r.title?.let { appendLine("Título: $it") }
                        r.artist?.let { appendLine("Artista: $it") }
                        r.album?.let { appendLine("Álbum: $it") }
                        r.date?.let { appendLine("Fecha: $it") }
                        r.durationMs?.let { appendLine("Duración: ${it / 1000}s") }
                        r.bitrateKbps?.let { appendLine("Bitrate: $it kbps") }
                        r.mimeType?.let { appendLine("Tipo: $it") }
                        if (r.hasVideo) {
                            appendLine("Vídeo: ${r.videoWidth}x${r.videoHeight} (rotación ${r.rotationDegrees ?: "0"}°)")
                        }
                        r.location?.let { appendLine("Ubicación (ISO 6709): $it") }
                        if (isBlank()) appendLine("Sin metadatos reconocibles en este archivo.")
                    }
                },
                onFailure = { result = "No se pudo leer: ${it.message}" },
            )
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Metadatos de audio/vídeo", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text("Título, artista, duración, resolución y, si el archivo la incluye, ubicación GPS.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { picker.launch(arrayOf("audio/*", "video/*")) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Leyendo..." else "Elegir archivo")
        }
        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        ResultBlock(result)
    }
}
