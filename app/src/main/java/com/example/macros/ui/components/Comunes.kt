package com.example.macros.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.FormaTarjeta
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.TextoSecundario

/** Margen lateral común de todas las pantallas. */
val MargenLateral = 16.dp

/** Cabecera grande de cada pantalla: título y subtítulo opcional. */
@Composable
fun CabeceraPantalla(titulo: String, subtitulo: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.padding(start = 4.dp, top = 24.dp, bottom = 12.dp)) {
        Text(titulo, style = MaterialTheme.typography.displaySmall)
        if (subtitulo != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitulo, style = MaterialTheme.typography.bodyLarge, color = TextoSecundario)
        }
    }
}

/** Tarjeta blanca con borde verde suave y esquinas de 20 dp. */
@Composable
fun TarjetaMacros(
    modifier: Modifier = Modifier,
    color: Color = Tarjeta,
    borde: Boolean = true,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        color = color,
        border = if (borde) BorderStroke(1.dp, Borde) else null,
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}
