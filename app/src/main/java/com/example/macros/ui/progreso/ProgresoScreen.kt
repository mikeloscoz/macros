package com.example.macros.ui.progreso

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.macros.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.macros.domain.Periodo
import com.example.macros.domain.Rango
import com.example.macros.domain.ResumenProgreso
import com.example.macros.ui.DialogoPeso
import com.example.macros.ui.Espanol
import com.example.macros.ui.components.CabeceraPantalla
import com.example.macros.ui.components.MargenLateral
import com.example.macros.ui.components.TarjetaMacros
import com.example.macros.ui.components.TextoAjustable
import com.example.macros.ui.entero
import com.example.macros.ui.fabrica
import com.example.macros.ui.primeraMayuscula
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.Texto
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.theme.VerdeClaro
import com.example.macros.ui.unDecimal
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun ProgresoScreen(vm: ProgresoViewModel = viewModel(factory = fabrica { ProgresoViewModel(it) })) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val periodo by vm.periodo.collectAsStateWithLifecycle()
    var registrando by remember { mutableStateOf(false) }
    var verHistorial by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        vm.actualizarHoy()
        onPauseOrDispose { }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MargenLateral)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CabeceraPantalla("Progreso", null)
        SelectorPeriodo(periodo, vm::elegir)

        val resumen = estado.resumen
        if (!estado.cargando && resumen != null) {
            NavegadorPeriodo(textoRango(estado.periodo, resumen.rango), !estado.esActual, vm::anterior, vm::siguiente)
            TarjetaPeso(estado, resumen, onRegistrar = { registrando = true }, onHistorial = { verHistorial = true })
            TarjetaKcal(periodo, resumen, estado.objetivoKcal)
            if (resumen.diasRegistrados > 0) DatosRapidos(resumen)
        }
    }

    if (registrando) {
        DialogoPeso(
            titulo = "Peso de hoy",
            explicacion = "Si ya habías registrado el peso hoy, se sustituye.",
            inicial = estado.pesoActual?.kg?.unDecimal() ?: "",
            onGuardar = { vm.registrarPeso(it); registrando = false },
            onCancelar = { registrando = false },
        )
    }

    if (verHistorial) {
        HojaPesos(
            pesos = estado.historialPesos,
            hoy = estado.hoy,
            onGuardar = vm::registrarPeso,
            onBorrar = vm::borrarPeso,
            onCerrar = { verHistorial = false },
        )
    }
}

/** ‹ Noviembre 2026 › para moverse entre periodos; no deja pasar del actual. */
@Composable
private fun NavegadorPeriodo(texto: String, puedeAvanzar: Boolean, onAnterior: () -> Unit, onSiguiente: () -> Unit) {
    val colores = IconButtonDefaults.iconButtonColors(
        containerColor = VerdeClaro,
        contentColor = Primario,
        disabledContainerColor = VerdeClaro.copy(alpha = 0.4f),
        disabledContentColor = Primario.copy(alpha = 0.3f),
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onAnterior, colors = colores, modifier = Modifier.size(44.dp)) {
            Icon(painterResource(R.drawable.ic_anterior), contentDescription = "Periodo anterior")
        }
        TextoAjustable(
            texto,
            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        IconButton(onClick = onSiguiente, enabled = puedeAvanzar, colors = colores, modifier = Modifier.size(44.dp)) {
            Icon(painterResource(R.drawable.ic_siguiente), contentDescription = "Periodo siguiente")
        }
    }
}

private fun textoRango(periodo: Periodo, rango: Rango): String = when (periodo) {
    Periodo.Semana -> {
        val fin = rango.hasta.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Espanol))
        if (rango.desde.month == rango.hasta.month) "${rango.desde.dayOfMonth} – $fin"
        else "${rango.desde.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Espanol))} – $fin"
    }
    Periodo.Mes -> rango.desde.format(DateTimeFormatter.ofPattern("MMMM yyyy", Espanol)).primeraMayuscula()
    Periodo.Anio -> rango.desde.year.toString()
}

