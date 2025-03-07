package com.alexismoraportal.smartble.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.model.MenuItem

/**
 * DrawerContent displays a navigation drawer containing a title and a list of menu items.
 *
 * @param menuItems The list of [MenuItem] objects to be displayed in the drawer.
 * @param currentRoute The current navigation route, used to highlight the selected menu item.
 * @param onMenuItemClick A callback function that is triggered when a menu item is clicked.
 */
@Composable
fun DrawerContent(
    menuItems: List<MenuItem>,
    currentRoute: String? = null,
    onMenuItemClick: (MenuItem) -> Unit
) {
    // ModalDrawerSheet is a container used for displaying the drawer content.
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            // Drawer title
            Text(
                text = stringResource(R.string.menu_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))

            // List of menu items
            menuItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = item.title), style = MaterialTheme.typography.bodyLarge) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(id = item.title)
                        )
                    },
                    selected = currentRoute == item.id,
                    onClick = { onMenuItemClick(item) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}
