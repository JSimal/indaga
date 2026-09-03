package com.apkinves.toolbox.features.emailverify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.EmailVerifier
import com.apkinves.toolbox.core.util.EmailHeaderAnalyzer
import com.apkinves.toolbox.core.util.EmailPermutator
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun EmailVerifyScreen() {
    var email by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var permNombre by remember { mutableStateOf("") }
    var permApellido by remember { mutableStateOf("") }
    var permDominio by remember { mutableStateOf("") }
    var permResult by remember { mutableStateOf("") }

    var headersInput by remember { mutableStateOf("") }
    var headersResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Verificador de email", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Text("Sintaxis y registros MX", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo a comprobar") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(
            onClick = {
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
            enabled = email.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Consultando..." else "Consultar") }
        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        ResultBlock(result)

        Text("Permutador de emails", style = MaterialTheme.typography.titleSmall)
        Text(
            "Genera direcciones probables a partir de nombre, apellido y dominio corporativo.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(value = permNombre, onValueChange = { permNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = permApellido, onValueChange = { permApellido = it }, label = { Text("Apellido (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = permDominio, onValueChange = { permDominio = it }, label = { Text("Dominio (ej: empresa.com)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(
            onClick = {
                val list = EmailPermutator.generate(permNombre, permApellido, permDominio)
                permResult = if (list.isEmpty()) "Introduce al menos nombre y dominio" else list.joinToString("\n")
            },
            enabled = permNombre.isNotBlank() && permDominio.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Generar") }
        ResultBlock(permResult)

        Text("Analizador de cabeceras de email", style = MaterialTheme.typography.titleSmall)
        Text(
            "Pega las cabeceras completas (raw headers) de un correo para extraer IPs de tránsito y resultados SPF/DKIM/DMARC.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = headersInput,
            onValueChange = { headersInput = it },
            label = { Text("Cabeceras (Received, From, To, Authentication-Results...)") },
            modifier = Modifier.fillMaxWidth().height(160.dp),
        )
        Button(
            onClick = {
                val r = EmailHeaderAnalyzer.analyze(headersInput)
                headersResult = buildString {
                    r.from?.let { appendLine("De: $it") }
                    r.to?.let { appendLine("Para: $it") }
                    r.subject?.let { appendLine("Asunto: $it") }
                    r.date?.let { appendLine("Fecha: $it") }
                    r.messageId?.let { appendLine("Message-ID: $it") }
                    appendLine()
                    appendLine("SPF: ${r.spf ?: "no encontrado"}")
                    appendLine("DKIM: ${r.dkim ?: "no encontrado"}")
                    appendLine("DMARC: ${r.dmarc ?: "no encontrado"}")
                    if (r.ips.isNotEmpty()) {
                        appendLine()
                        appendLine("IPs detectadas en la ruta de tránsito:")
                        r.ips.forEach { appendLine("  $it") }
                    }
                    if (r.hops.isNotEmpty()) {
                        appendLine()
                        appendLine("Saltos (Received), del más reciente al más antiguo:")
                        r.hops.forEach { hop ->
                            appendLine("  from ${hop.fromHost ?: "?"} by ${hop.byHost ?: "?"}${hop.ip?.let { " [$it]" } ?: ""}")
                        }
                    }
                    if (r.ips.isEmpty() && r.hops.isEmpty() && r.from == null) {
                        appendLine("No se han detectado cabeceras reconocibles. Asegúrate de pegar el texto completo (en Gmail: Mostrar original; en Outlook: Ver origen del mensaje).")
                    }
                }
            },
            enabled = headersInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Analizar") }
        ResultBlock(headersResult)
    }
}
