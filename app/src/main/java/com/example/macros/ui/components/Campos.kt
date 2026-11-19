package com.example.macros.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import com.example.macros.R
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.Fondo
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario

/** Etiqueta pequeña encima de un campo ("Peso actual", "Buscar alimento"...). */
@Composable
fun EtiquetaCampo(texto: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Text(texto, modifier = modifier, style = MaterialTheme.typography.labelLarge, color = color)
}

@Composable
fun coloresCampo(fondo: androidx.compose.ui.graphics.Color = Fondo) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = fondo,
    unfocusedContainerColor = fondo,
    errorContainerColor = fondo,
    focusedBorderColor = Primario,
    unfocusedBorderColor = Borde,
    errorBorderColor = Rojo,
    cursorColor = Primario,
    focusedSuffixColor = TextoSecundario,
    unfocusedSuffixColor = TextoSecundario,
    focusedPlaceholderColor = TextoSecundario,
    unfocusedPlaceholderColor = TextoSecundario,
)

/** Campo con etiqueta encima, fondo verde muy claro y unidad opcional a la derecha (kg, kcal, g). */
@Composable
fun CampoConEtiqueta(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    unidad: String? = null,
    numerico: Boolean = false,
    error: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    /** Si se indica: se llama al pulsar "Hecho" en el teclado o al salir del campo. */
    alTerminar: (() -> Unit)? = null,
) {
    val foco = LocalFocusManager.current
    var teniaFoco by remember { mutableStateOf(false) }
    Column(modifier) {
        EtiquetaCampo(etiqueta)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { f ->
                    if (teniaFoco && !f.isFocused) alTerminar?.invoke()
                    teniaFoco = f.isFocused
                },
            keyboardActions = KeyboardActions(onDone = {
                if (alTerminar != null) foco.clearFocus() else defaultKeyboardAction(ImeAction.Done)
            }),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            suffix = unidad?.let { { Text(it, style = MaterialTheme.typography.bodyLarge) } },
            supportingText = error?.let { { Text(it, color = Rojo) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numerico) KeyboardType.Decimal else KeyboardType.Text,
                capitalization = if (numerico) KeyboardCapitalization.None else KeyboardCapitalization.Sentences,
                imeAction = imeAction,
            ),
            colors = coloresCampo(),
        )
    }
}

/** Buscador con lupa (fondo blanco porque va sobre el fondo de la pantalla). */
@Composable
fun CampoBusqueda(
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Pollo, arroz, yogur…",
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(painterResource(R.drawable.ic_buscar), contentDescription = null, tint = TextoSecundario)
        },
        trailingIcon = if (valor.isNotEmpty()) {
            {
                IconButton(onClick = { onCambio("") }) {
                    Icon(painterResource(R.drawable.ic_cerrar), contentDescription = "Borrar búsqueda", tint = TextoSecundario)
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, capitalization = KeyboardCapitalization.Sentences),
        colors = coloresCampo(fondo = Tarjeta),
    )
}
