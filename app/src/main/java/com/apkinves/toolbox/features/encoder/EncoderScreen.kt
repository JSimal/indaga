package com.apkinves.toolbox.features.encoder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.apkinves.toolbox.core.util.EncoderUtils
import com.apkinves.toolbox.ui.common.ResultBlock

@Composable
fun EncoderScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Encoder / Decoder", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Texto") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Base64", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { result = EncoderUtils.base64Encode(input) }) { Text("Codificar") }
            Button(onClick = { result = EncoderUtils.base64Decode(input).getOrElse { "Error: ${it.message}" } }) { Text("Decodificar") }
        }

        Text("Hexadecimal", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { result = EncoderUtils.hexEncode(input) }) { Text("Codificar") }
            Button(onClick = { result = EncoderUtils.hexDecode(input).getOrElse { "Error: ${it.message}" } }) { Text("Decodificar") }
        }

        Text("URL", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { result = EncoderUtils.urlEncode(input) }) { Text("Codificar") }
            Button(onClick = { result = EncoderUtils.urlDecode(input).getOrElse { "Error: ${it.message}" } }) { Text("Decodificar") }
        }

        Text("JWT", style = MaterialTheme.typography.titleSmall)
        Button(onClick = {
            result = EncoderUtils.jwtDecode(input).fold(
                onSuccess = { "Header:\n${it.header}\n\nPayload:\n${it.payload}\n\nFirma (sin verificar):\n${it.signature}" },
                onFailure = { e -> "Error: ${e.message}" },
            )
        }) { Text("Decodificar JWT") }

        ResultBlock(result)
    }
}
