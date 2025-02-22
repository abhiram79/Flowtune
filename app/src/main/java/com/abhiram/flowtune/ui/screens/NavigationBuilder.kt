package com.abhiram.flowtune.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhiram.flowtune.ui.screens.artist.ArtistItemsScreen
import com.abhiram.flowtune.ui.screens.artist.ArtistScreen
import com.abhiram.flowtune.ui.screens.artist.ArtistSongsScreen
import com.abhiram.flowtune.ui.screens.library.LibraryScreen
import com.abhiram.flowtune.ui.screens.playlist.AutoPlaylistScreen
import com.abhiram.flowtune.ui.screens.playlist.LocalPlaylistScreen
import com.abhiram.flowtune.ui.screens.playlist.OnlinePlaylistScreen
import com.abhiram.flowtune.ui.screens.playlist.TopPlaylistScreen
import com.abhiram.flowtune.ui.screens.search.OnlineSearchResult
import com.abhiram.flowtune.ui.screens.settings.AboutScreen
import com.abhiram.flowtune.ui.screens.settings.AppearanceSettings
import com.abhiram.flowtune.ui.screens.settings.BackupAndRestore
import com.abhiram.flowtune.ui.screens.settings.ContentSettings
import com.abhiram.flowtune.ui.screens.settings.DiscordLoginScreen
import com.abhiram.flowtune.ui.screens.settings.DiscordSettings
import com.abhiram.flowtune.ui.screens.settings.PlayerSettings
import com.abhiram.flowtune.ui.screens.settings.PrivacySettings
import com.abhiram.flowtune.ui.screens.settings.SettingsScreen
import com.abhiram.flowtune.ui.screens.settings.StorageSettings

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    latestVersionName: String,
) {

// homescreen 

    composable(Screens.Home.route,
    enterTransition = {
            fadeIn(tween(500)) // Fade-in animation for entering the Home screen
        },
        exitTransition = {
            fadeOut(tween(500)) // Fade-out animation for exiting the Home screen
        },
        popEnterTransition = {
            fadeIn(tween(500)) // Fade-in animation when coming back to the Home screen
        },
        popExitTransition = {
            fadeOut(tween(500)) // Fade-out animation when navigating away from the Home screen
        }
    ) {
        HomeScreen(navController)
    }
    
 // library 
 
    composable(
        Screens.Library.route,
    enterTransition = {
            fadeIn(tween(500)) // Fade-in animation for entering the Library screen
        },
        exitTransition = {
            fadeOut(tween(500)) // Fade-out animation for exiting the Library screen
        },
        popEnterTransition = {
            fadeIn(tween(500)) // Fade-in animation when coming back to the Library screen
        },
        popExitTransition = {
            fadeOut(tween(500)) // Fade-out animation when navigating away from the Library screen
        }
    ) {
        LibraryScreen(navController)
    }
    
    
 // explore 
 
    composable(Screens.Explore.route,
    enterTransition = {
            fadeIn(tween(500)) // Fade-in animation for entering the explore screen
        },
        exitTransition = {
            fadeOut(tween(500)) // Fade-out animation for exiting the explore screen
        },
        popEnterTransition = {
            fadeIn(tween(500)) // Fade-in animation when coming back to the explore screen
        },
        popExitTransition = {
            fadeOut(tween(500)) // Fade-out animation when navigating away from the explore screen
        }
    ) {
        ExploreScreen(navController)
    }
    
 // history 
 
    composable("history",
    enterTransition = {
            fadeIn(tween(500)) // Fade-in animation for entering the history screen
        },
        exitTransition = {
            fadeOut(tween(500)) // Fade-out animation for exiting the history screen
        },
        popEnterTransition = {
            fadeIn(tween(500)) // Fade-in animation when coming back to the history screen
        },
        popExitTransition = {
            fadeOut(tween(500)) // Fade-out animation when navigating away from the History screen
        }
    ) {
        HistoryScreen(navController)
    }
    
    
    composable("stats") {
        StatsScreen(navController)
    }
    composable("mood_and_genres") {
        MoodAndGenresScreen(navController, scrollBehavior)
    }
    composable("account") {
        AccountScreen(navController, scrollBehavior)
    }
    composable("new_release") {
        NewReleaseScreen(navController, scrollBehavior)
    }
    composable(
        route = "search/{query}",
        arguments =
            listOf(
                navArgument("query") {
                    type = NavType.StringType
                },
            ),
        enterTransition = {
            fadeIn(tween(250))
        },
        exitTransition = {
            if (targetState.destination.route?.startsWith("search/") == true) {
                fadeOut(tween(200))
            } else {
                fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
            }
        },
        popEnterTransition = {
            if (initialState.destination.route?.startsWith("search/") == true) {
                fadeIn(tween(250))
            } else {
                fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
            }
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
    ) {
        OnlineSearchResult(navController)
    }
    composable(
        route = "album/{albumId}",
        arguments =
            listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                },
            ),
    ) {
        AlbumScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val artistId = backStackEntry.arguments?.getString("artistId")!!
        if (artistId.startsWith("LA")) {
            ArtistSongsScreen(navController, scrollBehavior)
        } else {
            ArtistScreen(navController, scrollBehavior)
        }
    }
    composable(
        route = "artist/{artistId}/songs",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        ArtistSongsScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/items?browseId={browseId}?params={params}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        ArtistItemsScreen(navController, scrollBehavior)
    }
    composable(
        route = "online_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        OnlinePlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "local_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        LocalPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "auto_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
                },
            ),
    ) {
        AutoPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "top_playlist/{top}",
        arguments =
            listOf(
                navArgument("top") {
                    type = NavType.StringType
                },
            ),
    ) {
        TopPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments =
            listOf(
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        YouTubeBrowseScreen(navController, scrollBehavior)
    }
    
 // settings 
 
    composable("settings",
    enterTransition = {
            slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(250)) // Slide in from right + fade-in
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(tween(250)) // Slide out to the left + fade-out
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }) + fadeIn(tween(250)) // Slide in from left + fade-in
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(250)) // Slide out to the right + fade-out
        }
    ) {
        SettingsScreen(navController, scrollBehavior, latestVersionName)
    }
    
  // 
    composable("settings/appearance") {
        AppearanceSettings(navController, scrollBehavior)
    }
    composable("settings/content") {
        ContentSettings(navController, scrollBehavior)
    }
    composable("settings/player") {
        PlayerSettings(navController, scrollBehavior)
    }
    composable("settings/storage") {
        StorageSettings(navController, scrollBehavior)
    }
    composable("settings/privacy") {
        PrivacySettings(navController, scrollBehavior)
    }
    composable("settings/backup_restore") {
        BackupAndRestore(navController, scrollBehavior)
    }
    composable("settings/discord") {
        DiscordSettings(navController, scrollBehavior)
    }
    composable("settings/discord/login") {
        DiscordLoginScreen(navController)
    }
    composable("settings/about") {
        AboutScreen(navController, scrollBehavior)
    }
    composable("login") {
        LoginScreen(navController)
    }
}
