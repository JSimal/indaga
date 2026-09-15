package com.apkinves.toolbox.features.traceroute

import androidx.compose.foundation.layout.padding
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
import com.apkinves.toolbox.core.net.TracerouteClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.OpsecWarning
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun TracerouteScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Conectividad y latencia",
        inputLabel = "Host o IP (ej: ejemplo.com)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        extraControls = { OpsecWarning() },
        error = error,
        onRun = {
            loading = true
            error = ""
            result = ""
            scope.launch {
                val host = target.trim()
                val probe = runCatching { TracerouteClient.probe(host) }.getOrNull()
                if (probe == null) {
                    error = "Error al resolver o contactar con $host"
                } else {
                    result = if (probe.reachable) {
                        "$host es alcanzable.\nLatencia: ${probe.rttMs} ms"
                    } else {
                        "$host no respondió en el tiempo de espera (puede estar caído, filtrado por firewall, o el sistema no permite ICMP)."
                    }
                }
                loading = false
                val summary = error.ifBlank { result }.lineSequence().first()
                repo.add("Conectividad", host, summary, error.ifBlank { result })
            }
        },
    ) {
        Text(
            "Nota: un traceroute real (hop a hop) requiere leer paquetes ICMP de " +
                "routers intermedios, algo que Android no permite sin root. Esta " +
                "herramienta comprueba si el destino final es alcanzable y con qué latencia.",
            modifier = Modifier.padding(vertical = 4.dp),
        )
        ResultBlock(result)
    }
}
