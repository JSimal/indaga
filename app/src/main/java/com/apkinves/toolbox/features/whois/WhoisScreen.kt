package com.apkinves.toolbox.features.whois

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.WhoisClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun WhoisScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "WHOIS",
        inputLabel = "Dominio o IP (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                result = runCatching {
                    if (target.contains(".") && target.toIntOrNullEachOctet()) {
                        WhoisClient.lookupIp(target.trim())
                    } else {
                        WhoisClient.lookupDomain(target.trim())
                    }
                }.getOrElse { "Error: ${it.message}" }
                loading = false
                repo.add("WHOIS", target.trim(), result.take(120), result)
            }
        },
    ) {
        ResultBlock(result)
    }
}

private fun String.toIntOrNullEachOctet(): Boolean =
    trim().split(".").let { parts -> parts.size == 4 && parts.all { part -> (part.toIntOrNull() ?: -1) in 0..255 } }
