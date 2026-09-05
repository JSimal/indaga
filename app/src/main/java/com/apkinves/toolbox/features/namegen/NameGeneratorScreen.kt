package com.apkinves.toolbox.features.namegen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.NameGenerator

@Composable
fun NameGeneratorScreen() {
    var theme by remember { mutableStateOf(NameGenerator.Theme.GENERAL) }
    var expanded by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Generador de nombres", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Nombres aleatorios de dos palabras para lo que necesites: alias, proyectos, personajes, partidas, nombres en clave...",
            style = MaterialTheme.typography.bodySmall,
        )

        Text("Temática", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                    .clickable { expanded = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(theme.label, style = MaterialTheme.typography.bodyLarge)
                Text("▾", style = MaterialTheme.typography.bodyLarge)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                NameGenerator.Theme.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { theme = option; expanded = false },
                    )
                }
            }
        }

        Button(onClick = { results = NameGenerator.generate(theme) }, modifier = Modifier.fillMaxWidth()) {
            Text("Generar nombres")
        }

        results.forEach { name ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = {
                        clipboard.setText(AnnotatedString(name))
                        Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                    }) { Text("Copiar") }
                }
            }
        }
    }
}
