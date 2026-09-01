package com.apkinves.toolbox.features.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.net.CryptoTickerClient
import com.apkinves.toolbox.core.net.CurrencyClient
import com.apkinves.toolbox.ui.common.Sparkline
import com.apkinves.toolbox.ui.theme.CyberColors
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen() {
    var amountText by remember { mutableStateOf("100") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<CurrencyClient.ConversionResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var tickers by remember { mutableStateOf<List<CryptoTickerClient.CoinTicker>>(emptyList()) }
    LaunchedEffect(Unit) {
        CryptoTickerClient.fetch().onSuccess { tickers = it }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Divisas y cripto", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        // --- Ticker BTC/ETH ---
        if (tickers.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cotización en tiempo real (últimos 7 días)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    tickers.forEach { t ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${t.symbol}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("€%,.2f".format(t.priceEur), style = MaterialTheme.typography.bodySmall)
                            }
                            Sparkline(
                                values = t.last7Days,
                                color = if (t.symbol == "BTC") CyberColors.NeonAmber else CyberColors.NeonTeal,
                            )
                        }
                    }
                }
            }
        }

        // --- Conversor ---
        Text(
            "Convierte euros a dólares y a los stablecoins USDT/USDC. Si no " +
                "eliges fecha, usa la cotización de hoy.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Cantidad en EUR") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showDatePicker = true }) {
                Text(selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Elegir fecha (opcional)")
            }
            if (selectedDate != null) {
                TextButton(onClick = { selectedDate = null }) { Text("Usar hoy") }
            }
        }

        Button(
            onClick = {
                val amount = amountText.replace(",", ".").toDoubleOrNull()
                if (amount == null) {
                    error = "Introduce una cantidad válida"
                    return@Button
                }
                error = ""
                loading = true
                scope.launch {
                    result = CurrencyClient.convert(amount, selectedDate)
                    loading = false
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Consultando..." else "Convertir") }

        if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

        result?.let { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Fecha usada: ${r.dateUsed}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%,.2f".format(r.amountEur)} € =", style = MaterialTheme.typography.bodyMedium)
                    r.usd?.let { Text("$ %,.2f USD".format(it), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                        ?: Text("USD: no disponible", style = MaterialTheme.typography.bodySmall)
                    r.usdt?.let { Text("₮ %,.2f USDT".format(it), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                        ?: Text("USDT: no disponible", style = MaterialTheme.typography.bodySmall)
                    r.usdc?.let { Text("Ⓤ %,.2f USDC".format(it), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                        ?: Text("USDC: no disponible", style = MaterialTheme.typography.bodySmall)
                    r.note?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = CyberColors.NeonAmber) }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
