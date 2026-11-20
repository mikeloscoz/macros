package com.example.macros.domain

import com.example.macros.data.db.AjustesDesde
import com.example.macros.data.db.Peso
import java.time.LocalDate
import kotlin.math.max

enum class Meta(val texto: String, val ajusteKcal: Int) {
    PerderGrasa("Perder grasa", -300),
    Mantener("Mantener", 0),
    GanarMasa("Ganar masa", 300),
}

enum class Sexo(val texto: String, val proteinaPorKg: Double, val grasaPorKg: Double) {
    Hombre("Hombre", 2.2, 0.8),
    Mujer("Mujer", 1.8, 1.2),
}

/** Objetivos diarios. Sin redondear: se redondean solo al mostrarlos. */
data class Objetivos(
    val kcal: Double,
    val carbohidratos: Double,
    val proteinas: Double,
    val grasas: Double,
)

/** Objetivos de un día concreto y el objetivo (meta) que estaba elegido ese día. */
data class ObjetivosDia(val objetivos: Objetivos, val meta: Meta)

/**
 * Objetivos de [fecha] con los ajustes vigentes ese día y el último peso registrado hasta ese día.
 * Si la fecha es anterior al primer registro, se usa el primero. Null si no hay ajustes o pesos.
 * [historial] y [pesos] pueden venir en cualquier orden.
 */
fun objetivosEn(fecha: LocalDate, historial: List<AjustesDesde>, pesos: List<Peso>): ObjetivosDia? {
    val ajustes = historial.filter { !it.fecha.isAfter(fecha) }.maxByOrNull { it.fecha }
        ?: historial.minByOrNull { it.fecha } ?: return null
    val peso = pesos.filter { !it.fecha.isAfter(fecha) }.maxByOrNull { it.fecha }
        ?: pesos.minByOrNull { it.fecha } ?: return null
    return ObjetivosDia(
        calcularObjetivos(ajustes.mantenimiento, ajustes.meta, ajustes.sexo, peso.kg),
        ajustes.meta,
    )
}

fun calcularObjetivos(mantenimiento: Int, meta: Meta, sexo: Sexo, pesoKg: Double): Objetivos {
    val kcal = (mantenimiento + meta.ajusteKcal).toDouble()
    val proteinas = sexo.proteinaPorKg * pesoKg
    val grasas = sexo.grasaPorKg * pesoKg
    val carbohidratos = max(0.0, (kcal - 4 * proteinas - 9 * grasas) / 4)
    return Objetivos(kcal, carbohidratos, proteinas, grasas)
}
