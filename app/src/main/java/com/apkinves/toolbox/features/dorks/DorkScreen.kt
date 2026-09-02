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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.DorkGenerator
import com.apkinves.toolbox.ui.theme.CyberColors

private fun riskColor(risk: DorkGenerator.Risk): Color = when (risk) {
    DorkGenerator.Risk.ALTO -> CyberColors.NeonRed
    DorkGenerator.Risk.MEDIO -> CyberColors.NeonAmber
    DorkGenerator.Risk.BAJO -> CyberColors.NeonGreen
}

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
            "Reconocimiento pasivo sobre un dominio, ordenado de mayor a " +
                "menor interés para una investigación: primero lo que suele " +
                "revelar fugas de datos reales (repos .git, backups, " +
                "credenciales, Pastebin, buckets S3...), luego superficie de " +
                "ataque (paneles, APIs, documentos), y por último " +
                "reconocimiento general.",
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                dork.risk.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = riskColor(dork.risk),
                                fontWeight = FontWeight.Bold,
                            )
                            Text(dork.label, style = MaterialTheme.typography.titleSmall)
                        }
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
