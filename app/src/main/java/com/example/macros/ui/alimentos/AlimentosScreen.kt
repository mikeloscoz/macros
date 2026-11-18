package com.example.macros.ui.alimentos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.macros.R
import com.example.macros.data.db.Alimento
import com.example.macros.ui.components.CabeceraPantalla
import com.example.macros.ui.components.CampoBusqueda
import com.example.macros.ui.components.CampoConEtiqueta
import com.example.macros.ui.components.EtiquetaCampo
import com.example.macros.ui.components.MargenLateral
import com.example.macros.ui.components.TarjetaMacros
import com.example.macros.ui.decimalCorto
import com.example.macros.ui.entero
import com.example.macros.ui.fabrica
import com.example.macros.ui.paraCampo
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.ColorCarbohidratos
import com.example.macros.ui.theme.ColorGrasas
import com.example.macros.ui.theme.ColorProteinas
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario
import kotlinx.coroutines.launch

@Composable
fun AlimentosScreen(
    vm: AlimentosViewModel = viewModel(factory = fabrica { AlimentosViewModel(it.alimentos) }),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    // Formulario abierto: null = cerrado. Si id != 0 se está editando ese alimento.
    var formulario by remember { mutableStateOf<FormAlimento?>(null) }
    var editando by remember { mutableStateOf<Alimento?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = MargenLateral, end = MargenLateral, bottom = 24.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CabeceraPantalla(
                    titulo = "Alimentos",
                    subtitulo = "${estado.total} alimentos · valores por 100 g",
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { editando = null; formulario = FormAlimento() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Primario),
                    contentPadding = PaddingValues(start = 16.dp, end = 20.dp),
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_mas), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Nuevo", style = MaterialTheme.typography.labelLarge, color = Tarjeta)
                }
            }
        }
        item {
            EtiquetaCampo("Buscar alimento", color = TextoSecundario, modifier = Modifier.padding(start = 2.dp))
            Spacer(Modifier.height(8.dp))
            CampoBusqueda(busqueda, vm::buscar)
            Spacer(Modifier.height(16.dp))
        }
        item {
            when {
                estado.cargando -> Unit
                estado.filtrados.isEmpty() -> TarjetaMacros {
                    Text(
                        if (busqueda.isBlank()) "Aún no tienes alimentos. Pulsa «Nuevo» para añadir uno."
                        else "No hay ningún alimento que coincida con «${busqueda.trim()}».",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoSecundario,
                    )
                }
                else -> TarjetaMacros(padding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)) {
                    estado.filtrados.forEachIndexed { i, alimento ->
                        if (i > 0) HorizontalDivider(color = Borde)
                        FilaAlimento(alimento) {
                            editando = alimento
                            formulario = FormAlimento(
                                id = alimento.id,
                                nombre = alimento.nombre,
                                kcal = alimento.kcal.paraCampo(),
                                carbohidratos = alimento.carbohidratos.paraCampo(),
                                proteinas = alimento.proteinas.paraCampo(),
                                grasas = alimento.grasas.paraCampo(),
                            )
                        }
                    }
                }
            }
        }
    }

    formulario?.let { inicial ->
        HojaAlimento(
            inicial = inicial,
            onGuardar = vm::guardar,
            onBorrar = editando?.let { a -> { vm.borrar(a) } },
            onCerrar = { formulario = null; editando = null },
        )
    }
}

@Composable
private fun FilaAlimento(alimento: Alimento, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(alimento.nombre, style = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize))
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Macro("C", alimento.carbohidratos, ColorCarbohidratos)
                Macro("P", alimento.proteinas, ColorProteinas)
                Macro("G", alimento.grasas, ColorGrasas)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(alimento.kcal.entero(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("kcal", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Macro(letra: String, valor: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text("$letra ${valor.decimalCorto()}", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
    }
}

/** Hoja inferior para crear o editar un alimento. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaAlimento(
    inicial: FormAlimento,
    onGuardar: (FormAlimento, (ErroresForm) -> Unit) -> Unit,
    onBorrar: (() -> Unit)?,
    onCerrar: () -> Unit,
) {
    val hoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var form by remember { mutableStateOf(inicial) }
    var errores by remember { mutableStateOf(ErroresForm()) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    fun cerrar() {
        scope.launch { hoja.hide() }.invokeOnCompletion { onCerrar() }
    }

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = hoja, containerColor = Tarjeta) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Text(
                if (inicial.id == 0L) "Nuevo alimento" else "Editar alimento",
                style = MaterialTheme.typography.titleLarge,
            )
            Text("Valores por 100 g", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(20.dp))

            CampoConEtiqueta("Nombre", form.nombre, { form = form.copy(nombre = it); errores = errores.copy(nombre = null) }, error = errores.nombre)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoConEtiqueta("Kcal", form.kcal, { form = form.copy(kcal = it); errores = errores.copy(kcal = null) },
                    Modifier.weight(1f), unidad = "kcal", numerico = true, error = errores.kcal)
                CampoConEtiqueta("Carbohidratos", form.carbohidratos, { form = form.copy(carbohidratos = it); errores = errores.copy(carbohidratos = null) },
                    Modifier.weight(1f), unidad = "g", numerico = true, error = errores.carbohidratos)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoConEtiqueta("Proteínas", form.proteinas, { form = form.copy(proteinas = it); errores = errores.copy(proteinas = null) },
                    Modifier.weight(1f), unidad = "g", numerico = true, error = errores.proteinas)
                CampoConEtiqueta("Grasas", form.grasas, { form = form.copy(grasas = it); errores = errores.copy(grasas = null) },
                    Modifier.weight(1f), unidad = "g", numerico = true, error = errores.grasas, imeAction = ImeAction.Done)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onGuardar(form) { e -> errores = e; if (!e.hayErrores) cerrar() } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
            ) {
                Text("Guardar", style = MaterialTheme.typography.labelLarge, color = Tarjeta)
            }
            if (onBorrar != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { confirmarBorrado = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text("Borrar alimento", style = MaterialTheme.typography.labelLarge, color = Rojo)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmarBorrado && onBorrar != null) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            containerColor = Tarjeta,
            title = { Text("¿Borrar «${inicial.nombre}»?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Desaparecerá de la lista. Los días en los que ya lo apuntaste no cambian.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoSecundario,
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmarBorrado = false; onBorrar(); cerrar() }) {
                    Text("Borrar", color = Rojo, style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) {
                    Text("Cancelar", color = Primario, style = MaterialTheme.typography.labelLarge)
                }
            },
        )
    }
}
