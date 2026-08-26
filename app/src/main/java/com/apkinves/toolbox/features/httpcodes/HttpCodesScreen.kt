package com.apkinves.toolbox.features.httpcodes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.util.HttpStatusCodes
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold

@Composable
fun HttpCodesScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    ToolScreenScaffold(
        title = "Diccionario de códigos HTTP",
        inputLabel = "Código (ej: 404) o palabra clave",
        inputValue = input,
        onInputChange = { input = it },
        loading = false,
        onRun = {
            val matches = HttpStatusCodes.search(input)
            result = if (matches.isEmpty()) "Sin resultados" else matches.joinToString("\n\n") { "${it.first}: ${it.second}" }
        },
    ) {
        ResultBlock(result)
    }
}
