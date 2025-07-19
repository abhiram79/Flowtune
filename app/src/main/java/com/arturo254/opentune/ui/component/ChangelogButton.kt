package com.abhiram.flowtune.ui.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhiram.flowtune.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class ChangelogState(
    val releases: List<Release> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastUpdated: String? = null
)

data class Release(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val isPrerelease: Boolean,
    val htmlUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogButton(
    viewModel: ChangelogViewModel = viewModel()
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    PreferenceEntry(
        title = { Text(stringResource(R.string.Changelog)) },
        icon = { Icon(painterResource(R.drawable.schedule), null) },
        onClick = { showBottomSheet = true }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(32.dp)
                        .height(4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ChangelogScreen(viewModel)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ChangelogScreen(viewModel: ChangelogViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChangelog("Arturo254", "OpenTune")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.changelogs),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }

            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadChangelog("Arturo254", "OpenTune") }
                )
            }

            uiState.releases.isEmpty() -> {
                EmptyContent()
            }

            else -> {
                SuccessContent(
                    releases = uiState.releases,
                    lastUpdated = uiState.lastUpdated
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Cargando changelog...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Error al cargar",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.no_changes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessContent(
    releases: List<Release>,
    lastUpdated: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        lastUpdated?.let {
            Text(
                text = "Última actualización: $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        releases.forEach { release ->
            ReleaseCard(release = release)
        }
    }
}

@Composable
private fun ReleaseCard(release: Release) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (release.isPrerelease)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = release.tagName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (release.isPrerelease) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiary
                            ) {
                                Text(
                                    text = "Pre-release",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (release.name.isNotEmpty() && release.name != release.tagName) {
                        Text(
                            text = release.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatDate(release.publishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    AdvancedMarkdownText(
                        markdown = release.body,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    // Limpieza y procesamiento del markdown
    val cleanedMarkdown = cleanMarkdown(markdown)
    val lines = cleanedMarkdown.lines()

    var inCodeBlock by remember { mutableStateOf(false) }
    var codeBlockContent by remember { mutableStateOf("") }
    var codeBlockLanguage by remember { mutableStateOf("") }
    var inList by remember { mutableStateOf(false) }
    var listItems by remember { mutableStateOf(mutableListOf<String>()) }

    Column(modifier = modifier) {
        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim()

            when {
                // Bloque de código
                trimmedLine.startsWith("```") -> {
                    // Finalizar lista si estaba activa
                    if (inList) {
                        ListContainer(listItems.toList())
                        listItems.clear()
                        inList = false
                    }

                    if (inCodeBlock) {
                        CodeBlock(
                            code = codeBlockContent.trimEnd(),
                            language = codeBlockLanguage
                        )
                        codeBlockContent = ""
                        codeBlockLanguage = ""
                        inCodeBlock = false
                    } else {
                        codeBlockLanguage = trimmedLine.substring(3).trim()
                        inCodeBlock = true
                    }
                }

                inCodeBlock -> {
                    codeBlockContent += line + "\n"
                }

                // Headers
                trimmedLine.matches(Regex("^#{1,6}\\s+.*")) -> {
                    if (inList) {
                        ListContainer(listItems.toList())
                        listItems.clear()
                        inList = false
                    }

                    val level = trimmedLine.takeWhile { it == '#' }.length
                    val text = trimmedLine.substring(level).trim()
                    HeaderText(text = text, level = level)
                }

                // Listas no ordenadas
                trimmedLine.matches(Regex("^[-*+]\\s+.*")) -> {
                    val content = trimmedLine.substring(2).trim()
                    if (!inList) {
                        inList = true
                        listItems.clear()
                    }
                    listItems.add(content)
                }

                // Listas ordenadas
                trimmedLine.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val content = trimmedLine.substringAfter(". ").trim()
                    if (!inList) {
                        inList = true
                        listItems.clear()
                    }
                    listItems.add(content)
                }

                // Citas
                trimmedLine.startsWith("> ") -> {
                    if (inList) {
                        ListContainer(listItems.toList())
                        listItems.clear()
                        inList = false
                    }
                    BlockQuote(trimmedLine.substring(2))
                }

                // Separadores
                trimmedLine.matches(Regex("^[-*_]{3,}$")) -> {
                    if (inList) {
                        ListContainer(listItems.toList())
                        listItems.clear()
                        inList = false
                    }
                    HorizontalRule()
                }

                // Línea vacía
                trimmedLine.isEmpty() -> {
                    if (inList) {
                        ListContainer(listItems.toList())
                        listItems.clear()
                        inList = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Texto normal
                else -> {
                    if (inList) {
                        ListContainer(listItems.toList())
                        listItems.clear()
                        inList = false
                    }
                    FormattedText(trimmedLine)
                }
            }
        }

        // Finalizar lista si quedó pendiente
        if (inList && listItems.isNotEmpty()) {
            ListContainer(listItems.toList())
        }
    }
}

// Función para limpiar HTML y otros elementos no deseados del markdown
private fun cleanMarkdown(markdown: String): String {
    var cleaned = markdown

    // Remover tags HTML completos
    cleaned = cleaned.replace(Regex("<[^>]+>"), "")

    // Remover imágenes markdown ![alt](url)
    cleaned = cleaned.replace(Regex("!\\[([^\\]]*)\\]\\([^)]*\\)"), "")

    // Convertir enlaces [texto](url) a solo el texto
    cleaned = cleaned.replace(Regex("\\[([^\\]]+)\\]\\([^)]*\\)")) { matchResult ->
        matchResult.groupValues[1]
    }

    // Remover referencias de enlaces [texto][ref]
    cleaned = cleaned.replace(Regex("\\[([^\\]]+)\\]\\[[^\\]]*\\]")) { matchResult ->
        matchResult.groupValues[1]
    }

    // Remover definiciones de enlaces [ref]: url
    cleaned = cleaned.replace(Regex("^\\[[^\\]]+\\]:.*$", RegexOption.MULTILINE), "")

    // Limpiar entidades HTML comunes
    val htmlEntities = mapOf(
        "&amp;" to "&",
        "&lt;" to "<",
        "&gt;" to ">",
        "&quot;" to "\"",
        "&apos;" to "'",
        "&nbsp;" to " ",
        "&#39;" to "'",
        "&#x27;" to "'",
        "&hellip;" to "...",
        "&mdash;" to "—",
        "&ndash;" to "–"
    )

    for ((entity, replacement) in htmlEntities) {
        cleaned = cleaned.replace(entity, replacement)
    }

    // Remover múltiples líneas vacías consecutivas
    cleaned = cleaned.replace(Regex("\n{3,}"), "\n\n")

    return cleaned.trim()
}

@Composable
private fun HeaderText(text: String, level: Int) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineLarge
        2 -> MaterialTheme.typography.headlineMedium
        3 -> MaterialTheme.typography.headlineSmall
        4 -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.titleMedium
    }

    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = (12 - level * 2).coerceAtLeast(4).dp)
    )
}

@Composable
private fun ListContainer(items: List<String>) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEach { item ->
            UnorderedListItem(item)
        }
    }
}

