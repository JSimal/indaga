package com.apkinves.toolbox.features.filehash

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
import com.apkinves.toolbox.core.util.FileHashUtils
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun FileHashScreen() {
    var uriA by remember { mutableStateOf<Uri?>(null) }
    var uriB by remember { mutableStateOf<Uri?>(null) }
    var hashA by remember { mutableStateOf("") }
    var hashB by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pickerA = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uriA = uri }
    val pickerB = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uriB = uri }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Comparador de hashes de archivos", style = MaterialTheme.typography.headlineSmall)
        Text("Comprueba si dos archivos son idénticos byte a byte (SHA-256).", style = MaterialTheme.typography.bodySmall)

        Button(onClick = { pickerA.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (uriA == null) "Elegir archivo A" else "Archivo A elegido ✓")
        }
        Button(onClick = { pickerB.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (uriB == null) "Elegir archivo B" else "Archivo B elegido ✓")
        }

        Button(
            onClick = {
                val a = uriA
                val b = uriB
                if (a == null || b == null) return@Button
                scope.launch {
                    hashA = FileHashUtils.hashFile(context, a).getOrElse { "Error: ${it.message}" }
                    hashB = FileHashUtils.hashFile(context, b).getOrElse { "Error: ${it.message}" }
                    result = if (hashA == hashB) "✓ Los archivos son idénticos" else "✗ Los archivos son diferentes"
                }
            },
            enabled = uriA != null && uriB != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Comparar") }

        ResultBlock(buildString {
            if (hashA.isNotBlank()) appendLine("SHA-256 A: $hashA")
            if (hashB.isNotBlank()) appendLine("SHA-256 B: $hashB")
            if (result.isNotBlank()) append("\n$result")
        })
    }
}
