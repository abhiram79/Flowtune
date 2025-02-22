package com.abhiram.flowtube.pages

import com.abhiram.flowtube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
