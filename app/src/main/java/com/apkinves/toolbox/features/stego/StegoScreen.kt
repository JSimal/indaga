package com.apkinves.toolbox.features.stego

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
import com.apkinves.toolbox.core.util.StegoDetector
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun StegoScreen() {
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val report = StegoDetector.analyze(context, uri)
            result = report.fold(
                onSuccess = {
                    buildString {
                        appendLine("Píxeles muestreados: ${it.sampledPixels}")
                        appendLine("Proporción de bits '1' en el LSB: %.4f".format(it.lsbOnesRatio))
                        appendLine()
                        appendLine(
                            if (it.suspicious) "⚠ El bit menos significativo se comporta de forma muy cercana al azar puro, lo cual es compatible con datos ocultos (no es una prueba definitiva)."
                            else "Sin indicios claros de esteganografía LSB básica.",
                        )
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
        Text("Detector de esteganografía (básico)", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Heurística simple sobre el bit menos significativo de la imagen. " +
                "No sustituye a herramientas de esteganálisis profesionales.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { launcher.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Elegir imagen")
        }
        ResultBlock(result)
    }
}
