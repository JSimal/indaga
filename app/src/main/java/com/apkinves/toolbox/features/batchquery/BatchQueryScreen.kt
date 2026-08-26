package com.apkinves.toolbox.features.batchquery

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.unified.InputKind
import com.apkinves.toolbox.core.unified.UnifiedQueryEngine
import com.apkinves.toolbox.core.unified.UnifiedReport
import com.apkinves.toolbox.core.unified.detectKind
import com.apkinves.toolbox.ui.common.UnifiedSummaryCards
import kotlinx.coroutines.launch

@Composable
fun BatchQueryScreen() {
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var progressText by remember { mutableStateOf("") }
    val reports = remember { mutableStateListOf<UnifiedReport>() }
    val expanded = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Consulta por lotes", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Pega varios dominios o IPs, uno por línea. Se consultan uno a " +
                "uno (no en paralelo entre sí, para no saturar la red) y puedes " +
                "compartir un informe combinado al final.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Un dominio o IP por línea") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
        Button(
            onClick = {
                val targets = input.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.distinct().toList()
                if (targets.isEmpty()) return@Button
                loading = true
                reports.clear()
                scope.launch {
                    targets.forEachIndexed { i, t ->
                        progressText = "Consultando ${i + 1}/${targets.size}: $t"
                        val kind = detectKind(t)
                        if (kind != InputKind.UNKNOWN) {
                            reports.add(UnifiedQueryEngine.run(t, kind))
                        }
                    }
                    progressText = ""
                    loading = false
                }
            },
            enabled = input.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Consultando..." else "Ejecutar consulta por lotes") }

        if (loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(progressText, style = MaterialTheme.typography.bodySmall)
        }

        if (reports.isNotEmpty()) {
            Button(
                onClick = {
                    val combined = reports.joinToString("\n\n") { UnifiedQueryEngine.buildRawSummary(it) }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Informe de investigación (lote)")
                        putExtra(Intent.EXTRA_TEXT, combined)
                    }
                    runCatching { context.startActivity(Intent.createChooser(intent, "Compartir informe")) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Compartir informe combinado (${reports.size} objetivos)") }
        }

        reports.forEach { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(r.target, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = {
                        if (expanded.contains(r.target)) expanded.remove(r.target) else expanded.add(r.target)
                    }) { Text(if (expanded.contains(r.target)) "Ocultar detalle" else "Ver detalle") }
                    if (expanded.contains(r.target)) UnifiedSummaryCards(r)
                }
            }
        }
    }
}
