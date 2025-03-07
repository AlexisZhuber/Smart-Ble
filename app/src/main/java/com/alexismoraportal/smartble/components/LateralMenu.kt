package com.alexismoraportal.smartble.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.model.MenuItem
import kotlinx.coroutines.launch

/**
 * LateralMenu displays a navigation drawer along with a top bar.
 *
 * @param menuItems The list of menu items to display in the drawer.
 * @param onMenuItemClick Callback invoked when a menu item is clicked.
 * @param topBarTitle Optional title for the top bar. Defaults to the app name if not provided.
 * @param topBarImageRes Optional image resource to display as a logo in the top bar.
 * @param enableGestures Determines whether swipe gestures are enabled for opening/closing the drawer.
 * @param content Composable content displayed in the main area of the screen.
 */
@Composable
fun LateralMenu(
    menuItems: List<MenuItem> = emptyList(),
    onMenuItemClick: (MenuItem) -> Unit = {},
    topBarTitle: String? = null,
    topBarImageRes: Int? = null,
    enableGestures: Boolean = true, // Change this parameter when needed
    content: @Composable (PaddingValues) -> Unit
) {
    // Create a drawer state to manage the drawer's open/closed status.
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    // Create a coroutine scope to handle drawer animations.
    val scope = rememberCoroutineScope()
    // Use the provided top bar title or fall back to the app name from resources.
    val finalTitle = topBarTitle ?: stringResource(R.string.app_name)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enableGestures, // Pass the parameter here
        drawerContent = {
            // Display the drawer content without a logout option.
            DrawerContent(
                menuItems = menuItems,
                onMenuItemClick = { item ->
                    // Close the drawer before handling the menu item click.
                    scope.launch { drawerState.close() }
                    onMenuItemClick(item)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    title = finalTitle,
                    showMenuButton = true,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    logoResId = topBarImageRes
                )
            }
        ) { paddingValues ->
            // Apply the padding values provided by the Scaffold to the main content.
            content(paddingValues)
        }
    }
}
