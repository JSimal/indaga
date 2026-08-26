package com.apkinves.toolbox.features.emailsec

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.EmailSecurityClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun EmailSecScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Seguridad de email de un dominio",
        inputLabel = "Dominio (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val domain = target.trim()
                val report = EmailSecurityClient.analyze(domain)
                result = buildString {
                    appendLine("Nota: ${report.grade}")
                    appendLine()
                    appendLine("SPF: ${report.spfRecord ?: "no encontrado"}")
                    appendLine("DMARC: ${report.dmarcRecord ?: "no encontrado"}")
                    if (report.notes.isNotEmpty()) {
                        appendLine()
                        appendLine("Observaciones:")
                        report.notes.forEach { appendLine("  - $it") }
                    }
                }
                loading = false
                repo.add("Email Security", domain, "Nota ${report.grade}", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
