package com.apkinves.toolbox.features.directory

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.core.util.PreservationRequest
import com.apkinves.toolbox.data.DirectoryEntry
import com.apkinves.toolbox.data.DirectoryRepository
import com.apkinves.toolbox.data.Firmante
import com.apkinves.toolbox.data.FirmanteRepository
import com.apkinves.toolbox.ui.common.ResultBlock
import com.apkinves.toolbox.ui.theme.CyberColors

@Composable
fun DirectoryScreen() {
    val context = LocalContext.current
    val repo = remember { DirectoryRepository(context) }
    var directorio by remember { mutableStateOf(repo.cargarLocal()) }
    var estadoRed by remember { mutableStateOf("Buscando fichas actualizadas…") }
    var busqueda by remember { mutableStateOf("") }
    var tipoFiltro by remember { mutableStateOf<String?>(null) }
    var abierta by remember { mutableStateOf<String?>(null) }
    var peticionPara by remember { mutableStateOf<DirectoryEntry?>(null) }

    LaunchedEffect(Unit) {
        val nuevo = repo.actualizarDesdeRed(directorio)
        estadoRed = if (nuevo != null) {
            directorio = nuevo
            "✓ Fichas actualizadas desde el repositorio"
        } else {
            ""
        }
    }

    val entradaPeticion = peticionPara
    if (entradaPeticion != null) {
        PreservationRequestPanel(entradaPeticion, onVolver = { peticionPara = null })
        return
    }

    val tipos = directorio.entradas.map { it.tipo }.distinct()
    val visibles = directorio.entradas.filter { (tipoFiltro == null || it.tipo == tipoFiltro) && it.coincide(busqueda) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("¿A quién requerir?", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Canal, entidad, plazos de conservación y qué piden plataformas, exchanges, operadoras y bancos. " +
                "Fichas actualizadas a ${directorio.actualizado.ifBlank { "—" }}.",
            style = MaterialTheme.typography.bodySmall,
        )
        if (directorio.aviso.isNotBlank()) {
            Text(directorio.aviso, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (estadoRed.isNotBlank()) {
            Text(estadoRed, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar (ej: instagram, gmail, binance, vodafone, iban…)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = tipoFiltro == null, onClick = { tipoFiltro = null }, label = { Text("Todo") })
            tipos.forEach { t ->
                FilterChip(selected = tipoFiltro == t, onClick = { tipoFiltro = if (tipoFiltro == t) null else t }, label = { Text(t) })
            }
        }

        if (visibles.isEmpty()) {
            Text(
                "No hay ficha para eso todavía. Si sabes cómo se requiere, propónla con el botón de abajo.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        visibles.forEach { e ->
            EntryCard(
                entrada = e,
                expandida = abierta == e.id,
                onClick = { abierta = if (abierta == e.id) null else e.id },
                onPeticion = { peticionPara = e },
            )
        }

        OutlinedButton(
            onClick = { compartirTexto(context, "Propuesta para el directorio de Indaga", plantillaPropuesta(busqueda)) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Proponer ficha nueva o corrección") }
    }
}

@Composable
private fun EntryCard(entrada: DirectoryEntry, expandida: Boolean, onClick: () -> Unit, onPeticion: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(entrada.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            val estado = if (entrada.verificado.isNotBlank()) {
                "✓ Comprobada el ${entrada.verificado}"
            } else {
                "⚠ Sin comprobar: verifica en la guía oficial antes de usarla"
            }
            Text(
                "${entrada.tipo} · $estado",
                style = MaterialTheme.typography.labelSmall,
                color = if (entrada.verificado.isNotBlank()) CyberColors.NeonGreen else CyberColors.NeonAmber,
            )

            if (expandida) {
                Campo("Entidad a la que dirigirse", entrada.entidad)
                Campo("Conservación de datos", entrada.conservacion)
                Campo("Qué piden", entrada.requisitos)
                Campo("Emergencias", entrada.emergencias)
                Campo("Notas", entrada.notas)
                Campo("Email", entrada.email)

                if (entrada.portal.isNotBlank()) {
                    Button(onClick = { abrirUrl(context, entrada.portal) }, modifier = Modifier.fillMaxWidth()) { Text("Abrir portal de requerimientos") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (entrada.email.isNotBlank()) {
                        OutlinedButton(onClick = { clipboard.setText(AnnotatedString(entrada.email)) }) { Text("Copiar email") }
                    }
                    if (entrada.guia.isNotBlank()) {
                        OutlinedButton(onClick = { abrirUrl(context, entrada.guia) }) { Text("Guía oficial") }
                    }
                }
                Button(onClick = onPeticion, modifier = Modifier.fillMaxWidth()) { Text("Preparar petición de conservación") }
            } else {
                Text("Toca para ver canal, plazos y requisitos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Campo(titulo: String, valor: String) {
    if (valor.isBlank()) return
    Column {
        Text(titulo, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(valor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PreservationRequestPanel(entrada: DirectoryEntry, onVolver: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val firmanteRepo = remember { FirmanteRepository(context) }
    val guardado = remember { firmanteRepo.cargar() }

    var identificadores by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }
    var hechos by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf(guardado.unidad) }
    var nombre by remember { mutableStateOf(guardado.nombre) }
    var email by remember { mutableStateOf(guardado.email) }
    var telefono by remember { mutableStateOf(guardado.telefono) }
    var idioma by remember { mutableStateOf(PreservationRequest.Idioma.EN) }

    val firmante = Firmante(unidad, nombre, email, telefono)
    val datos = PreservationRequest.Datos(identificadores, referencia, hechos, firmante)
    val texto = PreservationRequest.texto(entrada, datos, idioma)
    val asunto = PreservationRequest.asunto(entrada, datos, idioma)
    val huecos = texto.contains("[")

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextButton(onClick = onVolver) { Text("← Volver al directorio") }
        Text("Petición de conservación", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(entrada.nombre, style = MaterialTheme.typography.titleSmall)
        if (entrada.conservacion.isNotBlank()) {
            Text(entrada.conservacion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "Es un borrador: revísalo antes de enviarlo. Lo que falte sale [entre corchetes]. " +
                "Lo que escribas aquí no se guarda ni se envía a ningún sitio; solo se recuerdan tus datos de firma.",
            style = MaterialTheme.typography.bodySmall,
        )

        OutlinedTextField(
            value = identificadores,
            onValueChange = { identificadores = it },
            label = { Text("Identificadores (uno por línea: URL, usuario, email, teléfono, wallet…)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        OutlinedTextField(value = referencia, onValueChange = { referencia = it }, label = { Text("Nº de diligencias / referencia") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(
            value = hechos,
            onValueChange = { hechos = it },
            label = { Text(if (idioma == PreservationRequest.Idioma.EN) "Hechos, en inglés (ej: online investment fraud)" else "Hechos (ej: una estafa por inversión en criptomonedas)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Text("Firma (se recuerda en este móvil)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre / TIP") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email oficial") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = idioma == PreservationRequest.Idioma.EN, onClick = { idioma = PreservationRequest.Idioma.EN }, label = { Text("Inglés") })
            FilterChip(selected = idioma == PreservationRequest.Idioma.ES, onClick = { idioma = PreservationRequest.Idioma.ES }, label = { Text("Castellano") })
        }

        if (huecos) {
            Text("⚠ Quedan huecos [entre corchetes] por rellenar.", style = MaterialTheme.typography.bodySmall, color = CyberColors.NeonAmber)
        }
        ResultBlock(texto)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                firmanteRepo.guardar(firmante)
                clipboard.setText(AnnotatedString(texto))
            }) { Text("Copiar") }
            Button(onClick = {
                firmanteRepo.guardar(firmante)
                compartirTexto(context, asunto, texto, destinatario = entrada.email)
            }) { Text(if (entrada.email.isNotBlank()) "Enviar por email…" else "Compartir…") }
        }
        if (entrada.portal.isNotBlank()) {
            OutlinedButton(onClick = {
                firmanteRepo.guardar(firmante)
                clipboard.setText(AnnotatedString(texto))
                abrirUrl(context, entrada.portal)
            }, modifier = Modifier.fillMaxWidth()) { Text("Copiar y abrir el portal") }
        }
    }
}

private fun abrirUrl(context: android.content.Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun compartirTexto(context: android.content.Context, asunto: String, texto: String, destinatario: String = "") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, texto)
        if (destinatario.isNotBlank()) putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Enviar")) }
}

private fun plantillaPropuesta(busqueda: String) = buildString {
    appendLine("Propuesta para el directorio «¿A quién requerir?» de Indaga")
    appendLine()
    appendLine("Plataforma / entidad: ${busqueda.trim()}")
    appendLine("Entidad a la que dirigirse (razón social y país):")
    appendLine("Portal o email para requerimientos:")
    appendLine("Plazo de conservación:")
    appendLine("Qué piden (oficio, mandamiento, OEI, MLAT…):")
    appendLine("Emergencias:")
    appendLine("Notas:")
    appendLine("Fuente (enlace a la guía oficial) y fecha en que lo comprobaste:")
    appendLine()
    appendLine("No incluyas datos de ningún caso.")
}
