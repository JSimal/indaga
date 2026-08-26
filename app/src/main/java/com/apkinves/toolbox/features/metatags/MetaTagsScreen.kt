package com.apkinves.toolbox.features.metatags

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.MetaExtractor
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun MetaTagsScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Metadatos y Open Graph",
        inputLabel = "URL (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val url = target.trim()
                val report = runCatching { MetaExtractor.extract(url) }.getOrNull()?.getOrNull()
                result = if (report == null) "No se pudo analizar $url"
                else buildString {
                    appendLine("Título: ${report.title ?: "(sin título)"}")
                    appendLine()
                    if (report.tags.isEmpty()) appendLine("Sin etiquetas Open Graph/Twitter")
                    else report.tags.forEach { (k, v) -> appendLine("$k: $v") }
                }
                loading = false
                repo.add("Meta tags", url, result.lineSequence().firstOrNull() ?: "", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
