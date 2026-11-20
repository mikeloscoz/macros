package com.example.macros.ui.hoy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.macros.ui.components.SelectorFecha
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.macros.R
import com.example.macros.data.db.Alimento
import com.example.macros.data.db.EntradaConAlimento
import com.example.macros.domain.Objetivos
import com.example.macros.ui.components.Anillo
import com.example.macros.ui.components.BarraMacro
import com.example.macros.ui.components.BotonDiscontinuo
import com.example.macros.ui.components.MargenLateral
import com.example.macros.ui.components.TarjetaMacros
import com.example.macros.ui.components.TextoAjustable
import com.example.macros.ui.entero
import com.example.macros.ui.fabrica
import com.example.macros.ui.fechaLarga
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.ColorCarbohidratos
import com.example.macros.ui.theme.ColorGrasas
import com.example.macros.ui.theme.ColorProteinas
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.theme.VerdeClaro
import com.example.macros.ui.tituloDia
import java.time.LocalDate

/** Hoja abierta: añadir en una comida, o editar una entrada. */
private sealed interface Hoja {
    data class Anadir(val comida: Int) : Hoja
    data class Editar(val entrada: EntradaConAlimento) : Hoja
}

@Composable
fun HoyScreen(
    volverAHoy: Flow<Unit> = emptyFlow(),
    vm: HoyViewModel = viewModel(factory = fabrica { HoyViewModel(it) }),
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val resultados by vm.resultados.collectAsStateWithLifecycle()
    var hoja by remember { mutableStateOf<Hoja?>(null) }
    var calendario by remember { mutableStateOf(false) }

    LaunchedEffect(volverAHoy) { volverAHoy.collect { vm.irAHoy() } }

    LifecycleResumeEffect(Unit) {
        vm.actualizarHoy()
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = MargenLateral, end = MargenLateral, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Cabecera(
                fecha = estado.fecha,
                hoy = estado.hoy,
                onAnterior = vm::diaAnterior,
                onSiguiente = vm::diaSiguiente,
                onCalendario = { calendario = true },
            )
        }
        if (!estado.cargando) {
            item {
                val objetivos = estado.objetivos
                if (objetivos != null) Resumen(estado.totales, objetivos, estado.meta.texto, estado.excluido)
                else TarjetaMacros {
                    Text("Registra tu peso para calcular tus objetivos.", style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
                }
            }
            items(4) { i ->
                TarjetaComida(
                    numero = i + 1,
                    entradas = estado.comidas[i],
                    onAnadir = { vm.buscar(""); hoja = Hoja.Anadir(i + 1) },
                    onEntrada = { hoja = Hoja.Editar(it) },
                )
            }
            item { NoContar(estado.excluido, vm::marcarExcluido) }
        }
    }

    when (val h = hoja) {
        is Hoja.Anadir -> HojaAnadir(
            comida = h.comida,
            busqueda = busqueda,
            onBuscar = vm::buscar,
            resultados = resultados,
            onAnadir = { alimento: Alimento, gramos: Double -> vm.anadir(h.comida, alimento, gramos) },
            onCerrar = { hoja = null },
        )
        is Hoja.Editar -> HojaEditar(
            entrada = h.entrada,
            onGuardar = { gramos -> vm.cambiarGramos(h.entrada.entrada, gramos) },
            onBorrar = { vm.borrar(h.entrada.entrada) },
            onCerrar = { hoja = null },
        )
        null -> Unit
    }

    if (calendario) {
        SelectorFecha(
            fecha = estado.fecha,
            hoy = estado.hoy,
            onElegir = { vm.irA(it); calendario = false },
            onCerrar = { calendario = false },
            textoConfirmar = "Ir",
        )
    }
}

@Composable
private fun Cabecera(
    fecha: LocalDate,
    hoy: LocalDate,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onCalendario: () -> Unit,
) {
    Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            Modifier
                .weight(1f)
                .clickable(onClick = onCalendario)
                .padding(start = 4.dp, end = 8.dp, bottom = 4.dp),
        ) {
            Text(fecha.tituloDia(hoy), style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(2.dp))
            Text(fecha.fechaLarga(hoy), style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
        }
        val colores = IconButtonDefaults.iconButtonColors(
            containerColor = VerdeClaro,
            contentColor = Primario,
            disabledContainerColor = VerdeClaro.copy(alpha = 0.4f),
            disabledContentColor = Primario.copy(alpha = 0.3f),
        )
        IconButton(onClick = onAnterior, colors = colores, modifier = Modifier.size(44.dp)) {
            Icon(painterResource(R.drawable.ic_anterior), contentDescription = "Día anterior")
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onSiguiente, enabled = fecha < hoy, colors = colores, modifier = Modifier.size(44.dp)) {
            Icon(painterResource(R.drawable.ic_siguiente), contentDescription = "Día siguiente")
        }
    }
}

