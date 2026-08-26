package com.apkinves.toolbox.features.wifirange

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
import com.apkinves.toolbox.core.util.WifiRangeCalculator
import com.apkinves.toolbox.ui.common.ResultBlock

@Composable
fun WifiRangeScreen() {
    var txPower by remember { mutableStateOf("20") }
    var txGain by remember { mutableStateOf("2") }
    var rxGain by remember { mutableStateOf("2") }
    var sensitivity by remember { mutableStateOf("-90") }
    var frequency by remember { mutableStateOf("2400") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Calculadora de alcance WiFi", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Estimación teórica en espacio libre (sin paredes ni obstáculos). " +
                "El alcance real en interiores suele ser bastante menor.",
            style = MaterialTheme.typography.bodySmall,
        )

        OutlinedTextField(value = txPower, onValueChange = { txPower = it }, label = { Text("Potencia TX (dBm, típico 20)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = txGain, onValueChange = { txGain = it }, label = { Text("Ganancia antena TX (dBi)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = rxGain, onValueChange = { rxGain = it }, label = { Text("Ganancia antena RX (dBi)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sensitivity, onValueChange = { sensitivity = it }, label = { Text("Sensibilidad receptor (dBm, típico -90)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Frecuencia (MHz, 2400 o 5000)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val p = txPower.toDoubleOrNull()
                val tg = txGain.toDoubleOrNull()
                val rg = rxGain.toDoubleOrNull()
                val s = sensitivity.toDoubleOrNull()
                val f = frequency.toDoubleOrNull()
                result = if (p == null || tg == null || rg == null || s == null || f == null) "Introduce valores numéricos válidos"
                else {
                    val r = WifiRangeCalculator.estimateRange(p, tg, rg, s, f)
                    "Alcance teórico estimado: ${"%.0f".format(r.theoreticalRangeMeters)} metros\n" +
                        "(en espacio abierto sin obstáculos; en interior espera bastante menos)"
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        ResultBlock(result)
    }
}
