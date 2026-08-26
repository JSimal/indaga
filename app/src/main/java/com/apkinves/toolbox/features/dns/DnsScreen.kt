package com.apkinves.toolbox.features.dns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.DnsClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun DnsScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "DNS Lookup",
        inputLabel = "Dominio (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val domain = target.trim()
                val types = listOf(DnsClient.RecordType.A, DnsClient.RecordType.MX, DnsClient.RecordType.NS, DnsClient.RecordType.TXT)
                val sb = StringBuilder()
                types.forEach { type ->
                    val records = runCatching { DnsClient.query(domain, type) }.getOrElse { emptyList() }
                    sb.append("${type.name}:\n")
                    if (records.isEmpty()) {
                        sb.append("  (sin registros o timeout)\n")
                    } else {
                        records.forEach { sb.append("  ${it.value}  (TTL ${it.ttl}s)\n") }
                    }
                    sb.append("\n")
                }
                result = sb.toString()
                loading = false
                repo.add("DNS", domain, "Consulta A/MX/NS/TXT", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
