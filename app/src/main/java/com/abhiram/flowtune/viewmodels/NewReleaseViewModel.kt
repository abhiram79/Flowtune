package com.abhiram.flowtune.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhiram.flowtube.YouTube
import com.abhiram.flowtube.models.AlbumItem
import com.abhiram.flowtube.models.filterExplicit
import com.abhiram.flowtune.constants.HideExplicitKey
import com.abhiram.flowtune.db.MusicDatabase
import com.abhiram.flowtune.utils.dataStore
import com.abhiram.flowtune.utils.get
import com.abhiram.flowtune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewReleaseViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        database: MusicDatabase,
    ) : ViewModel() {
        private val _newReleaseAlbums = MutableStateFlow<List<AlbumItem>>(emptyList())
        val newReleaseAlbums = _newReleaseAlbums.asStateFlow()

        init {
            viewModelScope.launch {
                YouTube
                    .newReleaseAlbums()
                    .onSuccess { albums ->
                        val artists: MutableMap<Int, String> = mutableMapOf()
                        val favouriteArtists: MutableMap<Int, String> = mutableMapOf()
                        database.allArtistsByPlayTime().first().let { list ->
                            var favIndex = 0
                            for ((artistsIndex, artist) in list.withIndex()) {
                                artists[artistsIndex] = artist.id
                                if (artist.artist.bookmarkedAt != null) {
                                    favouriteArtists[favIndex] = artist.id
                                    favIndex++
                                }
                            }
                        }
                        _newReleaseAlbums.value =
                            albums
                                .sortedBy { album ->
                                    val artistIds = album.artists.orEmpty().mapNotNull { it.id }
                                    val firstArtistKey =
                                        artistIds.firstNotNullOfOrNull { artistId ->
                                            if (artistId in favouriteArtists.values) {
                                                favouriteArtists.entries.firstOrNull { it.value == artistId }?.key
                                            } else {
                                                artists.entries.firstOrNull { it.value == artistId }?.key
                                            }
                                        } ?: Int.MAX_VALUE
                                    firstArtistKey
                                }.filterExplicit(context.dataStore.get(HideExplicitKey, false))
                    }.onFailure {
                        reportException(it)
                    }
            }
        }
    }
