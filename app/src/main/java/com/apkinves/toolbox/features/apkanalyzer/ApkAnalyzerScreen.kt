package com.apkinves.toolbox.features.apkanalyzer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.apkinves.toolbox.core.util.ApkAnalyzer
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun ApkAnalyzerScreen() {
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val report = ApkAnalyzer.analyze(context, uri)
            result = report.fold(
                onSuccess = {
                    buildString {
                        appendLine("Paquete: ${it.packageName}")
                        appendLine("Versión: ${it.versionName ?: "?"} (código ${it.versionCode})")
                        appendLine("Target SDK: ${it.targetSdk}")
                        appendLine()
                        appendLine("Firmantes:")
                        if (it.signers.isEmpty()) appendLine("  (no se pudo leer)")
                        else it.signers.forEach { s -> appendLine("  $s") }
                        appendLine()
                        appendLine("Permisos solicitados (${it.permissions.size}):")
                        it.permissions.forEach { p -> appendLine("  $p") }
                    }
                },
                onFailure = { "Error: ${it.message}" },
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Analizador de APK", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Analiza un archivo .apk que elijas (paquete, versión, permisos, " +
                "firmantes). No accede a tus apps instaladas, solo al fichero " +
                "que selecciones.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { launcher.launch(arrayOf("application/vnd.android.package-archive")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Elegir APK")
        }
        ResultBlock(result)
    }
}
