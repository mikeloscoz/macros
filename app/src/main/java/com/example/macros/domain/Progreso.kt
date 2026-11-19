package com.example.macros.domain

import com.example.macros.data.db.Peso
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class Periodo(val texto: String) { Semana("Semana"), Mes("Mes"), Anio("Año") }

data class Rango(val desde: LocalDate, val hasta: LocalDate)

/** Semana de lunes a domingo, mes natural o año natural que contiene [hoy]. */
fun Periodo.rango(hoy: LocalDate): Rango = when (this) {
    Periodo.Semana -> {
        val lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        Rango(lunes, lunes.plusDays(6))
    }
    Periodo.Mes -> Rango(hoy.withDayOfMonth(1), hoy.with(TemporalAdjusters.lastDayOfMonth()))
    Periodo.Anio -> Rango(hoy.withDayOfYear(1), hoy.with(TemporalAdjusters.lastDayOfYear()))
}

/** Lo comido en un día que tiene algo apuntado. */
data class DiaComido(val fecha: LocalDate, val kcal: Double, val proteinas: Double)

/** Una barra de la gráfica de kcal: media de los días apuntados de su tramo (null si ninguno). */
data class Barra(val etiqueta: String, val kcal: Double?)

data class ResumenProgreso(
    val rango: Rango,
    val barras: List<Barra>,
    /** Media de kcal de los días apuntados del periodo. */
    val mediaKcal: Double?,
    val diasRegistrados: Int,
    val proteinaMedia: Double?,
    /** Pesos registrados dentro del periodo, por fecha. */
    val pesos: List<Peso>,
    /** Ritmo de cambio de peso en el periodo (negativo = bajando). */
    val kgPorSemana: Double?,
)

private val letrasDia = listOf("L", "M", "X", "J", "V", "S", "D")
private val letrasMes = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

/**
 * Calcula todo lo de la pantalla Progreso. Las medias cuentan solo los días con algo apuntado.
 * Barras: Semana → una por día; Mes → 4 tramos de 7 días (el último se queda los días sobrantes);
 * Año → una por mes.
 */
fun calcularProgreso(periodo: Periodo, hoy: LocalDate, dias: List<DiaComido>, pesos: List<Peso>): ResumenProgreso {
    val rango = periodo.rango(hoy)
    val enRango = dias.filter { it.fecha in rango }
    fun media(lista: List<DiaComido>) = if (lista.isEmpty()) null else lista.sumOf { it.kcal } / lista.size

    val barras = when (periodo) {
        Periodo.Semana -> (0..6).map { i ->
            val fecha = rango.desde.plusDays(i.toLong())
            Barra(letrasDia[i], media(enRango.filter { it.fecha == fecha }))
        }
        Periodo.Mes -> (0..3).map { i ->
            val primerDia = 1 + i * 7
            val ultimoDia = if (i == 3) rango.hasta.dayOfMonth else primerDia + 6
            Barra("Sem ${i + 1}", media(enRango.filter { it.fecha.dayOfMonth in primerDia..ultimoDia }))
        }
        Periodo.Anio -> (1..12).map { mes ->
            Barra(letrasMes[mes - 1], media(enRango.filter { it.fecha.monthValue == mes }))
        }
    }

    val pesosRango = pesos.filter { it.fecha in rango }.sortedBy { it.fecha }
    val kgPorSemana = if (pesosRango.size >= 2) {
        val primero = pesosRango.first()
        val ultimo = pesosRango.last()
        val dias = ChronoUnit.DAYS.between(primero.fecha, ultimo.fecha)
        (ultimo.kg - primero.kg) / dias * 7
    } else null

    return ResumenProgreso(
        rango = rango,
        barras = barras,
        mediaKcal = media(enRango),
        diasRegistrados = enRango.size,
        proteinaMedia = if (enRango.isEmpty()) null else enRango.sumOf { it.proteinas } / enRango.size,
        pesos = pesosRango,
        kgPorSemana = kgPorSemana,
    )
}

private operator fun Rango.contains(fecha: LocalDate) = !fecha.isBefore(desde) && !fecha.isAfter(hasta)
