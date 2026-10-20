# RTMON

A real-time system monitoring widget for Android. Shows live CPU, RAM, battery, and network stats directly on your home screen — with color-coded graphs and a retro LED Dot-Matrix aesthetic.

![RTMON widget screenshot](assets/screenshots/shots.jpg)

---

## Features

- Live CPU, RAM, battery, and network speed — real readings only; shows `--` when a value can't be measured
- Color-coded line graphs with 60-second history (CPU red, RAM green, battery blue, network amber)
- LED Dot-Matrix font
- Automatic light and dark theme support
- 3-second refresh, paused while the screen is off
- Widget only — no app UI

---

## Install

**Pre-built APK** — grab `app-release.apk` from the [latest release](https://github.com/n1th1n-19/RTMON/releases/latest) and sideload it.

> **Upgrading from v1.0.0?** Uninstall it first. v1.0.0 was debug-signed; v1.0.1+ use the release key, and Android refuses updates signed with a different key.

**Build from source** — requires Android Studio and JDK 17:

```bash
git clone https://github.com/n1th1n-19/RTMON.git
cd RTMON/android
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

---

## Add to Home Screen

1. Long-press your home screen
2. Tap **Widgets**
3. Find **RTMON** and drag it on
4. **Android 12+:** tap the RTMON app icon once and allow **Alarms & reminders** — needed for the 3-second refresh

---

## What the numbers mean

| Stat | Source | Notes |
| --- | --- | --- |
| **RAM** | `ActivityManager.MemoryInfo` | Used ÷ total — same figure as Android's memory settings |
| **BAT** | `BatteryManager` | Charge level |
| **NET** | `TrafficStats` | Device-wide upload + download bytes/s (Wi-Fi + mobile). Graph scales to the busiest point in the last 60s (min 64 KB/s) |
| **CPU** (Android 7 and below) | `/proc/stat` | True utilisation |
| **CPU** (Android 8+) | `/sys/devices/system/cpu/*/cpufreq` | **Frequency load**: current clock ÷ max clock across cores. Android 8+ blocks `/proc/stat` for normal apps (only root/system apps can read real CPU usage), so this is the closest real signal. Tracks load, but isn't identical to it |

Values show `--` for about 3 seconds after the screen turns on — each rate needs two readings.

---

## Customization

All customization is done by editing source files and rebuilding.

**Refresh rate** — `SystemStatsWidget.kt`:

```kotlin
private const val REFRESH_MS = 3000L  // interval in milliseconds
```

**Graph history** — `SystemStatsWidget.kt`:

```kotlin
private const val MAX_DATA_POINTS = 20  // 20 points × 3s = 60s
```

**Colors** — `res/values/colors.xml` (light) and `res/values-night/colors.xml` (dark):

```xml
<color name="widget_background">#E8E8E8</color>
<color name="widget_graph_background">#FFFFFF</color>
<color name="widget_grid">#D0D0D0</color>
<color name="widget_label_text">#000000</color>
```

---

## Refresh & battery

| Condition | Refresh |
| --- | --- |
| Screen on, *Alarms & reminders* allowed (or Android 11 and below) | Every 3 seconds |
| Screen on, permission not granted | Every ~5–10 seconds (Android batches inexact alarms) |
| Screen off | Paused — alarms never wake the device |

---

## Releasing

- **New version:** Actions → *Build and release APK* → **Run workflow** (on `main`) → pick `patch` / `minor` / `major`. Tags the next version (e.g. `v1.0.1`), builds a signed APK and publishes it as the latest release.
- **Every merge to `main`:** publishes a pre-release dev build (`vX.Y.Z-dev.<n>`).
- **Pull requests:** built and linted, not released.
- `versionCode` is the commit count on `main`, so every build installs as an upgrade.
- Signing needs repo secrets `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` (PKCS12 keystore — key password = store password). The release fails rather than ship a debug-signed APK if they're missing.

---

## Troubleshooting

**CPU shows `--`** — the device exposes neither `/proc/stat` nor cpufreq to apps (common on emulators)

**Updates every ~5–10s instead of 3s** — tap the RTMON app icon and allow *Alarms & reminders*

**Stats frozen** — disable battery optimization: Settings → Apps → RTMON → Battery → Unrestricted

**"App not installed" when updating** — uninstall the old version first (see the v1.0.0 note under Install)

**Widget missing** — reinstall the APK, then re-add the widget

**Build failed** — clean and retry:

```bash
cd android
./gradlew clean assembleRelease
```

---

## Limitations

- Android only
- Network speed is device-wide, not per-app
- CPU on Android 8+ is frequency load, not true utilisation (see [What the numbers mean](#what-the-numbers-mean))
- Dark/light auto-switching requires Android 10+

---

## Roadmap

See [ROADMAP.md](ROADMAP.md) — Nothing Phone Glyph Interface integration is planned.

---

## License

MIT — see [LICENSE](LICENSE)