@Composable
private fun UnorderedListItem(content: String) {
    Row(
        modifier = Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        FormattedText(
            text = content,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OrderedListItem(line: String) {
    // Esta función ya no se usa directamente, pero la mantenemos por compatibilidad
    val numberMatch = Regex("^(\\d+)\\.\\s+(.*)").find(line)
    if (numberMatch != null) {
        val (number, content) = numberMatch.destructured
        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$number.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
            )
            FormattedText(
                text = content,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BlockQuote(content: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            FormattedText(
                text = content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CodeBlock(code: String, language: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            if (language.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ) {
                    Text(
                        text = language,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = code.trimEnd(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun InlineCode(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun HorizontalRule() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.outline
    )
}



@Composable
private fun FormattedText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val annotatedString = buildAnnotatedString {
        parseMarkdownText(text, this)
    }

    Text(
        text = annotatedString,
        style = style,
        color = color,
        modifier = modifier.padding(vertical = 2.dp)
    )
}

private fun parseMarkdownText(text: String, builder: AnnotatedString.Builder) {
    var currentIndex = 0
    val processedText = text.trim()

    // Patrones ordenados por prioridad (más específicos primero)
    val patterns = listOf(
        // Código inline: `código` (debe ir antes que otros formatos)
        Triple(
            Regex("`([^`]+)`"),
            { match: MatchResult ->
                builder.withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                ) {
                    append(match.groupValues[1])
                }
            },
            1
        ),

        // Negrita fuerte: **texto**
        Triple(
            Regex("\\*\\*([^*]+)\\*\\*"),
            { match: MatchResult ->
                builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
            },
            2
        ),

        // Negrita alternativa: __texto__
        Triple(
            Regex("__([^_]+)__"),
            { match: MatchResult ->
                builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
            },
            2
        ),

        // Cursiva: *texto* (evitar conflicto con **)
        Triple(
            Regex("(?<!\\*)\\*([^*\\s][^*]*[^*\\s])\\*(?!\\*)"),
            { match: MatchResult ->
                builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groupValues[1])
                }
            },
            3
        ),

        // Cursiva alternativa: _texto_
        Triple(
            Regex("(?<!_)_([^_\\s][^_]*[^_\\s])_(?!_)"),
            { match: MatchResult ->
                builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groupValues[1])
                }
            },
            3
        ),

        // Tachado: ~~texto~~
        Triple(
            Regex("~~([^~]+)~~"),
            { match: MatchResult ->
                builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    append(match.groupValues[1])
                }
            },
            4
        )
    )

    // Encontrar todos los matches y ordenarlos por posición
    val allMatches = patterns.flatMap { (pattern, handler, priority) ->
        pattern.findAll(processedText).map { match ->
            Triple(match, handler, priority)
        }
    }.sortedWith(compareBy({ it.first.range.first }, { it.third }))

    // Procesar matches evitando solapamientos
    val processedRanges = mutableListOf<IntRange>()

    for ((match, handler, _) in allMatches) {
        val range = match.range

        // Verificar si este match se solapa con alguno ya procesado
        val overlaps = processedRanges.any { it.intersect(range).isNotEmpty() }

        if (!overlaps) {
            // Añadir texto antes del match
            if (range.first > currentIndex) {
                builder.append(processedText.substring(currentIndex, range.first))
            }

            // Aplicar formato
            handler(match)

            // Actualizar índice
            currentIndex = range.last + 1
            processedRanges.add(range)
        }
    }

    // Añadir texto restante
    if (currentIndex < processedText.length) {
        builder.append(processedText.substring(currentIndex))
    }

    // Si no se procesó nada, añadir el texto original
    if (builder.length == 0) {
        builder.append(processedText)
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

class ChangelogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChangelogState())
    val uiState: StateFlow<ChangelogState> = _uiState.asStateFlow()

    private val cache = ConcurrentHashMap<String, Pair<List<Release>, Long>>()
    private val cacheTimeMs = 30 * 60 * 1000 // 30 minutos

    fun loadChangelog(repoOwner: String, repoName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val cacheKey = "$repoOwner/$repoName"
                val cachedData = getCachedReleases(cacheKey)

                if (cachedData != null) {
                    _uiState.update {
                        it.copy(
                            releases = cachedData,
                            isLoading = false,
                            lastUpdated = getCurrentTimestamp()
                        )
                    }
                    return@launch
                }

                val releases = fetchReleases(repoOwner, repoName)
                cacheReleases(cacheKey, releases)

                _uiState.update {
                    it.copy(
                        releases = releases,
                        isLoading = false,
                        lastUpdated = getCurrentTimestamp()
                    )
                }
            } catch (e: Exception) {
                Log.e("ChangelogViewModel", "Error cargando changelog", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar los cambios: ${e.message}"
                    )
                }
            }
        }
    }

    private fun getCachedReleases(key: String): List<Release>? {
        val cached = cache[key] ?: return null
        val (releases, timestamp) = cached

        if (System.currentTimeMillis() - timestamp > cacheTimeMs) {
            cache.remove(key)
            return null
        }

        return releases
    }

    private fun cacheReleases(key: String, releases: List<Release>) {
        cache[key] = releases to System.currentTimeMillis()
    }

    private suspend fun fetchReleases(owner: String, repo: String): List<Release> =
        withContext(Dispatchers.IO) {
            repeat(3) { attempt ->
                try {
                    val url = URL("https://api.github.com/repos/$owner/$repo/releases")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        if (attempt == 2) throw IOException("Error HTTP: ${connection.responseCode}")
                        delay(1000)
                        return@repeat
                    }

                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(response)
                    val releases = mutableListOf<Release>()

                    for (i in 0 until jsonArray.length().coerceAtMost(10)) { // Limitar a 10 releases
                        val releaseJson = jsonArray.getJSONObject(i)
                        releases.add(
                            Release(
                                tagName = releaseJson.optString("tag_name", ""),
                                name = releaseJson.optString("name", ""),
                                body = releaseJson.optString("body", ""),
                                publishedAt = releaseJson.optString("published_at", ""),
                                isPrerelease = releaseJson.optBoolean("prerelease", false),
                                htmlUrl = releaseJson.optString("html_url", "")
                            )
                        )
                    }

                    return@withContext releases
                } catch (e: Exception) {
                    if (attempt == 2) throw e
                    delay(1000)
                }
            }

            throw IOException("No se pudo obtener la información después de los reintentos")
        }

    private fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
}