package com.apkinves.toolbox.features.emailverify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.net.EmailVerifier
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun EmailVerifyScreen() {
    var email by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolScreenScaffold(
        title = "Verificador de email",
        inputLabel = "Correo a comprobar",
        inputValue = email,
        onInputChange = { email = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val r = EmailVerifier.verify(email)
                result = buildString {
                    appendLine(if (r.validSyntax) "✓ Formato válido" else "✗ Formato inválido")
                    if (r.validSyntax) {
                        appendLine("Dominio: ${r.domain}")
                        appendLine(if (r.hasMxRecords) "✓ El dominio tiene servidores de correo (MX)" else "✗ El dominio NO tiene registros MX — no puede recibir correo")
                        if (r.mxHosts.isNotEmpty()) {
                            appendLine()
                            appendLine("Servidores MX:")
                            r.mxHosts.forEach { appendLine("  $it") }
                        }
                    }
                    appendLine()
                    appendLine("Nota: esto comprueba formato y capacidad de recibir correo del dominio, no si esa dirección exacta existe (verificarlo de forma fiable requeriría sondear el servidor SMTP, algo que la mayoría de redes móviles bloquean y muchos servidores tratan como spam).")
                }
                loading = false
            }
        },
    ) {
        ResultBlock(result)
    }
}
