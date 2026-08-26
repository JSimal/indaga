package com.apkinves.toolbox.features.techdetector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.TechDetector
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun TechDetectorScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Detector de tecnologías",
        inputLabel = "URL o dominio",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val url = target.trim()
                val report = runCatching { TechDetector.detect(url) }.getOrNull()?.getOrNull()
                result = if (report == null) "No se pudo analizar $url"
                else buildString {
                    appendLine("Servidor: ${report.server ?: "desconocido"}")
                    if (report.poweredBy != null) appendLine("X-Powered-By: ${report.poweredBy}")
                    appendLine()
                    appendLine("Tecnologías detectadas:")
                    if (report.detected.isEmpty()) appendLine("  (ninguna de las conocidas)")
                    else report.detected.forEach { appendLine("  - $it") }
                }
                loading = false
                repo.add("Tech Detector", url, result.lineSequence().firstOrNull() ?: "", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
