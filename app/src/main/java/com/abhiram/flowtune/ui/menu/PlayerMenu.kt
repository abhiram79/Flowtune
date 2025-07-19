package com.abhiram.flowtune.ui.menu

import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.abhiram.innertube.YouTube
import com.abhiram.innertube.models.WatchEndpoint
import com.abhiram.flowtune.LocalDatabase
import com.abhiram.flowtune.LocalDownloadUtil
import com.abhiram.flowtune.LocalPlayerConnection
import com.abhiram.flowtune.R
import com.abhiram.flowtune.constants.ListItemHeight
import com.abhiram.flowtune.constants.ListThumbnailSize
import com.abhiram.flowtune.constants.ThumbnailCornerRadius
import com.abhiram.flowtune.models.MediaMetadata
import com.abhiram.flowtune.playback.ExoDownloadService
import com.abhiram.flowtune.playback.queues.YouTubeQueue
import com.abhiram.flowtune.ui.component.BottomSheetState
import com.abhiram.flowtune.ui.component.DownloadGridMenu
import com.abhiram.flowtune.ui.component.GridMenu
import com.abhiram.flowtune.ui.component.GridMenuItem
import com.abhiram.flowtune.ui.component.ListDialog
import com.abhiram.flowtune.ui.component.ListItem
import com.abhiram.flowtune.utils.joinByBullet
import com.abhiram.flowtune.utils.makeTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

