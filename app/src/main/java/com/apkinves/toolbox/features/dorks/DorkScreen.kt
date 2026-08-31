package com.apkinves.toolbox.features.dorks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.DorkGenerator

@Composable
fun DorkScreen() {
    var domain by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Google/Bing Dorks", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Reconocimiento pasivo: genera búsquedas avanzadas sobre un " +
                "dominio (documentos expuestos, paneles de admin, backups...) " +
                "y las abre directamente en el buscador.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = domain,
            onValueChange = { domain = it },
            label = { Text("Dominio (ej: ejemplo.com)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (domain.isNotBlank()) {
            DorkGenerator.forDomain(domain).forEach { dork ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(dork.label, style = MaterialTheme.typography.titleSmall)
                        Text(dork.query, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { open(DorkGenerator.googleUrl(dork.query)) }) { Text("Google") }
                            Button(onClick = { open(DorkGenerator.bingUrl(dork.query)) }) { Text("Bing") }
                        }
                    }
                }
            }
        }
    }
}
