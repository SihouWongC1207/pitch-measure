# 音高测量 Android 应用 - 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个 Android 音高检测应用，通过麦克风实时检测音高并显示音名、偏差和曲线图。

**Architecture:** 单 Activity + Jetpack Compose UI，AudioRecord 采集音频，TarsosDSP YIN 算法提取音高，ViewModel 管理状态。5 个核心文件，精简分层。

**Tech Stack:** Kotlin, Jetpack Compose + Material 3, TarsosDSP (YIN), Android AudioRecord

---

## File Structure

| File | Responsibility |
|------|---------------|
| `settings.gradle.kts` | Gradle 项目配置，仓库源 |
| `build.gradle.kts` | 根级插件声明 |
| `gradle.properties` | Gradle/JVM 属性 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle wrapper 版本 |
| `app/build.gradle.kts` | 应用模块依赖、SDK 版本 |
| `app/src/main/AndroidManifest.xml` | 权限声明、Activity 注册 |
| `app/src/main/java/com/pitchmeasure/app/model/PitchUtils.kt` | PitchInfo 数据类 + 频率↔音名换算 |
| `app/src/main/java/com/pitchmeasure/app/audio/PitchDetector.kt` | 麦克风音频采集 + YIN 音高分析 |
| `app/src/main/java/com/pitchmeasure/app/viewmodel/PitchViewModel.kt` | UI 状态管理，连接音频层和 UI 层 |
| `app/src/main/java/com/pitchmeasure/app/ui/theme/Color.kt` | Material 3 颜色定义 |
| `app/src/main/java/com/pitchmeasure/app/ui/theme/Theme.kt` | Material 3 主题 |
| `app/src/main/java/com/pitchmeasure/app/ui/theme/Type.kt` | Material 3 排版 |
| `app/src/main/java/com/pitchmeasure/app/ui/PitchCurveView.kt` | 音高曲线图 Canvas 组件 |
| `app/src/main/java/com/pitchmeasure/app/ui/PitchScreen.kt` | 主界面（音名、偏差弧、曲线、按钮） |
| `app/src/main/java/com/pitchmeasure/app/MainActivity.kt` | 入口 Activity，权限处理 |
| `app/src/test/java/com/pitchmeasure/app/model/PitchUtilsTest.kt` | 音名换算单元测试 |
| `.gitignore` | Git 忽略规则 |

---

### Task 1: Create Project Scaffold

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `.gitignore`

- [ ] **Step 1: Create root config files**

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PitchMeasure"
include(":app")
```

`build.gradle.kts` (root):

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
}
```

`gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

`gradle/wrapper/gradle-wrapper.properties`:

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 2: Create app module build file**

`app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.pitchmeasure.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pitchmeasure.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")

    implementation("be.tarsos.dsp:core:2.5")

    testImplementation("junit:junit:4.13.2")
}
```

- [ ] **Step 3: Create AndroidManifest.xml**

`app/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application
        android:allowBackup="true"
        android:label="音高测量"
        android:supportsRtl="true"
        android:theme="@style/Theme.Material3.DayNight.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 4: Create .gitignore**

`.gitignore`:

```
*.iml
.gradle
/local.properties
.idea
.DS_Store
/build
/captures
*.apk
*.aab
/app/build
.superpowers/
```

- [ ] **Step 5: Create source directories**

```bash
mkdir -p app/src/main/java/com/pitchmeasure/app/model
mkdir -p app/src/main/java/com/pitchmeasure/app/audio
mkdir -p app/src/main/java/com/pitchmeasure/app/viewmodel
mkdir -p app/src/main/java/com/pitchmeasure/app/ui/theme
mkdir -p app/src/test/java/com/pitchmeasure/app/model
```

- [ ] **Step 6: Commit**

```bash
git init
git add .
git commit -m "chore: initialize Android project scaffold"
```

Note: The Gradle wrapper JAR (`gradle-wrapper.jar`) and scripts (`gradlew`, `gradlew.bat`) will be auto-generated when the project is first opened in Android Studio or by running `gradle wrapper`.

---

### Task 2: Create PitchUtils Model (TDD)

**Files:**
- Create: `app/src/test/java/com/pitchmeasure/app/model/PitchUtilsTest.kt`
- Create: `app/src/main/java/com/pitchmeasure/app/model/PitchUtils.kt`

- [ ] **Step 1: Write the failing test**

`app/src/test/java/com/pitchmeasure/app/model/PitchUtilsTest.kt`:

```kotlin
package com.pitchmeasure.app.model

import org.junit.Assert.*
import org.junit.Test

class PitchUtilsTest {

    @Test
    fun `A4 440Hz maps to note A4 with zero cents`() {
        val info = PitchUtils.frequencyToPitchInfo(440f)
        assertEquals("A", info.noteName)
        assertEquals(4, info.octave)
        assertEquals(0f, info.centsDeviation, 0.5f)
        assertEquals(440f, info.frequency, 0.1f)
    }

    @Test
    fun `middle C 261_63Hz maps to C4`() {
        val info = PitchUtils.frequencyToPitchInfo(261.63f)
        assertEquals("C", info.noteName)
        assertEquals(4, info.octave)
        assertTrue(Math.abs(info.centsDeviation) < 1f)
    }

    @Test
    fun `466_16Hz is A#4 with zero cents`() {
        val info = PitchUtils.frequencyToPitchInfo(466.16f)
        assertEquals("A#", info.noteName)
        assertEquals(4, info.octave)
        assertTrue(Math.abs(info.centsDeviation) < 2f)
    }

    @Test
    fun `442Hz is A4 with slight positive cents`() {
        val info = PitchUtils.frequencyToPitchInfo(442f)
        assertEquals("A", info.noteName)
        assertEquals(4, info.octave)
        assertTrue(info.centsDeviation > 0f)
        assertTrue(info.centsDeviation < 50f)
    }

    @Test
    fun `displayNote returns note name with octave`() {
        val info = PitchInfo(440f, "A", 4, 0f)
        assertEquals("A4", info.displayNote)
    }

    @Test
    fun `displayFrequency formats with one decimal`() {
        val info = PitchInfo(440f, "A", 4, 0f)
        assertTrue(info.displayFrequency.contains("440.0"))
    }

    @Test
    fun `frequencyToMidi returns 69 for 440Hz`() {
        assertEquals(69.0, PitchUtils.frequencyToMidi(440f), 0.01)
    }

    @Test
    fun `frequencyToMidi returns 60 for 261_63Hz`() {
        assertEquals(60.0, PitchUtils.frequencyToMidi(261.63f), 0.01)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd app && ../gradlew test --tests "com.pitchmeasure.app.model.PitchUtilsTest" 2>/dev/null || echo "Expected: compilation fails - PitchUtils not found"`

Expected: Compilation error — `PitchUtils` and `PitchInfo` don't exist yet.

- [ ] **Step 3: Write the implementation**

`app/src/main/java/com/pitchmeasure/app/model/PitchUtils.kt`:

```kotlin
package com.pitchmeasure.app.model

import kotlin.math.log2
import kotlin.math.roundToInt

data class PitchInfo(
    val frequency: Float,
    val noteName: String,
    val octave: Int,
    val centsDeviation: Float
) {
    val displayNote: String get() = "$noteName$octave"
    val displayFrequency: String get() = String.format("%.1f Hz", frequency)
    val displayCents: String get() = String.format("%+.0f 音分", centsDeviation)
}

object PitchUtils {

    private val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun frequencyToMidi(freq: Float): Double {
        return 69.0 + 12.0 * log2(freq / 440.0)
    }

    fun frequencyToPitchInfo(freq: Float): PitchInfo {
        val midi = frequencyToMidi(freq)
        val roundedMidi = midi.roundToInt()
        val cents = ((midi - roundedMidi) * 100).toFloat()
        val noteIndex = ((roundedMidi % 12) + 12) % 12
        val noteName = NOTE_NAMES[noteIndex]
        val octave = (roundedMidi / 12) - 1
        return PitchInfo(freq, noteName, octave, cents)
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd app && ../gradlew test --tests "com.pitchmeasure.app.model.PitchUtilsTest"`

Expected: All 8 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/pitchmeasure/app/model/PitchUtils.kt app/src/test/java/com/pitchmeasure/app/model/PitchUtilsTest.kt
git commit -m "feat: add PitchUtils model with frequency-to-note conversion and tests"
```

---

### Task 3: Create PitchDetector

**Files:**
- Create: `app/src/main/java/com/pitchmeasure/app/audio/PitchDetector.kt`

- [ ] **Step 1: Write PitchDetector**

`app/src/main/java/com/pitchmeasure/app/audio/PitchDetector.kt`:

```kotlin
package com.pitchmeasure.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import be.tarsos.dsp.pitch.Yin
import com.pitchmeasure.app.model.PitchInfo
import com.pitchmeasure.app.model.PitchUtils

class PitchDetector(
    private val onPitchDetected: (PitchInfo) -> Unit
) {
    private val sampleRate = 44100
    private val bufferSize = 2048
    private val yin = Yin(sampleRate.toFloat(), bufferSize)

    private var audioRecord: AudioRecord? = null
    private var thread: Thread? = null
    @Volatile
    private var isRunning = false

    fun start() {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBufferSize, bufferSize * 2)
        )

        audioRecord?.startRecording()
        isRunning = true

        thread = Thread {
            val shortBuffer = ShortArray(bufferSize)
            val floatBuffer = FloatArray(bufferSize)

            while (isRunning) {
                val read = audioRecord?.read(shortBuffer, 0, bufferSize) ?: break
                if (read <= 0) continue

                for (i in 0 until read) {
                    floatBuffer[i] = shortBuffer[i] / 32768f
                }

                val result = yin.getPitch(floatBuffer)
                if (result.pitched && result.probability > 0.8f && result.pitch > 0) {
                    val pitchInfo = PitchUtils.frequencyToPitchInfo(result.pitch)
                    onPitchDetected(pitchInfo)
                }
            }
        }.also { it.start() }
    }

    fun stop() {
        isRunning = false
        try {
            thread?.join(1000)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        thread = null
        try {
            audioRecord?.stop()
        } catch (_: IllegalStateException) {
            // already stopped or not initialized
        }
        audioRecord?.release()
        audioRecord = null
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd app && ../gradlew compileDebugKotlin`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/pitchmeasure/app/audio/PitchDetector.kt
git commit -m "feat: add PitchDetector with AudioRecord and TarsosDSP YIN analysis"
```

---

### Task 4: Create PitchViewModel

**Files:**
- Create: `app/src/main/java/com/pitchmeasure/app/viewmodel/PitchViewModel.kt`

- [ ] **Step 1: Write PitchViewModel**

`app/src/main/java/com/pitchmeasure/app/viewmodel/PitchViewModel.kt`:

```kotlin
package com.pitchmeasure.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pitchmeasure.app.audio.PitchDetector
import com.pitchmeasure.app.model.PitchInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PitchViewModel : ViewModel() {

    var pitchInfo by mutableStateOf<PitchInfo?>(null)
        private set

    var isDetecting by mutableStateOf(false)
        private set

    private var _curvePoints = mutableListOf<Float>()
    val curvePoints: List<Float> get() = _curvePoints.toList()

    private var detector: PitchDetector? = null

    fun startDetection() {
        if (isDetecting) return
        isDetecting = true
        _curvePoints.clear()

        detector = PitchDetector { info ->
            viewModelScope.launch(Dispatchers.Main) {
                pitchInfo = info
                _curvePoints.add(info.frequency)
                if (_curvePoints.size > 200) {
                    _curvePoints.removeAt(0)
                }
            }
        }
        detector?.start()
    }

    fun stopDetection() {
        if (!isDetecting) return
        isDetecting = false
        detector?.stop()
        detector = null
    }

    override fun onCleared() {
        super.onCleared()
        stopDetection()
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd app && ../gradlew compileDebugKotlin`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/pitchmeasure/app/viewmodel/PitchViewModel.kt
git commit -m "feat: add PitchViewModel for state management between audio and UI"
```

---

### Task 5: Create Material 3 Theme

**Files:**
- Create: `app/src/main/java/com/pitchmeasure/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/pitchmeasure/app/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/pitchmeasure/app/ui/theme/Type.kt`

- [ ] **Step 1: Create Color definitions**

`app/src/main/java/com/pitchmeasure/app/ui/theme/Color.kt`:

```kotlin
package com.pitchmeasure.app.ui.theme

import androidx.compose.ui.graphics.Color

val Blue500 = Color(0xFF1565C0)
val Blue400 = Color(0xFF42A5F5)
val Blue100 = Color(0xFFE3F2FD)
val Green500 = Color(0xFF4CAF50)
val Yellow500 = Color(0xFFFFC107)
val Red500 = Color(0xFFFF5722)
val GrayBackground = Color(0xFFFAFAFA)
val GrayLight = Color(0xFFE8E8E8)
val GrayGrid = Color(0xFFE0E0E0)
```

- [ ] **Step 2: Create Theme**

`app/src/main/java/com/pitchmeasure/app/ui/theme/Theme.kt`:

```kotlin
package com.pitchmeasure.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    secondary = Blue400,
    tertiary = Green500,
    background = GrayBackground,
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue400,
    secondary = Blue500,
    tertiary = Green500,
)

@Composable
fun PitchMeasureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 3: Create Typography**

`app/src/main/java/com/pitchmeasure/app/ui/theme/Type.kt`:

```kotlin
package com.pitchmeasure.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 80.sp,
        lineHeight = 88.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
```

- [ ] **Step 4: Verify compilation**

Run: `cd app && ../gradlew compileDebugKotlin`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/pitchmeasure/app/ui/theme/
git commit -m "feat: add Material 3 theme with blue color scheme"
```

---

### Task 6: Create PitchCurveView

**Files:**
- Create: `app/src/main/java/com/pitchmeasure/app/ui/PitchCurveView.kt`

- [ ] **Step 1: Write PitchCurveView**

`app/src/main/java/com/pitchmeasure/app/ui/PitchCurveView.kt`:

```kotlin
package com.pitchmeasure.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
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
```

- [ ] **Step 2: Verify compilation**

Run: `cd app && ../gradlew compileDebugKotlin`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/pitchmeasure/app/ui/PitchCurveView.kt
git commit -m "feat: add PitchCurveView canvas component for pitch history visualization"
```

---

### Task 7: Create PitchScreen and MainActivity

**Files:**
- Create: `app/src/main/java/com/pitchmeasure/app/ui/PitchScreen.kt`
- Create: `app/src/main/java/com/pitchmeasure/app/MainActivity.kt`

- [ ] **Step 1: Write PitchScreen with all UI components**

`app/src/main/java/com/pitchmeasure/app/ui/PitchScreen.kt`:

```kotlin
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val pitchInfo by remember { mutableStateOf(viewModel.pitchInfo) }
    val isDetecting by remember { mutableStateOf(viewModel.isDetecting) }
    val curvePoints = viewModel.curvePoints

    // Re-collect state on each recomposition
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
        val accuracyLabel = when {
            currentPitchInfo == null -> ""
            abs(currentPitchInfo.centsDeviation) < 10 -> "音准很好"
            abs(currentPitchInfo.centsDeviation) < 25 -> "接近准确"
            else -> "偏差较大"
        }
        if (accuracyLabel.isNotEmpty()) {
            val labelColor = when {
                abs(currentPitchInfo!!.centsDeviation) < 10 -> Green500
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

        // Background arc (full semicircle, 180°)
        drawArc(
            color = Color(0xFFE0E0E0),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Center tick mark (270° = bottom center of the semicircle)
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

        // Map deviation to angle: -1 = 180°, 0 = 270°, +1 = 360°
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
```

- [ ] **Step 2: Write MainActivity**

`app/src/main/java/com/pitchmeasure/app/MainActivity.kt`:

```kotlin
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
```

- [ ] **Step 3: Verify compilation**

Run: `cd app && ../gradlew compileDebugKotlin`

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/pitchmeasure/app/ui/PitchScreen.kt app/src/main/java/com/pitchmeasure/app/MainActivity.kt
git commit -m "feat: add PitchScreen UI and MainActivity with permission handling"
```

---

### Task 8: Build and Verify

- [ ] **Step 1: Run full project build**

Run: `cd app && ../gradlew assembleDebug`

Expected: BUILD SUCCESSFUL

Note: This requires the Gradle wrapper to be generated. Run `gradle wrapper` first if needed, or open the project in Android Studio which will auto-generate it.

- [ ] **Step 2: Run unit tests**

Run: `cd app && ../gradlew test`

Expected: All PitchUtilsTest tests PASS.

- [ ] **Step 3: Final commit (if any changes)**

```bash
git add -A
git commit -m "chore: finalize project build configuration"
```

---

## Self-Review

**Spec coverage:**
- Real-time pitch detection → Task 3 (PitchDetector with AudioRecord + YIN)
- Note name + frequency display → Task 7 (PitchScreen)
- Arc deviation indicator → Task 7 (ArcDeviationIndicator composable)
- Pitch curve graph → Task 6 (PitchCurveView)
- Material 3 bright theme → Task 5 (Theme/Color/Type)
- Microphone permission → Task 7 (MainActivity)
- Background lifecycle stop → Task 4 (PitchViewModel.onCleared)
- "未检测到声音" display → Task 7 (PitchScreen shows "等待检测..." when null)

**Placeholder scan:** No TBD/TODO/vague steps found. All steps contain complete code.

**Type consistency:** `PitchInfo` defined in Task 2 with `displayNote`, `displayFrequency`, `displayCents`, `centsDeviation` properties — all referenced consistently in Tasks 3, 4, and 7. `PitchUtils.frequencyToPitchInfo()` used in Tasks 3 and tested in Task 2. `PitchDetector` constructor `(PitchInfo) -> Unit` callback matches ViewModel usage in Task 4. `curvePoints: List<Float>` in Task 4 consumed by `PitchCurveView(frequencies: List<Float>)` in Task 6.
