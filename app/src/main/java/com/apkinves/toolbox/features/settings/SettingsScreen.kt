package com.apkinves.toolbox.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.ui.ProxyPreference
import com.apkinves.toolbox.ui.theme.CyberColors

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var host by remember { mutableStateOf(ProxyPreference.host) }
    var portText by remember { mutableStateOf(ProxyPreference.port.toString()) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Proxy SOCKS5 / Tor", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(
                    "Enruta las consultas de red de la app (WHOIS/RDAP, DNS, puertos, WHOIS...) a través de un proxy SOCKS5, " +
                        "en vez de salir con la IP real del dispositivo. Pensado para usarse con Orbot " +
                        "(Tor para Android), que expone SOCKS5 en 127.0.0.1:9050 por defecto — instálalo y arráncalo aparte, " +
                        "esto no incluye Tor en sí, solo enruta hacia él.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Activar proxy", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = ProxyPreference.enabled,
                        onCheckedChange = { ProxyPreference.setEnabled(context, it) },
                    )
                }
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it.filter { c -> c.isDigit() } },
                    label = { Text("Puerto") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { ProxyPreference.setHostPort(context, host.trim(), portText.toIntOrNull() ?: ProxyPreference.port) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Guardar host y puerto") }
                if (ProxyPreference.enabled) {
                    Text(
                        "Proxy activo: ${ProxyPreference.host}:${ProxyPreference.port}. Si Orbot no está corriendo, todas las consultas de red fallarán.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.NeonAmber,
                    )
                }
            }
        }
    }
}
