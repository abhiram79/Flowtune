package com.abhiram.flowtune.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.abhiram.flowtune.R
import com.abhiram.flowtune.models.MediaMetadata
import kotlinx.coroutines.launch

// Mantener ColorPreset con nombres originales
data class ColorPreset(
    val name: String,
    val backgroundColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val isDark: Boolean,
    val gradientColors: List<Color>? = null
)

// colorPresets actualizado con mejores temas
val colorPresets = listOf(
    ColorPreset("Oscuro Clásico", Color(0xFF0A0A0A), Color(0xFFFFFFFF), Color(0xFFB0B0B0), true),
    ColorPreset("Azul Nocturno", Color(0xFF0F172A), Color(0xFFF1F5F9), Color(0xFF94A3B8), true),
    ColorPreset("Verde Esmeralda", Color(0xFF064E3B), Color(0xFFECFDF5), Color(0xFFA7F3D0), true),
    ColorPreset("Púrpura Profundo", Color(0xFF7C2D12), Color(0xFFFED7AA), Color(0xFFEA580C), true,
        gradientColors = listOf(Color(0xFF7C2D12), Color(0xFFEA580C))),
    ColorPreset("Blanco Limpio", Color(0xFFFFFFFF), Color(0xFF0F172A), Color(0xFF64748B), false),
    ColorPreset("Crema Suave", Color(0xFFFEF7ED), Color(0xFF431407), Color(0xFF78716C), false),
    ColorPreset("Rosa Suave", Color(0xFFFFF1F2), Color(0xFF881337), Color(0xFFA21CAF), false),
    ColorPreset("Gradiente Sunset", Color(0xFFF0F9FF), Color(0xFF0C4A6E), Color(0xFF0369A1), false)
)

