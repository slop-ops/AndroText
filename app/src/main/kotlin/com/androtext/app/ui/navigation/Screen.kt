package com.androtext.app.ui.navigation

sealed class Screen {
    data object Editor : Screen()
    data object OpenFile : Screen()
    data object Settings : Screen()
}
