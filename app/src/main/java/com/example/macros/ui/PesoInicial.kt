package com.example.macros.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.example.macros.data.PesoRepository
import com.example.macros.domain.leerDecimal
import com.example.macros.ui.components.CampoConEtiqueta
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PesoInicialViewModel(private val pesos: PesoRepository) : ViewModel() {
    /** null = aún no se sabe; true = no hay ningún peso registrado. */
    val falta: StateFlow<Boolean?> = pesos.actual.map { it == null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun registrar(kg: Double) {
        viewModelScope.launch { pesos.registrar(kg) }
    }
}

/** Si no hay ningún peso registrado, lo pide (es necesario para calcular los objetivos). */
@Composable
fun PedirPesoSiFalta(vm: PesoInicialViewModel = viewModel(factory = fabrica { PesoInicialViewModel(it.pesos) })) {
    val falta by vm.falta.collectAsStateWithLifecycle()
    if (falta != true) return

    var texto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    fun guardar() {
        val kg = leerDecimal(texto)
        if (kg == null || kg < 20 || kg > 400) error = "Escribe un peso válido" else vm.registrar(kg)
    }

    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        containerColor = Tarjeta,
        title = { Text("¿Cuánto pesas?", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    "Lo necesito para calcular tus objetivos de proteínas y grasas. Podrás cambiarlo en Ajustes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                )
                Spacer(Modifier.height(16.dp))
                CampoConEtiqueta(
                    etiqueta = "Peso actual",
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
    )
}
