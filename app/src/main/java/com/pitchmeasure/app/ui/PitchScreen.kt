package com.pitchmeasure.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pitchmeasure.app.ui.theme.Blue400
import com.pitchmeasure.app.ui.theme.Blue500
import com.pitchmeasure.app.viewmodel.PitchViewModel

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
        // Pitch info - centered in the space above the curve
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentPitchInfo?.displayNote ?: "--",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (currentPitchInfo != null) {
                        "${currentPitchInfo.displayFrequency} · ${currentPitchInfo.displayCents}"
                    } else {
                        "等待检测..."
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Blue400
                )
            }
        }

        // Pitch curve
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
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
