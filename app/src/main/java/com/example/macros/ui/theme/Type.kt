package com.example.macros.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.macros.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

// Sin color fijo: el texto toma el color de su contenedor (onSurface = Texto en todo el tema),
// así funciona también sobre fondos oscuros como el snackbar.
private val base = TextStyle(fontFamily = Inter)

val MacrosTypography = Typography(
    // Títulos de pantalla ("Hoy", "Alimentos"...)
    displaySmall = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    // Números grandes (kcal restantes, peso)
    headlineMedium = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    // Títulos de tarjeta ("Comida 1", "Mis datos")
    titleLarge = base.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = base.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = base.copy(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = base.copy(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = base.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, color = TextoSecundario),
    labelLarge = base.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = base.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 16.sp),
)
