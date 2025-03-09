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
import com.alexismoraportal.smartble.screen.CodeScreen
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
            screen = Screen.Code,
            content = { CodeScreen() },
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
    // Create a composable destination for the provided screen route.
    composable(screen.route) {
        // Wrap the screen content inside a LateralMenu which provides a consistent top bar and navigation drawer.
        LateralMenu(
            menuItems = MenuItemsProvider.menuItems, // List of menu items to display in the navigation drawer.
            topBarTitle = stringResource(id = R.string.app_name), // Top bar title set to the app name from resources.
            topBarImageRes = null, // Optional logo image resource; set to null if not used.
            // Enable swipe gestures based on the current screen.
            // For example, gestures can be disabled on the Home screen.
            enableGestures = screen != Screen.Home,
            // Handle menu item click events.
            onMenuItemClick = { menuItem ->
                // Check if the selected menu item's route is different from the current destination.
                // This prevents re-navigating to the same screen and causing unnecessary UI refresh.
                if (navController.currentBackStackEntry?.destination?.route != menuItem.route) {
                    // Navigate to the selected route.
                    // The navigation configuration includes:
                    //   - popUpTo: Navigate back to the Home screen before opening the new destination.
                    //   - launchSingleTop: Avoid creating multiple copies of the same destination.
                    navController.navigate(menuItem.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                }
                // If the current route matches the selected menu item, do not navigate.
                // The LateralMenu will automatically handle closing the drawer.
            }
        ) { paddingValues: PaddingValues ->
            // Apply the padding provided by the Scaffold inside LateralMenu.
            // This ensures that the main content is correctly offset, taking into account the drawer and top bar.
            Surface(modifier = Modifier.padding(paddingValues)) {
                // Render the main composable content for the current screen.
                content()
            }
        }
    }
}
