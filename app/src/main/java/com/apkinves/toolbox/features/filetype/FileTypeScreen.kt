package com.apkinves.toolbox.features.filetype

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
import com.apkinves.toolbox.core.util.FileSignatures
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun FileTypeScreen() {
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val detection = FileSignatures.detect(context, uri)
            result = detection.fold(
                onSuccess = {
                    buildString {
                        appendLine("Extensión declarada: ${it.declaredExtension}")
                        appendLine("Tipo real detectado (magic bytes): ${it.detectedType}")
                        appendLine(if (it.matches) "✓ Coincide con la extensión" else "⚠ NO coincide con la extensión declarada")
                        appendLine()
                        appendLine("Cabecera (hex): ${it.headerHex}")
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
        Text("Identificador de tipo de archivo", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Comprueba el tipo real de un archivo por su cabecera binaria " +
                "(magic bytes), aunque le hayan cambiado la extensión.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { launcher.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Elegir archivo")
        }
        ResultBlock(result)
    }
}
