package com.apkinves.toolbox.features.history

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.data.CaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }
    val entries by repo.entries.collectAsState()
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Historial / Caso de investigación", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text("${entries.size} consultas guardadas (máx. 200)", style = MaterialTheme.typography.bodySmall)

        Button(
            onClick = {
                val markdown = repo.exportAsMarkdown()
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Informe de investigación")
                    putExtra(Intent.EXTRA_TEXT, markdown)
                }
                runCatching { context.startActivity(Intent.createChooser(intent, "Compartir informe")) }
            },
            enabled = entries.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Compartir informe") }

        Button(onClick = { scope.launch { repo.clear() } }, modifier = Modifier.fillMaxWidth()) {
            Text("Borrar historial")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(entries) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${entry.tool} — ${entry.target}", style = MaterialTheme.typography.titleSmall)
                        Text(dateFormat.format(Date(entry.timestamp * 1000)), style = MaterialTheme.typography.bodySmall)
                        Text(entry.summary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
