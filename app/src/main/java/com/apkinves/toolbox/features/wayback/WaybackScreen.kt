package com.apkinves.toolbox.features.wayback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.WaybackClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun WaybackScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Wayback Machine",
        inputLabel = "URL (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val url = target.trim()
                val snapshot = runCatching { WaybackClient.closestSnapshot(url) }.getOrNull()?.getOrNull()
                result = if (snapshot == null) "No hay copias archivadas de esta URL en Wayback Machine."
                else "Copia más cercana:\n${snapshot.url}\n\nFecha: ${snapshot.timestamp}\nEstado HTTP original: ${snapshot.status}"
                loading = false
                repo.add("Wayback Machine", url, result.lineSequence().firstOrNull() ?: "", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
