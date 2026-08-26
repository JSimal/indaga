package com.apkinves.toolbox.features.exif

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.apkinves.toolbox.core.util.ExifExtractor
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun ExifScreen() {
    var result by remember { mutableStateOf("") }
    var mapCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val report = ExifExtractor.extract(context, uri)
            mapCoords = null
            result = report.fold(
                onSuccess = {
                    if (it.latitude != null && it.longitude != null) mapCoords = it.latitude to it.longitude
                    buildString {
                        appendLine("Cámara: ${it.make ?: "?"} ${it.model ?: ""}")
                        appendLine("Fecha: ${it.dateTime ?: "no disponible"}")
                        appendLine("Dimensiones: ${it.width ?: "?"} x ${it.height ?: "?"}")
                        appendLine("Software: ${it.software ?: "no disponible"}")
                        if (it.latitude != null) appendLine("Coordenadas GPS: ${it.latitude}, ${it.longitude}")
                        else appendLine("Sin datos de ubicación GPS")
                    }
                },
                onFailure = { "Error: ${it.message}" },
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Metadatos EXIF de imágenes", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { launcher.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Elegir imagen")
        }
        ResultBlock(result)
        mapCoords?.let { (lat, lon) ->
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon"))
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Abrir ubicación en el mapa") }
        }
    }
}
