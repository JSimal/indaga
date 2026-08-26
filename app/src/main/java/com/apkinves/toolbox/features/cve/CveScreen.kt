package com.apkinves.toolbox.features.cve

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.CveClient
import com.apkinves.toolbox.ui.theme.CyberColors
import kotlinx.coroutines.launch

private fun severityColor(severity: String?): Color = when (severity?.uppercase()) {
    "CRITICAL" -> CyberColors.NeonRed
    "HIGH" -> CyberColors.NeonOrange
    "MEDIUM" -> CyberColors.NeonAmber
    "LOW" -> CyberColors.NeonGreen
    else -> Color.Gray
}

@Composable
fun CveScreen() {
    var keyword by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<CveClient.CveResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Vulnerabilidades (CVE)", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Busca por tecnología, producto o versión (ej. \"apache 2.4.49\", " +
                "\"log4j\", \"wordpress 6.0\") en la base de datos oficial NVD del NIST.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Palabra clave") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = {
                loading = true
                error = ""
                results = emptyList()
                scope.launch {
                    val r = CveClient.search(keyword)
                    r.fold(
                        onSuccess = { results = it },
                        onFailure = { error = it.message ?: "Error desconocido" },
                    )
                    loading = false
                }
            },
            enabled = keyword.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Buscando..." else "Buscar CVEs") }

        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (error.isNotBlank()) Text("Error: $error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (!loading && error.isBlank() && results.isEmpty() && keyword.isNotBlank()) {
            Text("Sin resultados (esto no garantiza que no existan CVEs, prueba con otra palabra clave).", style = MaterialTheme.typography.bodySmall)
        }

        results.forEach { cve ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(cve.id, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (cve.severity != null) {
                            Text(
                                "${cve.severity} ${cve.score?.let { "(%.1f)".format(it) } ?: ""}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = severityColor(cve.severity),
                            )
                        }
                    }
                    Text("Publicado: ${cve.published}", style = MaterialTheme.typography.bodySmall)
                    Text(cve.description, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cve.detailUrl))) }
                    }) { Text("Ver ficha completa (NVD)") }
                }
            }
        }
    }
}
