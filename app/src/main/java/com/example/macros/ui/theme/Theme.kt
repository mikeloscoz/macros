package com.example.macros.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val Esquemas = lightColorScheme(
    primary = Primario,
    onPrimary = Tarjeta,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = Primario,
    secondary = TextoSecundario,
    onSecondary = Tarjeta,
    background = Fondo,
    onBackground = Texto,
    surface = Tarjeta,
    onSurface = Texto,
    surfaceVariant = VerdeClaro,
    onSurfaceVariant = TextoSecundario,
    surfaceContainer = Tarjeta,
    surfaceContainerLow = Tarjeta,
    surfaceContainerHigh = Tarjeta,
    surfaceContainerHighest = Tarjeta,
    outline = Borde,
    outlineVariant = Borde,
    error = Rojo,
    onError = Tarjeta,
)

/** Esquinas de las tarjetas según el diseño. */
val FormaTarjeta = RoundedCornerShape(20.dp)

@Composable
fun MacrosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Esquemas,
        typography = MacrosTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = FormaTarjeta,
        ),
        content = content,
    )
}
