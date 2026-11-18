package com.example.macros.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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

val Espanol: Locale = Locale.forLanguageTag("es-ES")

fun String.primeraMayuscula(): String = replaceFirstChar { it.titlecase(Espanol) }

/** "Miércoles, 18 de noviembre" (con el año si no es el actual). */
fun LocalDate.fechaLarga(hoy: LocalDate = LocalDate.now()): String {
    val patron = if (year == hoy.year) "EEEE, d 'de' MMMM" else "EEEE, d 'de' MMMM 'de' yyyy"
    return format(DateTimeFormatter.ofPattern(patron, Espanol)).primeraMayuscula()
}

/** Título del día: "Hoy", "Ayer", el día de la semana si es de esta última semana, o "3 de noviembre". */
fun LocalDate.tituloDia(hoy: LocalDate): String {
    val dias = ChronoUnit.DAYS.between(this, hoy)
    return when {
        dias == 0L -> "Hoy"
        dias == 1L -> "Ayer"
        dias in 2..6 -> format(DateTimeFormatter.ofPattern("EEEE", Espanol)).primeraMayuscula()
        else -> format(DateTimeFormatter.ofPattern("d 'de' MMMM", Espanol))
    }
}
