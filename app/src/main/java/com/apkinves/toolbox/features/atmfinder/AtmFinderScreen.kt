package com.apkinves.toolbox.features.atmfinder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
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
import com.apkinves.toolbox.core.net.AtmFinderClient
import kotlinx.coroutines.launch

@Composable
fun AtmFinderScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var atms by remember { mutableStateOf<List<AtmFinderClient.Atm>>(emptyList()) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Cajeros automáticos cercanos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Datos de OpenStreetMap (comunidad, gratis, sin registro). La " +
                "cobertura depende de lo mapeada que esté tu zona — puede que " +
                "falten cajeros que sí existen.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (!hasPermission) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Se necesita tu ubicación para buscar cajeros cerca de ti. " +
                            "No se envía a ningún sitio salvo a Overpass (OpenStreetMap) " +
                            "para la propia búsqueda.",
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
                    error = ""
                    scope.launch {
                        val location = getLastKnownLocation(context)
                        if (location == null) {
                            error = "No se pudo obtener tu ubicación (activa el GPS/ubicación e inténtalo de nuevo)"
                        } else {
                            val result = AtmFinderClient.findNearby(location.first, location.second)
                            result.fold(
                                onSuccess = { atms = it },
                                onFailure = { error = "Error: ${it.message}" },
                            )
                        }
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (loading) "Buscando..." else "Buscar cajeros cercanos") }
        }

        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

        atms.forEach { atm ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(atm.name, style = MaterialTheme.typography.titleSmall)
                    Text("${"%.2f".format(atm.distanceKm)} km", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = {
                        val uri = Uri.parse("geo:${atm.lat},${atm.lon}?q=${atm.lat},${atm.lon}(${atm.name})")
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    }) { Text("Abrir en el mapa") }
                }
            }
        }
    }
}

@Suppress("MissingPermission")
private fun getLastKnownLocation(context: android.content.Context): Pair<Double, Double>? {
    val manager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    val providers = manager.getProviders(true)
    for (provider in providers) {
        val loc = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        if (loc != null) return loc.latitude to loc.longitude
    }
    return null
}
