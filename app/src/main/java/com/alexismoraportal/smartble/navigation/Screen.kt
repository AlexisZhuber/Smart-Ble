package com.alexismoraportal.smartble.navigation

/**
 * Represents the various screens (routes) available in the application.
 *
 * Each object in this sealed class corresponds to a distinct screen and its associated navigation route.
 */
sealed class Screen(val route: String) {

    /** Home screen route. */
    object Home : Screen("home")

    /** About screen route. */
    object About : Screen("about")

    /** Color Picker screen route. */
    object ColorPicker : Screen("colorPicker")
}
