package com.apkinves.toolbox.features.namegen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.TranslatorClient
import com.apkinves.toolbox.core.util.NameGenerator
import kotlinx.coroutines.launch

private const val ANY_THEME_LABEL = "Cualquiera (todas)"
private const val RANDOM_LANG_LABEL = "Aleatorio"

@Composable
fun NameGeneratorScreen() {
    var theme by remember { mutableStateOf<NameGenerator.Theme?>(null) }
    var themeExpanded by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf<NameGenerator.Language?>(null) }
    var langExpanded by remember { mutableStateOf(false) }
    var word by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var resultLangLabel by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun runTranslate(text: String, lang: NameGenerator.Language) {
        loading = true
        error = ""
        result = null
        scope.launch {
            TranslatorClient.translate(text, lang.code).fold(
                onSuccess = {
                    result = it.replaceFirstChar { c -> c.uppercase() }
                    resultLangLabel = lang.label
                },
                onFailure = { error = "No se pudo traducir (${it.message ?: "error de red"}). Prueba otro idioma." },
            )
            loading = false
        }
    }

    fun runGenerate() {
        loading = true
        error = ""
        result = null
        scope.launch {
            var success = false
            repeat(6) { _ ->
                if (success) return@repeat
                val w = NameGenerator.randomWord(theme)
                val lang = language ?: NameGenerator.randomLanguage()
                TranslatorClient.translate(w, lang.code).onSuccess {
                    word = w
                    result = it.replaceFirstChar { c -> c.uppercase() }
                    resultLangLabel = lang.label
                    success = true
                }
            }
            if (!success) error = "No se encontró un resultado en alfabeto latino tras varios intentos, prueba de nuevo."
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Generador de nombres", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Sugiere una palabra por temática (o de cualquiera) y tradúcela a un idioma poco habitual: el " +
                "resultado suele ser un nombre corto, legible y llamativo. Se filtran los resultados con " +
                "alfabeto no latino (cirílico, chino...), así que siempre sale en letras normales + ñ. " +
                "Sirve para alias, proyectos, personajes o cualquier nombre en clave que necesites.",
            style = MaterialTheme.typography.bodySmall,
        )

        Text("Temática", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Dropdown(
            label = theme?.label ?: ANY_THEME_LABEL,
            expanded = themeExpanded,
            onExpandedChange = { themeExpanded = it },
        ) {
            DropdownMenuItem(text = { Text(ANY_THEME_LABEL) }, onClick = { theme = null; themeExpanded = false })
            NameGenerator.Theme.entries.forEach { option ->
                DropdownMenuItem(text = { Text(option.label) }, onClick = { theme = option; themeExpanded = false })
            }
        }

        Button(onClick = { word = NameGenerator.randomWord(theme) }, modifier = Modifier.fillMaxWidth()) {
            Text("Sugerir palabra")
        }

        OutlinedTextField(
            value = word,
            onValueChange = { word = it },
            label = { Text("Palabra a traducir (en español)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Text("Idioma destino", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Dropdown(
            label = language?.label ?: RANDOM_LANG_LABEL,
            expanded = langExpanded,
            onExpandedChange = { langExpanded = it },
        ) {
            DropdownMenuItem(text = { Text(RANDOM_LANG_LABEL) }, onClick = { language = null; langExpanded = false })
            NameGenerator.LANGUAGES.forEach { option ->
                DropdownMenuItem(text = { Text(option.label) }, onClick = { language = option; langExpanded = false })
            }
        }

        Button(
            onClick = { runTranslate(word, language ?: NameGenerator.randomLanguage()) },
            enabled = word.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Traduciendo..." else "Traducir") }

        Button(
            onClick = { runGenerate() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Generar nombre (sugerir + traducir)") }

        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

        result?.let { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(r, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("\"$word\" en $resultLangLabel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = {
                        clipboard.setText(AnnotatedString(r))
                        Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                    }) { Text("Copiar") }
                }
            }
        }
    }
}

@Composable
private fun Dropdown(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: @Composable () -> Unit,
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                .clickable { onExpandedChange(true) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text("▾", style = MaterialTheme.typography.bodyLarge)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            items()
        }
    }
}
