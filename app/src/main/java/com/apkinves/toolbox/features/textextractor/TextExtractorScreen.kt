package com.apkinves.toolbox.features.textextractor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.TextExtractor
import com.apkinves.toolbox.ui.common.ResultBlock

@Composable
fun TextExtractorScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Extractor de enlaces/emails/teléfonos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Pega un artículo, una captura de texto o el contenido de una página y saca todos los enlaces, correos y teléfonos que contenga.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Texto") },
            modifier = Modifier.fillMaxWidth().height(180.dp),
        )
        Button(
            onClick = {
                val e = TextExtractor.extract(input)
                result = buildString {
                    appendLine("Enlaces (${e.urls.size}):")
                    if (e.urls.isEmpty()) appendLine("  (ninguno)") else e.urls.forEach { appendLine("  $it") }
                    appendLine()
                    appendLine("Correos (${e.emails.size}):")
                    if (e.emails.isEmpty()) appendLine("  (ninguno)") else e.emails.forEach { appendLine("  $it") }
                    appendLine()
                    appendLine("Teléfonos (${e.phones.size}):")
                    if (e.phones.isEmpty()) appendLine("  (ninguno)") else e.phones.forEach { appendLine("  $it") }
                }
            },
            enabled = input.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Extraer") }
        ResultBlock(result)
    }
}
