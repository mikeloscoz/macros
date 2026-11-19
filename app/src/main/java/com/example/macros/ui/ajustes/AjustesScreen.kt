package com.example.macros.ui.ajustes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.macros.R
import com.example.macros.domain.Meta
import com.example.macros.domain.Objetivos
import com.example.macros.domain.Sexo
import com.example.macros.domain.leerDecimal
import com.example.macros.ui.components.CabeceraPantalla
import com.example.macros.ui.components.CampoConEtiqueta
import com.example.macros.ui.components.EtiquetaCampo
import com.example.macros.ui.components.MargenLateral
import com.example.macros.ui.components.TarjetaMacros
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.macros.ui.entero
import com.example.macros.ui.fabrica
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.ColorCarbohidratos
import com.example.macros.ui.theme.ColorGrasas
import com.example.macros.ui.theme.ColorProteinas
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.Texto
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.theme.VerdeClaro
import com.example.macros.ui.unDecimal
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun AjustesScreen(vm: AjustesViewModel = viewModel(factory = fabrica { AjustesViewModel(it) })) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmarImportar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.mensajes.collect { snackbar.showSnackbar(it) } }

    val exportar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(vm::exportar)
    }
    val importar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importar)
    }
    val importarCsv = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::importarCsv)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MargenLateral)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CabeceraPantalla("Ajustes")
            if (!estado.cargando) {
                MisDatos(estado, vm)
                ObjetivosDiarios(estado.objetivos)
            }
            TarjetaMacros(padding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
                Text("Copia de seguridad", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                FilaAccion(R.drawable.ic_exportar, "Exportar copia") {
                    exportar.launch("macros-copia-${LocalDate.now()}.json")
                }
                HorizontalDivider(color = Borde)
                FilaAccion(R.drawable.ic_importar, "Importar copia") { confirmarImportar = true }
                HorizontalDivider(color = Borde)
                FilaAccion(R.drawable.ic_archivo, "Importar alimentos (CSV)") { importarCsv.launch(arrayOf("*/*")) }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
            Snackbar(it, containerColor = Texto, contentColor = Tarjeta, shape = RoundedCornerShape(14.dp))
        }
    }

    if (confirmarImportar) {
        AlertDialog(
            onDismissRequest = { confirmarImportar = false },
            containerColor = Tarjeta,
            title = { Text("¿Importar una copia?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Se sustituirán TODOS los datos de la app (alimentos, diario, pesos y ajustes) por los de la copia. " +
                        "Si no estás seguro, exporta antes una copia de lo que tienes ahora.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoSecundario,
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmarImportar = false; importar.launch(arrayOf("*/*")) }) {
                    Text("Elegir archivo", color = Rojo, style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarImportar = false }) {
                    Text("Cancelar", color = Primario, style = MaterialTheme.typography.labelLarge)
                }
            },
        )
    }
}

