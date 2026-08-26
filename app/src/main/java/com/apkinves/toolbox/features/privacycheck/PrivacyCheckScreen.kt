package com.apkinves.toolbox.features.privacycheck

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
import com.apkinves.toolbox.core.net.PrivacySelfCheckClient
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun PrivacyCheckScreen() {
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Qué expone tu conexión", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Comprueba tu IP pública, tu operador/ISP y los servidores DNS que " +
                "usa tu conexión actual.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = {
                loading = true
                scope.launch {
                    val r = PrivacySelfCheckClient.check(context)
                    result = buildString {
                        appendLine("IP pública: ${r.publicIp ?: "no detectada"}")
                        appendLine("ISP/Organización: ${r.ispOrg ?: "no detectado"}")
                        appendLine("País: ${r.country ?: "no detectado"}")
                        appendLine()
                        appendLine("Servidores DNS en uso:")
                        if (r.dnsServers.isEmpty()) appendLine("  (no se pudo leer)")
                        else r.dnsServers.forEach { appendLine("  $it") }
                        appendLine()
                        appendLine(if (r.usingKnownPublicDns) "Usas un DNS público conocido." else "No pareces usar un DNS público habitual (probablemente el de tu operador/router).")
                    }
                    loading = false
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Comprobando..." else "Comprobar") }
        if (loading) CircularProgressIndicator()
        ResultBlock(result)
    }
}
