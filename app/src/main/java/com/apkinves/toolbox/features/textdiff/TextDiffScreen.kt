package com.apkinves.toolbox.features.textdiff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.TextDiff
import com.apkinves.toolbox.ui.theme.CyberColors

@Composable
fun TextDiffScreen() {
    var oldText by remember { mutableStateOf("") }
    var newText by remember { mutableStateOf("") }
    var diffLines by remember { mutableStateOf<List<TextDiff.DiffLine>>(emptyList()) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Comparador de textos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text("Compara dos versiones de un mensaje o documento línea a línea.", style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(value = oldText, onValueChange = { oldText = it }, label = { Text("Texto original") }, modifier = Modifier.fillMaxWidth().height(140.dp))
        OutlinedTextField(value = newText, onValueChange = { newText = it }, label = { Text("Texto nuevo") }, modifier = Modifier.fillMaxWidth().height(140.dp))

        Button(onClick = { diffLines = TextDiff.diff(oldText, newText) }, modifier = Modifier.fillMaxWidth()) { Text("Comparar") }

        diffLines.forEach { line ->
            val (bg, prefix) = when (line.type) {
                TextDiff.LineType.ADDED -> CyberColors.NeonGreen.copy(alpha = 0.15f) to "+ "
                TextDiff.LineType.REMOVED -> CyberColors.NeonRed.copy(alpha = 0.15f) to "- "
                TextDiff.LineType.EQUAL -> MaterialTheme.colorScheme.surfaceVariant to "  "
            }
            Text(
                "$prefix${line.text}",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().background(bg).padding(4.dp),
            )
        }
    }
}
