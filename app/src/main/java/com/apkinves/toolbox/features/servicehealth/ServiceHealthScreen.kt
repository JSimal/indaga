package com.apkinves.toolbox.features.servicehealth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.ServiceHealthClient
import com.apkinves.toolbox.ui.theme.CyberColors
import kotlinx.coroutines.launch

private fun statusEmoji(status: ServiceHealthClient.Status): String = when (status) {
    ServiceHealthClient.Status.UP -> "🟢"
    ServiceHealthClient.Status.DEGRADED -> "🟡"
    ServiceHealthClient.Status.DOWN -> "🔴"
}

private fun statusColor(status: ServiceHealthClient.Status) = when (status) {
    ServiceHealthClient.Status.UP -> CyberColors.NeonGreen
    ServiceHealthClient.Status.DEGRADED -> CyberColors.NeonAmber
    ServiceHealthClient.Status.DOWN -> CyberColors.NeonRed
}

@Composable
fun ServiceHealthScreen() {
    var results by remember { mutableStateOf<List<ServiceHealthClient.ServiceCheck>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun runCheck() {
        loading = true
        scope.launch {
            results = ServiceHealthClient.checkAll()
            loading = false
        }
    }

    LaunchedEffect(Unit) { runCheck() }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Estado de servicios", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Comprueba si las APIs externas gratuitas que usa la app están respondiendo ahora mismo. " +
                "Un servicio caído explica por qué una consulta concreta puede fallar sin que sea un fallo de Indaga.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { runCheck() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Comprobando..." else "Volver a comprobar")
        }
        if (loading && results.isEmpty()) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        if (results.isNotEmpty()) {
            val up = results.count { it.status == ServiceHealthClient.Status.UP }
            Text(
                "$up/${results.size} servicios respondiendo con normalidad",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        results.forEach { check ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(statusEmoji(check.status), style = MaterialTheme.typography.titleSmall)
                        Text(check.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    val detail = buildString {
                        check.httpCode?.let { append("HTTP $it") }
                        check.rttMs?.let { if (isNotEmpty()) append(" · "); append("${it} ms") }
                    }
                    if (detail.isNotEmpty()) Text(detail, style = MaterialTheme.typography.bodySmall, color = statusColor(check.status))
                    check.detail?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}
