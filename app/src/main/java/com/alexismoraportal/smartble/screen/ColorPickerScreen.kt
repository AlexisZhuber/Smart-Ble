package com.alexismoraportal.smartble.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alexismoraportal.smartble.components.ColorPicker

@Composable
fun ColorPickerScreen(){
    Box(modifier = Modifier
        .fillMaxSize() // Hace que el Box ocupe toda la pantalla
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center) // Centra la Column dentro del Box
                .padding(16.dp),
            verticalArrangement = Arrangement.Center, // Centra verticalmente el contenido de la Column
            horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente el contenido de la Column
        ) {
            ColorPicker()
        }
    }
}