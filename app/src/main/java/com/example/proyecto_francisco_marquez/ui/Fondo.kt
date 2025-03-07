package com.example.proyecto_francisco_marquez.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Modifier.gradientBackground(): Modifier {
    return this.background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF00E676), // Verde portal de Rick
                Color(0xFF1B1E38), // Azul espacio cósmico
                Color(0xFF121212)  // Fondo oscuro interdimensional
            )
        )
    )
}

// Definir una paleta de colores basada en Rick and Morty
val PrimaryColor = Color(0xFF00E676) // Verde portal de Rick
val SecondaryColor = Color(0xFF1B1E38) // Azul cósmico
val BackgroundColor = Color(0xFF121212) // Oscuro interdimensional
val TextColor = Color(0xFFB5FF59) // Verde fluorescente
val WhiteTextColor = Color.White

// Definir estilos de texto modernos
val TitleStyle = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = FontFamily.SansSerif,
    color = TextColor
)

val SubtitleStyle = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = FontFamily.SansSerif,
    color = WhiteTextColor
)

// Estilo de botones
fun Modifier.buttonModifier() = this.background(PrimaryColor).padding(12.dp)