/** Selector Semana / Mes / Año: fondo verde claro y la opción elegida en blanco. */
@Composable
private fun SelectorPeriodo(elegido: Periodo, onElegir: (Periodo) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VerdeClaro)
            .padding(4.dp),
    ) {
        Periodo.entries.forEach { p ->
            val activo = p == elegido
            val fondo by animateColorAsState(if (activo) Tarjeta else VerdeClaro, label = "periodo")
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .then(if (activo) Modifier.shadow(2.dp, RoundedCornerShape(12.dp)) else Modifier)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fondo)
                    .clickable { onElegir(p) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    p.texto,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                    color = if (activo) Texto else TextoSecundario,
                )
            }
        }
    }
}

@Composable
private fun TarjetaPeso(estado: ProgresoUiState, resumen: ResumenProgreso, onRegistrar: () -> Unit, onHistorial: () -> Unit) {
    val actual = estado.pesoActual
    val inicial = estado.pesoInicial
    TarjetaMacros {
        if (actual == null || inicial == null) {
            Text("Aún no has registrado ningún peso.", style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
        } else {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("Peso actual", style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)) { append(actual.kg.unDecimal()) }
                            withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) { append(" kg") }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 46.sp),
                    )
                    Text(
                        "Desde ${inicial.kg.unDecimal()} kg al empezar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario,
                    )
                }
                val cambio = actual.kg - inicial.kg
                if (abs(cambio) >= 0.05) {
                    Text(
                        "${if (cambio < 0) "↓" else "↑"} ${abs(cambio).unDecimal()} kg",
                        style = MaterialTheme.typography.labelLarge,
                        color = Primario,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(VerdeClaro)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (resumen.pesos.size >= 2) {
                GraficaPeso(resumen.pesos)
            } else {
                Text(
                    "Registra tu peso en más días de este periodo para ver la gráfica.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRegistrar,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, Borde),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primario),
        ) {
            Text("Registrar peso de hoy", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onHistorial, modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Text("Historial de pesos", color = Primario, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TarjetaKcal(periodo: Periodo, resumen: ResumenProgreso, objetivo: Double?) {
    val (titulo, nombre) = when (periodo) {
        Periodo.Semana -> "Kcal por día" to "de la semana"
        Periodo.Mes -> "Media de kcal por semana" to "del mes"
        Periodo.Anio -> "Media de kcal por mes" to "del año"
    }
    TarjetaMacros {
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        val media = resumen.mediaKcal
        Text(
            buildAnnotatedString {
                if (objetivo != null) append("Objetivo ${objetivo.entero()} kcal")
                if (media != null) {
                    if (objetivo != null) append(" · ")
                    append("media $nombre ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Texto)) { append(media.entero()) }
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario,
        )
        Spacer(Modifier.height(16.dp))
        if (media == null) {
            Text(
                "Aún no hay comidas apuntadas en este periodo.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextoSecundario,
            )
        } else {
            GraficaKcal(resumen.barras, objetivo)
        }
    }
}

@Composable
private fun DatosRapidos(resumen: ResumenProgreso) {
    TarjetaMacros(padding = PaddingValues(horizontal = 12.dp, vertical = 20.dp)) {
        Row {
            Dato(resumen.diasRegistrados.toString(), "días registrados", Modifier.weight(1f))
            Dato(resumen.proteinaMedia?.let { "${it.entero()} g" } ?: "—", "proteína media", Modifier.weight(1f))
            Dato(
                resumen.kgPorSemana?.let { (if (it > 0.05) "+" else if (it < -0.05) "−" else "") + abs(it).unDecimal() } ?: "—",
                "kg por semana",
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Dato(valor: String, etiqueta: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TextoAjustable(valor, MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp))
        TextoAjustable(etiqueta, MaterialTheme.typography.bodyMedium, color = TextoSecundario)
    }
}
