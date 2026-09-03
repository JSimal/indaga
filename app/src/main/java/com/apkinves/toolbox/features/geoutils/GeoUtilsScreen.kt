package com.apkinves.toolbox.features.geoutils

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
import com.apkinves.toolbox.core.util.CoordinateConverter
import com.apkinves.toolbox.core.util.PlusCodes
import com.apkinves.toolbox.core.util.SunMoonCalculator
import com.apkinves.toolbox.core.util.TldLookup
import com.apkinves.toolbox.ui.common.ResultBlock
import java.util.Calendar
import java.util.TimeZone

@Composable
fun GeoUtilsScreen() {
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var dmsResult by remember { mutableStateOf("") }

    var domainInput by remember { mutableStateOf("") }
    var tldResult by remember { mutableStateOf("") }

    val now = remember { Calendar.getInstance() }
    var sunLat by remember { mutableStateOf("") }
    var sunLon by remember { mutableStateOf("") }
    var sunDate by remember { mutableStateOf("%04d-%02d-%02d".format(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))) }
    var sunUtcOffset by remember { mutableStateOf((TimeZone.getDefault().rawOffset / 3600000.0).let { if (it == it.toInt().toDouble()) it.toInt().toString() else it.toString() }) }
    var sunResult by remember { mutableStateOf("") }

    var plusLat by remember { mutableStateOf("") }
    var plusLon by remember { mutableStateOf("") }
    var plusEncodeResult by remember { mutableStateOf("") }
    var plusCodeInput by remember { mutableStateOf("") }
    var plusDecodeResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Utilidades geográficas", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Text("Decimal → DMS (grados/minutos/segundos)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = latInput, onValueChange = { latInput = it }, label = { Text("Latitud (ej: 40.4168)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lonInput, onValueChange = { lonInput = it }, label = { Text("Longitud (ej: -3.7038)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val lat = latInput.toDoubleOrNull()
            val lon = lonInput.toDoubleOrNull()
            dmsResult = if (lat == null || lon == null) "Introduce números válidos"
            else "${CoordinateConverter.decimalToDms(lat, true)}, ${CoordinateConverter.decimalToDms(lon, false)}"
        }, modifier = Modifier.fillMaxWidth()) { Text("Convertir") }
        ResultBlock(dmsResult)

        Text("País por dominio/TLD", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = domainInput, onValueChange = { domainInput = it }, label = { Text("Dominio (ej: ejemplo.es)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val r = TldLookup.lookup(domainInput)
            tldResult = if (r == null) "TLD no reconocido en la tabla local" else "${r.flag} .${r.tld} → ${r.country}"
        }, modifier = Modifier.fillMaxWidth()) { Text("Buscar") }
        ResultBlock(tldResult)

        Text("Sol y luna (orto/ocaso + fase lunar)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = sunLat, onValueChange = { sunLat = it }, label = { Text("Latitud") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sunLon, onValueChange = { sunLon = it }, label = { Text("Longitud") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sunDate, onValueChange = { sunDate = it }, label = { Text("Fecha (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sunUtcOffset, onValueChange = { sunUtcOffset = it }, label = { Text("Desfase horario respecto a UTC (ej: 1, 2 en verano en España)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val lat = sunLat.toDoubleOrNull()
            val lon = sunLon.toDoubleOrNull()
            val offset = sunUtcOffset.toDoubleOrNull()
            val parts = sunDate.split("-").mapNotNull { it.toIntOrNull() }
            if (lat == null || lon == null || offset == null || parts.size != 3) {
                sunResult = "Revisa los campos: latitud, longitud, fecha (AAAA-MM-DD) y desfase UTC deben ser válidos"
            } else {
                val (y, m, d) = parts
                val sun = SunMoonCalculator.sunTimes(lat, lon, y, m, d, offset)
                val moon = SunMoonCalculator.moonPhase(y, m, d)
                sunResult = buildString {
                    if (sun.neverRises) appendLine("☀ El sol no sale ese día en esa latitud (noche polar)")
                    else if (sun.neverSets) appendLine("☀ El sol no se pone ese día en esa latitud (sol de medianoche)")
                    else {
                        appendLine("Amanecer: ${sun.sunrise}")
                        appendLine("Atardecer: ${sun.sunset}")
                        appendLine("Duración del día: ${sun.dayLengthMinutes?.div(60)}h ${sun.dayLengthMinutes?.rem(60)}min")
                    }
                    appendLine()
                    appendLine("🌙 Fase lunar: ${moon.name}")
                    appendLine("Iluminación aproximada: ${moon.illuminationPercent}%")
                    appendLine()
                    appendLine("Nota: cálculo astronómico aproximado (±minutos), útil como orientación para verificar horarios y sombras en fotos, no como referencia oficial.")
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Calcular") }
        ResultBlock(sunResult)

        Text("Plus Code (Open Location Code)", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(value = plusLat, onValueChange = { plusLat = it }, label = { Text("Latitud") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = plusLon, onValueChange = { plusLon = it }, label = { Text("Longitud") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val lat = plusLat.toDoubleOrNull()
            val lon = plusLon.toDoubleOrNull()
            plusEncodeResult = if (lat == null || lon == null) "Introduce números válidos"
            else PlusCodes.encode(lat, lon)
        }, modifier = Modifier.fillMaxWidth()) { Text("Generar código") }
        ResultBlock(plusEncodeResult)

        OutlinedTextField(value = plusCodeInput, onValueChange = { plusCodeInput = it }, label = { Text("Plus Code completo (ej: 8CGRJ2VV+RR)") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            val area = PlusCodes.decode(plusCodeInput)
            plusDecodeResult = if (area == null) "Código no válido (usa un Plus Code completo, sin abreviar)"
            else "Centro: ${area.centerLat}, ${area.centerLon}\nÁrea: ${area.loLat}..${area.hiLat}, ${area.loLon}..${area.hiLon}"
        }, modifier = Modifier.fillMaxWidth()) { Text("Decodificar") }
        ResultBlock(plusDecodeResult)
    }
}
