package com.example.macros.domain

import com.example.macros.data.db.Peso
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class Periodo(val texto: String) { Semana("Semana"), Mes("Mes"), Anio("Año") }

data class Rango(val desde: LocalDate, val hasta: LocalDate) {
    operator fun contains(fecha: LocalDate) = !fecha.isBefore(desde) && !fecha.isAfter(hasta)
}

/** Semana de lunes a domingo, mes natural o año natural que contiene [fecha]. */
fun Periodo.rango(fecha: LocalDate): Rango = when (this) {
    Periodo.Semana -> {
        val lunes = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        Rango(lunes, lunes.plusDays(6))
    }
    Periodo.Mes -> Rango(fecha.withDayOfMonth(1), fecha.with(TemporalAdjusters.lastDayOfMonth()))
    Periodo.Anio -> Rango(fecha.withDayOfYear(1), fecha.with(TemporalAdjusters.lastDayOfYear()))
}

/** Una fecha del periodo anterior (-1) o siguiente (+1). */
fun Periodo.desplazar(fecha: LocalDate, pasos: Long): LocalDate = when (this) {
    Periodo.Semana -> fecha.plusWeeks(pasos)
    Periodo.Mes -> fecha.plusMonths(pasos)
    Periodo.Anio -> fecha.plusYears(pasos)
}

/**
 * Lo comido en un día que tiene algo apuntado (y que cuenta: los días excluidos no se pasan).
 * [objetivoKcal] es el objetivo que tenía ese día.
 */
data class DiaComido(val fecha: LocalDate, val kcal: Double, val proteinas: Double, val objetivoKcal: Double? = null)

/**
 * Una barra de la gráfica de kcal: media de los días apuntados de su tramo (null si ninguno)
 * y media de los objetivos de esos mismos días, para colorearla.
 */
data class Barra(val etiqueta: String, val kcal: Double?, val objetivo: Double? = null)

data class ResumenProgreso(
    val rango: Rango,
    val barras: List<Barra>,
    /** Media de kcal de los días apuntados del periodo. */
    val mediaKcal: Double?,
    /** Media de los objetivos de esos días (pueden haber cambiado a lo largo del periodo). */
    val objetivoMedio: Double?,
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
fun calcularProgreso(periodo: Periodo, referencia: LocalDate, dias: List<DiaComido>, pesos: List<Peso>): ResumenProgreso {
    val rango = periodo.rango(referencia)
    val enRango = dias.filter { it.fecha in rango }
    fun media(lista: List<DiaComido>) = if (lista.isEmpty()) null else lista.sumOf { it.kcal } / lista.size
    fun mediaObjetivo(lista: List<DiaComido>): Double? {
        val conObjetivo = lista.mapNotNull { it.objetivoKcal }
        return if (conObjetivo.isEmpty()) null else conObjetivo.average()
    }
    fun barra(etiqueta: String, lista: List<DiaComido>) = Barra(etiqueta, media(lista), mediaObjetivo(lista))

    val barras = when (periodo) {
        Periodo.Semana -> (0..6).map { i ->
            val fecha = rango.desde.plusDays(i.toLong())
            barra(letrasDia[i], enRango.filter { it.fecha == fecha })
        }
        Periodo.Mes -> (0..3).map { i ->
            val primerDia = 1 + i * 7
            val ultimoDia = if (i == 3) rango.hasta.dayOfMonth else primerDia + 6
            barra("Sem ${i + 1}", enRango.filter { it.fecha.dayOfMonth in primerDia..ultimoDia })
        }
        Periodo.Anio -> (1..12).map { mes ->
            barra(letrasMes[mes - 1], enRango.filter { it.fecha.monthValue == mes })
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
        objetivoMedio = mediaObjetivo(enRango),
        diasRegistrados = enRango.size,
        proteinaMedia = if (enRango.isEmpty()) null else enRango.sumOf { it.proteinas } / enRango.size,
        pesos = pesosRango,
        kgPorSemana = kgPorSemana,
    )
}

private operator fun Rango.contains(fecha: LocalDate) = !fecha.isBefore(desde) && !fecha.isAfter(hasta)
