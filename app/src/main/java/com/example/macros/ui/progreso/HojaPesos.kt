package com.example.macros.ui.progreso

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.macros.data.db.Peso
import com.example.macros.ui.DialogoPeso
import com.example.macros.ui.components.MargenLateral
import com.example.macros.ui.components.SelectorFecha
import com.example.macros.ui.fechaLarga
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.unDecimal
import java.time.LocalDate

/** Qué peso se está editando: uno existente, o uno nuevo de un día elegido. */
private data class Edicion(val fecha: LocalDate, val existente: Peso?, val avisarSustituye: Boolean = false)

/**
 * Historial de pesos (del más reciente al más antiguo). Tocar uno permite corregirlo o borrarlo;
 * "Añadir peso de otro día" registra el de cualquier día pasado (si ya había uno, lo sustituye).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaPesos(
    pesos: List<Peso>,
    hoy: LocalDate,
    onGuardar: (Double, LocalDate) -> Unit,
    onBorrar: (Peso) -> Unit,
    onCerrar: () -> Unit,
) {
    val hoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var eligiendoFecha by remember { mutableStateOf(false) }
    var edicion by remember { mutableStateOf<Edicion?>(null) }

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = hoja, containerColor = Tarjeta) {
        Column(Modifier.padding(horizontal = MargenLateral).navigationBarsPadding()) {
            Text("Historial de pesos", style = MaterialTheme.typography.titleLarge)
            Text(
                "Toca un peso para corregirlo o borrarlo.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { eligiendoFecha = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, Borde),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primario),
            ) {
                Text("Añadir peso de otro día", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(pesos, key = { it.fecha.toEpochDay() }) { peso ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clickable { edicion = Edicion(peso.fecha, peso) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(peso.fecha.fechaLarga(hoy), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            "${peso.kg.unDecimal()} kg",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                    HorizontalDivider(color = Borde)
                }
            }
        }
    }

    if (eligiendoFecha) {
        SelectorFecha(
            fecha = hoy,
            hoy = hoy,
            onElegir = { fecha ->
                eligiendoFecha = false
                val existente = pesos.firstOrNull { it.fecha == fecha }
                edicion = Edicion(fecha, existente, avisarSustituye = existente != null)
            },
            onCerrar = { eligiendoFecha = false },
        )
    }

    edicion?.let { e ->
        DialogoPeso(
            titulo = "Peso · ${e.fecha.fechaLarga(hoy)}",
            explicacion = if (e.avisarSustituye) "Ese día ya tiene un peso: se sustituye." else null,
            inicial = e.existente?.kg?.unDecimal() ?: "",
            onGuardar = { kg -> onGuardar(kg, e.fecha); edicion = null },
            onCancelar = { edicion = null },
            onBorrar = e.existente?.let { p -> { onBorrar(p); edicion = null } },
        )
    }
}
