package com.example.macros

import com.example.macros.domain.Meta
import com.example.macros.domain.Sexo
import com.example.macros.domain.calcularObjetivos
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class ObjetivosTest {
    @Test
    fun ejemploDelClaudeMd() {
        // 82 kg, 2300 de mantenimiento, Perder grasa, Hombre → 2000 kcal, 172 C, 180 P, 66 G
        val o = calcularObjetivos(2300, Meta.PerderGrasa, Sexo.Hombre, 82.0)
        assertEquals(2000, o.kcal.roundToInt())
        assertEquals(172, o.carbohidratos.roundToInt())
        assertEquals(180, o.proteinas.roundToInt())
        assertEquals(66, o.grasas.roundToInt())
    }

    @Test
    fun ganarMasaMujer() {
        val o = calcularObjetivos(2000, Meta.GanarMasa, Sexo.Mujer, 60.0)
        assertEquals(2300.0, o.kcal, 0.001)
        assertEquals(108.0, o.proteinas, 0.001)
        assertEquals(72.0, o.grasas, 0.001)
        assertEquals((2300 - 4 * 108.0 - 9 * 72.0) / 4, o.carbohidratos, 0.001)
    }

    @Test
    fun carbohidratosNuncaNegativos() {
        val o = calcularObjetivos(1200, Meta.PerderGrasa, Sexo.Hombre, 120.0)
        assertEquals(0.0, o.carbohidratos, 0.0)
    }
}
