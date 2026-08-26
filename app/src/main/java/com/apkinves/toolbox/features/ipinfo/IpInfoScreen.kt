package com.apkinves.toolbox.features.ipinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.apkinves.toolbox.core.net.IpInfoClient
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun IpInfoScreen() {
    var target by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    ToolScreenScaffold(
        title = "Información de IP",
        inputLabel = "Dirección IP (ej: 8.8.8.8)",
        inputValue = target,
        onInputChange = { target = it },
        loading = loading,
        onRun = {
            loading = true
            scope.launch {
                val info = runCatching { IpInfoClient.lookup(target.trim()) }.getOrNull()
                result = if (info == null || info.status != "success") {
                    "No se pudo resolver la IP: ${info?.message ?: "error de red"}"
                } else {
                    buildString {
                        appendLine("IP: ${info.query}")
                        appendLine("País: ${info.country} (${info.countryCode})")
                        appendLine("Región: ${info.regionName}")
                        appendLine("Ciudad: ${info.city}  CP: ${info.zip}")
                        appendLine("Coordenadas: ${info.lat}, ${info.lon}")
                        appendLine("Zona horaria: ${info.timezone}")
                        appendLine("ISP: ${info.isp}")
                        appendLine("Organización: ${info.org}")
                        appendLine("AS: ${info.`as`}")
                        appendLine("¿Proxy/VPN/Tor?: ${if (info.proxy) "Sí" else "No"}")
                        appendLine("¿Hosting/datacenter?: ${if (info.hosting) "Sí" else "No"}")
                        appendLine("¿Red móvil?: ${if (info.mobile) "Sí" else "No"}")
                    }
                }
                loading = false
                repo.add("IP Info", target.trim(), result.lineSequence().firstOrNull() ?: "", result)
            }
        },
    ) {
        ResultBlock(result)
    }
}
