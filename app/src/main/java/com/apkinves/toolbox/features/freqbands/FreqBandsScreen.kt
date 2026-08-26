package com.apkinves.toolbox.features.freqbands

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.util.FrequencyBands
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold

@Composable
fun FreqBandsScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember {
        mutableStateOf(FrequencyBands.BANDS.joinToString("\n\n") { "${it.range}\n${it.name}: ${it.use}" })
    }

    ToolScreenScaffold(
        title = "Bandas de frecuencia",
        inputLabel = "Buscar (ej: WiFi, GPS, radioaficionado)",
        inputValue = input,
        onInputChange = { input = it },
        loading = false,
        runEnabled = true,
        onRun = {
            val matches = FrequencyBands.search(input)
            result = if (matches.isEmpty()) "Sin resultados" else matches.joinToString("\n\n") { "${it.range}\n${it.name}: ${it.use}" }
        },
    ) {
        ResultBlock(result)
    }
}
