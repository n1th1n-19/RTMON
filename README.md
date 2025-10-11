# RTMON – Real-Time System Monitor Widget

A sleek, real-time system monitoring widget for Android, designed with a **retro LED aesthetic**. RTMON displays live CPU, RAM, battery, and network statistics directly on your home screen with dynamic graphs.

---

## ✨ Features

* Real-Time CPU Monitoring (from `/proc/stat`)
* RAM Usage Tracking
* Battery Level Monitoring with historical graph
* Network Speed in KB/s and MB/s
* Live Graphs for all metrics
* LED Dot-Matrix Font
* Optimized performance
* Widget-only app (no launcher icon)
* Compact 3×3 Grid (180dp × 180dp)
* 1-second update interval

---

## 🛠 Technical Details

| Property          | Value                    |
| ----------------- | ------------------------ |
| Size              | 180dp × 180dp (3×3 grid) |
| Border Radius     | 28dp                     |
| Update Frequency  | 1 second                 |
| Graph Data Points | 30 (30-second history)   |
| Font              | LED Dot-Matrix 400       |
| Update Cycle Time | 30–40 ms                 |
| Memory Usage      | ~15 MB                   |
| CPU Impact        | 0.5–1%                   |
| Battery Drain     | <1% per hour (screen on) |

---

## 📦 Installation

### Option 1: Pre-built APK

Download the latest `app-release.apk` from [Releases](https://github.com/n1th1n-19/Utilisation-Widget/releases) and install.

### Option 2: Build from Source

```bash
git clone https://github.com/n1th1n-19/Utilisation-Widget.git
cd Utilisation-Widget
npm install
cd android
./gradlew assembleRelease
```

Output: `android/app/build/outputs/apk/release/app-release.apk`

### Option 3: Using EAS Build

```bash
npm install -g eas-cli
eas login
eas build --platform android --profile preview
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

* **Update Frequency**: Edit `SystemStatsWidget.kt`
* **Graph Data Points**: Modify `MAX_DATA_POINTS`
* **Colors**: Adjust line/fill colors in widget code

---

## 🚀 Building for Production

```bash
cd android
./gradlew assembleRelease
```

For signed APKs, generate a keystore and configure `build.gradle` before building.

---

## 🐛 Troubleshooting

* Check permissions and disable battery optimization if stats don’t update.
* Wait a few seconds for CPU readings to stabilize.
* Network speed shows total device traffic, not per-app.

---

## ⚠️ Known Limitations

* Android only (no iOS support)
* Network speed = total device traffic
* 30-second graph history
* 1-second minimum update interval

---

## 🧭 Roadmap

* [x] Real-time CPU usage
* [x] Network speed monitoring
* [ ] Storage monitoring
* [ ] Multiple widget sizes
* [ ] Theme customization

---

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch
3. Commit your changes
4. Push and open a PR

---

## 📜 License

MIT License

---

## 📡 Support

* [GitHub Issues](https://github.com/n1th1n-19/Utilisation-Widget/issues)
* [GitHub Discussions](https://github.com/n1th1n-19/Utilisation-Widget/discussions)

---

**Built for Android** ⚡ Powered by Kotlin & Expo 🟢 Optimized for Real-Time Performance
