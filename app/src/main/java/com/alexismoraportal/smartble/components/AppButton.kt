package com.alexismoraportal.smartble.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.ui.theme.Primary
import com.alexismoraportal.smartble.ui.theme.TextPrimaryDark

/**
 * ProfessionalButton is a reusable, customizable button component designed to ensure consistent styling
 * across the application and improve maintainability.
 *
 * @param text The text to be displayed inside the button.
 * @param onClick Callback function to be invoked when the button is clicked.
 * @param modifier Modifier for styling and layout adjustments.
 * @param enabled Boolean flag to enable or disable the button.
 * @param icon (Optional) An [ImageVector] icon to be displayed alongside the text.
 *             If provided, the icon is displayed to the left of the text.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = TextPrimaryDark
        )
    ) {
        if (icon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display the icon.
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null, // Decorative element
                    tint = TextPrimaryDark,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