@Composable
private fun Resumen(totales: Totales, objetivos: Objetivos, meta: String, excluido: Boolean) {
    val restantes = objetivos.kcal - totales.kcal
    // Un día que no cuenta no se marca en rojo: lo apuntado puede estar incompleto o ser aproximado.
    val pasado = restantes < -0.5 && !excluido
    TarjetaMacros(padding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)) {
        if (excluido) {
            Text(
                "Este día no cuenta en Progreso",
                style = MaterialTheme.typography.labelLarge,
                color = TextoSecundario,
                modifier = Modifier
                    .background(VerdeClaro, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Anillo(
                progreso = if (objetivos.kcal > 0) (totales.kcal / objetivos.kcal).toFloat() else 1f,
                color = if (pasado) Rojo else Primario,
                modifier = Modifier.size(136.dp),
            ) {
                Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TextoAjustable(
                        (if (pasado) -restantes else restantes).entero(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp, lineHeight = 38.sp),
                        color = if (pasado) Rojo else MaterialTheme.colorScheme.onSurface,
                        tamanoMinimo = 20.sp,
                    )
                    TextoAjustable(
                        if (pasado) "kcal de más" else "kcal restantes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (pasado) Rojo else TextoSecundario,
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                val grande = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                TextoAjustable("Consumidas", MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                TextoAjustable("${totales.kcal.entero()} kcal", grande, tamanoMinimo = 16.sp)
                Spacer(Modifier.height(12.dp))
                TextoAjustable("Objetivo · $meta", MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                TextoAjustable("${objetivos.kcal.entero()} kcal", grande, tamanoMinimo = 16.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Borde)
        Spacer(Modifier.height(20.dp))
        BarraMacro("Carbohidratos", totales.carbohidratos, objetivos.carbohidratos, ColorCarbohidratos)
        Spacer(Modifier.height(16.dp))
        BarraMacro("Proteínas", totales.proteinas, objetivos.proteinas, ColorProteinas)
        Spacer(Modifier.height(16.dp))
        BarraMacro("Grasas", totales.grasas, objetivos.grasas, ColorGrasas)
    }
}

@Composable
private fun TarjetaComida(
    numero: Int,
    entradas: List<EntradaConAlimento>,
    onAnadir: () -> Unit,
    onEntrada: (EntradaConAlimento) -> Unit,
) {
    TarjetaMacros(padding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Comida $numero", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (entradas.isEmpty()) {
                Text("Vacía", style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
            } else {
                Text(
                    "${entradas.sumOf { it.kcal }.entero()} kcal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Primario,
                )
            }
        }
        Spacer(Modifier.height(if (entradas.isEmpty()) 12.dp else 4.dp))
        entradas.forEachIndexed { i, e ->
            if (i > 0) HorizontalDivider(color = Borde)
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable { onEntrada(e) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(e.alimento.nombre, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp))
                    Text("${e.entrada.gramos.entero()} g", style = MaterialTheme.typography.bodySmall)
                }
                Text("${e.kcal.entero()} kcal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }
        if (entradas.isNotEmpty()) Spacer(Modifier.height(12.dp))
        BotonDiscontinuo("Añadir alimento", onAnadir)
    }
}

/** Interruptor para que el día no cuente en Progreso (comida fuera, vacaciones...). */
@Composable
private fun NoContar(excluido: Boolean, onCambiar: (Boolean) -> Unit) {
    TarjetaMacros(padding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { onCambiar(!excluido) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text("No contar este día", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Para comidas fuera o vacaciones: no entra en las medias de Progreso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                )
            }
            Switch(
                checked = excluido,
                onCheckedChange = onCambiar,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Primario,
                    uncheckedTrackColor = Tarjeta,
                    uncheckedBorderColor = Borde,
                    uncheckedThumbColor = TextoSecundario,
                ),
            )
        }
    }
}