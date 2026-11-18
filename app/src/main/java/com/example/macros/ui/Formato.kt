package com.example.macros.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

private val simbolos = DecimalFormatSymbols(Locale.forLanguageTag("es-ES"))
private val unDecimalOpcional = DecimalFormat("0.#", simbolos)
private val unDecimal = DecimalFormat("0.0", simbolos)

/** Entero redondeado (kcal y gramos). */
fun Double.entero(): String = roundToInt().toString()

/** Hasta un decimal, sin ",0" sobrante: 8,5 · 2 · 28,2 (valores por 100 g). */
fun Double.decimalCorto(): String = unDecimalOpcional.format(this)

/** Siempre un decimal: 82,0 (peso). */
fun Double.unDecimal(): String = unDecimal.format(this)

/** Para rellenar un campo editable: sin decimales sobrantes y con coma. */
fun Double.paraCampo(): String = DecimalFormat("0.##", simbolos).format(this)
