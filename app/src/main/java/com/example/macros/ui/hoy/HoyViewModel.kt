package com.example.macros.ui.hoy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macros.data.AppContainer
import com.example.macros.data.db.Alimento
import com.example.macros.data.db.Entrada
import com.example.macros.data.db.EntradaConAlimento
import com.example.macros.domain.Meta
import com.example.macros.domain.Objetivos
import com.example.macros.domain.normalizar
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

data class Totales(
    val kcal: Double = 0.0,
    val carbohidratos: Double = 0.0,
    val proteinas: Double = 0.0,
    val grasas: Double = 0.0,
)

fun List<EntradaConAlimento>.totales() = Totales(
    kcal = sumOf { it.kcal },
    carbohidratos = sumOf { it.carbohidratos },
    proteinas = sumOf { it.proteinas },
    grasas = sumOf { it.grasas },
)

data class HoyUiState(
    val cargando: Boolean = true,
    val fecha: LocalDate = LocalDate.now(),
    val hoy: LocalDate = LocalDate.now(),
    /** Siempre 4 listas: comidas 1 a 4. */
    val comidas: List<List<EntradaConAlimento>> = List(4) { emptyList() },
    val totales: Totales = Totales(),
    val objetivos: Objetivos? = null,
    val meta: Meta = Meta.Mantener,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HoyViewModel(private val c: AppContainer) : ViewModel() {
    private val _hoy = MutableStateFlow(LocalDate.now())
    private val _fecha = MutableStateFlow(LocalDate.now())

    val estado: StateFlow<HoyUiState> = combine(
        _fecha.flatMapLatest { c.diario.dia(it) },
        _fecha,
        _hoy,
        c.objetivos,
        c.ajustes.ajustes,
    ) { entradas, fecha, hoy, objetivos, ajustes ->
        HoyUiState(
            cargando = false,
            fecha = fecha,
            hoy = hoy,
            comidas = (1..4).map { n -> entradas.filter { it.entrada.comida == n } },
            totales = entradas.totales(),
            objetivos = objetivos,
            meta = ajustes.meta,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HoyUiState())

    // --- Buscador de "Añadir alimento" ---
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val resultados: StateFlow<List<Alimento>> = combine(c.alimentos.alimentos, _busqueda) { lista, texto ->
        val q = normalizar(texto)
        if (q.isEmpty()) lista else lista.filter { normalizar(it.nombre).contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    // --- Cambio de día ---
    fun irA(fecha: LocalDate) {
        _fecha.value = minOf(fecha, _hoy.value)
    }

    fun irAHoy() {
        actualizarHoy()
        _fecha.value = _hoy.value
    }

    fun diaAnterior() = irA(_fecha.value.minusDays(1))
    fun diaSiguiente() = irA(_fecha.value.plusDays(1))

    /** Al volver a la app: si pasó la medianoche y estabas viendo "hoy", pasa al nuevo hoy. */
    fun actualizarHoy() {
        val nuevo = LocalDate.now()
        if (nuevo != _hoy.value) {
            if (_fecha.value == _hoy.value) _fecha.value = nuevo
            _hoy.value = nuevo
        }
    }

    // --- Diario ---
    fun anadir(comida: Int, alimento: Alimento, gramos: Double) {
        val fecha = _fecha.value
        viewModelScope.launch { c.diario.anadir(fecha, comida, alimento.id, gramos) }
    }

    fun cambiarGramos(entrada: Entrada, gramos: Double) {
        viewModelScope.launch { c.diario.cambiarGramos(entrada, gramos) }
    }

    fun borrar(entrada: Entrada) {
        viewModelScope.launch { c.diario.borrar(entrada) }
    }
}