@Composable
fun PlayerMenu(
    mediaMetadata: MediaMetadata?,
    navController: NavController,
    playerBottomSheetState: BottomSheetState,
    isQueueTrigger: Boolean? = false,
    onShowDetailsDialog: () -> Unit,
    onDismiss: () -> Unit,
) {
    mediaMetadata ?: return
    val context = LocalContext.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playerVolume = playerConnection.service.playerVolume.collectAsState()
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val librarySong by database.song(mediaMetadata.id).collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()


    val download by LocalDownloadUtil.current.getDownload(mediaMetadata.id)
        .collectAsState(initial = null)

    val artists =
        remember(mediaMetadata.artists) {
            mediaMetadata.artists.filter { it.id != null }
        }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showErrorPlaylistAddDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        isVisible = showChoosePlaylistDialog,
        onGetSong = { playlist ->
            database.transaction {
                insert(mediaMetadata)
            }
            coroutineScope.launch(Dispatchers.IO) {
                playlist.playlist.browseId?.let { YouTube.addToPlaylist(it, mediaMetadata.id) }
            }
            listOf(mediaMetadata.id)
        },
        onDismiss = {
            showChoosePlaylistDialog = false
        }
    )



    if (showErrorPlaylistAddDialog) {
        ListDialog(
            onDismiss = {
                showErrorPlaylistAddDialog = false
                onDismiss()
            },
        ) {
            item {
                ListItem(
                    title = stringResource(R.string.already_in_playlist),
                    thumbnailContent = {
                        Image(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.size(ListThumbnailSize),
                        )
                    },
                    modifier =
                        Modifier
                            .clickable { showErrorPlaylistAddDialog = false },
                )
            }

            item {
                ListItem(
                    title = mediaMetadata.title,
                    thumbnailContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(ListThumbnailSize),
                        ) {
                            AsyncImage(
                                model = mediaMetadata.thumbnailUrl,
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                            )
                        }
                    },
                    subtitle =
                        joinByBullet(
                            mediaMetadata.artists.joinToString { it.name },
                            makeTimeString(mediaMetadata.duration * 1000L),
                        ),
                )
            }
        }
    }

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false },
        ) {
            items(artists) { artist ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier =
                        Modifier
                            .fillParentMaxWidth()
                            .height(ListItemHeight)
                            .clickable {
                                navController.navigate("artist/${artist.id}")
                                showSelectArtistDialog = false
                                playerBottomSheetState.collapseSoft()
                                onDismiss()
                            }
                            .padding(horizontal = 24.dp),
                ) {
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    var showPitchTempoDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showPitchTempoDialog) {
        TempoPitchDialog(
            onDismiss = { showPitchTempoDialog = false },
        )
    }
    if (isQueueTrigger != true) {
        // State to track if audio is muted
        var isMuted by remember { mutableStateOf(false) }

        // Store the volume before muting to restore later
        var previousVolume by remember { mutableFloatStateOf(playerVolume.value) }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 }, // Reduce la distancia de deslizamiento
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 100)) +
                    slideOutVertically(
                        targetOffsetY = { it / 4 }, // Reduce la distancia de deslizamiento
                        animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing)
                    )
        ) {

            AlertDialog(
                onDismissRequest = onDismiss,
                title = {

                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(android.R.string.ok))
                    }

                },
                text = {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {

                            Icon(
                                painter = painterResource(
                                    // Dynamic icon based on mute state
                                    id = if (isMuted) R.drawable.volume_off
                                    else R.drawable.volume_up
                                ),
                                contentDescription = stringResource(
                                    if (isMuted) R.string.unmute
                                    else R.string.mute
                                ),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        isMuted = !isMuted
                                        if (isMuted) {
                                            // Store current volume before muting
                                            previousVolume = playerVolume.value
                                            // Set volume to 0
                                            playerConnection.service.playerVolume.value = 0f
                                        } else {
                                            // Restore previous volume when unmuting
                                            playerConnection.service.playerVolume.value =
                                                previousVolume
                                        }
                                    }
                                    .padding(4.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Enhanced Seek Bar with More Precise Control
                            Slider(
                                value = if (isMuted) 0f else playerVolume.value,
                                onValueChange = { newVolume ->
                                    // Disable slider when muted
                                    if (!isMuted) {
                                        // Update volume and ensure muted state is off
                                        playerConnection.service.playerVolume.value = newVolume
                                        previousVolume = newVolume
                                    }
                                },
                                valueRange = 0f..1f,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )


                            Text(
                                text = if (isMuted) "0%" else "${(playerVolume.value * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(40.dp)
                            )
                        }
                        GridMenu(
                            contentPadding =
                                PaddingValues(
                                    start = 8.dp,
                                    top = 8.dp,
                                    end = 8.dp,
                                    bottom = 8.dp + WindowInsets.systemBars.asPaddingValues()
                                        .calculateBottomPadding(),
                                ),
                        ) {
                            GridMenuItem(
                                icon = R.drawable.radio,
                                title = R.string.start_radio,
                            ) {
                                playerConnection.playQueue(
                                    YouTubeQueue(
                                        WatchEndpoint(videoId = mediaMetadata.id),
                                        mediaMetadata
                                    )
                                )
                                onDismiss()
                            }
                            GridMenuItem(
                                icon = R.drawable.playlist_add,
                                title = R.string.add_to_playlist,
                            ) {
                                showChoosePlaylistDialog = true
                            }
                            DownloadGridMenu(
                                state = download?.state,
                                onDownload = {
                                    database.transaction {
                                        insert(mediaMetadata)
                                    }
                                    val downloadRequest =
                                        DownloadRequest
                                            .Builder(mediaMetadata.id, mediaMetadata.id.toUri())
                                            .setCustomCacheKey(mediaMetadata.id)
                                            .setData(mediaMetadata.title.toByteArray())
                                            .build()
                                    DownloadService.sendAddDownload(
                                        context,
                                        ExoDownloadService::class.java,
                                        downloadRequest,
                                        false,
                                    )
                                },
                                onRemoveDownload = {
                                    DownloadService.sendRemoveDownload(
                                        context,
                                        ExoDownloadService::class.java,
                                        mediaMetadata.id,
                                        false,
                                    )
                                },
                            )
                            if (librarySong?.song?.inLibrary != null) {
                                GridMenuItem(
                                    icon = R.drawable.library_add_check,
                                    title = R.string.remove_from_library,
                                ) {
                                    database.query {
                                        inLibrary(mediaMetadata.id, null)
                                    }
                                }
                            } else {
                                GridMenuItem(
                                    icon = R.drawable.library_add,
                                    title = R.string.add_to_library,
                                ) {
                                    database.transaction {
                                        insert(mediaMetadata)
                                        inLibrary(mediaMetadata.id, LocalDateTime.now())
                                    }
                                }
                            }
                            if (artists.isNotEmpty()) {
                                GridMenuItem(
                                    icon = R.drawable.artist,
                                    title = R.string.view_artist,
                                ) {
                                    if (mediaMetadata.artists.size == 1) {
                                        navController.navigate("artist/${mediaMetadata.artists[0].id}")
                                        playerBottomSheetState.collapseSoft()
                                        onDismiss()
                                    } else {
                                        showSelectArtistDialog = true
                                    }
                                }
                            }
                            if (mediaMetadata.album != null) {
                                GridMenuItem(
                                    icon = R.drawable.album,
                                    title = R.string.view_album,
                                ) {
                                    navController.navigate("album/${mediaMetadata.album.id}")
                                    playerBottomSheetState.collapseSoft()
                                    onDismiss()
                                }
                            }
                            GridMenuItem(
                                icon = R.drawable.share,
                                title = R.string.share,
                            ) {
                                val intent =
                                    Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "https://music.youtube.com/watch?v=${mediaMetadata.id}"
                                        )
                                    }
                                context.startActivity(Intent.createChooser(intent, null))
                                onDismiss()
                            }
                            if (isQueueTrigger != true) {
                                GridMenuItem(
                                    icon = R.drawable.info,
                                    title = R.string.details,
                                ) {
                                    onShowDetailsDialog()
                                    onDismiss()
                                }
                                GridMenuItem(
                                    icon = R.drawable.equalizer,
                                    title = R.string.equalizer,
                                ) {
                                    val intent =
                                        Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                            putExtra(
                                                AudioEffect.EXTRA_AUDIO_SESSION,
                                                playerConnection.player.audioSessionId,
                                            )
                                            putExtra(
                                                AudioEffect.EXTRA_PACKAGE_NAME,
                                                context.packageName
                                            )
                                            putExtra(
                                                AudioEffect.EXTRA_CONTENT_TYPE,
                                                AudioEffect.CONTENT_TYPE_MUSIC
                                            )
                                        }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        activityResultLauncher.launch(intent)
                                    }
                                    onDismiss()
                                }
                                GridMenuItem(
                                    icon = R.drawable.tune,
                                    title = R.string.advanced,
                                ) {
                                    showPitchTempoDialog = true
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TempoPitchDialog(onDismiss: () -> Unit) {
    val playerConnection = LocalPlayerConnection.current ?: return
    var tempo by remember {
        mutableFloatStateOf(playerConnection.player.playbackParameters.speed)
    }
    var transposeValue by remember {
        mutableIntStateOf(round(12 * log2(playerConnection.player.playbackParameters.pitch)).toInt())
    }
    val updatePlaybackParameters = {
        playerConnection.player.playbackParameters =
            PlaybackParameters(tempo, 2f.pow(transposeValue.toFloat() / 12))
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.tempo_and_pitch))
        },
        dismissButton = {
            TextButton(
                onClick = {
                    tempo = 1f
                    transposeValue = 0
                    updatePlaybackParameters()
                },
            ) {
                Text(stringResource(R.string.reset))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        text = {
            Column {
                ValueAdjuster(
                    icon = R.drawable.speed,
                    currentValue = tempo,
                    values = (0..35).map { round((0.25f + it * 0.05f) * 100) / 100 },
                    onValueUpdate = {
                        tempo = it
                        updatePlaybackParameters()
                    },
                    valueText = { "x$it" },
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                ValueAdjuster(
                    icon = R.drawable.discover_tune,
                    currentValue = transposeValue,
                    values = (-12..12).toList(),
                    onValueUpdate = {
                        transposeValue = it
                        updatePlaybackParameters()
                    },
                    valueText = { "${if (it > 0) "+" else ""}$it" },
                )
            }
        },
    )
}

@Composable
fun <T> ValueAdjuster(
    @DrawableRes icon: Int,
    currentValue: T,
    values: List<T>,
    onValueUpdate: (T) -> Unit,
    valueText: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )

        IconButton(
            enabled = currentValue != values.first(),
            onClick = {
                onValueUpdate(values[values.indexOf(currentValue) - 1])
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.remove),
                contentDescription = null,
            )
        }

        Text(
            text = valueText(currentValue),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp),
        )

        IconButton(
            enabled = currentValue != values.last(),
            onClick = {
                onValueUpdate(values[values.indexOf(currentValue) + 1])
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = null,
            )
        }
    }
}