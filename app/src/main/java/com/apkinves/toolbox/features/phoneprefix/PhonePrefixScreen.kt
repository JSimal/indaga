package com.apkinves.toolbox.features.phoneprefix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apkinves.toolbox.core.util.PhonePrefixLookup
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.common.ToolScreenScaffold

@Composable
fun PhonePrefixScreen() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    ToolScreenScaffold(
        title = "Prefijo telefónico",
        inputLabel = "Número con prefijo (ej: +34600...)",
        inputValue = input,
        onInputChange = { input = it },
        loading = false,
        onRun = {
            val r = PhonePrefixLookup.lookup(input)
            result = if (r == null) "Prefijo no reconocido en la tabla local" else "+${r.prefix} → ${r.country}"
        },
    ) {
        ResultBlock(result)
    }
}
