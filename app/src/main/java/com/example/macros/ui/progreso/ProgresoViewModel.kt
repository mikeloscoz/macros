package com.example.macros.ui.progreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macros.data.AppContainer
import com.example.macros.data.db.Peso
import com.example.macros.domain.DiaComido
import com.example.macros.domain.Periodo
import com.example.macros.domain.ResumenProgreso
import com.example.macros.domain.calcularProgreso
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
    /** Último peso registrado y el primero de todos (para "Desde X kg al empezar"). */
    val pesoActual: Peso? = null,
    val pesoInicial: Peso? = null,
    val objetivoKcal: Double? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgresoViewModel(private val c: AppContainer) : ViewModel() {
    private val _periodo = MutableStateFlow(Periodo.Mes)
    val periodo: StateFlow<Periodo> = _periodo.asStateFlow()

    val estado: StateFlow<ProgresoUiState> = _periodo.flatMapLatest { periodo ->
        val hoy = LocalDate.now()
        val rango = periodo.rango(hoy)
        combine(c.diario.rango(rango.desde, rango.hasta), c.pesos.historial, c.objetivos) { entradas, pesos, objetivos ->
            val dias = entradas.groupBy { it.entrada.fecha }.map { (fecha, lista) ->
                DiaComido(fecha, lista.sumOf { it.kcal }, lista.sumOf { it.proteinas })
            }
            ProgresoUiState(
                cargando = false,
                periodo = periodo,
                hoy = hoy,
                resumen = calcularProgreso(periodo, hoy, dias, pesos),
                pesoActual = pesos.lastOrNull(),
                pesoInicial = pesos.firstOrNull(),
                objetivoKcal = objetivos?.kcal,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgresoUiState())

    fun elegir(periodo: Periodo) {
        _periodo.value = periodo
    }

    fun registrarPeso(kg: Double) {
        viewModelScope.launch { c.pesos.registrar(kg) }
    }
}
