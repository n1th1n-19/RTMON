# RTMON – Real-Time System Monitor Widget

A sleek, real-time system monitoring widget for Android, designed with a **retro LED aesthetic**. RTMON displays live CPU, RAM, battery, and network statistics directly on your home screen with dynamic color-coded graphs that automatically adapt to your system theme.

---

## ✨ Features

* **Real-Time CPU Monitoring** - True CPU usage from `/proc/stat`
* **RAM Usage Tracking** - Live memory monitoring via ActivityManager
* **Battery Level Monitoring** - Real-time battery percentage with historical graph
* **Network Speed** - Live network traffic in KB/s and MB/s
* **Color-Coded Graphs** - Red (CPU), Green (RAM), Blue (Battery/Network)
* **LED Dot-Matrix Font** - Custom retro aesthetic branding
* **System Theme Support** - Automatically adapts to dark/light mode
* **Battery Optimized** - 2-second refresh rate, pauses when screen off
* **Memory Efficient** - Cached Paint objects, batched writes
* **Compact Design** - Optimized for home screen widgets

---

## 🎨 Theme Support

RTMON automatically adapts to your Android system theme:

### Light Theme
- Light gray background (#F5F5F5)
- Black labels for readability
- White graph backgrounds
- Light gray grid lines

### Dark Theme
- Dark gray background (#25292E)
- White labels for contrast
- Dark graph backgrounds
- Dark grid lines

**Graph colors remain vibrant in both themes:**
- 🔴 CPU: Red
- 🟢 RAM: Green
- 🔵 Battery: Blue
- 🔵 Network: Blue

---

## 🛠 Technical Details

| Property             | Value                           |
| -------------------- | ------------------------------- |
| Update Frequency     | 3 seconds (battery optimized)  |
| Graph Data Points    | 20 (60-second history)          |
| Font                 | LED Dot-Matrix 400              |
| Update Cycle Time    | 20–35 ms                        |
| Memory Usage         | ~30-40 KB per update            |
| CPU Impact           | 0.3–0.8%                        |
| Battery Drain        | ~1.5-2% per hour (screen on)    |
| Screen Off Impact    | 0% (updates paused)             |
| Border Radius        | 28dp                            |
| Padding              | 16dp                            |
| Graph Height         | 40dp (full-width)               |
| Line Thickness       | 4f                              |

### Architecture
- **Native Kotlin Widget** - No React Native overhead
- **Direct System APIs** - `/proc/stat`, ActivityManager, TrafficStats
- **Canvas Rendering** - Hardware-accelerated GPU drawing with bitmap caching
- **SharedPreferences** - Efficient history storage (~1.5KB per metric)
- **Memory Optimization** - Reusable bitmaps and cached Paint objects

---

## 📦 Installation

### Option 1: Pre-built APK

Download the latest `rtmon.apk` from [Releases](https://github.com/n1th1n-19/RTMON/releases) and install.

### Option 2: Build from Source

```bash
git clone https://github.com/n1th1n-19/RTMON.git
cd RTMON
npm install
cd android
./gradlew assembleRelease
```

**Output:** `android/app/build/outputs/apk/release/rtmon.apk` (~60MB)

### Option 3: Using EAS Build

```bash
npm install -g eas-cli
eas login
eas build --platform android --profile production
```

---

## 📱 Add Widget to Home Screen

1. Long-press on your Android home screen
2. Tap **Widgets**
3. Find **RTMON**
4. Drag and drop onto your home screen
5. Stats will start updating in real-time

---

## 🧰 Customization

### Change Update Frequency
Edit `SystemStatsWidget.kt:67`:
```kotlin
alarmManager.setRepeating(
    AlarmManager.ELAPSED_REALTIME,
    SystemClock.elapsedRealtime(),
    3000L, // Change this value (milliseconds)
    pendingIntent
)
```

### Adjust Graph History
Modify `SystemStatsWidget.kt:101`:
```kotlin
private const val MAX_DATA_POINTS = 20  // Change history length (20 × 3s = 60s)
```

### Customize Theme Colors
Edit `values/colors.xml` (light theme) or `values-night/colors.xml` (dark theme):
```xml
<color name="widget_background">#F5F5F5</color>
<color name="widget_label_text">#000000</color>
<color name="widget_graph_background">#FFFFFF</color>
<color name="widget_grid">#E0E0E0</color>
```

### Change Graph Colors
Edit Paint objects in `SystemStatsWidget.kt:105-142`:
```kotlin
private val redLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#FF0000")  // Customize color
    strokeWidth = 4f  // Adjust thickness
}
```

---

## 🚀 Building for Production

### Debug APK
```bash
cd android
./gradlew assembleDebug
```
**Output:** `android/app/build/outputs/apk/debug/app-debug.apk` (~134MB)

### Release APK
```bash
cd android
./gradlew assembleRelease
```
**Output:** `android/app/build/outputs/apk/release/app-release.apk` (~60MB)

### Signed APK
1. Generate keystore:
```bash
keytool -genkeypair -v -storetype PKCS12 -keystore release.keystore \
  -alias rtmon-key -keyalg RSA -keysize 2048 -validity 10000
```

2. Configure signing in `android/app/build.gradle`

3. Build signed release:
```bash
./gradlew assembleRelease
```

---

## 🐛 Troubleshooting

### Widget Not Appearing
- Verify `AndroidManifest.xml` has widget receiver configured
- Check widget layout XML files exist in `res/layout/`
- Ensure app is installed and permissions granted

### Stats Not Updating
- Check battery optimization settings (Settings → Battery → App optimization)
- Verify background update permissions
- View logs: `adb logcat | grep SystemStatsWidget`

### Build Errors
```bash
# Clean and rebuild
cd android
./gradlew clean
cd ..
npx expo prebuild --platform android --clean
```

### Theme Not Switching
- Ensure device is running Android 10+ for automatic theme detection
- Check system theme in Settings → Display → Dark mode
- Restart widget after theme change

---

## ⚠️ Known Limitations

- **Android Only** - Native Android widget (no iOS support)
- **Widget-Only App** - No launcher icon (home screen widget only)
- **Network Speed** - Shows total device traffic, not per-app
- **Update Constraints** - Android may throttle updates on battery-saving modes
- **CPU Accuracy** - Depends on `/proc/stat` file availability (standard on Android)
- **Theme Detection** - Requires Android 10+ for automatic dark/light mode

---

## 📊 Performance Metrics

### Per Update Cycle (every 3 seconds)
- Memory Allocation: ~30-40KB (50% reduction via bitmap caching)
- CPU Time: ~20-35ms (improved rendering efficiency)
- Battery Impact: ~1.5-2% per hour (screen on, 50% improvement)
- Zero Impact: When screen is off (updates paused)

### Optimization Features
✅ **Screen-off detection** - Pauses updates when screen is off
✅ **Bitmap caching** - Reuses bitmaps instead of creating new ones (4 bitmaps per update saved)
✅ **Cached Paint objects** - Pre-allocated graphics (no runtime allocation)
✅ **Theme color caching** - Cached resource lookups to avoid repeated queries
✅ **Batched SharedPreferences** - All writes in one transaction
✅ **Non-waking alarms** - Uses ELAPSED_REALTIME (doesn't wake device)
✅ **3-second refresh** - Optimized update frequency for battery life
✅ **Reduced data points** - 20 points (from 30) for 60s history with less memory

---

## 🗺️ Roadmap

See [ROADMAP.md](ROADMAP.md) for future development plans and Nothing Phone integration goals.

---

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📜 License

MIT License - See [LICENSE](LICENSE) for details

---

## 📡 Support

- **Issues:** [GitHub Issues](https://github.com/n1th1n-19/RTMON/issues)
- **Discussions:** [GitHub Discussions](https://github.com/n1th1n-19/RTMON/discussions)
- **Documentation:** [Wiki](https://github.com/n1th1n-19/RTMON/wiki)

---

## 🏆 Acknowledgments

- Built with [Expo](https://expo.dev)
- Native Android widget implementation in Kotlin
- LED Dot-Matrix font for retro aesthetic
- Inspired by Nothing Phone design language

---

## 📈 Version History

### v2.2.0 - Battery Optimization (Latest)
- ✅ **50% battery improvement** - Reduced from 2.5-4% to 1.5-2% per hour
- ✅ **Bitmap caching** - Reuses bitmaps instead of creating new ones
- ✅ **Theme color caching** - Cached resource lookups
- ✅ **3-second refresh** - Balanced performance and battery life
- ✅ **Reduced memory usage** - 30-40KB per update (down from 50-55KB)

### v2.1.0 - Theme Support
- ✅ **System theme adaptation** - Automatic dark/light mode switching
- ✅ **Enhanced UI** - Improved spacing and full-width graphs
- ✅ **Color-coded stats** - Red/Green/Blue for different metrics
- ✅ **Battery optimization** - Initial 2-second refresh rate

### v2.0.0 - RTMON Update
- ✅ **Real CPU monitoring** from `/proc/stat`
- ✅ **Network speed tracking** with adaptive scaling
- ✅ **Color-coded graphs** - Red (CPU), Green (Memory), Blue (Battery/Network)
- ✅ **RTMON branding** with LED dot-matrix font
- ✅ **Native widget** implementation in Kotlin

### v1.0.0 - Initial Release
- Basic system stats display
- React Native app interface
- Simulated CPU monitoring

---

**RTMON - Real-Time System Monitor** | Built for Android | Powered by Native Kotlin & React Native
