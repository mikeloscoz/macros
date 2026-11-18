package com.example.macros.domain

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

fun calcularObjetivos(mantenimiento: Int, meta: Meta, sexo: Sexo, pesoKg: Double): Objetivos {
    val kcal = (mantenimiento + meta.ajusteKcal).toDouble()
    val proteinas = sexo.proteinaPorKg * pesoKg
    val grasas = sexo.grasaPorKg * pesoKg
    val carbohidratos = max(0.0, (kcal - 4 * proteinas - 9 * grasas) / 4)
    return Objetivos(kcal, carbohidratos, proteinas, grasas)
}
