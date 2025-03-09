package com.alexismoraportal.smartble.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.components.AppButton
import com.alexismoraportal.smartble.ui.theme.BackgroundDark
import com.alexismoraportal.smartble.ui.theme.TextPrimaryDark

/**
 * AboutScreen displays personal and professional information about the developer.
 * It also provides buttons to open the GitHub repository and portfolio website.
 *
 * The screen uses string resources for all texts to support internationalization.
 */
@Composable
fun AboutScreen() {
    // Obtain the current context required for starting new activities
    val context = LocalContext.current

    // Main container with vertical scrolling enabled.
    // This ensures that all content is accessible even on small screens.
    Column(
        modifier = Modifier
            .fillMaxSize() // Use full screen height and width.
            .background(BackgroundDark) // Set background color from the theme.
            .verticalScroll(rememberScrollState()) // Enable vertical scrolling.
            .padding(16.dp), // Add padding around the content.
        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally.
    ) {
        // Display the title text using a string resource.
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimaryDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display the personal/professional description using a string resource.
        Text(
            text = stringResource(R.string.about_description),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimaryDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Button to open the GitHub repository.
        AppButton(
            text = stringResource(R.string.open_github), // Get localized text.
            onClick = {
                // Create an intent to open the GitHub repository URL.
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/AlexisZhuber")
                )
                // Start the activity using the context.
                context.startActivity(intent)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Button to open the portfolio website.
        AppButton(
            text = stringResource(R.string.open_portfolio), // Get localized text.
            onClick = {
                // Create an intent to open the portfolio URL.
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://alexismoraportal.com")
                )
                // Start the activity using the context.
                context.startActivity(intent)
            }
        )
    }
}
