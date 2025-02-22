package com.abhiram.flowtune.ui.utils

import androidx.compose.ui.util.fastAny
import androidx.navigation.NavController
import com.abhiram.flowtune.ui.screens.Screens

fun NavController.backToMain() {
    while (!Screens.MainScreens.fastAny { it.route == currentBackStackEntry?.destination?.route }) {
        navigateUp()
    }
}
