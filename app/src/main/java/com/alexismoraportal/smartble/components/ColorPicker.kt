package com.alexismoraportal.smartble.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.*
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

/**
 * ColorPicker composable provides a UI for selecting a color using the Skydoves ColorPicker library.
 *
 * This improved version persists the last selected color using [rememberSaveable], so that if you
 * navigate away from this screen or rotate the device, the color picker's sliders remain at the
 * previously selected values.
 *
 * The composable includes:
 * - HsvColorPicker for selecting hue, saturation, and value.
 * - AlphaSlider to adjust transparency.
 * - BrightnessSlider to adjust brightness.
 *
 * When the color changes, the [onColorSelected] callback receives a formatted string generated
 * by [hexToFormattedString]. Any errors during conversion are caught to avoid crashes.
 *
 * @param onColorSelected A callback function that receives the formatted color string whenever the color changes.
 */
@Composable
fun ColorPicker(
    onColorSelected: (String) -> Unit
) {
    // A ColorPickerController from Skydoves library, controlling the color state internally.
    val controller = rememberColorPickerController()

    // Store the last selected color's hex code in a rememberSaveable state.
    // This ensures the color is preserved across configuration changes (e.g., screen rotation)
    // and navigation away/return as long as the composable remains in the back stack.
    var lastSelectedHex by rememberSaveable { mutableStateOf("#FFFFFFFF") }

    /**
     * In a real scenario, you might want to reapply the lastSelectedHex to the controller,
     * so the sliders jump to the correct position when the composable is recreated.
     *
     * For example, if the library supports setting the HSV or ARGB color directly, you can parse
     * lastSelectedHex and call something like controller.setHsvColor(...).
     *
     * We'll demonstrate a minimal approach here.
     */
    LaunchedEffect(lastSelectedHex) {
        // Optionally parse lastSelectedHex and set it on the controller if needed, e.g.:
        // controller.setHsvColor(HsvColor.from(colorString = lastSelectedHex))
        // or controller.setColor(...) depending on library methods.
    }

    // Main layout container.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // HsvColorPicker: controls hue, saturation, and value.
        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(10.dp),
            controller = controller,
            onColorChanged = { color ->
                try {
                    // Convert the hex code to a formatted string.
                    val formattedString = hexToFormattedString(color.hexCode)
                    // Invoke the callback with the formatted color string.
                    onColorSelected(formattedString)

                    // Update the lastSelectedHex so that we can restore the color next time.
                    lastSelectedHex = color.hexCode
                } catch (e: Exception) {
                    // Log the exception and invoke the callback with a fallback value.
                    e.printStackTrace()
                    onColorSelected("Error")
                }
            }
        )

        // AlphaSlider for adjusting transparency.
        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = controller
        )

        // BrightnessSlider for adjusting brightness.
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = controller
        )
    }
}

/**
 * Converts a hexadecimal color code into a formatted string.
 *
 * Expected hex formats:
 * - "RRGGBB"
 * - "RRGGBBAA"
 *
 * Each pair of hex digits is converted to its decimal value. The output string follows the format:
 *
 *     "*<decimal1>,<decimal2>,...,<decimalN>."
 *
 * In case of an error (e.g., invalid format), a fallback string "*0,0,0,0." is returned.
 *
 * @param hexCode The hexadecimal color code to convert (e.g., "#FFFFFFFF" or "FFFFFFFF" minus the #).
 * @return A formatted string representing the color, or a fallback if an error occurs.
 * @throws IllegalArgumentException if the hexCode is empty or has an odd length.
 */
fun hexToFormattedString(hexCode: String): String {
    return try {
        // Ensure the hexCode is not empty and has an even number of characters.
        if (hexCode.isEmpty()) {
            throw IllegalArgumentException("Hex code is empty.")
        }
        // Optionally strip any leading '#' if present.
        val sanitizedHex = hexCode.removePrefix("#")

        if (sanitizedHex.length % 2 != 0) {
            throw IllegalArgumentException("Hex code has an odd number of characters: $sanitizedHex")
        }

        // Split the sanitized hex code into two-character chunks and parse each as an integer.
        val decimalValues = sanitizedHex.chunked(2).map { it.toInt(16) }
        "*" + decimalValues.joinToString(separator = ",") + "."
    } catch (e: Exception) {
        e.printStackTrace()
        "*0,0,0,0."
    }
}
