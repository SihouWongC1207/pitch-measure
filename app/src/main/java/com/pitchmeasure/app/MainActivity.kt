package com.pitchmeasure.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pitchmeasure.app.ui.PitchScreen
import com.pitchmeasure.app.ui.theme.PitchMeasureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PitchMeasureTheme {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

        if (hasPermission) {
            PitchScreen()
        } else {
            PermissionRequestScreen(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            )
        }
    }

    @Composable
    private fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "需要麦克风权限",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "音高检测需要使用麦克风来采集声音",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRequestPermission) {
                    Text("授予权限")
                }
            }
        }
    }
}
