package com.apkinves.toolbox.features.username

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.UsernameCheckClient
import com.apkinves.toolbox.ui.theme.CyberColors
import kotlinx.coroutines.launch

@Composable
fun UsernameCheckScreen() {
    var username by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UsernameCheckClient.PlatformResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Buscador de nombre de usuario", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Comprueba si un username existe en varias plataformas. Solo se " +
                "incluyen sitios donde se puede comprobar de forma fiable por " +
                "código HTTP — muchas redes (Instagram, X, TikTok...) son apps de " +
                "una sola página que siempre responden igual, así que se han " +
                "dejado fuera para no dar falsos resultados.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = {
                loading = true
                results = emptyList()
                scope.launch {
                    results = UsernameCheckClient.check(username)
                    loading = false
                }
            },
            enabled = username.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Comprobando..." else "Buscar") }

        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

        results.forEach { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(r.platform, style = MaterialTheme.typography.bodyMedium)
                    val (label, color) = when (r.exists) {
                        true -> "Encontrado" to CyberColors.NeonGreen
                        false -> "No encontrado" to MaterialTheme.colorScheme.onSurfaceVariant
                        null -> "Error" to CyberColors.NeonAmber
                    }
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
                }
                if (r.exists == true) {
                    Button(
                        onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.url))) } },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                    ) { Text("Abrir perfil") }
                }
            }
        }
    }
}
