package com.apkinves.toolbox.features.pdfmeta

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
import com.apkinves.toolbox.core.util.PdfMetadataExtractor
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun PdfMetaScreen() {
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val report = PdfMetadataExtractor.extract(context, uri)
            result = report.fold(
                onSuccess = {
                    if (it.fields.isEmpty()) "No se encontraron metadatos legibles (puede que el PDF esté comprimido internamente)."
                    else it.fields.entries.joinToString("\n") { (k, v) -> "$k: $v" }
                },
                onFailure = { "Error: ${it.message}" },
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Metadatos de PDF", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Extrae autor, creador y fechas ocultas del documento (extracción " +
                "básica, puede no funcionar con PDFs con streams comprimidos).",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { launcher.launch(arrayOf("application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Elegir PDF")
        }
        ResultBlock(result)
    }
}
