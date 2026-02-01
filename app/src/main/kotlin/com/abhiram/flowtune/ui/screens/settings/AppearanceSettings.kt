/**
 * Flowtune YT Music Client (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.abhiram.flowtune.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.abhiram.flowtune.LocalPlayerAwareWindowInsets
import com.abhiram.flowtune.R
import com.abhiram.flowtune.constants.ChipSortTypeKey
import com.abhiram.flowtune.constants.DarkModeKey
import com.abhiram.flowtune.constants.DefaultOpenTabKey
import com.abhiram.flowtune.constants.EnableDynamicIconKey
import com.abhiram.flowtune.constants.DynamicThemeKey
import com.abhiram.flowtune.constants.GridItemSize
import com.abhiram.flowtune.constants.GridItemsSizeKey
import com.abhiram.flowtune.constants.HidePlayerThumbnailKey
import com.abhiram.flowtune.constants.LibraryFilter
import com.abhiram.flowtune.constants.LyricsClickKey
import com.abhiram.flowtune.constants.LyricsScrollKey
import com.abhiram.flowtune.constants.LyricsTextPositionKey
import com.abhiram.flowtune.constants.LyricsAnimationStyle
import com.abhiram.flowtune.constants.LyricsAnimationStyleKey
import com.abhiram.flowtune.constants.LyricsTextSizeKey
import com.abhiram.flowtune.constants.LyricsLineSpacingKey
import com.abhiram.flowtune.constants.LyricsGlowEffectKey
import com.abhiram.flowtune.constants.MiniPlayerOutlineKey
import com.abhiram.flowtune.constants.PlayerBackgroundStyle
import com.abhiram.flowtune.constants.PlayerBackgroundStyleKey
import com.abhiram.flowtune.constants.PlayerButtonsStyle
import com.abhiram.flowtune.constants.PlayerButtonsStyleKey
import com.abhiram.flowtune.constants.PureBlackKey
import com.abhiram.flowtune.constants.PureBlackMiniPlayerKey
import com.abhiram.flowtune.constants.ShowCachedPlaylistKey
import com.abhiram.flowtune.constants.ShowDownloadedPlaylistKey
import com.abhiram.flowtune.constants.ShowLikedPlaylistKey
import com.abhiram.flowtune.constants.ShowTopPlaylistKey
import com.abhiram.flowtune.constants.ShowUploadedPlaylistKey
import com.abhiram.flowtune.constants.SliderStyle
import com.abhiram.flowtune.constants.SliderStyleKey
import com.abhiram.flowtune.constants.SquigglySliderKey
import com.abhiram.flowtune.constants.SlimNavBarKey
import com.abhiram.flowtune.constants.SwipeSensitivityKey
import com.abhiram.flowtune.constants.SwipeThumbnailKey
import com.abhiram.flowtune.constants.SwipeToSongKey
import com.abhiram.flowtune.constants.SwipeToRemoveSongKey
import com.abhiram.flowtune.constants.UseNewMiniPlayerDesignKey
import com.abhiram.flowtune.constants.UseNewPlayerDesignKey
import com.abhiram.flowtune.ui.component.DefaultDialog
import com.abhiram.flowtune.ui.component.EnumDialog
import com.abhiram.flowtune.ui.component.IconButton
import com.abhiram.flowtune.ui.component.Material3SettingsGroup
import com.abhiram.flowtune.ui.component.Material3SettingsItem
import com.abhiram.flowtune.ui.component.PlayerSliderTrack
import com.abhiram.flowtune.ui.theme.PlayerSliderColors
import com.abhiram.flowtune.ui.utils.backToMain
import com.abhiram.flowtune.utils.IconUtils
import com.abhiram.flowtune.utils.rememberEnumPreference
import com.abhiram.flowtune.utils.rememberPreference
import com.abhiram.flowtune.ui.component.WavySlider
import me.saket.squiggles.SquigglySlider
import kotlin.math.roundToInt
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import android.app.Activity
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    activity: Activity,
    snackbarHostState: SnackbarHostState,
) {
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(
        DynamicThemeKey,
        defaultValue = true
    )
    val (enableDynamicIcon, onEnableDynamicIconChange) = rememberPreference(
        EnableDynamicIconKey,
        defaultValue = true
    )
    val coroutineScope = rememberCoroutineScope()

    fun handleIconChange(enabled: Boolean) {
        onEnableDynamicIconChange(enabled)
        IconUtils.setIcon(activity, enabled)
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Icon updated, restart to apply",
                actionLabel = "Restart"
            )
            if (result == SnackbarResult.ActionPerformed) {
                val packageManager = activity.packageManager
                val intent = packageManager.getLaunchIntentForPackage(activity.packageName)
                val componentName = intent?.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                activity.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
        }
    }

    val (darkMode, onDarkModeChange) = rememberEnumPreference(
        DarkModeKey,
        defaultValue = DarkMode.ON
    )
    val (useNewPlayerDesign, onUseNewPlayerDesignChange) = rememberPreference(
        UseNewPlayerDesignKey,
        defaultValue = false
    )
    val (useNewMiniPlayerDesign, onUseNewMiniPlayerDesignChange) = rememberPreference(
        UseNewMiniPlayerDesignKey,
        defaultValue = true
    )
    val (hidePlayerThumbnail, onHidePlayerThumbnailChange) = rememberPreference(
        HidePlayerThumbnailKey,
        defaultValue = false
    )
    val (playerBackground, onPlayerBackgroundChange) =
        rememberEnumPreference(
            PlayerBackgroundStyleKey,
            defaultValue = PlayerBackgroundStyle.GRADIENT,
        )
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = true)
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(
        DefaultOpenTabKey,
        defaultValue = NavigationTab.HOME
    )
    val (playerButtonsStyle, onPlayerButtonsStyleChange) = rememberEnumPreference(
        PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.TERTIARY
    )
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(
        LyricsTextPositionKey,
        defaultValue = LyricsPosition.CENTER
    )
    val (lyricsClick, onLyricsClickChange) = rememberPreference(LyricsClickKey, defaultValue = true)
    val (lyricsScroll, onLyricsScrollChange) = rememberPreference(
        LyricsScrollKey,
        defaultValue = true
    )
    val (lyricsAnimationStyle, onLyricsAnimationStyleChange) = rememberEnumPreference(
        LyricsAnimationStyleKey,
        defaultValue = LyricsAnimationStyle.NONE
    )
    val (lyricsTextSize, onLyricsTextSizeChange) = rememberPreference(LyricsTextSizeKey, defaultValue = 24f)
    val (lyricsLineSpacing, onLyricsLineSpacingChange) = rememberPreference(LyricsLineSpacingKey, defaultValue = 1.3f)
    val (lyricsGlowEffect, onLyricsGlowEffectChange) = rememberPreference(LyricsGlowEffectKey, defaultValue = false)

    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(
        SliderStyleKey,
        defaultValue = SliderStyle.SLIM
    )
    val (squigglySlider, onSquigglySliderChange) = rememberPreference(
        SquigglySliderKey,
        defaultValue = false
    )
    val (swipeThumbnail, onSwipeThumbnailChange) = rememberPreference(
        SwipeThumbnailKey,
        defaultValue = false
    )
    val (swipeSensitivity, onSwipeSensitivityChange) = rememberPreference(
        SwipeSensitivityKey,
        defaultValue = 0.73f
    )
    val (gridItemSize, onGridItemSizeChange) = rememberEnumPreference(
        GridItemsSizeKey,
        defaultValue = GridItemSize.SMALL
    )

    val (slimNav, onSlimNavChange) = rememberPreference(
        SlimNavBarKey,
        defaultValue = false
    )

    val (swipeToSong, onSwipeToSongChange) = rememberPreference(
        SwipeToSongKey,
        defaultValue = false
    )

    val (swipeToRemoveSong, onSwipeToRemoveSongChange) = rememberPreference(
        SwipeToRemoveSongKey,
        defaultValue = false
    )

    val (showLikedPlaylist, onShowLikedPlaylistChange) = rememberPreference(
        ShowLikedPlaylistKey,
        defaultValue = true
    )
    val (showDownloadedPlaylist, onShowDownloadedPlaylistChange) = rememberPreference(
        ShowDownloadedPlaylistKey,
        defaultValue = true
    )
    val (showTopPlaylist, onShowTopPlaylistChange) = rememberPreference(
        ShowTopPlaylistKey,
        defaultValue = false
    )
    val (showCachedPlaylist, onShowCachedPlaylistChange) = rememberPreference(
        ShowCachedPlaylistKey,
        defaultValue = true
    )
    val (showUploadedPlaylist, onShowUploadedPlaylistChange) = rememberPreference(
        ShowUploadedPlaylistKey,
        defaultValue = false
    )

    val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
        it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme =
        remember(darkMode, isSystemInDarkTheme) {
            if (darkMode == DarkMode.AUTO) isSystemInDarkTheme else darkMode == DarkMode.ON
        }

    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(
        key = ChipSortTypeKey,
        defaultValue = LibraryFilter.LIBRARY
    )

    var showSliderOptionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showDarkModeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showPlayerBackgroundDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showPlayerButtonsStyleDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsPositionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsAnimationStyleDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsTextSizeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLyricsLineSpacingDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showLyricsPositionDialog) {
        EnumDialog(
            onDismiss = { showLyricsPositionDialog = false },
            onSelect = {
                onLyricsPositionChange(it)
                showLyricsPositionDialog = false
            },
            title = stringResource(R.string.lyrics_text_position),
            current = lyricsPosition,
            values = LyricsPosition.values().toList(),
            valueText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
            }
        )
    }

    if (showLyricsAnimationStyleDialog) {
        EnumDialog(
            onDismiss = { showLyricsAnimationStyleDialog = false },
            onSelect = {
                onLyricsAnimationStyleChange(it)
                showLyricsAnimationStyleDialog = false
            },
            title = stringResource(R.string.lyrics_animation_style),
            current = lyricsAnimationStyle,
            values = LyricsAnimationStyle.values().toList(),
            valueText = {
                when (it) {
                    LyricsAnimationStyle.NONE -> stringResource(R.string.none)
                    LyricsAnimationStyle.FADE -> stringResource(R.string.fade)
                    LyricsAnimationStyle.GLOW -> stringResource(R.string.glow)
                    LyricsAnimationStyle.SLIDE -> stringResource(R.string.slide)
                    LyricsAnimationStyle.KARAOKE -> stringResource(R.string.karaoke)
                    LyricsAnimationStyle.APPLE -> stringResource(R.string.apple_music_style)
                }
            }
        )
    }

    if (showLyricsTextSizeDialog) {
        var tempTextSize by remember { mutableFloatStateOf(lyricsTextSize) }
        
        DefaultDialog(
            onDismiss = { 
                tempTextSize = lyricsTextSize
                showLyricsTextSizeDialog = false 
            },
            buttons = {
                TextButton(
                    onClick = { 
                        tempTextSize = 24f
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = { 
                        tempTextSize = lyricsTextSize
                        showLyricsTextSizeDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = { 
                        onLyricsTextSizeChange(tempTextSize)
                        showLyricsTextSizeDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.lyrics_text_size),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "${tempTextSize.roundToInt()} sp",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Slider(
                    value = tempTextSize,
                    onValueChange = { tempTextSize = it },
                    valueRange = 16f..36f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showLyricsLineSpacingDialog) {
        var tempLineSpacing by remember { mutableFloatStateOf(lyricsLineSpacing) }
        
        DefaultDialog(
            onDismiss = { 
                tempLineSpacing = lyricsLineSpacing
                showLyricsLineSpacingDialog = false 
            },
            buttons = {
                TextButton(
                    onClick = { 
                        tempLineSpacing = 1.3f
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = { 
                        tempLineSpacing = lyricsLineSpacing
                        showLyricsLineSpacingDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = { 
                        onLyricsLineSpacingChange(tempLineSpacing)
                        showLyricsLineSpacingDialog = false 
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.lyrics_line_spacing),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "${String.format("%.1f", tempLineSpacing)}x",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Slider(
                    value = tempLineSpacing,
                    onValueChange = { tempLineSpacing = it },
                    valueRange = 1.0f..2.0f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showPlayerButtonsStyleDialog) {
        EnumDialog(
            onDismiss = { showPlayerButtonsStyleDialog = false },
            onSelect = {
                onPlayerButtonsStyleChange(it)
                showPlayerButtonsStyleDialog = false
            },
            title = stringResource(R.string.player_buttons_style),
            current = playerButtonsStyle,
            values = PlayerButtonsStyle.values().toList(),
            valueText = {
                when (it) {
                    PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
                    PlayerButtonsStyle.PRIMARY -> stringResource(R.string.primary_color_style)
                    PlayerButtonsStyle.TERTIARY -> stringResource(R.string.tertiary_color_style)
                }
            }
        )
    }

    if (showPlayerBackgroundDialog) {
        EnumDialog(
            onDismiss = { showPlayerBackgroundDialog = false },
            onSelect = {
                onPlayerBackgroundChange(it)
                showPlayerBackgroundDialog = false
            },
            title = stringResource(R.string.player_background_style),
            current = playerBackground,
            values = availableBackgroundStyles,
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                }
            }
        )
    }

    if (showDarkModeDialog) {
        EnumDialog(
            onDismiss = { showDarkModeDialog = false },
            onSelect = {
                onDarkModeChange(it)
                showDarkModeDialog = false
            },
            title = stringResource(R.string.dark_theme),
            current = darkMode,
            values = DarkMode.values().toList(),
            valueText = {
                when (it) {
                    DarkMode.ON -> stringResource(R.string.dark_theme_on)
                    DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                    DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                }
            }
        )
    }

    var showDefaultOpenTabDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDefaultOpenTabDialog) {
        EnumDialog(
            onDismiss = { showDefaultOpenTabDialog = false },
            onSelect = {
                onDefaultOpenTabChange(it)
                showDefaultOpenTabDialog = false
            },
            title = stringResource(R.string.default_open_tab),
            current = defaultOpenTab,
            values = NavigationTab.values().toList(),
            valueText = {
                when (it) {
                    NavigationTab.HOME -> stringResource(R.string.home)
                    NavigationTab.SEARCH -> stringResource(R.string.search)
                    NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                }
            }
        )
    }

    var showDefaultChipDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDefaultChipDialog) {
        EnumDialog(
            onDismiss = { showDefaultChipDialog = false },
            onSelect = {
                onDefaultChipChange(it)
                showDefaultChipDialog = false
            },
            title = stringResource(R.string.default_lib_chips),
            current = defaultChip,
            values = LibraryFilter.values().toList(),
            valueText = {
                when (it) {
                    LibraryFilter.SONGS -> stringResource(R.string.songs)
                    LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                    LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                    LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                    LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                }
            }
        )
    }

    var showGridSizeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showGridSizeDialog) {
        EnumDialog(
            onDismiss = { showGridSizeDialog = false },
            onSelect = {
                onGridItemSizeChange(it)
                showGridSizeDialog = false
            },
            title = stringResource(R.string.grid_cell_size),
            current = gridItemSize,
            values = GridItemSize.values().toList(),
            valueText = {
                when (it) {
                    GridItemSize.BIG -> stringResource(R.string.big)
                    GridItemSize.SMALL -> stringResource(R.string.small)
                }
            }
        )
    }

    if (showSliderOptionDialog) {
        DefaultDialog(
            buttons = {
                TextButton(
                    onClick = { showSliderOptionDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            onDismiss = {
                showSliderOptionDialog = false
            }
        ) {
            val sliderPreviewColors = PlayerSliderColors.getSliderColors(
                MaterialTheme.colorScheme.primary,
                PlayerBackgroundStyle.DEFAULT,
                isSystemInDarkTheme()
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.DEFAULT && !squigglySlider) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.DEFAULT)
                                onSquigglySliderChange(false)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.35f
                        Slider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                            onValueChange = { /* preview only */ },
                            colors = sliderPreviewColors,
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.default_),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.WAVY && !squigglySlider) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.WAVY)
                                onSquigglySliderChange(false)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.5f
                        WavySlider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                            onValueChange = { /* preview only */ },
                            colors = sliderPreviewColors,
                            modifier = Modifier.weight(1f),
                            isPlaying = true,
                            enabled = false
                        )
                        Text(
                            text = stringResource(R.string.wavy),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.SLIM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.SLIM)
                                onSquigglySliderChange(false)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.65f
                        Slider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                            onValueChange = { /* preview only */ },
                            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                            track = { sliderState ->
                                PlayerSliderTrack(
                                    sliderState = sliderState,
                                    colors = sliderPreviewColors
                                )
                            },
                            colors = sliderPreviewColors,
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = stringResource(R.string.slim),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (sliderStyle == SliderStyle.WAVY && squigglySlider) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSliderStyleChange(SliderStyle.WAVY)
                                onSquigglySliderChange(true)
                                showSliderOptionDialog = false
                            }
                            .padding(12.dp)
                    ) {
                        val sliderValue = 0.5f
                        SquigglySlider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                            onValueChange = { /* preview only */ },
                            modifier = Modifier
                                .weight(1f)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent()
                                        }
                                    }
                                },
                            squigglesSpec = SquigglySlider.SquigglesSpec(
                                amplitude = 2.dp,
                                strokeWidth = 3.dp,
                            ),
                        )
                        Text(
                            text = stringResource(R.string.squiggly),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Material3SettingsGroup(
            title = stringResource(R.string.theme),
            items = buildList {
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.ic_dynamic_icon),
                        title = { Text(stringResource(R.string.enable_dynamic_icon)) },
                        trailingContent = {
                            Switch(
                                checked = enableDynamicIcon,
                                onCheckedChange = { handleIconChange(it) },
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (enableDynamicIcon) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { handleIconChange(!enableDynamicIcon) }
                    )
                )
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.palette),
                        title = { Text(stringResource(R.string.enable_dynamic_theme)) },
                        trailingContent = {
                            Switch(
                                checked = dynamicTheme,
                                onCheckedChange = onDynamicThemeChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (dynamicTheme) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { onDynamicThemeChange(!dynamicTheme) }
                    )
                )
                if (useDarkTheme) {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.contrast),
                            title = { Text(stringResource(R.string.pure_black)) },
                            trailingContent = {
                                Switch(
                                    checked = pureBlack,
                                    onCheckedChange = onPureBlackChange,
                                    thumbContent = {
                                        Icon(
                                            painter = painterResource(
                                                id = if (pureBlack) R.drawable.check else R.drawable.close
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                )
                            },
                            onClick = { onPureBlackChange(!pureBlack) }
                        )
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(27.dp))

        val (pureBlackMiniPlayer, onPureBlackMiniPlayerChange) = rememberPreference(
            PureBlackMiniPlayerKey,
            defaultValue = false
        )

        Spacer(modifier = Modifier.height(27.dp))

        var showSensitivityDialog by rememberSaveable { mutableStateOf(false) }

        Material3SettingsGroup(
            title = stringResource(R.string.player),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.swipe),
                    title = { Text(stringResource(R.string.enable_swipe_thumbnail)) },
                    trailingContent = {
                        Switch(
                            checked = swipeThumbnail,
                            onCheckedChange = onSwipeThumbnailChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (swipeThumbnail) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onSwipeThumbnailChange(!swipeThumbnail) }
                )
            ) + if (swipeThumbnail) listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.tune),
                    title = { Text(stringResource(R.string.swipe_sensitivity)) },
                    description = {
                        Text(
                            stringResource(
                                R.string.sensitivity_percentage,
                                (swipeSensitivity * 100).roundToInt()
                            )
                        )
                    },
                    onClick = { showSensitivityDialog = true }
                )
            ) else emptyList()
        )

        if (showSensitivityDialog) {
            var tempSensitivity by remember { mutableFloatStateOf(swipeSensitivity) }

            DefaultDialog(
                onDismiss = {
                    tempSensitivity = swipeSensitivity
                    showSensitivityDialog = false
                },
                buttons = {
                    TextButton(
                        onClick = {
                            tempSensitivity = 0.73f
                        }
                    ) {
                        Text(stringResource(R.string.reset))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = {
                            tempSensitivity = swipeSensitivity
                            showSensitivityDialog = false
                        }
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            onSwipeSensitivityChange(tempSensitivity)
                            showSensitivityDialog = false
                        }
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.swipe_sensitivity),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(
                            R.string.sensitivity_percentage,
                            (tempSensitivity * 100).roundToInt()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Slider(
                        value = tempSensitivity,
                        onValueChange = { tempSensitivity = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(27.dp))

        Material3SettingsGroup(
            title = stringResource(R.string.lyrics),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.lyrics),
                    title = { Text(stringResource(R.string.lyrics_auto_scroll)) },
                    trailingContent = {
                        Switch(
                            checked = lyricsScroll,
                            onCheckedChange = onLyricsScrollChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (lyricsScroll) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = { onLyricsScrollChange(!lyricsScroll) }
                )
            )
        )
        
        Spacer(modifier = Modifier.height(27.dp))

    }

    TopAppBar(
        title = { Text(stringResource(R.string.appearance)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        }
    )
}

enum class DarkMode {
    ON,
    OFF,
    AUTO,
}

enum class NavigationTab {
    HOME,
    SEARCH,
    LIBRARY,
}

enum class LyricsPosition {
    LEFT,
    CENTER,
    RIGHT,
}

enum class PlayerTextAlignment {
    SIDED,
    CENTER,
}
