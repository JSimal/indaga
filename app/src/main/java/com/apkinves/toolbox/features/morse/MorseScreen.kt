package com.apkinves.toolbox.features.morse

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
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.MorseTranslator
import com.apkinves.toolbox.ui.common.ResultBlock

@Composable
fun MorseScreen() {
    var text by remember { mutableStateOf("") }
    var textResult by remember { mutableStateOf("") }
    var morse by remember { mutableStateOf("") }
    var morseResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Traductor Morse", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Text("Texto → Morse", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Texto") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { textResult = MorseTranslator.textToMorse(text) }, modifier = Modifier.fillMaxWidth()) { Text("Convertir") }
        ResultBlock(textResult)

        Text("Morse → Texto (usa espacio entre letras y \"/\" entre palabras)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = morse, onValueChange = { morse = it }, label = { Text("Morse (ej: .... --- .-.. .-)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { morseResult = MorseTranslator.morseToText(morse) }, modifier = Modifier.fillMaxWidth()) { Text("Convertir") }
        ResultBlock(morseResult)
    }
}
