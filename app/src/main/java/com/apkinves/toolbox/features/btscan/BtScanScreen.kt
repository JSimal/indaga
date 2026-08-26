package com.apkinves.toolbox.features.btscan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.apkinves.toolbox.core.net.BluetoothScanner
import kotlinx.coroutines.launch

private fun requiredPermission(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    Manifest.permission.BLUETOOTH_SCAN
} else {
    Manifest.permission.ACCESS_FINE_LOCATION
}

@Composable
fun BtScanScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permission = remember { requiredPermission() }

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val devices = remember { mutableStateListOf<BluetoothScanner.BtDevice>() }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Dispositivos Bluetooth cercanos", style = MaterialTheme.typography.headlineSmall)

        if (!hasPermission) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Por qué se pide este permiso: es necesario para buscar " +
                            "dispositivos Bluetooth cercanos. Esta app lo declara como " +
                            "'nunca para ubicación' donde el sistema lo permite, y no " +
                            "usa los resultados para geolocalizarte.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(onClick = { permissionLauncher.launch(permission) }) {
                        Text("Conceder permiso")
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    loading = true
                    error = ""
                    devices.clear()
                    scope.launch {
                        runCatching {
                            BluetoothScanner.discover(context).collect { device ->
                                if (devices.none { it.address == device.address }) devices.add(device)
                            }
                        }.onFailure { error = it.message ?: "Error desconocido" }
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (loading) "Buscando (unos 12s)..." else "Buscar dispositivos") }
        }

        if (loading) CircularProgressIndicator()
        if (error.isNotBlank()) Text("Error: $error", color = MaterialTheme.colorScheme.error)

        devices.forEach { device ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(device.name, style = MaterialTheme.typography.titleSmall)
                    Text("${device.address}${device.rssi?.let { "  ($it dBm)" } ?: ""}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
