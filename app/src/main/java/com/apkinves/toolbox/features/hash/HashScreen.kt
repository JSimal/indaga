package com.apkinves.toolbox.features.hash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.util.HashUtils
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold

@Composable
fun HashScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    ToolScreenScaffold(
        title = "Generador de hashes",
        inputLabel = "Texto a hashear",
        inputValue = input,
        onInputChange = { input = it },
        loading = false,
        onRun = {
            result = HashUtils.hashAll(input).entries.joinToString("\n\n") { (alg, value) -> "$alg:\n$value" }
        },
    ) {
        ResultBlock(result)
    }
}
