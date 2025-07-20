package com.abhiram.innertube.pages

import com.abhiram.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
