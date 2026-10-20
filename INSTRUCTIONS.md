# RTMON Build Instructions

---

## Prerequisites

- **Android Studio** with Android SDK — [Download](https://developer.android.com/studio)
- **JDK 11+** (bundled with Android Studio, or install separately)
- **Git** (to clone the repo)
- **ADB** (included with Android SDK Platform Tools)

---

## Quick Start

### Option 1: Install Pre-built APK

1. Download the latest `app-release.apk` from [Releases](https://github.com/n1th1n-19/RTMON/releases)
2. Transfer to your Android device
3. Enable "Install from Unknown Sources" if prompted
4. Install and add the widget to your home screen

### Option 2: Build from Source

```bash
git clone https://github.com/n1th1n-19/RTMON.git
cd RTMON/android
./gradlew assembleRelease
```

Output: `android/app/build/outputs/apk/release/app-release.apk`

---

## Environment Setup

### Set ANDROID_HOME

**Windows:**
```bash
setx ANDROID_HOME "C:\Users\YourUsername\AppData\Local\Android\Sdk"
setx PATH "%PATH%;%ANDROID_HOME%\platform-tools"
```

**macOS/Linux:**
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### Create `android/local.properties`

```bash
# Windows
echo "sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk" > android/local.properties

# macOS/Linux
echo "sdk.dir=/Users/YourUsername/Library/Android/sdk" > android/local.properties
```

### Verify setup

```bash
java --version   # Should be 11+
adb version
adb devices
```

---

## Building

### Debug APK

```bash
cd android
./gradlew assembleDebug
```

Output: `android/app/build/outputs/apk/debug/app-debug.apk`

### Release APK

```bash
cd android
./gradlew assembleRelease
```

Output: `android/app/build/outputs/apk/release/app-release.apk`

### Clean build

```bash
cd android
./gradlew clean assembleRelease
```

### Install directly to connected device

```bash
cd android
./gradlew installDebug
```

---

## Adding the Widget

1. Long-press your home screen
2. Tap **Widgets**
3. Find **RTMON** and drag it onto your home screen

---

## Customization

### Update interval

Edit `REFRESH_MS` in `SystemStatsWidget.kt`:

```kotlin
private const val REFRESH_MS = 3000L  // milliseconds — 1000 = 1s, 3000 = 3s (default), 5000 = 5s
```

Battery impact: ~4-5%/hr at 1s, ~1.5-2%/hr at 3s, ~1%/hr at 5s

### Graph history length

```kotlin
private const val MAX_DATA_POINTS = 20  // 20 × 3s = 60 seconds
```

### Theme colors

**Light** — `android/app/src/main/res/values/colors.xml`:

```xml
<color name="widget_background">#E8E8E8</color>
<color name="widget_label_text">#000000</color>
<color name="widget_graph_background">#FFFFFF</color>
<color name="widget_grid">#D0D0D0</color>
```

**Dark** — `android/app/src/main/res/values-night/colors.xml`:

```xml
<color name="widget_background">#1A1A1A</color>
<color name="widget_label_text">#FFFFFF</color>
<color name="widget_graph_background">#0D0D0D</color>
<color name="widget_grid">#050505</color>
```

### Graph colors

Edit Paint objects in `SystemStatsWidget.kt`:

```kotlin
color = Color.parseColor("#FF0000")  // CPU (red)
color = Color.parseColor("#00FF00")  // RAM (green)
color = Color.parseColor("#0099FF")  // Battery (blue)
color = Color.parseColor("#FFCC00")  // Network (yellow)
```

---

## Building for Release (Signed)

### Generate a keystore

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore rtmon-release.keystore \
  -alias rtmon-key \
  -keyalg RSA -keysize 2048 -validity 10000
```

### Configure signing in `android/app/build.gradle`

```groovy
signingConfigs {
    release {
        storeFile file('rtmon-release.keystore')
        storePassword 'YOUR_STORE_PASSWORD'
        keyAlias 'rtmon-key'
        keyPassword 'YOUR_KEY_PASSWORD'
    }
}
buildTypes {
    release {
        signingConfig signingConfigs.release
    }
}
```

### Build signed release

```bash
cd android
./gradlew assembleRelease
```

### Build AAB for Play Store

```bash
cd android
./gradlew bundleRelease
```

Output: `android/app/build/outputs/bundle/release/app-release.aab`

---

## Troubleshooting

### Gradle build failed

```bash
cd android
./gradlew clean
./gradlew assembleDebug --stacktrace
```

### Missing SDK components

```bash
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
```

### Permission denied (Linux/macOS)

```bash
chmod +x android/gradlew
```

### Stats not updating

Disable battery optimization: Settings → Apps → RTMON → Battery → Unrestricted

View logs:

```bash
adb logcat | grep SystemStatsWidget
adb logcat *:E | grep SystemStatsWidget
```

### Widget not appearing

1. Reinstall the APK
2. Restart the device
3. Check `adb shell dumpsys appwidget` for receiver registration

### Theme not switching

Requires Android 10+. After changing system theme, remove and re-add the widget.

---

## Debugging

```bash
# Widget logs
adb logcat -v time | grep "SystemStatsWidget\|RTMON"

# Memory usage
adb shell dumpsys meminfo com.nothing.systemwidget

# CPU usage
adb shell top -m 10 | grep systemwidget

# Widget host info
adb shell dumpsys appwidget

# Battery stats
adb shell dumpsys battery

# Uninstall
adb uninstall com.nothing.systemwidget
```

---

## Key Files

| File | Purpose |
| --- | --- |
| `android/app/src/main/java/com/nothing/systemwidget/SystemStatsWidget.kt` | All widget logic |
| `android/app/src/main/res/layout/widget_system_stats.xml` | Widget UI layout |
| `android/app/src/main/res/values/colors.xml` | Light theme colors |
| `android/app/src/main/res/values-night/colors.xml` | Dark theme colors |
| `android/app/src/main/res/drawable/widget_background.xml` | Rounded background shape |
| `android/app/src/main/AndroidManifest.xml` | Permissions and receivers |

---

## Version Info

- **Version:** v2.2.0
- **Min Android:** 6.0 (API 23)
- **Target Android:** 14 (API 34)
- **Package:** `com.nothing.systemwidget`
