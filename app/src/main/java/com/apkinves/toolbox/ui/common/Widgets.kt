package com.apkinves.toolbox.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun ToolScreenScaffold(
    title: String,
    inputLabel: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onRun: () -> Unit,
    loading: Boolean,
    runEnabled: Boolean = inputValue.isNotBlank(),
    extraControls: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
            value = inputValue,
            onValueChange = onInputChange,
            label = { Text(inputLabel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        extraControls?.invoke()
        Button(onClick = onRun, enabled = runEnabled && !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Consultando..." else "Consultar")
        }
        if (loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        content()
    }
}

/** Bloque de resultados con aspecto de terminal: cabecera con "semáforo" y texto monoespaciado. */
@Composable
fun ResultBlock(text: String) {
    if (text.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TerminalDot(MaterialTheme.colorScheme.error)
                TerminalDot(com.apkinves.toolbox.ui.theme.CyberColors.NeonAmber)
                TerminalDot(com.apkinves.toolbox.ui.theme.CyberColors.NeonGreen)
                Text(
                    "resultado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).padding(bottom = 12.dp),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TerminalDot(color: androidx.compose.ui.graphics.Color) {
    Box(modifier = Modifier.size(9.dp).background(color, CircleShape))
}
