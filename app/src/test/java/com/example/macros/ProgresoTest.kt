package com.example.macros

import com.example.macros.data.db.Peso
import com.example.macros.domain.DiaComido
import com.example.macros.domain.Periodo
import com.example.macros.domain.calcularProgreso
import com.example.macros.domain.desplazar
import com.example.macros.domain.rango
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ProgresoTest {
    private val hoy = LocalDate.of(2026, 11, 18) // miércoles

    private fun dia(d: Int, kcal: Double, prot: Double = 100.0) = DiaComido(LocalDate.of(2026, 11, d), kcal, prot)

    @Test
    fun rangos() {
        assertEquals(LocalDate.of(2026, 11, 16), Periodo.Semana.rango(hoy).desde)
        assertEquals(LocalDate.of(2026, 11, 22), Periodo.Semana.rango(hoy).hasta)
        assertEquals(LocalDate.of(2026, 11, 30), Periodo.Mes.rango(hoy).hasta)
        assertEquals(LocalDate.of(2026, 12, 31), Periodo.Anio.rango(hoy).hasta)
    }

    @Test
    fun mediasSoloConDiasApuntados() {
        // Días 1 y 3 apuntados; el 2 no cuenta para la media.
        val r = calcularProgreso(Periodo.Mes, hoy, listOf(dia(1, 2000.0, 150.0), dia(3, 2200.0, 130.0)), emptyList())
        assertEquals(2, r.diasRegistrados)
        assertEquals(2100.0, r.mediaKcal!!, 0.001)
        assertEquals(140.0, r.proteinaMedia!!, 0.001)
    }

    @Test
    fun barrasDelMes() {
        // Noviembre tiene 30 días: Sem 4 = días 22 a 30.
        val r = calcularProgreso(
            Periodo.Mes, hoy,
            listOf(dia(1, 1800.0), dia(7, 2200.0), dia(8, 2100.0), dia(29, 1900.0)),
            emptyList(),
        )
        assertEquals(listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4"), r.barras.map { it.etiqueta })
        assertEquals(2000.0, r.barras[0].kcal!!, 0.001)
        assertEquals(2100.0, r.barras[1].kcal!!, 0.001)
        assertNull(r.barras[2].kcal)
        assertEquals(1900.0, r.barras[3].kcal!!, 0.001)
    }

    @Test
    fun ignoraDiasFueraDelPeriodo() {
        val fuera = DiaComido(LocalDate.of(2026, 10, 31), 5000.0, 0.0)
        val r = calcularProgreso(Periodo.Semana, hoy, listOf(fuera, dia(16, 2000.0)), emptyList())
        assertEquals(1, r.diasRegistrados)
        assertEquals(2000.0, r.barras[0].kcal!!, 0.001) // lunes 16
    }

    @Test
    fun objetivoDeCadaDia() {
        // El objetivo cambió a mitad de semana: la barra compara con la media de los objetivos de sus días.
        val dias = listOf(
            DiaComido(LocalDate.of(2026, 11, 2), 2000.0, 100.0, objetivoKcal = 2000.0),
            DiaComido(LocalDate.of(2026, 11, 3), 2000.0, 100.0, objetivoKcal = 1800.0),
        )
        val r = calcularProgreso(Periodo.Mes, hoy, dias, emptyList())
        assertEquals(1900.0, r.barras[0].objetivo!!, 0.001)
        assertEquals(1900.0, r.objetivoMedio!!, 0.001)
    }

    @Test
    fun periodoAnterior() {
        val ref = Periodo.Mes.desplazar(hoy, -1)
        assertEquals(LocalDate.of(2026, 10, 1), Periodo.Mes.rango(ref).desde)
    }

    @Test
    fun kgPorSemana() {
        val pesos = listOf(
            Peso(LocalDate.of(2026, 11, 1), 85.0),
            Peso(LocalDate.of(2026, 11, 15), 83.0),
        )
        val r = calcularProgreso(Periodo.Mes, hoy, emptyList(), pesos)
        assertEquals(-1.0, r.kgPorSemana!!, 0.001)
        assertNull(calcularProgreso(Periodo.Mes, hoy, emptyList(), pesos.take(1)).kgPorSemana)
    }
}
