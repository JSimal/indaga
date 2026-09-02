package com.apkinves.toolbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apkinves.toolbox.BuildConfig
import com.apkinves.toolbox.R
import com.apkinves.toolbox.data.FavoritesRepository
import com.apkinves.toolbox.features.update.ApkDownloader
import com.apkinves.toolbox.features.update.ApkInstaller
import com.apkinves.toolbox.features.update.DownloadState
import com.apkinves.toolbox.features.update.UpdateChecker
import com.apkinves.toolbox.features.update.UpdateResult
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomeScreenContent(tools: List<ToolEntry>, onToolClick: (String) -> Unit) {
    val context = LocalContext.current
    val favoritesRepo = remember { FavoritesRepository(context) }
    val favorites by favoritesRepo.favorites.collectAsState()
    var updateAvailable by remember { mutableStateOf<UpdateResult.UpdateAvailable?>(null) }
    var query by remember { mutableStateOf("") }
    val expandedCategories = remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        val result = UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
        if (result is UpdateResult.UpdateAvailable) updateAvailable = result
    }

    val filtered = if (query.isBlank()) tools else tools.filter {
        it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
    }
    val favoriteTools = tools.filter { it.route in favorites }
    val grouped = filtered.groupBy { it.category }

    fun toggleCategory(category: String) {
        val current = expandedCategories.value
        expandedCategories.value = if (category in current) current - category else current + category
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp)) }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar herramienta...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }

        updateAvailable?.let { update ->
            item { UpdateBanner(update) }
        }

        if (query.isBlank() && favoriteTools.isNotEmpty()) {
            item {
                CategoryHeader(
                    emoji = "⭐",
                    title = "Favoritos",
                    color = com.apkinves.toolbox.ui.theme.CyberColors.NeonAmber,
                    expanded = true,
                    onClick = {},
                )
            }
            items(favoriteTools.chunked(2)) { pair ->
                ToolCardRow(pair, com.apkinves.toolbox.ui.theme.CyberColors.NeonAmber, favorites, onToolClick) { favoritesRepo.toggle(it) }
            }
        }

        if (query.isBlank()) {
            // Modo normal: categorías plegables, todo colapsado salvo lo que el usuario abra.
            grouped.forEach { (category, toolsInCategory) ->
                val style = CATEGORY_STYLES[category]
                val isExpanded = category in expandedCategories.value
                item {
                    CategoryHeader(
                        emoji = style?.emoji ?: "",
                        title = "$category (${toolsInCategory.size})",
                        color = style?.color ?: MaterialTheme.colorScheme.primary,
                        expanded = isExpanded,
                        onClick = { toggleCategory(category) },
                    )
                }
                if (isExpanded) {
                    items(toolsInCategory.chunked(2)) { pair ->
                        val accent = style?.color ?: MaterialTheme.colorScheme.primary
                        ToolCardRow(pair, accent, favorites, onToolClick) { favoritesRepo.toggle(it) }
                    }
                }
            }
        } else {
            // Modo búsqueda: resultados planos, sin agrupar ni plegar (feedback inmediato).
            items(filtered.chunked(2)) { pair ->
                val style = CATEGORY_STYLES[pair.first().category]
                ToolCardRow(pair, style?.color ?: MaterialTheme.colorScheme.primary, favorites, onToolClick) { favoritesRepo.toggle(it) }
            }
            if (filtered.isEmpty()) {
                item { Text("Sin resultados para \"$query\"", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 16.dp)) }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("▸ INDAGA v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text("Creado por Simal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private sealed class UpdateUiState {
    object Idle : UpdateUiState()
    data class Downloading(val progress: Float) : UpdateUiState()
    data class ReadyToInstall(val file: File) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

@Composable
private fun UpdateBanner(update: UpdateResult.UpdateAvailable) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UpdateUiState>(UpdateUiState.Idle) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "🚀 Actualización disponible: ${update.release.tag_name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            when (val s = state) {
                is UpdateUiState.Idle -> {
                    Button(onClick = {
                        scope.launch {
                            ApkDownloader.download(context, update.apkUrl, "indaga_update.apk").collect { ds ->
                                state = when (ds) {
                                    is DownloadState.Progress -> {
                                        val total = ds.totalBytes
                                        UpdateUiState.Downloading(if (total > 0) ds.bytesRead.toFloat() / total else 0f)
                                    }
                                    is DownloadState.Done -> UpdateUiState.ReadyToInstall(ds.file)
                                    is DownloadState.Failed -> UpdateUiState.Error(ds.message)
                                }
                            }
                        }
                    }) { Text("Actualizar") }
                }
                is UpdateUiState.Downloading -> {
                    LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth())
                    Text("${(s.progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
                is UpdateUiState.ReadyToInstall -> {
                    if (!ApkInstaller.canInstallPackages(context)) {
                        Text(
                            "Falta autorizar a Indaga a instalar apps (una sola vez).",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Button(onClick = { ApkInstaller.requestInstallPermission(context) }) {
                            Text("Autorizar instalación")
                        }
                    }
                    Button(onClick = { ApkInstaller.install(context, s.file) }) {
                        Text("Instalar ahora")
                    }
                }
                is UpdateUiState.Error -> {
                    Text("Error al descargar: ${s.message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { state = UpdateUiState.Idle }) { Text("Reintentar") }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    emoji: String,
    title: String,
    color: androidx.compose.ui.graphics.Color,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 16.dp, bottom = 6.dp),
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(color, RoundedCornerShape(2.dp)))
        Text(
            "$emoji  $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp).weight(1f),
        )
        Icon(
            painterResource(R.drawable.ic_back),
            contentDescription = if (expanded) "Contraer" else "Expandir",
            tint = color,
            modifier = Modifier.rotate(if (expanded) 90f else 270f),
        )
    }
}

@Composable
private fun ToolCardRow(
    pair: List<ToolEntry>,
    accent: androidx.compose.ui.graphics.Color,
    favorites: Set<String>,
    onToolClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        pair.forEach { tool ->
            ToolCard(
                tool = tool,
                accent = accent,
                isFavorite = tool.route in favorites,
                onToolClick = onToolClick,
                onToggleFavorite = { onToggleFavorite(tool.route) },
                modifier = Modifier.weight(1f),
            )
        }
        if (pair.size == 1) Box(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ToolCard(
    tool: ToolEntry,
    accent: androidx.compose.ui.graphics.Color,
    isFavorite: Boolean,
    onToolClick: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(bottom = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(1.dp, accent.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
            .clickable { onToolClick(tool.route) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(accent))
            Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(
                        tool.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.width(28.dp).height(28.dp)) {
                        Icon(
                            painterResource(if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                            tint = if (isFavorite) com.apkinves.toolbox.ui.theme.CyberColors.NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(tool.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
