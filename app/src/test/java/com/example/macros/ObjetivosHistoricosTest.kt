package com.example.macros

import com.example.macros.data.db.AjustesDesde
import com.example.macros.data.db.Peso
import com.example.macros.domain.Meta
import com.example.macros.domain.Sexo
import com.example.macros.domain.objetivosEn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import kotlin.math.roundToInt

class ObjetivosHistoricosTest {
    private fun d(dia: Int, mes: Int = 11) = LocalDate.of(2026, mes, dia)

    private val historial = listOf(
        AjustesDesde(LocalDate.of(2000, 1, 1), 2300, Meta.PerderGrasa, Sexo.Hombre),
        AjustesDesde(d(20), 2200, Meta.PerderGrasa, Sexo.Hombre),
    )
    private val pesos = listOf(Peso(d(1), 85.0), Peso(d(15), 82.0))

    @Test
    fun cadaDiaUsaLosAjustesYElPesoDeEntonces() {
        val dia10 = objetivosEn(d(10), historial, pesos)!!
        assertEquals(2000, dia10.objetivos.kcal.roundToInt())
        assertEquals(187, dia10.objetivos.proteinas.roundToInt()) // 2,2 × 85

        val dia18 = objetivosEn(d(18), historial, pesos)!!
        assertEquals(2000, dia18.objetivos.kcal.roundToInt())
        assertEquals(180, dia18.objetivos.proteinas.roundToInt()) // 2,2 × 82

        val dia20 = objetivosEn(d(20), historial, pesos)!!
        assertEquals(1900, dia20.objetivos.kcal.roundToInt()) // el cambio cuenta desde ese mismo día
    }

    @Test
    fun antesDelPrimerPesoUsaElPrimero() {
        val o = objetivosEn(LocalDate.of(2026, 10, 1), historial, pesos)!!
        assertEquals(187, o.objetivos.proteinas.roundToInt())
    }

    @Test
    fun sinDatosNoHayObjetivos() {
        assertNull(objetivosEn(d(10), emptyList(), pesos))
        assertNull(objetivosEn(d(10), historial, emptyList()))
    }
}
