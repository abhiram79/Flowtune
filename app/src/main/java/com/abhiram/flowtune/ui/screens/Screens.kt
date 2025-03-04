package com.abhiram.flowtune.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.abhiram.flowtune.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int,
    val route: String,
) {
    object Home : Screens(R.string.home, R.drawable.home, "home")

    object Explore : Screens(R.string.explore, R.drawable.explore, "explore")
    
    object Offline : Screens(R.string.offline_tab, R.drawable.shortcut_songs, "history")

    object Library : Screens(R.string.filter_library, R.drawable.library_music, "library")

    companion object {
        val MainScreens = listOf(Home, Explore, Offline, Library)
    }
}
