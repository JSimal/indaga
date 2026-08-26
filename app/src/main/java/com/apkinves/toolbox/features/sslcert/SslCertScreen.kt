package com.apkinves.toolbox.features.sslcert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.SslCertClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun SslCertScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Certificado SSL",
        inputLabel = "Host (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val host = target.trim()
                val certs = runCatching { SslCertClient.inspect(host) }.getOrNull()
                result = certs?.fold(
                    onSuccess = { list ->
                        list.joinToString("\n\n") { c ->
                            buildString {
                                appendLine("Sujeto: ${c.subject}")
                                appendLine("Emisor: ${c.issuer}")
                                appendLine("Válido: ${c.validFrom} -> ${c.validTo}")
                                val warning = if (c.daysUntilExpiry < 0) " (¡CADUCADO!)" else if (c.daysUntilExpiry < 30) " (caduca pronto)" else ""
                                appendLine("Días hasta caducar: ${c.daysUntilExpiry}$warning")
                                append("Firma: ${c.signatureAlgorithm}")
                            }
                        }
                    },
                    onFailure = { "Error: ${it.message}" },
                ) ?: "Error al conectar"
                loading = false
                repo.add("SSL Cert", host, result.lineSequence().firstOrNull() ?: "", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
