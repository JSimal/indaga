package com.apkinves.toolbox.features.redirects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.RedirectChecker
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun RedirectsScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Cadena de redirecciones",
        inputLabel = "URL (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val url = target.trim()
                val hops = runCatching { RedirectChecker.trace(url) }.getOrElse { emptyList() }
                result = hops.mapIndexed { i, hop -> "${i + 1}. [${hop.statusCode}] ${hop.url}" }.joinToString("\n")
                loading = false
                repo.add("Redirecciones", url, "${hops.size} saltos", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
