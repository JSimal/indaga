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
import com.apkinves.toolbox.core.util.TldLookup
import com.apkinves.toolbox.ui.common.ResultBlock

@Composable
fun GeoUtilsScreen() {
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var dmsResult by remember { mutableStateOf("") }

    var domainInput by remember { mutableStateOf("") }
    var tldResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Utilidades geográficas", style = MaterialTheme.typography.headlineSmall)

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
    }
}