@Composable
private fun MisDatos(estado: AjustesUiState, vm: AjustesViewModel) {
    val ajustes = estado.ajustes
    // Texto de los campos: se reinicia si cambia el valor guardado.
    var peso by remember(estado.peso) { mutableStateOf(estado.peso?.unDecimal() ?: "") }
    var errorPeso by remember { mutableStateOf<String?>(null) }
    var mantenimiento by remember(ajustes.mantenimiento) { mutableStateOf(ajustes.mantenimiento.toString()) }
    var errorMantenimiento by remember { mutableStateOf<String?>(null) }

    TarjetaMacros {
        Text("Mis datos", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        CampoConEtiqueta(
            etiqueta = "Peso actual",
            valor = peso,
            onCambio = { peso = it; errorPeso = null },
            unidad = "kg",
            numerico = true,
            error = errorPeso,
            imeAction = ImeAction.Done,
            alTerminar = {
                val kg = leerDecimal(peso)
                when {
                    kg == null || kg < 20 || kg > 400 -> errorPeso = "Escribe un peso válido"
                    // Solo se registra si de verdad ha cambiado (comparando con un decimal).
                    estado.peso == null || (kg * 10).roundToInt() != (estado.peso * 10).roundToInt() -> vm.guardarPeso(kg)
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        CampoConEtiqueta(
            etiqueta = "Calorías de mantenimiento",
            valor = mantenimiento,
            onCambio = { mantenimiento = it; errorMantenimiento = null },
            unidad = "kcal",
            numerico = true,
            error = errorMantenimiento,
            imeAction = ImeAction.Done,
            alTerminar = {
                val kcal = leerDecimal(mantenimiento)?.roundToInt()
                if (kcal == null || kcal < 800 || kcal > 6000) errorMantenimiento = "Entre 800 y 6000 kcal"
                else vm.guardarMantenimiento(kcal)
            },
        )
        Spacer(Modifier.height(16.dp))
        EtiquetaCampo("Objetivo")
        Spacer(Modifier.height(8.dp))
        Opciones(Meta.entries, ajustes.meta, { it.texto }, vm::elegirMeta)
        Spacer(Modifier.height(16.dp))
        EtiquetaCampo("Sexo")
        Spacer(Modifier.height(8.dp))
        Opciones(Sexo.entries, ajustes.sexo, { it.texto }, vm::elegirSexo)
    }
}

/** Botones de opción en fila, del mismo ancho; el elegido en verde. */
@Composable
private fun <T> Opciones(opciones: List<T>, elegida: T, texto: (T) -> String, onElegir: (T) -> Unit) = BoxWithConstraints {
    // Mismo tamaño de letra en todas las opciones: el que permite que quepa la más larga.
    val separacion = 10.dp
    val relleno = 8.dp
    val medidor = rememberTextMeasurer()
    val base = MaterialTheme.typography.labelLarge
    val densidad = LocalDensity.current
    val anchoTexto = with(densidad) {
        ((maxWidth - separacion * (opciones.size - 1)) / opciones.size - relleno * 2).toPx()
    }
    val factor = opciones.minOf { o ->
        val ancho = medidor.measure(texto(o), base, maxLines = 1, softWrap = false).size.width
        if (ancho > 0) minOf(1f, anchoTexto / ancho) else 1f
    }
    val estilo = base.copy(fontSize = base.fontSize * factor)

    Row(horizontalArrangement = Arrangement.spacedBy(separacion)) {
        opciones.forEach { opcion ->
            val activa = opcion == elegida
            val forma = RoundedCornerShape(14.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(forma)
                    .background(if (activa) VerdeClaro else Tarjeta)
                    .border(BorderStroke(if (activa) 1.5.dp else 1.dp, if (activa) Primario else Borde), forma)
                    .clickable { onElegir(opcion) }
                    .padding(horizontal = relleno),
                contentAlignment = Alignment.Center,
            ) {
                Text(texto(opcion), style = estilo, color = if (activa) Primario else Texto, maxLines = 1, softWrap = false)
            }
        }
    }
}

@Composable
private fun ObjetivosDiarios(objetivos: Objetivos?) {
    TarjetaMacros(color = VerdeClaro, borde = false) {
        Text("Tus objetivos diarios", style = MaterialTheme.typography.titleLarge)
        Text("Se calculan solos con tus datos.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        if (objetivos == null) {
            Text("Registra tu peso para calcularlos.", style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
        } else {
            FilaObjetivo("Calorías", "${objetivos.kcal.entero()} kcal", Texto)
            FilaObjetivo("Carbohidratos", "${objetivos.carbohidratos.entero()} g", ColorCarbohidratos)
            FilaObjetivo("Proteínas", "${objetivos.proteinas.entero()} g", ColorProteinas)
            FilaObjetivo("Grasas", "${objetivos.grasas.entero()} g", ColorGrasas)
        }
    }
}

@Composable
private fun FilaObjetivo(nombre: String, valor: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).background(color, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(nombre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun FilaAccion(icono: Int, texto: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icono), contentDescription = null, tint = Primario, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_siguiente), contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
    }
}
