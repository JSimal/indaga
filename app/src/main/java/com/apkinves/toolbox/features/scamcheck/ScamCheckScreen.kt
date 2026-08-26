package com.apkinves.toolbox.features.scamcheck

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.net.URLEncoder

private data class ScamSite(val label: String, val urlTemplate: String, val directLookup: Boolean)

private val SITES = listOf(
    ScamSite("ScamAdviser", "https://www.scamadviser.com/check-website/%s", directLookup = true),
    ScamSite("ScamCharge", "https://scamcharge.com/?s=%s", directLookup = false),
    ScamSite("Fraud.org", "https://fraud.org/?s=%s", directLookup = false),
)

@Composable
fun ScamCheckScreen() {
    var domain by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun open(template: String) {
        val encoded = URLEncoder.encode(domain.trim(), "UTF-8")
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(template.format(encoded)))) }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Verificación de fraude/scam", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Ninguno de estos servicios ofrece una API gratuita, así que se " +
                "abren directamente en el navegador con el dominio ya puesto. " +
                "ScamAdviser sí tiene una URL de consulta directa confirmada; " +
                "ScamCharge y Fraud.org pueden abrir una búsqueda genérica en " +
                "vez de una ficha específica del dominio, según cómo esté " +
                "montada su web en cada momento.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = domain,
            onValueChange = { domain = it },
            label = { Text("Dominio (ej: ejemplo.com)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        SITES.forEach { site ->
            Button(onClick = { open(site.urlTemplate) }, enabled = domain.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(if (site.directLookup) "Comprobar en ${site.label}" else "Buscar en ${site.label} (no garantizado)")
            }
        }
    }
}
