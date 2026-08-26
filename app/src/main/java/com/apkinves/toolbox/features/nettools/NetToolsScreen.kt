package com.apkinves.toolbox.features.nettools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.apkinves.toolbox.core.net.PingClient
import com.apkinves.toolbox.core.net.TelnetClient
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun NetToolsScreen() {
    var pingHost by remember { mutableStateOf("") }
    var pingResult by remember { mutableStateOf("") }

    var telnetHost by remember { mutableStateOf("") }
    var telnetPort by remember { mutableStateOf("80") }
    var telnetCommand by remember { mutableStateOf("") }
    var telnetResult by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ping", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = pingHost, onValueChange = { pingHost = it }, label = { Text("Host o IP") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            scope.launch {
                val r = PingClient.ping(pingHost.trim())
                pingResult = r.fold(
                    onSuccess = { "Enviados: ${it.attempts}, recibidos: ${it.received}\nPérdida: ${it.lossPercent}%\nRTT medio: %.0f ms".format(it.avgMs) },
                    onFailure = { "Error: ${it.message}" },
                )
            }
        }, enabled = pingHost.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Hacer ping") }
        ResultBlock(pingResult)

        Text("Conexión TCP en crudo (\"telnet\")", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Conecta a un puerto TCP y opcionalmente envía una línea de texto. " +
                "Útil para probar servicios de texto plano.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(value = telnetHost, onValueChange = { telnetHost = it }, label = { Text("Host o IP") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = telnetPort, onValueChange = { telnetPort = it }, label = { Text("Puerto") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = telnetCommand, onValueChange = { telnetCommand = it }, label = { Text("Línea a enviar (opcional)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val port = telnetPort.toIntOrNull()
            if (port == null) {
                telnetResult = "Puerto inválido"
            } else {
                scope.launch {
                    val r = TelnetClient.connect(telnetHost.trim(), port, telnetCommand.ifBlank { null })
                    telnetResult = if (r.connected) "Conectado.\n\nRespuesta:\n${r.banner}" else "Error: ${r.error}"
                }
            }
        }, enabled = telnetHost.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Conectar") }
        ResultBlock(telnetResult)
    }
}
