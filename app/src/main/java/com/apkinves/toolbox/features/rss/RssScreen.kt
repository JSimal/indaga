package com.apkinves.toolbox.features.rss

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.RssClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun RssScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Lector RSS/Atom",
        inputLabel = "URL del feed",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val url = target.trim()
                val report = runCatching { RssClient.fetch(url) }.getOrNull()?.getOrNull()
                result = if (report == null) "No se pudo leer el feed"
                else buildString {
                    appendLine("Feed: ${report.feedTitle ?: "(sin título)"}")
                    appendLine("${report.items.size} entradas:\n")
                    report.items.forEach { appendLine("• ${it.title}\n  ${it.link}\n") }
                }
                loading = false
                repo.add("RSS", url, "${report?.items?.size ?: 0} entradas", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
