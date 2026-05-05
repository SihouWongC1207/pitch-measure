package com.pitchmeasure.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pitchmeasure.app.ui.theme.Blue400
import com.pitchmeasure.app.ui.theme.Blue500
import com.pitchmeasure.app.ui.theme.Green500
import com.pitchmeasure.app.ui.theme.Red500
import com.pitchmeasure.app.ui.theme.Yellow500
import com.pitchmeasure.app.viewmodel.PitchViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.toRadians

@Composable
fun PitchScreen(
    viewModel: PitchViewModel = viewModel()
) {
    val currentPitchInfo = viewModel.pitchInfo
    val currentIsDetecting = viewModel.isDetecting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Note name
        Text(
            text = currentPitchInfo?.displayNote ?: "--",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Frequency + cents
        Text(
            text = if (currentPitchInfo != null) {
                "${currentPitchInfo.displayFrequency} · ${currentPitchInfo.displayCents}"
            } else {
                "等待检测..."
            },
            style = MaterialTheme.typography.headlineMedium,
            color = Blue400
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Arc deviation indicator
        ArcDeviationIndicator(
            centsDeviation = currentPitchInfo?.centsDeviation ?: 0f,
            modifier = Modifier.size(220.dp, 100.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Accuracy label
        if (currentPitchInfo != null) {
            val accuracyLabel = when {
                abs(currentPitchInfo.centsDeviation) < 10 -> "音准很好"
                abs(currentPitchInfo.centsDeviation) < 25 -> "接近准确"
                else -> "偏差较大"
            }
            val labelColor = when {
                abs(currentPitchInfo.centsDeviation) < 10 -> Green500
                abs(currentPitchInfo.centsDeviation) < 25 -> Yellow500
                else -> Red500
            }
            Text(
                text = accuracyLabel,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pitch curve
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "音高曲线",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "最近 10 秒",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                PitchCurveView(
                    frequencies = viewModel.curvePoints,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start/Stop button
        Button(
            onClick = {
                if (currentIsDetecting) viewModel.stopDetection()
                else viewModel.startDetection()
            },
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue500
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (currentIsDetecting) "停止检测" else "开始检测",
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ArcDeviationIndicator(
    centsDeviation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 8f
        val arcRadius = size.width * 0.38f
        val center = Offset(size.width / 2, size.height * 0.85f)

        // Background arc (full semicircle, 180 degrees)
        drawArc(
            color = Color(0xFFE0E0E0),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Center tick mark (270 degrees = bottom center of the semicircle)
        val tickLength = 12f
        val centerAngleRad = toRadians(270.0)
        drawLine(
            color = Color.DarkGray,
            start = Offset(
                center.x + (arcRadius - tickLength) * cos(centerAngleRad).toFloat(),
                center.y + (arcRadius - tickLength) * sin(centerAngleRad).toFloat()
            ),
            end = Offset(
                center.x + (arcRadius + tickLength) * cos(centerAngleRad).toFloat(),
                center.y + (arcRadius + tickLength) * sin(centerAngleRad).toFloat()
            ),
            strokeWidth = 2f
        )

        // Colored arc from center to indicator position
        val normalizedDeviation = (centsDeviation / 50f).coerceIn(-1f, 1f)
        val arcColor = when {
            abs(centsDeviation) < 10 -> Green500
            abs(centsDeviation) < 25 -> Yellow500
            else -> Red500
        }

        // Map deviation to angle: -1 = 180deg, 0 = 270deg, +1 = 360deg
        val indicatorAngle = 270f + normalizedDeviation * 90f

        if (normalizedDeviation >= 0) {
            drawArc(
                color = arcColor,
                startAngle = 270f,
                sweepAngle = normalizedDeviation * 90f,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        } else {
            drawArc(
                color = arcColor,
                startAngle = indicatorAngle,
                sweepAngle = -normalizedDeviation * 90f,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Indicator dot
        val dotAngleRad = toRadians(indicatorAngle.toDouble())
        drawCircle(
            color = arcColor,
            radius = 8f,
            center = Offset(
                center.x + arcRadius * cos(dotAngleRad).toFloat(),
                center.y + arcRadius * sin(dotAngleRad).toFloat()
            )
        )
    }
}
