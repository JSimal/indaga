package com.apkinves.toolbox.features.wifiscan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.core.content.ContextCompat
import com.apkinves.toolbox.core.net.WifiScanner
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun WifiScanScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("WiFi cercanas", style = MaterialTheme.typography.headlineSmall)

        if (!hasPermission) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Por qué se pide ubicación: es un requisito del propio " +
                            "Android para listar redes WiFi cercanas (los nombres de " +
                            "red pueden usarse para inferir dónde estás). Esta app no " +
                            "usa tu ubicación para nada más ni la envía a ningún sitio.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                        Text("Conceder permiso de ubicación")
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    loading = true
                    scope.launch {
                        val networks = WifiScanner.scanResults(context)
                        val warnings = WifiScanner.detectSuspicious(networks)
                        result = buildString {
                            if (warnings.isNotEmpty()) {
                                appendLine("⚠ Posibles anomalías:")
                                warnings.forEach { appendLine("  $it") }
                                appendLine()
                            }
                            appendLine("${networks.size} redes detectadas:\n")
                            networks.sortedByDescending { it.level }.forEach {
                                appendLine("${it.ssid}  (${it.level} dBm, ${it.capabilities})")
                            }
                        }
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (loading) "Escaneando..." else "Escanear WiFi cercanas") }
        }

        if (loading) CircularProgressIndicator()
        ResultBlock(result)
    }
}
