package com.apkinves.toolbox.features.unified

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.unified.InputKind
import com.apkinves.toolbox.core.unified.UnifiedQueryEngine
import com.apkinves.toolbox.core.unified.UnifiedReport
import com.apkinves.toolbox.core.unified.detectKind
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.UnifiedSummaryCards
import kotlinx.coroutines.launch

@Composable
fun UnifiedQueryScreen() {
    var target by remember { mutableStateOf("") }
    var report by remember { mutableStateOf<UnifiedReport?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    var showRaw by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Consulta única", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Introduce una IP o un dominio: registrador, fechas, DNS, hosting, " +
                "VPN/proxy y puertos comunes, todo en una sola consulta.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("IP o dominio") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = {
                val value = target.trim()
                val kind = detectKind(value)
                if (kind == InputKind.UNKNOWN) {
                    errorText = "No se reconoce como IP ni como dominio."
                    report = null
                    return@Button
                }
                errorText = ""
                loading = true
                report = null
                scope.launch {
                    val result = UnifiedQueryEngine.run(value, kind)
                    report = result
                    loading = false
                    repo.add("Consulta única", value, "Informe combinado", UnifiedQueryEngine.buildRawSummary(result))
                }
            },
            enabled = target.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (loading) "Consultando todo..." else "Lanzar todas las consultas")
        }

        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (errorText.isNotBlank()) Text(errorText, color = MaterialTheme.colorScheme.error)

        report?.let { r -> UnifiedSummaryCards(r) }

        if (report != null) {
            Button(onClick = { showRaw = !showRaw }, modifier = Modifier.fillMaxWidth()) {
                Text(if (showRaw) "Ocultar WHOIS en crudo" else "Ver WHOIS en crudo")
            }
            if (showRaw) ResultBlock(report?.rawWhois.orEmpty())
        }
    }
}
