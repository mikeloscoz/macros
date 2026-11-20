package com.example.macros.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.Texto
import com.example.macros.ui.theme.VerdeClaro
import java.time.LocalDate

/**
 * Botones de opción en fila, del mismo ancho; el elegido en verde.
 * Todas las opciones usan el mismo tamaño de letra: el que permite que quepa la más larga.
 */
@Composable
fun <T> Opciones(opciones: List<T>, elegida: T?, texto: (T) -> String, onElegir: (T) -> Unit) = BoxWithConstraints {
    val separacion = 10.dp
    val relleno = 8.dp
    val medidor = rememberTextMeasurer()
    val base = MaterialTheme.typography.labelLarge
    val anchoTexto = with(LocalDensity.current) {
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

/** Calendario para elegir un día (no deja elegir días posteriores a [hoy]). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFecha(
    fecha: LocalDate,
    hoy: LocalDate,
    onElegir: (LocalDate) -> Unit,
    onCerrar: () -> Unit,
    textoConfirmar: String = "Aceptar",
) {
    val msDia = 86_400_000L
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = fecha.toEpochDay() * msDia,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis / msDia <= hoy.toEpochDay()
            override fun isSelectableYear(year: Int) = year <= hoy.year
        },
    )
    val colores = DatePickerDefaults.colors(
        containerColor = Tarjeta,
        selectedDayContainerColor = Primario,
        todayDateBorderColor = Primario,
        todayContentColor = Primario,
        selectedYearContainerColor = Primario,
    )
    DatePickerDialog(
        onDismissRequest = onCerrar,
        colors = colores,
        confirmButton = {
            TextButton(onClick = {
                estado.selectedDateMillis?.let { onElegir(LocalDate.ofEpochDay(it / msDia)) } ?: onCerrar()
            }) { Text(textoConfirmar, color = Primario, style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar", color = Primario, style = MaterialTheme.typography.labelLarge) }
        },
    ) {
        DatePicker(state = estado, colors = colores, showModeToggle = false)
    }
}
