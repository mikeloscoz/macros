package com.example.macros.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.macros.data.Ajustes
import com.example.macros.data.AppContainer
import com.example.macros.domain.Meta
import com.example.macros.domain.Sexo
import com.example.macros.domain.leerDecimal
import com.example.macros.ui.components.CampoConEtiqueta
import com.example.macros.ui.components.EtiquetaCampo
import com.example.macros.ui.components.MargenLateral
import com.example.macros.ui.components.Opciones
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** Qué datos faltan para poder calcular los objetivos. */
sealed interface DatosQueFaltan {
    data object Cargando : DatosQueFaltan
    data object Ninguno : DatosQueFaltan
    /** Primera vez: faltan los ajustes (y quizá el peso, si [pesoActual] es null). */
    data class Todos(val pesoActual: Double?) : DatosQueFaltan
    /** Hay ajustes pero ningún peso (por ejemplo, tras importar una copia sin pesos). */
    data object Peso : DatosQueFaltan
}

class BienvenidaViewModel(private val c: AppContainer) : ViewModel() {
    val faltan: StateFlow<DatosQueFaltan> = combine(c.ajustes.historial, c.pesos.actual) { historial, peso ->
        when {
            historial.isEmpty() -> DatosQueFaltan.Todos(peso?.kg)
            peso == null -> DatosQueFaltan.Peso
            else -> DatosQueFaltan.Ninguno
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DatosQueFaltan.Cargando)

    fun registrarPeso(kg: Double) {
        viewModelScope.launch { c.pesos.registrar(kg) }
    }

    /** Los primeros ajustes valen para todos los días; el peso se registra hoy si es nuevo o ha cambiado. */
    fun guardarTodo(kg: Double, ajustes: Ajustes, pesoActual: Double?) {
        viewModelScope.launch {
            if (pesoActual == null || abs(pesoActual - kg) >= 0.05) c.pesos.registrar(kg)
            c.ajustes.guardarIniciales(ajustes)
        }
    }
}

/** Si faltan datos para calcular los objetivos, los pide (no se puede cerrar sin guardar). */
@Composable
fun PedirDatosSiFaltan(vm: BienvenidaViewModel = viewModel(factory = fabrica { BienvenidaViewModel(it) })) {
    when (val f = vm.faltan.collectAsStateWithLifecycle().value) {
        is DatosQueFaltan.Todos -> DialogoBienvenida(f.pesoActual) { kg, ajustes -> vm.guardarTodo(kg, ajustes, f.pesoActual) }
        DatosQueFaltan.Peso -> DialogoPeso(
            titulo = "¿Cuánto pesas?",
            explicacion = "Lo necesito para calcular tus objetivos de proteínas y grasas. Podrás cambiarlo en Ajustes.",
            inicial = "",
            onGuardar = vm::registrarPeso,
            onCancelar = null,
        )
        else -> Unit
    }
}

@Composable
private fun DialogoBienvenida(pesoActual: Double?, onGuardar: (Double, Ajustes) -> Unit) {
    var peso by remember { mutableStateOf(pesoActual?.unDecimal() ?: "") }
    var errorPeso by remember { mutableStateOf<String?>(null) }
    var mantenimiento by remember { mutableStateOf("") }
    var errorMantenimiento by remember { mutableStateOf<String?>(null) }
    var meta by remember { mutableStateOf<Meta?>(null) }
    var sexo by remember { mutableStateOf<Sexo?>(null) }
    var errorOpciones by remember { mutableStateOf(false) }

    fun guardar() {
        val kg = leerDecimal(peso)
        val kcal = leerDecimal(mantenimiento)?.roundToInt()
        errorPeso = if (kg == null || kg < 20 || kg > 400) "Escribe un peso válido" else null
        errorMantenimiento = if (kcal == null || kcal < 800 || kcal > 6000) "Entre 800 y 6000 kcal" else null
        errorOpciones = meta == null || sexo == null
        if (errorPeso == null && errorMantenimiento == null && !errorOpciones) {
            onGuardar(kg!!, Ajustes(kcal!!, meta!!, sexo!!))
        }
    }

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = MargenLateral),
        containerColor = Tarjeta,
        title = { Text("Tus datos", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Con ellos calculo tus objetivos diarios. Podrás cambiarlos en Ajustes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                )
                Spacer(Modifier.height(16.dp))
                CampoConEtiqueta(
                    etiqueta = "Peso",
                    valor = peso,
                    onCambio = { peso = it; errorPeso = null },
                    unidad = "kg",
                    numerico = true,
                    error = errorPeso,
                    imeAction = ImeAction.Next,
                )
                Spacer(Modifier.height(12.dp))
                CampoConEtiqueta(
                    etiqueta = "Calorías de mantenimiento",
                    valor = mantenimiento,
                    onCambio = { mantenimiento = it; errorMantenimiento = null },
                    unidad = "kcal",
                    numerico = true,
                    error = errorMantenimiento,
                    imeAction = ImeAction.Done,
                )
                Spacer(Modifier.height(16.dp))
                EtiquetaCampo("Objetivo")
                Spacer(Modifier.height(8.dp))
                Opciones(Meta.entries, meta, { it.texto }) { meta = it; errorOpciones = false }
                Spacer(Modifier.height(16.dp))
                EtiquetaCampo("Sexo")
                Spacer(Modifier.height(8.dp))
                Opciones(Sexo.entries, sexo, { it.texto }) { sexo = it; errorOpciones = false }
                if (errorOpciones) {
                    Spacer(Modifier.height(8.dp))
                    Text("Elige objetivo y sexo", style = MaterialTheme.typography.bodySmall, color = Rojo)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = ::guardar) {
                Text("Empezar", color = Primario, style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

/**
 * Diálogo para escribir un peso. Sin [onCancelar] no se puede cerrar sin guardar.
 * Con [onBorrar] muestra también un botón para borrar el registro.
 */
@Composable
fun DialogoPeso(
    titulo: String,
    explicacion: String?,
    inicial: String,
    onGuardar: (Double) -> Unit,
    onCancelar: (() -> Unit)?,
    onBorrar: (() -> Unit)? = null,
) {
    var texto by remember { mutableStateOf(inicial) }
    var error by remember { mutableStateOf<String?>(null) }
    fun guardar() {
        val kg = leerDecimal(texto)
        if (kg == null || kg < 20 || kg > 400) error = "Escribe un peso válido" else onGuardar(kg)
    }

    AlertDialog(
        onDismissRequest = { onCancelar?.invoke() },
        properties = DialogProperties(dismissOnBackPress = onCancelar != null, dismissOnClickOutside = onCancelar != null),
        containerColor = Tarjeta,
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                if (explicacion != null) {
                    Text(explicacion, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                    Spacer(Modifier.height(16.dp))
                }
                CampoConEtiqueta(
                    etiqueta = "Peso",
                    valor = texto,
                    onCambio = { texto = it; error = null },
                    unidad = "kg",
                    numerico = true,
                    error = error,
                    imeAction = ImeAction.Done,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = ::guardar) {
                Text("Guardar", color = Primario, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = if (onCancelar == null && onBorrar == null) null else {
            {
                Row {
                    if (onBorrar != null) {
                        TextButton(onClick = onBorrar) {
                            Text("Borrar", color = Rojo, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (onCancelar != null) {
                        TextButton(onClick = onCancelar) {
                            Text("Cancelar", color = Primario, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        },
    )
}