@Composable
fun rememberAdjustedFontSize(
    text: String,
    maxWidth: Dp,
    maxHeight: Dp,
    density: Density,
    initialFontSize: TextUnit = 20.sp,
    minFontSize: TextUnit = 14.sp,
    style: TextStyle = TextStyle.Default,
    textMeasurer: androidx.compose.ui.text.TextMeasurer? = null
): TextUnit {
    val measurer = textMeasurer ?: rememberTextMeasurer()

    var calculatedFontSize by remember(text, maxWidth, maxHeight, style, density) {
        val initialSize = when {
            text.length < 30 -> (initialFontSize.value * 1.1f).sp
            text.length < 60 -> initialFontSize
            text.length < 120 -> (initialFontSize.value * 0.85f).sp
            text.length < 200 -> (initialFontSize.value * 0.7f).sp
            else -> (initialFontSize.value * 0.6f).sp
        }
        mutableStateOf(initialSize)
    }

    LaunchedEffect(key1 = text, key2 = maxWidth, key3 = maxHeight) {
        val targetWidthPx = with(density) { maxWidth.toPx() * 0.85f }
        val targetHeightPx = with(density) { maxHeight.toPx() * 0.8f }

        if (text.isBlank()) {
            calculatedFontSize = minFontSize
            return@LaunchedEffect
        }

        var minSize = minFontSize.value
        var maxSize = (initialFontSize.value * 1.2f)
        var bestFit = minSize
        var iterations = 0

        while (minSize <= maxSize && iterations < 20) {
            iterations++
            val midSize = (minSize + maxSize) / 2
            val midSizeSp = midSize.sp

            val result = measurer.measure(
                text = AnnotatedString(text),
                style = style.copy(
                    fontSize = midSizeSp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = (midSize * 1.3f).sp,
                    letterSpacing = 0.3.sp
                )
            )

            if (result.size.width <= targetWidthPx && result.size.height <= targetHeightPx) {
                bestFit = midSize
                minSize = midSize + 0.5f
            } else {
                maxSize = midSize - 0.5f
            }
        }

        calculatedFontSize = if (bestFit < minFontSize.value) minFontSize else bestFit.sp
    }

    return calculatedFontSize
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsImageCard(
    lyricText: String,
    mediaMetadata: MediaMetadata,
    selectedPreset: ColorPreset = colorPresets[0],
    onPresetChange: (ColorPreset) -> Unit = {},
    onSaveImage: () -> Unit = {},
    showControls: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Controls superiores rediseñados
        if (showControls) {
            ModernControlsSection(
                selectedPreset = selectedPreset,
                isDropdownExpanded = isDropdownExpanded,
                isGenerating = isGenerating,
                onDropdownToggle = { isDropdownExpanded = !isDropdownExpanded },
                onPresetChange = { preset ->
                    onPresetChange(preset)
                    isDropdownExpanded = false
                },
                onSaveImage = {
                    isGenerating = true
                    onSaveImage()
                    // Simular proceso
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(1500)
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
        }

        // Theme selector horizontal
        AnimatedVisibility(
            visible = isDropdownExpanded,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            ColorPresetSelector(
                selectedPreset = selectedPreset,
                isExpanded = isDropdownExpanded,
                onToggle = { isDropdownExpanded = !isDropdownExpanded },
                onPresetChange = { preset ->
                    onPresetChange(preset)
                    isDropdownExpanded = false
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card principal rediseñada
        LyricsImageCard(
            lyricText = lyricText,
            mediaMetadata = mediaMetadata,
            backgroundColor = selectedPreset.backgroundColor,
            textColor = selectedPreset.textColor,
            secondaryTextColor = selectedPreset.secondaryTextColor,
            darkMode = selectedPreset.isDark,
            gradientColors = selectedPreset.gradientColors,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Preview hint
        Text(
            text = "La imagen se guardará en alta resolución",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernControlsSection(
    selectedPreset: ColorPreset,
    isDropdownExpanded: Boolean,
    isGenerating: Boolean,
    onDropdownToggle: () -> Unit,
    onPresetChange: (ColorPreset) -> Unit,
    onSaveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Compartir Letra",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tema: ${selectedPreset.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Theme button
                FloatingActionButton(
                    onClick = onDropdownToggle,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.expand_more),
                        contentDescription = "Cambiar tema",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Save button
                FloatingActionButton(
                    onClick = onSaveImage,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.image),
                            contentDescription = "Guardar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPresetSelector(
    selectedPreset: ColorPreset,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onPresetChange: (ColorPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(colorPresets) { preset ->
            ColorPresetItem(
                preset = preset,
                isSelected = preset == selectedPreset,
                onClick = { onPresetChange(preset) }
            )
        }
    }
}

@Composable
private fun ColorPresetItem(
    preset: ColorPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = if (preset.gradientColors != null) {
                        Brush.linearGradient(preset.gradientColors)
                    } else {
                        Brush.linearGradient(listOf(preset.backgroundColor, preset.backgroundColor))
                    }
                )
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aa",
                color = preset.textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = preset.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (isSelected) 1f else 0.7f
            ),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LyricsImageCard(
    lyricText: String,
    mediaMetadata: MediaMetadata,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    secondaryTextColor: Color? = null,
    darkMode: Boolean = true,
    gradientColors: List<Color>? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val corner = 28.dp
    val cardSize = 380.dp
    val outerPadding = 32.dp
    val thumbnailSize = 80.dp

    // Color scheme configuration
    val bg = backgroundColor ?: if (darkMode) Color(0xFF0D1117) else Color(0xFFFAFBFC)
    val mainText = textColor ?: if (darkMode) Color(0xFFF0F6FC) else Color(0xFF24292F)
    val secondaryText = secondaryTextColor ?: mainText.copy(alpha = if (darkMode) 0.7f else 0.6f)

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(mediaMetadata.thumbnailUrl)
            .crossfade(true)
            .placeholder(R.drawable.music_note)
            .error(R.drawable.music_note)
            .build()
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .size(cardSize)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(corner),
                    ambientColor = mainText.copy(alpha = 0.3f),
                    spotColor = mainText.copy(alpha = 0.5f)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(corner)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (gradientColors != null) {
                            Brush.linearGradient(
                                colors = gradientColors,
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        } else {
                            Brush.linearGradient(listOf(bg, bg))
                        }
                    )
            ) {
                // Patrón de fondo sutil
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pattern = 40.dp.toPx()
                    for (x in 0..size.width.toInt() step pattern.toInt()) {
                        for (y in 0..size.height.toInt() step pattern.toInt()) {
                            drawCircle(
                                color = mainText.copy(alpha = 0.03f),
                                radius = 2.dp.toPx(),
                                center = Offset(x.toFloat(), y.toFloat())
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(outerPadding),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header mejorado: thumbnail, title, artist
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Thumbnail con glassmorphism
                        Box(
                            modifier = Modifier
                                .size(thumbnailSize)
                                .clip(RoundedCornerShape(20.dp))
                                .background(mainText.copy(alpha = 0.1f))
                                .border(
                                    1.dp,
                                    mainText.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mediaMetadata.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                fontWeight = FontWeight.Black,
                                color = mainText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mediaMetadata.artists.joinToString { it.name },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = secondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Lyrics body mejorado
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val textMeasurer = rememberTextMeasurer()
                        val optimalFontSize = rememberAdjustedFontSize(
                            lyricText, maxWidth, maxHeight, density,
                            textMeasurer = textMeasurer
                        )

                        Text(
                            text = lyricText,
                            textAlign = TextAlign.Center,
                            fontSize = optimalFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = mainText,
                            lineHeight = optimalFontSize * 1.3f,
                            modifier = Modifier.fillMaxWidth(),
                            letterSpacing = 0.3.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = bg.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = secondaryText)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(mainText.copy(alpha = 0.15f))
                                    .border(1.dp, mainText.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.opentune),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Text(
                            text = context.getString(R.string.app_name),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryText,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}