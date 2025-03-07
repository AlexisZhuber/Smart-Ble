package com.alexismoraportal.smartble.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.ui.theme.BackgroundDark
import com.alexismoraportal.smartble.ui.theme.BackgroundLight
import com.alexismoraportal.smartble.ui.theme.TextPrimaryDark

/**
 * TopBar displays an app bar with an optional back button or menu button.
 *
 * @param title The title to display.
 * @param modifier Optional modifier for styling.
 * @param showBackButton Whether to show a back button.
 * @param onBackClick Callback when the back button is clicked.
 * @param showMenuButton Whether to show a menu button.
 * @param onMenuClick Callback when the menu button is clicked.
 * @param actions Optional additional actions.
 * @param logoResId Optional logo image resource.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    showMenuButton: Boolean = false,
    onMenuClick: () -> Unit = {},
    actions: @Composable (() -> Unit)? = null,
    logoResId: Int? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        navigationIcon = {
            when {
                showBackButton -> {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Button",
                            tint = BackgroundLight
                        )
                    }
                }
                showMenuButton -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Button",
                            tint = BackgroundLight
                        )
                    }
                }
            }
        },
        actions = {
            // If a logo resource is provided, display it before any additional actions.
            logoResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
            }
            actions?.invoke()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundDark,
            titleContentColor = TextPrimaryDark,
            navigationIconContentColor = BackgroundLight,
            actionIconContentColor = BackgroundLight
        ),
        modifier = modifier
    )
}
