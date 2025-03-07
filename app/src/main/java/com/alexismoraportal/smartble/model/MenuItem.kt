package com.alexismoraportal.smartble.model

/**
 * Data class representing a menu item in the navigation drawer.
 *
 * @param id Unique identifier for the menu item.
 * @param titleRes Resource ID for the title string.
 * @param icon Icon associated with the menu item.
 * @param route Navigation route corresponding to the menu item.
 */

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItem(
    val id: String,
    val title: Int,
    val icon: ImageVector,
    val route: String
)
