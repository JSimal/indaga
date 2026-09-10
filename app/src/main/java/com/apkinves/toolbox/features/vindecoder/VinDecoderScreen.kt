package com.apkinves.toolbox.features.vindecoder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.util.VinDecoder
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold

@Composable
fun VinDecoderScreen() {
    var vin by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    ToolScreenScaffold(
        title = "Decodificador VIN",
        inputLabel = "Número de bastidor (17 caracteres)",
        inputValue = vin,
        onInputChange = { vin = it },
        loading = false,
        onRun = {
            val info = VinDecoder.decode(vin)
            result = buildString {
                if (!info.validLength) {
                    appendLine("⚠ Longitud u caracteres no válidos: un VIN tiene 17 caracteres y no usa I, O ni Q.")
                    appendLine()
                }
                appendLine("País probable: ${info.country ?: "no reconocido en la tabla local"}")
                appendLine("Fabricante probable: ${info.manufacturer ?: "no reconocido en la tabla local"}")
                if (info.possibleYears.isNotEmpty()) {
                    appendLine("Año de modelo probable: ${info.possibleYears.joinToString(" o ")}")
                }
                info.checkDigitValid?.let {
                    appendLine(if (it) "✓ Dígito de control válido (norma Norteamérica/NHTSA)" else "✗ Dígito de control no coincide (normal en VINs fuera de Norteamérica, que no siempre lo usan)")
                }
                appendLine()
                appendLine("Nota: tabla de fabricantes reducida a los más comunes; un resultado vacío no significa que el VIN sea inválido.")
            }
        },
    ) {
        ResultBlock(result)
    }
}
