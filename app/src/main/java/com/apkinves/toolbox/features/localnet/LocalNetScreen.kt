package com.apkinves.toolbox.features.localnet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.LocalNetworkScanner
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun LocalNetScreen() {
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Dispositivos en tu red WiFi", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Comprueba qué IPs responden en tu subred local (barrido de " +
                "conectividad, no necesita permisos especiales). Puede tardar " +
                "unos segundos.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = {
                loading = true
                scope.launch {
                    val prefix = LocalNetworkScanner.ownIpAndPrefix(context)
                    if (prefix == null) {
                        result = "No se pudo determinar tu subred (¿estás conectado a una WiFi?)"
                    } else {
                        val devices = LocalNetworkScanner.scan(prefix)
                        result = if (devices.isEmpty()) "No se encontró ningún dispositivo activo en $prefix.0/24"
                        else "${devices.size} dispositivos activos en $prefix.0/24:\n\n" +
                            devices.joinToString("\n") { "${it.ip}  (${it.rttMs} ms)" }
                    }
                    loading = false
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Escaneando..." else "Escanear red local") }
        if (loading) CircularProgressIndicator()
        ResultBlock(result)
    }
}
