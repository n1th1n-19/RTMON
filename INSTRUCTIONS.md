# RTMON Build Instructions

This guide provides detailed instructions for building, installing, and customizing the RTMON system monitoring widget.

---

## Prerequisites

### Required Software
1. **Node.js** (v16 or higher) - [Download](https://nodejs.org)
2. **Android Studio** with Android SDK - [Download](https://developer.android.com/studio)
3. **Java Development Kit (JDK)** 11 or higher
4. **Git** (optional, for cloning repository)

### Required Tools
- **npm** (comes with Node.js)
- **Expo CLI** (will be installed via npm)
- **Android SDK Platform Tools** (adb)
- **Gradle** (comes with Android project)

---

## Quick Start

### Method 1: Install Pre-built APK (Recommended)

1. Download latest `app-release.apk` from [Releases](https://github.com/n1th1n-19/RTMON/releases)
2. Transfer to your Android device
3. Enable "Install from Unknown Sources" if prompted
4. Install the APK
5. Add widget to home screen

### Method 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/n1th1n-19/RTMON.git
cd RTMON

# Install dependencies
npm install

# Build for Android
cd android
./gradlew assembleRelease

# APK will be at: android/app/build/outputs/apk/release/app-release.apk
```

---

## Detailed Build Instructions

### Step 1: Environment Setup

#### Windows
```bash
# Set Android SDK path
setx ANDROID_HOME "C:\Users\YourUsername\AppData\Local\Android\Sdk"

# Set Java path
setx JAVA_HOME "C:\Program Files\Java\jdk-11.0.x"

# Add to PATH
setx PATH "%PATH%;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools"
```

#### macOS/Linux
```bash
# Add to ~/.bashrc or ~/.zshrc
export ANDROID_HOME=$HOME/Library/Android/sdk
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.x.jdk/Contents/Home
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Reload shell configuration
source ~/.bashrc  # or source ~/.zshrc
```

### Step 2: Verify Installation

```bash
# Check Node.js
node --version  # Should be v16+

# Check npm
npm --version

# Check Java
java --version  # Should be 11+

# Check Android SDK
adb version

# List connected devices
adb devices
```

### Step 3: Clone and Install

```bash
# Clone repository
git clone https://github.com/n1th1n-19/RTMON.git
cd RTMON

# Install Node dependencies
npm install

# Verify package.json
cat package.json
```

### Step 4: Configure Android SDK

```bash
# Create local.properties (if not exists)
echo "sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk" > android/local.properties

# Or for macOS/Linux
echo "sdk.dir=/Users/YourUsername/Library/Android/sdk" > android/local.properties
```

### Step 5: Build the Project

#### Debug Build
```bash
cd android
./gradlew assembleDebug
cd ..
```
Output: `android/app/build/outputs/apk/debug/app-debug.apk` (~134MB)

#### Release Build
```bash
cd android
./gradlew assembleRelease
cd ..
```
Output: `android/app/build/outputs/apk/release/app-release.apk` (~60MB)

---

## Development Mode

### Using Expo CLI

```bash
# Start Metro bundler
npx expo start

# Run on Android (in another terminal)
npx expo run:android
```

### Direct Android Build and Install

```bash
# Build and install debug APK
cd android
./gradlew installDebug
cd ..

# View logs
adb logcat | grep SystemStatsWidget
```

---

## Customization Guide

### 1. Change Update Frequency

Edit `android/app/src/main/java/com/nothing/systemwidget/SystemStatsWidget.kt`:

```kotlin
// Line 67
alarmManager.setRepeating(
    AlarmManager.ELAPSED_REALTIME,
    SystemClock.elapsedRealtime(),
    3000L, // Change this value (milliseconds)
    pendingIntent
)
```

Recommended values:
- Fast: 1000L (1 second) - Higher battery drain (~4-5%/hour)
- Balanced: 3000L (3 seconds) - Current default (~1.5-2%/hour)
- Slow: 5000L (5 seconds) - Best battery life (~1%/hour)

### 2. Customize Theme Colors

#### Light Theme
Edit `android/app/src/main/res/values/colors.xml`:
```xml
<color name="widget_background">#F5F5F5</color>
<color name="widget_label_text">#000000</color>
<color name="widget_graph_background">#FFFFFF</color>
<color name="widget_grid">#E0E0E0</color>
```

#### Dark Theme
Edit `android/app/src/main/res/values-night/colors.xml`:
```xml
<color name="widget_background">#25292E</color>
<color name="widget_label_text">#FFFFFF</color>
<color name="widget_graph_background">#25292E</color>
<color name="widget_grid">#1A1A1A</color>
```

### 3. Change Graph Colors

Edit `SystemStatsWidget.kt` (lines 105-142):
```kotlin
private val redLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#FF0000")  // Change CPU color
    strokeWidth = 4f  // Adjust line thickness
}

private val greenLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#00FF00")  // Change Memory color
    strokeWidth = 4f
}

private val blueLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#0099FF")  // Change Battery/Network color
    strokeWidth = 4f
}
```

### 4. Adjust Graph History

Edit `SystemStatsWidget.kt` (line 101):
```kotlin
private const val MAX_DATA_POINTS = 20  // 20 points × 3s = 60 seconds of history
// Change to 40 for 2 minutes of history
// Change to 100 for 5 minutes of history
```

### 5. Modify Widget Padding

Edit `android/app/src/main/res/layout/widget_system_stats.xml` (line 7):
```xml
android:padding="16dp"  <!-- Change padding size -->
```

---

## Troubleshooting

### Build Errors

#### Gradle Build Failed
```bash
# Clean build
cd android
./gradlew clean
cd ..

