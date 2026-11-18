package com.example.macros.domain

import java.text.Normalizer

/** Minúsculas y sin tildes, para buscar "atun" y encontrar "Atún". */
fun normalizar(texto: String): String =
    Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

/** Lee un número escrito con coma o punto decimal. Null si no es válido o es negativo. */
fun leerDecimal(texto: String): Double? =
    texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 && it.isFinite() }
