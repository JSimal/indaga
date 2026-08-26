package com.apkinves.toolbox.features.watchlist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.apkinves.toolbox.data.WatchRepository
import com.apkinves.toolbox.data.WatchType
import com.apkinves.toolbox.work.WatchWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WatchlistScreen() {
    val context = LocalContext.current
    val repo = remember { WatchRepository(context) }
    val items by repo.items.collectAsState()
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    var newTarget by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf(WatchType.CONTENT) }

    val needsNotifPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var hasNotifPermission by remember {
        mutableStateOf(
            !needsNotifPermission ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasNotifPermission = granted
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Vigilancia", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Añade una web (para detectar cambios de contenido) o un dominio " +
                "(para detectar subdominios nuevos vía Certificate Transparency). " +
                "Se revisa automáticamente cada ~6 horas y avisa con una " +
                "notificación si algo cambia.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (!hasNotifPermission) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Sin permiso de notificaciones no podrás recibir avisos de " +
                            "cambios; la vigilancia seguirá funcionando pero tendrás que " +
                            "venir a revisar esta pantalla manualmente.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                        Text("Conceder permiso de notificaciones")
                    }
                }
            }
        }

        OutlinedTextField(value = newTarget, onValueChange = { newTarget = it }, label = { Text("URL o dominio") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { newType = WatchType.CONTENT }, enabled = newType != WatchType.CONTENT) { Text("Vigilar contenido") }
            Button(onClick = { newType = WatchType.SUBDOMAINS }, enabled = newType != WatchType.SUBDOMAINS) { Text("Vigilar subdominios") }
        }
        Button(
            onClick = {
                scope.launch {
                    repo.add(newType, newTarget.trim())
                    newTarget = ""
                    WatchWorker.schedulePeriodic(context)
                }
            },
            enabled = newTarget.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Añadir a vigilancia") }

        Button(
            onClick = { WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WatchWorker>().build()) },
            enabled = items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Revisar ahora") }

        items.forEach { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.target, style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (item.type == WatchType.SUBDOMAINS.name) "Vigilando subdominios" else "Vigilando contenido",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        if (item.lastCheckedAt == 0L) "Aún sin comprobar"
                        else "Última comprobación: ${dateFormat.format(Date(item.lastCheckedAt))}" +
                            (item.lastChangedAt?.let { "  ·  último cambio: ${dateFormat.format(Date(it))}" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(onClick = { scope.launch { repo.remove(item.id) } }) { Text("Quitar") }
                }
            }
        }
    }
}
