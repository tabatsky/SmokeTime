package jatx.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit

data class BarChartItem(
    val value: Float,
    val label: String,
    val color: Color
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun JatxBarChart(
    modifier: Modifier,
    backgroundColor: Color,
    lineColor: Color,
    textColor: Color,
    textSize: TextUnit,
    maxValue: Float,
    valueStep: Float,
    items: List<BarChartItem>
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .background(backgroundColor)
    ) {
        val dW = size.width * 0.15f
        val dH = size.height * 0.15f

        val W = size.width - 2 * dW
        val H = size.height - 2 * dH

        val dOffset = Offset(x = dW * 1.5f, y = dH * 0.5f)

        drawLine(
            color = lineColor,
            start = Offset(x = 0f, y = 0f) + dOffset,
            end = Offset(x = 0f, y = H) + dOffset,
            strokeWidth = 2.0f
        )

        val style = TextStyle(
            fontSize = textSize,
            color = textColor
        )

        var currentLineValue = 0f
        while (currentLineValue <= maxValue) {
            val percentValue = currentLineValue / maxValue
            val y = H * (1 - percentValue)

            drawLine(
                color = lineColor,
                start = Offset(x = 0f, y = y) + dOffset,
                end = Offset(x = W, y = y) + dOffset,
                strokeWidth = 2.0f
            )

            val textToDraw = currentLineValue.toString()

            drawText(
                textMeasurer = textMeasurer,
                text = textToDraw,
                style = style,
                topLeft = Offset(x = -0.9f * dW, y = y) + dOffset
            )

            if (currentLineValue < maxValue) {
                currentLineValue += valueStep
            } else {
                break
            }
        }

        drawLine(
            color = lineColor,
            start = Offset(x = 0f, y = 0f) + dOffset,
            end = Offset(x = W, y = 0f) + dOffset,
            strokeWidth = 2.0f
        )

        val textToDraw = maxValue.toString()

        drawText(
            textMeasurer = textMeasurer,
            text = textToDraw,
            style = style,
            topLeft = Offset(x = -0.9f * dW, y = 0f) + dOffset
        )

        val count = items.count()
        val A = W / (2 * count + 1)

        items.forEachIndexed { index, item ->
            val x = A * (2 * index + 1)

            val percentValue = item.value / maxValue

            val y = H * (1 - percentValue)
            val h = H * percentValue

            val offset = Offset(x = x, y = y)
            val size = Size(width = A, height = h)

            drawRect(
                color = item.color,
                topLeft = offset + dOffset,
                size = size
            )

            val offsetText = Offset(x = x + A * 0.1f, y = H + dH)

            rotate(
                degrees = -90f,
                pivot = offsetText + dOffset
            ) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = item.label,
                    style = style,
                    topLeft = offsetText + dOffset
                )
            }
        }
    }
}