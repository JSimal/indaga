package com.apkinves.toolbox.features.imageforensics

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.ImageForensics
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun ImageForensicsScreen() {
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var heatmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        loading = true
        result = ""
        heatmap = null
        scope.launch {
            ImageForensics.analyze(context, uri).fold(
                onSuccess = { r ->
                    heatmap = r.heatmap.asImageBitmap()
                    result = buildString {
                        if (!r.likelyJpeg) appendLine("⚠ La imagen no parece ser JPEG original: el análisis ELA pierde fiabilidad (funciona comparando niveles de recompresión JPEG).")
                        appendLine("Diferencia media: %.2f".format(r.averageDifference))
                        appendLine("Diferencia máxima: ${r.maxDifference}")
                        appendLine()
                        appendLine("Las zonas más brillantes del mapa han cambiado más al recomprimir: pueden indicar una edición o un pegado de otra imagen, aunque también aparecen por bordes nítidos o texto sin que haya manipulación.")
                    }
                },
                onFailure = { result = "No se pudo analizar: ${it.message}" },
            )
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Análisis de manipulación (ELA)", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Recomprime la imagen en JPEG y compara con el original: las zonas editadas o pegadas suelen recomprimirse de forma distinta y quedan resaltadas. Es una heurística, no una prueba concluyente.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { picker.launch(arrayOf("image/*")) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Analizando..." else "Elegir imagen")
        }
        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        heatmap?.let { Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxWidth()) }
        ResultBlock(result)
    }
}
