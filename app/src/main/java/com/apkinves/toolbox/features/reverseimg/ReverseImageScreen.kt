package com.apkinves.toolbox.features.reverseimg

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.net.URLEncoder

@Composable
fun ReverseImageScreen() {
    var url by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun open(targetUrl: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))) }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Búsqueda inversa de imágenes", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Pega la URL de una imagen (no un archivo local) y ábrela en el " +
                "buscador de imágenes que prefieras.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL de la imagen") }, modifier = Modifier.fillMaxWidth())

        val encoded = remember(url) { runCatching { URLEncoder.encode(url.trim(), "UTF-8") }.getOrDefault("") }

        Button(onClick = { open("https://lens.google.com/uploadbyurl?url=$encoded") }, enabled = url.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Buscar en Google Lens")
        }
        Button(onClick = { open("https://www.yandex.com/images/search?rpt=imageview&url=$encoded") }, enabled = url.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Buscar en Yandex")
        }
        Button(onClick = { open("https://tineye.com/search?url=$encoded") }, enabled = url.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Buscar en TinEye")
        }
    }
}
