@file:OptIn(ExperimentalCoroutinesApi::class)

package com.abhiram.flowtune.viewmodels

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import com.abhiram.flowtube.YouTube
import com.abhiram.flowtune.constants.AlbumFilter
import com.abhiram.flowtune.constants.AlbumFilterKey
import com.abhiram.flowtune.constants.AlbumSortDescendingKey
import com.abhiram.flowtune.constants.AlbumSortType
import com.abhiram.flowtune.constants.AlbumSortTypeKey
import com.abhiram.flowtune.constants.ArtistFilter
import com.abhiram.flowtune.constants.ArtistFilterKey
import com.abhiram.flowtune.constants.ArtistSongSortDescendingKey
import com.abhiram.flowtune.constants.ArtistSongSortType
import com.abhiram.flowtune.constants.ArtistSongSortTypeKey
import com.abhiram.flowtune.constants.ArtistSortDescendingKey
import com.abhiram.flowtune.constants.ArtistSortType
import com.abhiram.flowtune.constants.ArtistSortTypeKey
import com.abhiram.flowtune.constants.LibraryFilter
import com.abhiram.flowtune.constants.PlaylistSortDescendingKey
import com.abhiram.flowtune.constants.PlaylistSortType
import com.abhiram.flowtune.constants.PlaylistSortTypeKey
import com.abhiram.flowtune.constants.SongFilter
import com.abhiram.flowtune.constants.SongFilterKey
import com.abhiram.flowtune.constants.SongSortDescendingKey
import com.abhiram.flowtune.constants.SongSortType
import com.abhiram.flowtune.constants.SongSortTypeKey
import com.abhiram.flowtune.constants.TopSize
import com.abhiram.flowtune.db.MusicDatabase
import com.abhiram.flowtune.extensions.reversed
import com.abhiram.flowtune.extensions.toEnum
import com.abhiram.flowtune.playback.DownloadUtil
import com.abhiram.flowtune.utils.dataStore
import com.abhiram.flowtune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LibrarySongsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        downloadUtil: DownloadUtil,
    ) : ViewModel() {
        val allSongs =
            context.dataStore.data
                .map {
                    Triple(
                        it[SongFilterKey].toEnum(SongFilter.LIBRARY),
                        it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE),
                        (it[SongSortDescendingKey] ?: true),
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (filter, sortType, descending) ->
                    when (filter) {
                        SongFilter.LIBRARY -> database.songs(sortType, descending)
                        SongFilter.LIKED -> database.likedSongs(sortType, descending)
                        SongFilter.DOWNLOADED ->
                            downloadUtil.downloads.flatMapLatest { downloads ->
                                database
                                    .allSongs()
                                    .flowOn(Dispatchers.IO)
                                    .map { songs ->
                                        songs.filter {
                                            downloads[it.id]?.state == Download.STATE_COMPLETED
                                        }
                                    }.map { songs ->
                                        when (sortType) {
                                            SongSortType.CREATE_DATE -> songs.sortedBy { downloads[it.id]?.updateTimeMs ?: 0L }
                                            SongSortType.NAME -> songs.sortedBy { it.song.title }
                                            SongSortType.ARTIST -> {
                                                val collator =
                                                    Collator.getInstance(Locale.getDefault())
                                                collator.strength = Collator.PRIMARY
                                                songs
                                                    .sortedWith(
                                                        compareBy(collator) { song ->
                                                            song.artists.joinToString(
                                                                "",
                                                            ) { it.name }
                                                        },
                                                    ).groupBy { it.album?.title }
                                                    .flatMap { (_, songsByAlbum) ->
                                                        songsByAlbum.sortedBy { album ->
                                                            album.artists.joinToString(
                                                                "",
                                                            ) { it.name }
                                                        }
                                                    }
                                            }

                                            SongSortType.PLAY_TIME -> songs.sortedBy { it.song.totalPlayTime }
                                        }.reversed(descending)
                                    }
                            }
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

@HiltViewModel
class LibraryArtistsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
    ) : ViewModel() {
        val allArtists =
            context.dataStore.data
                .map {
                    Triple(
                        it[ArtistFilterKey].toEnum(ArtistFilter.LIBRARY),
                        it[ArtistSortTypeKey].toEnum(ArtistSortType.CREATE_DATE),
                        it[ArtistSortDescendingKey] ?: true,
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (filter, sortType, descending) ->
                    when (filter) {
                        ArtistFilter.LIBRARY -> database.artists(sortType, descending)
                        ArtistFilter.LIKED -> database.artistsBookmarked(sortType, descending)
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            viewModelScope.launch(Dispatchers.IO) {
                allArtists.collect { artists ->
                    artists
                        .map { it.artist }
                        .filter {
                            it.thumbnailUrl == null || Duration.between(it.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)
                        }.forEach { artist ->
                            YouTube.artist(artist.id).onSuccess { artistPage ->
                                database.query {
                                    update(artist, artistPage)
                                }
                            }
                        }
                }
            }
        }
    }

@HiltViewModel
class LibraryAlbumsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
    ) : ViewModel() {
        val allAlbums =
            context.dataStore.data
                .map {
                    Triple(
                        it[AlbumFilterKey].toEnum(AlbumFilter.LIBRARY),
                        it[AlbumSortTypeKey].toEnum(AlbumSortType.CREATE_DATE),
                        it[AlbumSortDescendingKey] ?: true,
                    )
                }.distinctUntilChanged()
                .flatMapLatest { (filter, sortType, descending) ->
                    when (filter) {
                        AlbumFilter.LIBRARY -> database.albums(sortType, descending)
                        AlbumFilter.LIKED -> database.albumsLiked(sortType, descending)
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            viewModelScope.launch(Dispatchers.IO) {
                allAlbums.collect { albums ->
                    albums
                        .filter {
                            it.album.songCount == 0
                        }.forEach { album ->
                            YouTube
                                .album(album.id)
                                .onSuccess { albumPage ->
                                    database.query {
                                        update(album.album, albumPage, album.artists)
                                    }
                                }.onFailure {
                                    reportException(it)
                                    if (it.message?.contains("NOT_FOUND") == true) {
                                        database.query {
                                            delete(album.album)
                                        }
                                    }
                                }
                        }
                }
            }
        }
    }

@HiltViewModel
class LibraryPlaylistsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
    ) : ViewModel() {
        val allPlaylists =
            context.dataStore.data
                .map {
                    it[PlaylistSortTypeKey].toEnum(PlaylistSortType.CREATE_DATE) to (it[PlaylistSortDescendingKey] ?: true)
                }.distinctUntilChanged()
                .flatMapLatest { (sortType, descending) ->
                    database.playlists(sortType, descending)
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        val topValue =
            context.dataStore.data
                .map { it[TopSize] ?: "50" }
                .distinctUntilChanged()
    }

@HiltViewModel
class ArtistSongsViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val artistId = savedStateHandle.get<String>("artistId")!!
        val artist =
            database
                .artist(artistId)
                .stateIn(viewModelScope, SharingStarted.Lazily, null)

        val songs =
            context.dataStore.data
                .map {
                    it[ArtistSongSortTypeKey].toEnum(ArtistSongSortType.CREATE_DATE) to (it[ArtistSongSortDescendingKey] ?: true)
                }.distinctUntilChanged()
                .flatMapLatest { (sortType, descending) ->
                    database.artistSongs(artistId, sortType, descending)
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

@HiltViewModel
class LibraryMixViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        database: MusicDatabase,
    ) : ViewModel() {
        val topValue =
            context.dataStore.data
                .map { it[TopSize] ?: "50" }
                .distinctUntilChanged()
        var artists =
            database
                .artistsBookmarked(
                    ArtistSortType.CREATE_DATE,
                    true,
                ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        var albums = database.albumsLiked(AlbumSortType.CREATE_DATE, true).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        var playlists = database.playlists(PlaylistSortType.CREATE_DATE, true).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            viewModelScope.launch(Dispatchers.IO) {
                albums.collect { albums ->
                    albums
                        .filter {
                            it.album.songCount == 0
                        }.forEach { album ->
                            YouTube
                                .album(album.id)
                                .onSuccess { albumPage ->
                                    database.query {
                                        update(album.album, albumPage, album.artists)
                                    }
                                }.onFailure {
                                    reportException(it)
                                    if (it.message?.contains("NOT_FOUND") == true) {
                                        database.query {
                                            delete(album.album)
                                        }
                                    }
                                }
                        }
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                artists.collect { artists ->
                    artists
                        .map { it.artist }
                        .filter {
                            it.thumbnailUrl == null ||
                                Duration.between(
                                    it.lastUpdateTime,
                                    LocalDateTime.now(),
                                ) > Duration.ofDays(10)
                        }.forEach { artist ->
                            YouTube.artist(artist.id).onSuccess { artistPage ->
                                database.query {
                                    update(artist, artistPage)
                                }
                            }
                        }
                }
            }
        }
    }

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor() : ViewModel() {
        private val curScreen = mutableStateOf(LibraryFilter.LIBRARY)
        val filter: MutableState<LibraryFilter> = curScreen
    }
