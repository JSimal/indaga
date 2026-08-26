package com.apkinves.toolbox.features.cidr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.util.CidrCalculator
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold

@Composable
fun CidrScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    ToolScreenScaffold(
        title = "Calculadora CIDR",
        inputLabel = "CIDR (ej: 192.168.1.0/24)",
        inputValue = input,
        onInputChange = { input = it },
        loading = false,
        onRun = {
            val r = CidrCalculator.calculate(input.trim())
            result = r.fold(
                onSuccess = {
                    buildString {
                        appendLine("Máscara: ${it.subnetMask}")
                        appendLine("Red: ${it.networkAddress}")
                        appendLine("Broadcast: ${it.broadcastAddress}")
                        appendLine("Primer host: ${it.firstHost}")
                        appendLine("Último host: ${it.lastHost}")
                        appendLine("Total direcciones: ${it.totalHosts}")
                        appendLine("Hosts usables: ${it.usableHosts}")
                    }
                },
                onFailure = { "Error: ${it.message}" },
            )
        },
    ) {
        ResultBlock(result)
    }
}
