package com.example.macros.domain

import com.example.macros.data.db.Alimento

/**
 * Lee alimentos de un CSV con columnas: Alimento, Kcal, Carbohidratos, Proteínas, Grasas.
 * - Acepta coma decimal ("8,5") y campos entre comillas.
 * - Ignora columnas de más, filas vacías y filas cuyos números no se puedan leer (como la cabecera).
 * - Si un nombre se repite, se queda el último.
 */
fun leerCsvAlimentos(texto: String): List<Alimento> {
    val resultado = LinkedHashMap<String, Alimento>()
    texto.removePrefix("﻿").lineSequence().forEach { linea ->
        if (linea.isBlank()) return@forEach
        val campos = separarCampos(linea, detectarSeparador(linea))
        if (campos.size < 5) return@forEach
        val nombre = campos[0].trim()
        if (nombre.isEmpty()) return@forEach
        val numeros = campos.subList(1, 5).map { leerNumero(it) ?: return@forEach }
        if (numeros.any { it < 0 }) return@forEach
        resultado[nombre.lowercase()] = Alimento(
            nombre = nombre,
            kcal = numeros[0],
            carbohidratos = numeros[1],
            proteinas = numeros[2],
            grasas = numeros[3],
        )
    }
    return resultado.values.toList()
}

/** Algunos CSV exportados en español usan ';' para poder llevar coma decimal sin comillas. */
private fun detectarSeparador(linea: String): Char =
    if (linea.count { it == ';' } >= 4) ';' else ','

private fun leerNumero(texto: String): Double? {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return null
    return limpio.replace(',', '.').toDoubleOrNull()
}

/** Separa una línea de CSV respetando comillas ("" dentro de comillas es una comilla). */
private fun separarCampos(linea: String, separador: Char): List<String> {
    val campos = mutableListOf<String>()
    val actual = StringBuilder()
    var entreComillas = false
    var i = 0
    while (i < linea.length) {
        val c = linea[i]
        when {
            c == '"' && entreComillas && i + 1 < linea.length && linea[i + 1] == '"' -> {
                actual.append('"'); i++
            }
            c == '"' -> entreComillas = !entreComillas
            c == separador && !entreComillas -> {
                campos += actual.toString(); actual.clear()
            }
            c == '\r' -> Unit
            else -> actual.append(c)
        }
        i++
    }
    campos += actual.toString()
    return campos
}
