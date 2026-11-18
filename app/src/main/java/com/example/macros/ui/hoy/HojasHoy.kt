package com.example.macros.ui.hoy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macros.R
import com.example.macros.data.db.Alimento
import com.example.macros.data.db.EntradaConAlimento
import com.example.macros.domain.leerDecimal
import com.example.macros.ui.components.CampoBusqueda
import com.example.macros.ui.components.coloresCampo
import com.example.macros.ui.entero
import com.example.macros.ui.paraCampo
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.ColorCarbohidratos
import com.example.macros.ui.theme.ColorGrasas
import com.example.macros.ui.theme.ColorProteinas
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.theme.VerdeClaro
import kotlinx.coroutines.launch

/** Cierra la hoja con su animación y luego avisa. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberCerrar(hoja: SheetState, onCerrar: () -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    return { scope.launch { hoja.hide() }.invokeOnCompletion { onCerrar() } }
}

/** Buscar alimento → escribir gramos → añadir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaAnadir(
    comida: Int,
    busqueda: String,
    onBuscar: (String) -> Unit,
    resultados: List<Alimento>,
    onAnadir: (Alimento, Double) -> Unit,
    onCerrar: () -> Unit,
) {
    val hoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cerrar = rememberCerrar(hoja, onCerrar)
    var elegido by remember { mutableStateOf<Alimento?>(null) }

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = hoja, containerColor = Tarjeta) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            val alimento = elegido
            if (alimento == null) {
                Text("Añadir a Comida $comida", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                CampoBusqueda(busqueda, onBuscar)
                Spacer(Modifier.height(8.dp))
                if (resultados.isEmpty()) {
                    Text(
                        "No hay ningún alimento que coincida. Puedes crearlo en la pestaña Alimentos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoSecundario,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
                LazyColumn(Modifier.weight(1f)) {
                    items(resultados, key = { it.id }) { a ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable { elegido = a }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(a.nombre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text("${a.kcal.entero()} kcal", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                        }
                        HorizontalDivider(color = Borde)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { elegido = null }, modifier = Modifier.size(44.dp)) {
                        Icon(painterResource(R.drawable.ic_anterior), contentDescription = "Volver a la búsqueda", tint = Primario)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir a Comida $comida", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(12.dp))
                EditorGramos(
                    alimento = alimento,
                    inicial = "",
                    textoBoton = "Añadir",
                    onConfirmar = { g -> onAnadir(alimento, g); cerrar() },
                )
            }
        }
    }
}

/** Cambiar gramos o borrar un alimento ya apuntado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaEditar(
    entrada: EntradaConAlimento,
    onGuardar: (Double) -> Unit,
    onBorrar: () -> Unit,
    onCerrar: () -> Unit,
) {
    val hoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cerrar = rememberCerrar(hoja, onCerrar)

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = hoja, containerColor = Tarjeta) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Text("Comida ${entrada.entrada.comida}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            EditorGramos(
                alimento = entrada.alimento,
                inicial = entrada.entrada.gramos.paraCampo(),
                textoBoton = "Guardar",
                onConfirmar = { g -> onGuardar(g); cerrar() },
            )
            TextButton(
                onClick = { onBorrar(); cerrar() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text("Quitar de la comida", style = MaterialTheme.typography.labelLarge, color = Rojo)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Nombre del alimento, campo de gramos con vista previa de kcal y macros, y botón. */
@Composable
private fun EditorGramos(
    alimento: Alimento,
    inicial: String,
    textoBoton: String,
    onConfirmar: (Double) -> Unit,
) {
    var campo by remember { mutableStateOf(TextFieldValue(inicial, TextRange(inicial.length))) }
    val gramos = leerDecimal(campo.text)?.takeIf { it > 0 }
    val foco = remember { FocusRequester() }
    LaunchedEffect(Unit) { foco.requestFocus() }

    Text(alimento.nombre, style = MaterialTheme.typography.titleLarge)
    Text("${alimento.kcal.entero()} kcal por 100 g", style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = campo,
        onValueChange = { campo = it },
        modifier = Modifier.fillMaxWidth().focusRequester(foco),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
        placeholder = { Text("Gramos", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)) },
        suffix = { Text("g", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { gramos?.let(onConfirmar) }),
        colors = coloresCampo(),
    )
    Spacer(Modifier.height(12.dp))
    // Vista previa con los gramos escritos
    val f = (gramos ?: 0.0) / 100
    Row(
        Modifier
            .fillMaxWidth()
            .background(VerdeClaro, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${(alimento.kcal * f).entero()} kcal",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Punto("C", alimento.carbohidratos * f, ColorCarbohidratos)
            Punto("P", alimento.proteinas * f, ColorProteinas)
            Punto("G", alimento.grasas * f, ColorGrasas)
        }
    }
    Spacer(Modifier.height(20.dp))
    Button(
        onClick = { gramos?.let(onConfirmar) },
        enabled = gramos != null,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Primario, disabledContainerColor = Borde),
    ) {
        Text(textoBoton, style = MaterialTheme.typography.labelLarge, color = Tarjeta)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Punto(letra: String, valor: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text("$letra ${valor.entero()}", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
    }
}
