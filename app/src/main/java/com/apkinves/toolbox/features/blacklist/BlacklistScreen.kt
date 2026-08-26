package com.apkinves.toolbox.features.blacklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.BlacklistChecker
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun BlacklistScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Comprobador de listas negras",
        inputLabel = "Dirección IP (ej: 8.8.8.8)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val ip = target.trim()
                val results = BlacklistChecker.check(ip)
                val listedCount = results.count { it.listed }
                result = buildString {
                    appendLine(if (listedCount == 0) "No aparece en ninguna lista negra comprobada." else "¡Aparece en $listedCount lista(s) negra(s)!")
                    appendLine()
                    results.forEach { appendLine("${if (it.listed) "⚠" else "✓"} ${it.zone}") }
                }
                loading = false
                repo.add("Blacklist", ip, "$listedCount listados", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
