package com.example.macros.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.macros.R
import com.example.macros.ui.entero
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Texto
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.theme.VerdeClaro

private val FondoBarra = Color(0xFFEEF2EF)
private val animacion = tween<Float>(durationMillis = 600)

/** Anillo de progreso (0..1) que empieza arriba y avanza en el sentido del reloj. */
@Composable
fun Anillo(
    progreso: Float,
    color: Color,
    modifier: Modifier = Modifier,
    grosor: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val animado by animateFloatAsState(progreso.coerceIn(0f, 1f), animacion, label = "anillo")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val trazo = grosor.toPx()
            val inset = trazo / 2
            val tamano = androidx.compose.ui.geometry.Size(size.width - trazo, size.height - trazo)
            val origen = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(VerdeClaro, 0f, 360f, false, origen, tamano, style = Stroke(trazo))
            if (animado > 0f) {
                drawArc(color, -90f, 360f * animado, false, origen, tamano, style = Stroke(trazo, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

/** Barra de un macro: "Proteínas   75 / 180 g", la barra y "Te quedan 105 g". */
@Composable
fun BarraMacro(nombre: String, consumido: Double, objetivo: Double, color: Color, modifier: Modifier = Modifier) {
    val restante = objetivo - consumido
    val pasado = restante < -0.5
    val progreso = if (objetivo > 0) (consumido / objetivo).toFloat() else if (consumido > 0) 1f else 0f
    val animado by animateFloatAsState(progreso.coerceIn(0f, 1f), animacion, label = "barra")

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Texto)) { append(consumido.entero()) }
                    withStyle(SpanStyle(color = TextoSecundario)) { append(" / ${objetivo.entero()} g") }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(FondoBarra),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animado)
                    .clip(CircleShape)
                    .background(if (pasado) Rojo else color),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (pasado) "Te has pasado ${(-restante).entero()} g" else "Te quedan ${maxOf(0.0, restante).entero()} g",
            style = MaterialTheme.typography.bodyMedium,
            color = if (pasado) Rojo else TextoSecundario,
        )
    }
}

/** Botón con borde discontinuo: "+ Añadir alimento". */
@Composable
fun BotonDiscontinuo(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(14.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(forma)
            .clickable(onClick = onClick)
            .drawBehind {
                val r = 14.dp.toPx()
                drawRoundRect(
                    color = Color(0xFFA9CDB8),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            },
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_mas), contentDescription = null, tint = Primario, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge, color = Primario)
    }
}
