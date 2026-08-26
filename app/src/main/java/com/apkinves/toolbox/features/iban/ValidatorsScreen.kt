package com.apkinves.toolbox.features.iban

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.BinLookupClient
import com.apkinves.toolbox.core.util.IbanCountryData
import com.apkinves.toolbox.core.util.Validators
import com.apkinves.toolbox.data.CaseRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import kotlinx.coroutines.launch

@Composable
fun ValidatorsScreen() {
    var cardInput by remember { mutableStateOf("") }
    var cardResult by remember { mutableStateOf("") }

    var ibanInput by remember { mutableStateOf("") }
    var ibanResult by remember { mutableStateOf("") }
    var showIbanCountries by remember { mutableStateOf(false) }

    var idInput by remember { mutableStateOf("") }
    var idResult by remember { mutableStateOf("") }

    var binInput by remember { mutableStateOf("") }
    var binResult by remember { mutableStateOf("") }
    var binLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repo = remember { CaseRepository(context) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Validadores", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Text("Número de tarjeta (algoritmo de Luhn)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = cardInput, onValueChange = { cardInput = it }, label = { Text("Número de tarjeta") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { cardResult = if (Validators.luhnCheck(cardInput)) "✓ Número válido (pasa Luhn)" else "✗ Número inválido" }, modifier = Modifier.fillMaxWidth()) {
            Text("Comprobar")
        }
        ResultBlock(cardResult)

        Text("BIN de tarjeta (entidad emisora)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = binInput, onValueChange = { binInput = it }, label = { Text("6-8 primeros dígitos") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                binLoading = true
                scope.launch {
                    val info = BinLookupClient.lookup(binInput.trim())
                    binResult = info.fold(
                        onSuccess = {
                            buildString {
                                appendLine("Esquema: ${it.scheme}   Tipo: ${it.type}")
                                if (it.brand.isNotBlank()) appendLine("Marca: ${it.brand}")
                                appendLine("País: ${it.country.emoji} ${it.country.name}")
                                if (it.bank.name.isNotBlank()) appendLine("Banco emisor: ${it.bank.name}")
                            }
                        },
                        onFailure = { "Error: ${it.message}" },
                    )
                    binLoading = false
                    repo.add("BIN Checker", binInput.trim(), binResult.lineSequence().firstOrNull() ?: "", binResult)
                }
            },
            enabled = binInput.isNotBlank() && !binLoading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (binLoading) "Consultando..." else "Comprobar") }
        ResultBlock(binResult)

        Text("IBAN", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = ibanInput, onValueChange = { ibanInput = it }, label = { Text("IBAN") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val r = Validators.validateIban(ibanInput)
            val countryName = IbanCountryData.byCode(r.countryCode)?.country
            ibanResult = "${if (r.valid) "✓" else "✗"} ${r.message} (país: ${r.countryCode}${countryName?.let { " · $it" } ?: ""})"
        }, modifier = Modifier.fillMaxWidth()) { Text("Comprobar") }
        ResultBlock(ibanResult)

        Button(onClick = { showIbanCountries = !showIbanCountries }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showIbanCountries) "Ocultar lista de países IBAN" else "Ver lista de países IBAN")
        }
        if (showIbanCountries) {
            ResultBlock(IbanCountryData.COUNTRIES.joinToString("\n") { "${it.code}  ${it.country} — ${it.length} caracteres" })
        }

        Text("NIF / NIE / CIF español", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = idInput, onValueChange = { idInput = it }, label = { Text("Documento") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val r = Validators.validateSpanishId(idInput)
            idResult = "${if (r.valid) "✓" else "✗"} [${r.type}] ${r.message}"
        }, modifier = Modifier.fillMaxWidth()) { Text("Comprobar") }
        ResultBlock(idResult)
    }
}
