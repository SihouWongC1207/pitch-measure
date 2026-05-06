# 音高测量 (PitchMeasure)

一款 Android 音高检测应用，通过手机麦克风实时检测声音音高，显示音名、频率和音高历史曲线。

## 功能

- 实时音高检测（基于 YIN 算法）
- 大字体显示当前音名（如 A4）和频率
- 音高历史曲线图（最近 10 秒，Y 轴带音名标签）
- 无声时曲线自动降至底部，音名显示重置
- 自定义应用图标

## 截图

```
┌──────────────────────┐
│                      │
│         A4           │  ← 音名（大字体）
│      440.0 Hz · +0   │  ← 频率 · 音分偏差
│                      │
│  ┌──────────────────┐│
│  │C6 ───────────────││
│  │E5 ───────────────││  ← 音高曲线
│  │A4 ────────●──────││     Y轴音名标签
│  │D4 ───────────────││
│  │G3 ───────────────││
│  │C3 ───────────────││
│  │ 音高曲线  最近10秒 ││
│  └──────────────────┘│
│                      │
│      [ 开始检测 ]     │
│                      │
└──────────────────────┘
```

## 技术栈

- Kotlin + Jetpack Compose
- Material Design 3
- TarsosDSP（YIN 音高检测算法）
- 最低支持 Android 8.0（API 26）

## 构建与运行

### 使用 Android Studio

1. 克隆仓库并安装 [Android Studio](https://developer.android.com/studio)
2. 打开项目：File → Open → 选择项目目录
3. 等待 Gradle 同步完成
4. 连接手机或启动模拟器，点击 Run

### 构建 APK

1. Android Studio 菜单：**Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. 构建完成后在 `app/build/outputs/apk/debug/app-debug.apk` 找到 APK 文件
3. 将 APK 传输到手机并安装

## 项目结构

```
app/src/main/java/com/pitchmeasure/app/
├── MainActivity.kt          # 入口，权限处理
├── audio/PitchDetector.kt   # 麦克风采集 + YIN 音高分析
├── model/PitchUtils.kt      # 频率↔音名换算
├── ui/
│   ├── PitchScreen.kt       # 主界面
│   ├── PitchCurveView.kt    # 音高曲线图（含Y轴音名标签）
│   └── theme/               # Material 3 主题
└── viewmodel/PitchViewModel.kt  # 状态管理

app/src/main/res/
├── drawable/
│   ├── ic_launcher_foreground.xml  # 图标前景（声波曲线）
│   └── ic_launcher_background.xml  # 图标背景
├── mipmap-anydpi-v26/
│   └── ic_launcher.xml             # 自适应图标
└── values/
    └── themes.xml                  # 应用主题
```

## 许可证

MIT
