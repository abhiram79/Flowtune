/**
 * Flowtune YT Music Client (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.abhiram.flowtune.ui.screens.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun LibraryScreen(navController: NavController) {
    LibraryMixScreen(
        navController = navController,
        filterContent = {},
    )
}
