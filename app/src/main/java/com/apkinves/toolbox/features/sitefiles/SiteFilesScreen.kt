package com.apkinves.toolbox.features.sitefiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.SiteFilesClient
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun SiteFilesScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolScreenScaffold(
        title = "robots.txt / sitemap.xml",
        inputLabel = "Dominio (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        runEnabled = false,
        onRun = {},
        extraControls = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    loading = true
                    scope.launch {
                        val r = SiteFilesClient.fetch(target.trim(), "robots.txt")
                        result = if (r.found) r.content else "No encontrado (código ${r.statusCode})"
                        loading = false
                    }
                }, enabled = target.isNotBlank() && !loading) { Text("robots.txt") }

                Button(onClick = {
                    loading = true
                    scope.launch {
                        val r = SiteFilesClient.fetch(target.trim(), "sitemap.xml")
                        result = if (r.found) r.content else "No encontrado (código ${r.statusCode})"
                        loading = false
                    }
                }, enabled = target.isNotBlank() && !loading) { Text("sitemap.xml") }
            }
        },
    ) {
        ResultBlock(result)
    }
}
