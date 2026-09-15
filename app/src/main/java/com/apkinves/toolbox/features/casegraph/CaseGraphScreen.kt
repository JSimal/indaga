package com.apkinves.toolbox.features.casegraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.EntityCorrelator
import com.apkinves.toolbox.data.CaseRepository

@Composable
fun CaseGraphScreen() {
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }
    val entries by repo.entries.collectAsState()
    val correlations = remember(entries) { EntityCorrelator.correlate(entries) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Correlación de casos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "IPs y dominios que aparecen en más de una consulta guardada del historial, para detectar conexiones entre investigaciones distintas.",
            style = MaterialTheme.typography.bodySmall,
        )
        if (correlations.isEmpty()) {
            Text(
                "Todavía no hay entidades repetidas entre dos o más consultas guardadas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(correlations.toList()) { (entity, related) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(entity, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Aparece en ${related.map { it.id }.distinct().size} consultas:", style = MaterialTheme.typography.labelSmall)
                        related.distinctBy { it.id }.forEach { entry ->
                            Text("• ${entry.tool} — ${entry.target}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
