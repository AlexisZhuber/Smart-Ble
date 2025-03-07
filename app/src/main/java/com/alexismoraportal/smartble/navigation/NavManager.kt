package com.alexismoraportal.smartble.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.components.LateralMenu
import com.alexismoraportal.smartble.model.MenuItemsProvider
import com.alexismoraportal.smartble.screen.AboutScreen
import com.alexismoraportal.smartble.screen.ColorPickerScreen
import com.alexismoraportal.smartble.screen.HomeScreen

/**
 * NavManager sets up the main navigation of the application.
 * It wraps each screen with a [LateralMenu] that provides a consistent top bar and drawer.
 * The start destination is set to [Screen.Home].
 */
@Composable
fun NavManager() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        addScreen(
            screen = Screen.Home,
            content = { HomeScreen() },
            navController = navController
        )
        addScreen(
            screen = Screen.About,
            content = { AboutScreen() },
            navController = navController
        )
        addScreen(
            screen = Screen.ColorPicker,
            content = { ColorPickerScreen() },
            navController = navController
        )
    }
}

/**
 * Adds a screen to the NavHost graph, wrapping it in a [LateralMenu].
 * This ensures a consistent top bar and navigation drawer across all screens.
 * For the ColorPicker screen, drawer swipe gestures are disabled.
 *
 * @param screen The [Screen] object representing the route for this destination.
 * @param content The composable function that defines the screen's UI.
 * @param navController The NavHostController used for navigation actions.
 */
private fun NavGraphBuilder.addScreen(
    screen: Screen,
    content: @Composable () -> Unit,
    navController: androidx.navigation.NavHostController
) {
    composable(screen.route) {
        LateralMenu(
            menuItems = MenuItemsProvider.menuItems,
            topBarTitle = stringResource(id = R.string.app_name),
            topBarImageRes = null,
            // Enable gestures for Home and About, disable for ColorPicker
            enableGestures = screen != Screen.ColorPicker,
            onMenuItemClick = { menuItem ->
                navController.navigate(menuItem.route) {
                    popUpTo(Screen.Home.route)
                    launchSingleTop = true
                }
            }
        ) { paddingValues: PaddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        }
    }
}
