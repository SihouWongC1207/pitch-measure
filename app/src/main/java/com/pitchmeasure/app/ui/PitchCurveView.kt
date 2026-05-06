package com.pitchmeasure.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.pitchmeasure.app.ui.theme.Blue400
import com.pitchmeasure.app.ui.theme.GrayGrid
import kotlin.math.log10

@Composable
fun PitchCurveView(
    frequencies: List<Float>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val minFreq = 80f
        val maxFreq = 1200f
        val logRange = log10(maxFreq / minFreq)

        // Y-axis note labels with grid lines
        val yLabels = listOf(
            "C3" to 130.81f,
            "G3" to 196.00f,
            "D4" to 293.66f,
            "A4" to 440.00f,
            "E5" to 659.26f
        )

        yLabels.forEach { (label, freq) ->
            val normalized = log10(freq / minFreq) / logRange
            val y = height - (normalized * height)
            drawLine(
                color = GrayGrid,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            val textLayout = textMeasurer.measure(
                label,
                style = TextStyle(color = Color.Gray, fontSize = 10.sp)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(4f, y - textLayout.size.height.toFloat() - 2f)
            )
        }

        if (frequencies.size < 2) return@Canvas

        val path = Path()
        frequencies.forEachIndexed { index, freq ->
            val x = (index.toFloat() / (frequencies.size - 1)) * width
            val y = if (freq <= 0f) {
                height
            } else {
                val clamped = freq.coerceIn(minFreq, maxFreq)
                val normalized = log10(clamped / minFreq) / logRange
                height - (normalized * height)
            }
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Blue400,
            style = Stroke(width = 3f)
        )

        // Highlight last data point
        val lastFreq = frequencies.last()
        val lastX = width
        val lastY = if (lastFreq <= 0f) {
            height
        } else {
            val clamped = lastFreq.coerceIn(minFreq, maxFreq)
            val lastNormalized = log10(clamped / minFreq) / logRange
            height - (lastNormalized * height)
        }
        drawCircle(
            color = Color(0xFF1565C0),
            radius = 6f,
            center = Offset(lastX, lastY)
        )
    }
}