# Regenerate project
npx expo prebuild --platform android --clean
```

#### Missing SDK
```bash
# Install required SDK components via Android Studio
# Or use sdkmanager:
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
```

#### Permission Denied (Linux/Mac)
```bash
# Make gradlew executable
chmod +x android/gradlew
```

### Widget Issues

#### Widget Not Appearing
1. Check AndroidManifest.xml for widget receiver
2. Verify widget layout files exist
3. Reinstall app
4. Restart device

#### Stats Not Updating
1. Disable battery optimization for RTMON
   - Settings → Apps → RTMON → Battery → Unrestricted
2. Check background permissions
3. View logs: `adb logcat | grep SystemStatsWidget`

#### Theme Not Switching
1. Requires Android 10+
2. Change system theme: Settings → Display → Dark mode
3. Restart widget or app
4. Check color resource files exist

---

## Building for Release

### Step 1: Generate Signing Key

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore rtmon-release.keystore \
  -alias rtmon-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Step 2: Configure Signing

Create `android/keystore.properties`:
```properties
storeFile=rtmon-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=rtmon-key
keyPassword=YOUR_KEY_PASSWORD
```

Add to `android/app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file('../keystore.properties')
            // ... configuration
        }
    }
}
```

### Step 3: Build Signed Release

```bash
cd android
./gradlew assembleRelease
cd ..
```

---

## Testing

### Install on Device

```bash
# Via ADB
adb install android/app/build/outputs/apk/debug/app-debug.apk

# Or release
adb install android/app/build/outputs/apk/release/app-release.apk
```

### Add Widget to Home Screen

1. Long-press on home screen
2. Tap "Widgets"
3. Find "RTMON"
4. Drag to home screen
5. Widget starts updating immediately

### Monitor Widget Logs

```bash
# All widget logs
adb logcat | grep SystemStatsWidget

# Errors only
adb logcat *:E | grep SystemStatsWidget

