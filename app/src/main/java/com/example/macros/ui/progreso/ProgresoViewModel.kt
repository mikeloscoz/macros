package com.example.macros.ui.progreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macros.data.AppContainer
import com.example.macros.data.db.Peso
import com.example.macros.domain.DiaComido
import com.example.macros.domain.Periodo
import com.example.macros.domain.ResumenProgreso
import com.example.macros.domain.calcularProgreso
import com.example.macros.domain.desplazar
import com.example.macros.domain.objetivosEn
import com.example.macros.domain.rango
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProgresoUiState(
    val cargando: Boolean = true,
    val periodo: Periodo = Periodo.Mes,
    val hoy: LocalDate = LocalDate.now(),
    val resumen: ResumenProgreso? = null,
    /** ¿Se está viendo el periodo actual? (si no, se puede avanzar). */
    val esActual: Boolean = true,
    /** Último peso registrado y el primero de todos (para "Desde X kg al empezar"). */
    val pesoActual: Peso? = null,
    val pesoInicial: Peso? = null,
    /** Todos los pesos, del más reciente al más antiguo (historial editable). */
    val historialPesos: List<Peso> = emptyList(),
    /** Objetivo de kcal a mostrar: la media de los objetivos de los días apuntados, o el del final del periodo. */
    val objetivoKcal: Double? = null,
)

/** Qué periodo se está viendo: tipo y una fecha cualquiera dentro de él. */
private data class Vista(val periodo: Periodo, val referencia: LocalDate)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgresoViewModel(private val c: AppContainer) : ViewModel() {
    private val _hoy = MutableStateFlow(LocalDate.now())
    private val _vista = MutableStateFlow(Vista(Periodo.Mes, LocalDate.now()))

    private val _periodo = MutableStateFlow(Periodo.Mes)
    val periodo: StateFlow<Periodo> = _periodo.asStateFlow()

    val estado: StateFlow<ProgresoUiState> = combine(_vista, _hoy) { v, hoy -> v to hoy }.flatMapLatest { (vista, hoy) ->
        val rango = vista.periodo.rango(vista.referencia)
        combine(
            c.diario.rango(rango.desde, rango.hasta),
            c.pesos.historial,
            c.ajustes.historial,
            c.diasExcluidos.excluidos,
        ) { entradas, pesos, historial, excluidos ->
            // Los días marcados como "no contar" se tratan como días sin apuntar.
            val dias = entradas
                .filter { it.entrada.fecha !in excluidos }
                .groupBy { it.entrada.fecha }
                .map { (fecha, lista) ->
                    DiaComido(
                        fecha = fecha,
                        kcal = lista.sumOf { it.kcal },
                        proteinas = lista.sumOf { it.proteinas },
                        objetivoKcal = objetivosEn(fecha, historial, pesos)?.objetivos?.kcal,
                    )
                }
            val resumen = calcularProgreso(vista.periodo, vista.referencia, dias, pesos)
            val finVisible = minOf(rango.hasta, hoy)
            ProgresoUiState(
                cargando = false,
                periodo = vista.periodo,
                hoy = hoy,
                resumen = resumen,
                esActual = hoy in rango,
                pesoActual = pesos.lastOrNull(),
                pesoInicial = pesos.firstOrNull(),
                historialPesos = pesos.reversed(),
                objetivoKcal = resumen.objetivoMedio ?: objetivosEn(finVisible, historial, pesos)?.objetivos?.kcal,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgresoUiState())

    fun elegir(periodo: Periodo) {
        actualizarHoy()
        _periodo.value = periodo
        // Al cambiar de tipo de periodo se vuelve al actual.
        _vista.value = Vista(periodo, _hoy.value)
    }

    fun anterior() = mover(-1)
    fun siguiente() = mover(1)

    private fun mover(pasos: Long) {
        val v = _vista.value
        val nueva = v.periodo.desplazar(v.referencia, pasos)
        // No se puede ir más allá del periodo actual.
        if (nueva.isAfter(_hoy.value) && !v.periodo.rango(nueva).contains(_hoy.value)) return
        _vista.value = v.copy(referencia = minOf(nueva, _hoy.value))
    }

    /** Al volver a la app: si pasó la medianoche, actualiza "hoy" (y el periodo si se veía el actual). */
    fun actualizarHoy() {
        val nuevo = LocalDate.now()
        val viejo = _hoy.value
        if (nuevo == viejo) return
        val v = _vista.value
        if (viejo in v.periodo.rango(v.referencia)) _vista.value = v.copy(referencia = nuevo)
        _hoy.value = nuevo
    }

    fun registrarPeso(kg: Double, fecha: LocalDate = _hoy.value) {
        viewModelScope.launch { c.pesos.registrar(kg, fecha) }
    }

    fun borrarPeso(peso: Peso) {
        viewModelScope.launch { c.pesos.borrar(peso) }
    }
}
