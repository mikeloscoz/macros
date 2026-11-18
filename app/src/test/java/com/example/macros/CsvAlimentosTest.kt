package com.example.macros

import com.example.macros.domain.leerCsvAlimentos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CsvAlimentosTest {
    @Test
    fun leeComaDecimalComillasYColumnasDeMas() {
        val csv = """
            Alimento,Kcal /100 g,Carbohidratos,Proteínas,Grasas,,"Nota, con coma"
            Aguacate,160,"8,5",2,"14,7",,
            ,,,,,,
            Aceite,884,0,0,100,,
        """.trimIndent()
        val lista = leerCsvAlimentos(csv)
        assertEquals(2, lista.size)
        val aguacate = lista.first { it.nombre == "Aguacate" }
        assertEquals(160.0, aguacate.kcal, 0.0)
        assertEquals(8.5, aguacate.carbohidratos, 0.0)
        assertEquals(2.0, aguacate.proteinas, 0.0)
        assertEquals(14.7, aguacate.grasas, 0.0)
    }

    @Test
    fun aceptaPuntoYComa() {
        val lista = leerCsvAlimentos("Pan;250;49,5;9;3,2\r\nQueso;400;1;25;33")
        assertEquals(listOf("Pan", "Queso"), lista.map { it.nombre })
        assertEquals(49.5, lista[0].carbohidratos, 0.0)
    }

    @Test
    fun archivoRealDeAssets() {
        val texto = File("src/main/assets/alimentos.csv").readText()
        val lista = leerCsvAlimentos(texto)
        assertEquals(81, lista.size)
        assertTrue(lista.any { it.nombre == "Atún (lata 55 g)" && it.carbohidratos == 0.5 })
    }
}
