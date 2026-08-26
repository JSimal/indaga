package com.apkinves.toolbox.features.nfc

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.NfcTagHolder
import com.apkinves.toolbox.ui.common.ResultBlock

@Composable
fun NfcScreen() {
    val tag by NfcTagHolder.lastTag.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Lector NFC", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Con esta pantalla abierta, acerca un tag/tarjeta NFC a la parte " +
                "trasera del móvil.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (tag == null) {
            Text("Esperando un tag NFC...", style = MaterialTheme.typography.bodyMedium)
        } else {
            val t = tag!!
            ResultBlock(
                buildString {
                    appendLine("ID: ${t.idHex}")
                    appendLine("Tecnologías: ${t.techList.joinToString(", ")}")
                    if (t.ndefRecords.isNotEmpty()) {
                        appendLine()
                        appendLine("Contenido NDEF:")
                        t.ndefRecords.forEach { appendLine("  $it") }
                    } else {
                        appendLine()
                        appendLine("(sin mensaje NDEF legible)")
                    }
                },
            )
            Button(onClick = { NfcTagHolder.clear() }) { Text("Limpiar") }
        }
    }
}
