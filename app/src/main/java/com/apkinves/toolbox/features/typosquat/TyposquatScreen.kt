package com.apkinves.toolbox.features.typosquat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.TyposquattingDetector
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun TyposquatScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Detector de typosquatting",
        inputLabel = "Tu dominio (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val domain = target.trim()
                val variants = TyposquattingDetector.generateVariants(domain)
                val checked = TyposquattingDetector.checkRegistered(variants)
                val registered = checked.filter { it.registered }
                result = buildString {
                    appendLine("${variants.size} variantes generadas, ${registered.size} ya están registradas:")
                    appendLine()
                    if (registered.isEmpty()) appendLine("(ninguna registrada, buena señal)")
                    else registered.forEach { appendLine("  ⚠ ${it.domain}") }
                }
                loading = false
                repo.add("Typosquatting", domain, "${registered.size} variantes registradas", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
