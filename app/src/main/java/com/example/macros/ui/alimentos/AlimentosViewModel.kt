package com.example.macros.ui.alimentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macros.data.AlimentosRepository
import com.example.macros.data.db.Alimento
import com.example.macros.domain.leerDecimal
import com.example.macros.domain.normalizar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlimentosUiState(
    val cargando: Boolean = true,
    val total: Int = 0,
    val filtrados: List<Alimento> = emptyList(),
)

/** Lo que el usuario ha escrito en el formulario de alimento (texto tal cual). */
data class FormAlimento(
    val id: Long = 0,
    val nombre: String = "",
    val kcal: String = "",
    val carbohidratos: String = "",
    val proteinas: String = "",
    val grasas: String = "",
)

/** Errores por campo; vacío = todo bien. */
data class ErroresForm(
    val nombre: String? = null,
    val kcal: String? = null,
    val carbohidratos: String? = null,
    val proteinas: String? = null,
    val grasas: String? = null,
) {
    val hayErrores get() = listOf(nombre, kcal, carbohidratos, proteinas, grasas).any { it != null }
}

class AlimentosViewModel(private val repo: AlimentosRepository) : ViewModel() {
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val estado: StateFlow<AlimentosUiState> =
        combine(repo.alimentos, _busqueda) { lista, texto ->
            val q = normalizar(texto)
            AlimentosUiState(
                cargando = false,
                total = lista.size,
                filtrados = if (q.isEmpty()) lista else lista.filter { normalizar(it.nombre).contains(q) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlimentosUiState())

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    /** Valida y guarda. Llama a [alTerminar] con los errores (vacíos si se ha guardado). */
    fun guardar(form: FormAlimento, alTerminar: (ErroresForm) -> Unit) {
        viewModelScope.launch {
            val nombre = form.nombre.trim()
            val kcal = leerDecimal(form.kcal)
            val c = leerDecimal(form.carbohidratos)
            val p = leerDecimal(form.proteinas)
            val g = leerDecimal(form.grasas)
            val errores = ErroresForm(
                nombre = when {
                    nombre.isEmpty() -> "Escribe un nombre"
                    repo.nombreOcupado(nombre, form.id) -> "Ya hay un alimento con ese nombre"
                    else -> null
                },
                kcal = if (kcal == null) "Número no válido" else null,
                carbohidratos = if (c == null) "Número no válido" else null,
                proteinas = if (p == null) "Número no válido" else null,
                grasas = if (g == null) "Número no válido" else null,
            )
            if (!errores.hayErrores) {
                repo.guardar(Alimento(form.id, nombre, kcal!!, c!!, p!!, g!!))
            }
            alTerminar(errores)
        }
    }

    fun borrar(alimento: Alimento) {
        viewModelScope.launch { repo.borrar(alimento) }
    }
}
