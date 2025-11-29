package com.streamatico.polymarketviewer.ui.event_detail.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Legend
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.ui.event_detail.LegendLabelKey
import com.streamatico.polymarketviewer.ui.event_detail.TimeRange
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EventChartSection(
    chartModelProducer: CartesianChartModelProducer,
    event: EventDto,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeRanges = TimeRange.entries
        .filter { it != TimeRange.H6 }
        .toTypedArray()

    val lineColors = remember(event.markets.size) {
        (0 until event.markets.size).map { index ->
            Color.hsl(
                hue = (index * (360f / (event.markets.size.takeIf { it > 0 } ?: 1))) % 360f,
                saturation = 0.9f,
                lightness = 0.6f
            )
        }
    }

    val xAxisValueFormatter = remember(selectedRange) {
        CartesianValueFormatter { _: CartesianMeasuringContext, value: Double, _ ->
            val timestampSeconds = value.toLong()

            val instant = Instant.ofEpochSecond(timestampSeconds)
            val zoneId = ZoneId.systemDefault()
            val pattern = when (selectedRange) {
                TimeRange.H1, TimeRange.H6, TimeRange.D1 -> "HH:mm"
                TimeRange.W1, TimeRange.M1 -> "dd MMM"
                TimeRange.ALL -> "MMM uuuu"
            }
            val formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
            formatter.format(instant)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center
        ) {
            SingleChoiceSegmentedButtonRow {
                timeRanges
                    .forEachIndexed { index, range ->

                        val rangeName = when (range) {
                            TimeRange.H1 -> "1H"
                            TimeRange.H6 -> "6H"
                            TimeRange.D1 -> "1D"
                            TimeRange.W1 -> "1W"
                            TimeRange.M1 -> "1M"
                            TimeRange.ALL -> "ALL"
                        }

                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = timeRanges.size),
                            onClick = { onRangeSelected(range) },
                            selected = range == selectedRange
                        ) {
                            Text(rangeName)
                        }
                    }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        val lineLayer = rememberLineCartesianLayer(
            LineCartesianLayer.LineProvider.series(
                lineColors.map { color ->
                    LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(fill(color)))
                }
            ),
            rangeProvider = EventChartRangeProvider
        )

        val legend = if(event.markets.size > 1) {
            rememberEventChartLegend(
                lineColors = lineColors
            )
        } else null

        CartesianChartHost(
            modifier = Modifier.height(280.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            chart = rememberCartesianChart(
                lineLayer,
                marker = rememberChartMarker(),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = CartesianValueFormatter { _: CartesianMeasuringContext, value: Double, _ ->
                        "${value.toInt()}%"
                    }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = xAxisValueFormatter,
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.aligned()
                    }
                ),

                legend = legend,
            ),
            modelProducer = chartModelProducer,
            placeholder = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }
        )
    }
}

@Composable
private fun rememberEventChartLegend(
    lineColors: List<Color>
) : Legend<CartesianMeasuringContext, CartesianDrawingContext> {
    val legendItemLabelComponent = rememberTextComponent(vicoTheme.textColor)

    return rememberHorizontalLegend(
        items = { extraStore ->
            extraStore[LegendLabelKey]
                .toList()
                .sortedBy { it.order }
                .forEachIndexed { index, orderedLabel ->
                    add(
                        LegendItem(
                            shapeComponent(fill(lineColors[index]), CorneredShape.Pill),
                            legendItemLabelComponent,
                            orderedLabel.label,
                        )
                    )
                }
        },
        padding = insets(top = 16.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun EventChartSectionPreview() {
    EventChartSection(
        chartModelProducer = PreviewMocks.previewChartModelProducer,
        event = PreviewMocks.sampleEvent1,
        selectedRange = TimeRange.D1,
        onRangeSelected = {}
    )
}

