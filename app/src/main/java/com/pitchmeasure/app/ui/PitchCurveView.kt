package com.pitchmeasure.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pitchmeasure.app.ui.theme.Blue400
import com.pitchmeasure.app.ui.theme.GrayGrid
import kotlin.math.log10

@Composable
fun PitchCurveView(
    frequencies: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines (5 lines)
        for (i in 1..4) {
            val y = height * i / 5f
            drawLine(
                color = GrayGrid,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        if (frequencies.size < 2) return@Canvas

        val minFreq = 80f
        val maxFreq = 1200f
        val logRange = log10(maxFreq / minFreq)

        val path = Path()
        frequencies.forEachIndexed { index, freq ->
            val clamped = freq.coerceIn(minFreq, maxFreq)
            val x = (index.toFloat() / (frequencies.size - 1)) * width
            val normalized = log10(clamped / minFreq) / logRange
            val y = height - (normalized * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Blue400,
            style = Stroke(width = 3f)
        )

        // Highlight last data point
        val lastFreq = frequencies.last().coerceIn(minFreq, maxFreq)
        val lastX = width
        val lastNormalized = log10(lastFreq / minFreq) / logRange
        val lastY = height - (lastNormalized * height)
        drawCircle(
            color = Color(0xFF1565C0),
            radius = 6f,
            center = Offset(lastX, lastY)
        )
    }
}
