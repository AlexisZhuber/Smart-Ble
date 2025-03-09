package com.alexismoraportal.smartble.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.navigation.Screen

/**
 * MenuItemsProvider provides a list of menu items used in the navigation drawer.
 *
 * Each menu item contains an id, a title resource id, an icon, and a navigation route.
 */
object MenuItemsProvider {
    val menuItems: List<MenuItem> = listOf(
        MenuItem(
            id = "home",
            title = R.string.menu_item_home, // Reference to the string resource for "Home"
            icon = Icons.Filled.Home,
            route = Screen.Home.route
        ),
        MenuItem(
            id = "code",
            title = R.string.menu_item_code, // Reference to the string resource for "Code ESP32"
            icon = Icons.Filled.Code,
            route = Screen.Code.route
        ),
        MenuItem(
            id = "about",
            title = R.string.menu_item_about, // Reference to the string resource for "About"
            icon = Icons.Filled.Info,
            route = Screen.About.route
        )
    )
}
