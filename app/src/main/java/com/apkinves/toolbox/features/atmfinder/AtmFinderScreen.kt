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
import androidx.compose.runtime.LaunchedEffect
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
import com.apkinves.toolbox.ui.common.MapController
import com.apkinves.toolbox.ui.common.MapWebView
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class AtmJson(val name: String, val lat: Double, val lon: Double, val distanceKm: Double)

@Composable
fun AtmFinderScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapController = remember { MapController() }

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

    fun runSearch(lat: Double, lon: Double) {
        loading = true
        error = ""
        scope.launch {
            val result = AtmFinderClient.findNearby(lat, lon)
            result.fold(
                onSuccess = {
                    atms = it
                    mapController.setCenter(lat, lon)
                    mapController.setAtms(Json.encodeToString(it.map { a -> AtmJson(a.name, a.lat, a.lon, a.distanceKm) }))
                },
                onFailure = { error = "Error: ${it.message}" },
            )
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Cajeros automáticos cercanos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Datos de OpenStreetMap (comunidad, gratis, sin registro). Puedes " +
                "explorar el mapa y pulsar \"Buscar en esta zona\" para repetir la " +
                "búsqueda centrada donde quieras.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (!hasPermission) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Se necesita tu ubicación para centrar el mapa cerca de ti. " +
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
            MapWebView(
                controller = mapController,
                onSearchHere = { lat, lon -> runSearch(lat, lon) },
            )

            LaunchedEffect(Unit) {
                val location = getLastKnownLocation(context)
                if (location != null) runSearch(location.first, location.second)
                else error = "No se pudo obtener tu ubicación (activa el GPS/ubicación); mueve el mapa y pulsa \"Buscar en esta zona\""
            }

            Button(
                onClick = {
                    scope.launch {
                        val location = getLastKnownLocation(context)
                        if (location != null) runSearch(location.first, location.second)
                        else error = "No se pudo obtener tu ubicación"
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (loading) "Buscando..." else "Buscar cerca de mi ubicación") }
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
                    }) { Text("Abrir en Google Maps") }
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
