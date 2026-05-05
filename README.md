# 音高测量 (PitchMeasure)

一款 Android 音高检测应用，通过手机麦克风实时检测声音音高，显示音名、频率、音准偏差和历史曲线。

## 功能

- 实时音高检测（基于 YIN 算法）
- 大字体显示当前音名（如 A4）和频率
- 弧形偏差指示器（音准/接近/偏差，颜色区分）
- 音高历史曲线图（最近 10 秒）

## 技术栈

- Kotlin + Jetpack Compose
- Material Design 3
- TarsosDSP（YIN 音高检测算法）
- 最低支持 Android 8.0（API 26）

## 构建与运行

1. 克隆仓库并安装 [Android Studio](https://developer.android.com/studio)
2. 打开项目：File → Open → 选择项目目录
3. 等待 Gradle 同步完成
4. 连接手机或启动模拟器，点击 Run

首次构建需要下载依赖，可能需要几分钟。

## 项目结构

```
app/src/main/java/com/pitchmeasure/app/
├── MainActivity.kt          # 入口，权限处理
├── audio/PitchDetector.kt   # 麦克风采集 + YIN 音高分析
├── model/PitchUtils.kt      # 频率↔音名换算
├── ui/
│   ├── PitchScreen.kt       # 主界面
│   ├── PitchCurveView.kt    # 音高曲线图
│   └── theme/               # Material 3 主题
└── viewmodel/PitchViewModel.kt  # 状态管理
```

## 许可证

MIT
