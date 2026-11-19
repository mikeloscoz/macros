package com.example.macros.ui.progreso

import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.macros.R
import com.example.macros.data.db.Peso
import com.example.macros.domain.Barra
import com.example.macros.ui.Espanol
import com.example.macros.ui.entero
import com.example.macros.ui.theme.Borde
import com.example.macros.ui.theme.Primario
import com.example.macros.ui.theme.Rojo
import com.example.macros.ui.theme.Tarjeta
import com.example.macros.ui.theme.Texto
import com.example.macros.ui.theme.TextoSecundario
import com.example.macros.ui.theme.VerdeClaro
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shape.dashedShape
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

@Composable
private fun fuenteInter(): Typeface {
    val context = LocalContext.current
    return remember { ResourcesCompat.getFont(context, R.font.inter_medium) ?: Typeface.DEFAULT }
}

/** Línea de peso con área suave debajo y un círculo en el último registro. Necesita 2 pesos o más. */
@Composable
fun GraficaPeso(pesos: List<Peso>, modifier: Modifier = Modifier) {
    val productor = remember { CartesianChartModelProducer() }
    val inicio = pesos.first().fecha.toEpochDay()
    val xs = pesos.map { (it.fecha.toEpochDay() - inicio).toDouble() }
    val ultimoX = xs.last()
    val minY = floor(pesos.minOf { it.kg } - 1)
    val maxY = ceil(pesos.maxOf { it.kg } + 1)

    LaunchedEffect(pesos) {
        productor.runTransaction { lineSeries { series(xs, pesos.map { it.kg }) } }
    }

    val circulo = LineCartesianLayer.point(
        rememberShapeComponent(fill(Tarjeta), CorneredShape.Pill, strokeFill = fill(Primario), strokeThickness = 2.5.dp),
        12.dp,
    )
    val puntos = remember(ultimoX, circulo) {
        object : LineCartesianLayer.PointProvider {
            override fun getPoint(entry: LineCartesianLayerModel.Entry, seriesIndex: Int, extraStore: ExtraStore) =
                if (entry.x == ultimoX) circulo else null

            override fun getLargestPoint(extraStore: ExtraStore) = circulo
        }
    }
    val linea = LineCartesianLayer.rememberLine(
        fill = remember { LineCartesianLayer.LineFill.single(fill(Primario)) },
        stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.5.dp),
        areaFill = remember(minY) { LineCartesianLayer.AreaFill.single(fill(VerdeClaro), splitY = { minY }) },
        pointProvider = puntos,
    )
    val formatoFecha = remember { DateTimeFormatter.ofPattern("d MMM", Espanol) }
    // Unas 3 etiquetas: inicio, mitad y final.
    val separacion = max(1, ceil(ultimoX / 2).toInt())

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(linea),
                rangeProvider = remember(minY, maxY) { CartesianLayerRangeProvider.fixed(minY = minY, maxY = maxY) },
            ),
            startAxis = VerticalAxis.rememberStart(
                line = null,
                label = null,
                tick = null,
                guideline = rememberAxisGuidelineComponent(fill(Borde), 1.dp, shape = Shape.Rectangle),
                itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 4 }) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = null,
                tick = null,
                guideline = null,
                label = rememberAxisLabelComponent(color = TextoSecundario, typeface = fuenteInter(), textSize = 12.sp),
                valueFormatter = remember(inicio) {
                    CartesianValueFormatter { _, x, _ ->
                        LocalDate.ofEpochDay(inicio + x.toLong()).format(formatoFecha).replace(".", "")
                    }
                },
                itemPlacer = remember(separacion) { HorizontalAxis.ItemPlacer.aligned(spacing = { separacion }) },
            ),
            getXStep = { 1.0 },
            layerPadding = { cartesianLayerPadding(scalableStart = 4.dp, scalableEnd = 8.dp) },
        ),
        modelProducer = productor,
        modifier = modifier.fillMaxWidth().height(180.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

/** Barras de kcal: verdes, o rojas si pasan del objetivo; con la cifra encima y la línea del objetivo. */
@Composable
fun GraficaKcal(barras: List<Barra>, objetivo: Double?, modifier: Modifier = Modifier) {
    val productor = remember { CartesianChartModelProducer() }
    val valores = barras.map { it.kcal ?: 0.0 }
    LaunchedEffect(valores) {
        productor.runTransaction { columnSeries { series(valores) } }
    }
    val maxY = max(valores.maxOrNull() ?: 0.0, objetivo ?: 0.0) * 1.18
    val grosor = when (barras.size) { in 0..4 -> 40.dp; in 5..7 -> 26.dp; else -> 15.dp }
    val forma = CorneredShape.rounded(allPercent = 20)
    val verde = rememberLineComponent(fill(Primario), grosor, forma)
    val rojo = rememberLineComponent(fill(Rojo), grosor, forma)
    val columnas = remember(verde, rojo, objetivo) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(entry: ColumnCartesianLayerModel.Entry, seriesIndex: Int, extraStore: ExtraStore): LineComponent =
                if (objetivo != null && entry.y > objetivo + 0.5) rojo else verde

            override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore) = verde
        }
    }
    val fuente = fuenteInter()
    val lineaObjetivo = rememberLineComponent(
        fill(TextoSecundario),
        1.dp,
        dashedShape(dashLength = 4.dp, gapLength = 3.dp),
    )
    val decoraciones = remember(objetivo, lineaObjetivo) {
        if (objetivo == null) emptyList() else listOf(HorizontalLine(y = { objetivo }, line = lineaObjetivo))
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = columnas,
                columnCollectionSpacing = if (barras.size > 7) 8.dp else 16.dp,
                dataLabel = rememberTextComponent(color = Texto, typeface = fuente, textSize = if (barras.size > 7) 9.sp else 12.sp),
                dataLabelValueFormatter = remember {
                    CartesianValueFormatter { _, y, _ -> if (y <= 0.0) "" else y.entero() }
                },
                rangeProvider = remember(maxY) { CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = max(maxY, 1.0)) },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = null,
                tick = null,
                guideline = null,
                label = rememberAxisLabelComponent(color = TextoSecundario, typeface = fuente, textSize = 12.sp),
                valueFormatter = remember(barras) {
                    CartesianValueFormatter { _, x, _ -> barras.getOrNull(x.toInt())?.etiqueta ?: "" }
                },
            ),
            decorations = decoraciones,
        ),
        modelProducer = productor,
        modifier = modifier.fillMaxWidth().height(200.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}
