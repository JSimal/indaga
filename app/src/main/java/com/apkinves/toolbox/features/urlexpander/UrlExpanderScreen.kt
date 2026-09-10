package com.apkinves.toolbox.features.urlexpander

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.net.RedirectChecker
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun UrlExpanderScreen() {
    var url by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolScreenScaffold(
        title = "Expansor de enlaces acortados",
        inputLabel = "Enlace (bit.ly, tinyurl, t.co...)",
        inputValue = url,
        onInputChange = { url = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val target = if (url.startsWith("http")) url else "https://$url"
                val hops = runCatching { RedirectChecker.trace(target) }.getOrElse { emptyList() }
                result = if (hops.isEmpty()) "No se pudo seguir el enlace"
                else buildString {
                    appendLine("Saltos (${hops.size}):")
                    hops.forEachIndexed { i, hop -> appendLine("${i + 1}. [${hop.statusCode}] ${hop.url}") }
                    appendLine()
                    appendLine("Destino final: ${hops.last().url}")
                }
                loading = false
            }
        },
    ) {
        ResultBlock(result)
    }
}
