package com.apkinves.toolbox.features.qrscan

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.ui.common.ResultBlock
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun QrScanScreen() {
    var content by remember { mutableStateOf("") }
    var isUrl by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Escáner de QR / código de barras", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Usa el escáner gestionado de Google Play Services: la cámara la " +
                "abre su propia pantalla, no la nuestra, así que esta app no " +
                "necesita declarar permiso de cámara.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = {
                error = ""
                val scanner = GmsBarcodeScanning.getClient(context)
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val raw = barcode.rawValue ?: ""
                        content = raw
                        isUrl = raw.startsWith("http://") || raw.startsWith("https://")
                    }
                    .addOnFailureListener { e -> error = e.message ?: "No se pudo escanear (¿Google Play Services disponible?)" }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Escanear") }

        if (error.isNotBlank()) Text("Error: $error", color = MaterialTheme.colorScheme.error)

        if (content.isNotBlank()) {
            ResultBlock(content)
            if (isUrl) {
                Text(
                    "⚠ Es una URL. Revisa el dominio con cuidado antes de abrirla " +
                        "(puedes usar las herramientas de esta app para analizarla primero).",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.apkinves.toolbox.ui.theme.CyberColors.NeonAmber,
                )
                Button(
                    onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content))) } },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Abrir enlace de todas formas") }
            }
        }
    }
}