# Continuous monitoring
adb logcat -v time | grep "SystemStatsWidget\|RTMON"
```

---

## Performance Optimization

### Reduce Battery Drain

1. Increase update interval (3s → 5s) - Currently optimized at 3s
2. Reduce graph data points (20 → 10) - Less history
3. Screen-off detection is already implemented
4. Bitmap caching already enabled (50% battery improvement)

### Memory Usage

Current optimizations already in place:
1. Bitmap caching - Reuses bitmaps (~90KB saved per update)
2. Paint objects cached - No runtime allocation
3. Theme colors cached - Reduces resource lookups
4. SharedPreferences writes batched - Single transaction
5. Reduced data points (20 vs 30) - 33% memory savings

### Improve Responsiveness

1. Decrease update interval (3s → 1s) - Higher battery drain (~4-5%/hour)
2. Increase graph data points (20 → 40) - More history, higher memory usage

---

## Advanced Configuration

### Multiple Widget Instances

Each widget instance maintains its own update cycle. No additional configuration needed.

### Custom Metrics

To add new metrics (e.g., storage, temperature):
1. Add measurement function in `SystemStatsWidget.kt`
2. Add new graph section in `widget_system_stats.xml`
3. Add new Paint objects for graph colors
4. Update `updateAppWidget()` to include new metric

### Export/Import Settings

Currently not supported. Settings are hardcoded. Future versions will include settings app.

---

## Debugging Tips

### Enable Debug Mode

```bash
# React Native debug
npx react-native log-android

# Expo debug
npx expo start --dev-client
```

### Inspect Widget Rendering

```bash
# Dump widget info
adb shell dumpsys appwidget

# Check memory usage
adb shell dumpsys meminfo com.nothing.systemwidget

# Monitor CPU usage
adb shell top -m 10 | grep systemwidget
```

### View System Resources

```bash
# Battery stats
adb shell dumpsys battery

# CPU info
adb shell cat /proc/stat

# Memory info
adb shell cat /proc/meminfo
```

---

## Publishing

### Google Play Store

1. Build signed AAB:
```bash
cd android
./gradlew bundleRelease
```

2. Upload to Play Console:
   - File: `android/app/build/outputs/bundle/release/app-release.aab`
   - Fill in store listing
   - Set up content rating
   - Submit for review

### Direct Distribution

1. Build signed APK (see Building for Release)
2. Distribute APK file directly
3. Users must enable "Install from Unknown Sources"

---

## Quick Reference

### Common Commands

```bash
# Install dependencies
npm install

# Clean build
cd android && ./gradlew clean && cd ..

# Build debug
cd android && ./gradlew assembleDebug && cd ..

# Build release
cd android && ./gradlew assembleRelease && cd ..

# Install debug
adb install android/app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep SystemStatsWidget

# Uninstall
adb uninstall com.nothing.systemwidget
```

### File Locations

- **Widget Logic:** `android/app/src/main/java/com/nothing/systemwidget/SystemStatsWidget.kt`
- **Widget Layout:** `android/app/src/main/res/layout/widget_system_stats.xml`
- **Colors:** `android/app/src/main/res/values/colors.xml`
- **Dark Theme Colors:** `android/app/src/main/res/values-night/colors.xml`
- **Background Shape:** `android/app/src/main/res/drawable/widget_background.xml`
- **Manifest:** `android/app/src/main/AndroidManifest.xml`

---

## Getting Help

- **GitHub Issues:** [Report bugs](https://github.com/n1th1n-19/RTMON/issues)
- **GitHub Discussions:** [Ask questions](https://github.com/n1th1n-19/RTMON/discussions)
- **Documentation:** [README.md](README.md)
- **Roadmap:** [ROADMAP.md](ROADMAP.md)

---

## Version Information

- **Current Version:** v2.2.0
- **Min Android:** 6.0 (API 23)
- **Target Android:** 14 (API 34)
- **Package:** com.nothing.systemwidget
- **Battery Efficiency:** ~1.5-2% per hour (50% improvement from v2.1.0)

---

**Happy Building! 🚀**
