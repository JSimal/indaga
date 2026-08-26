package com.apkinves.toolbox.features.portscanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.PortScanner
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun PortScannerScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Escáner de puertos",
        inputLabel = "Host o IP (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val host = target.trim()
                val results = runCatching { PortScanner.scan(host) }.getOrElse { emptyList() }
                result = if (results.isEmpty()) {
                    "No se pudo escanear el host (¿nombre correcto? ¿hay conexión?)"
                } else {
                    buildString {
                        appendLine("Puertos abiertos en $host:")
                        val open = results.filter { it.open }
                        if (open.isEmpty()) appendLine("  Ninguno de los puertos comunes está abierto")
                        open.forEach { appendLine("  ${it.port}/tcp  ${it.service}") }
                    }
                }
                loading = false
                repo.add("Port Scanner", host, "Puertos comunes escaneados", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
