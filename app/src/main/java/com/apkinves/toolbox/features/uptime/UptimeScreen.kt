package com.apkinves.toolbox.features.uptime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.UptimeChecker
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun UptimeScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "¿Está caído?",
        inputLabel = "URL (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val url = target.trim()
                val r = UptimeChecker.check(url)
                result = if (r.up) "✓ $url está arriba (código ${r.statusCode}, ${r.rttMs} ms)"
                else "✗ $url no responde correctamente (código ${r.statusCode}${r.error?.let { ", $it" } ?: ""})"
                loading = false
                repo.add("Uptime", url, result, result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
