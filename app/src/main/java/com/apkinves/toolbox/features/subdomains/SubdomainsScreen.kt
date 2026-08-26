package com.apkinves.toolbox.features.subdomains

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.SubdomainFinder
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun SubdomainsScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Buscador de subdominios",
        inputLabel = "Dominio (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val domain = target.trim()
                val subs = runCatching { SubdomainFinder.find(domain) }.getOrElse { listOf("Error: ${it.message}") }
                result = if (subs.isEmpty()) "No se encontraron subdominios en Certificate Transparency."
                else "Encontrados ${subs.size}:\n\n" + subs.joinToString("\n")
                loading = false
                repo.add("Subdominios", domain, "${subs.size} encontrados", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
