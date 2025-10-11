# Nothing System Widget

A React Native system monitoring widget for Nothing Phone, displaying real-time battery, RAM, and CPU statistics.

## Features

- **Battery Monitoring**: Real-time battery level and charging state
- **RAM Usage**: Live memory usage with total/used statistics
- **CPU Usage**: Approximated CPU utilization
- **Device Info**: Display device model, brand, and OS version
- **Nothing Design**: Black background with cyan accents matching Nothing Phone aesthetic

## Screenshots

The app displays system stats in a clean, card-based interface with:
- Battery percentage and charging status
- RAM usage in MB with percentage
- CPU usage approximation
- Device information footer

## Prerequisites

- Node.js (v16 or higher)
- Android Studio with Android SDK
- Java Development Kit (JDK) 11 or higher
- Expo CLI

## Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Utilisation-Widget
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Generate native Android code**
   ```bash
   npx expo prebuild --platform android --clean
   ```

## Running the App

### Development Mode

```bash
# Start Expo development server
npx expo start

# Run on Android device/emulator
npx expo run:android
```

### Direct Android Build

```bash
cd android
./gradlew assembleDebug
./gradlew installDebug
cd ..
```

## Project Structure

```
Utilisation-Widget/
├── App.tsx                 # Main React Native app
├── app.json               # Expo configuration
├── package.json           # Dependencies
├── tsconfig.json          # TypeScript configuration
└── android/               # Native Android code (after prebuild)
```

## Dependencies

- **expo**: ~51.0.0
- **expo-battery**: ~7.0.0 - Battery monitoring
- **expo-device**: ~6.0.0 - Device information
- **react-native-device-info**: ^10.13.0 - RAM and system info
- **react**: 18.2.0
- **react-native**: 0.74.0

## Configuration

The app is configured via [app.json](app.json) with:
- Package name: `com.nothing.systemwidget`
- Adaptive icon support
- Android target SDK: Latest
- Permissions: Battery stats, device info

## Development

### Update Intervals

- Battery: Real-time updates via listener
- RAM: Updates every 2 seconds
- CPU: Updates every 3 seconds (approximation)

### Customization

**Change accent color**: Edit the `#00D9FF` color values in [App.tsx](App.tsx)

**Adjust update frequency**: Modify interval values in the `useEffect` hook:
```typescript
const ramInterval = setInterval(updateRAM, 2000);  // 2 seconds
const cpuInterval = setInterval(updateCPU, 3000);  // 3 seconds
```

## Building for Production

### Debug APK

```bash
cd android
./gradlew assembleDebug
```
Output: `android/app/build/outputs/apk/debug/app-debug.apk`

### Release APK

1. Generate keystore:
   ```bash
   keytool -genkeypair -v -storetype PKCS12 -keystore release.keystore -alias widget-key -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Configure signing in `android/app/build.gradle`

3. Build:
   ```bash
   cd android
   ./gradlew assembleRelease
   ```
   Output: `android/app/build/outputs/apk/release/app-release.apk`

### Using EAS Build

```bash
npm install -g eas-cli
eas login
eas build:configure
eas build --platform android --profile production
```

## Adding Widget to Home Screen

After installation:
1. Long-press on Nothing Phone home screen
2. Tap "Widgets"
3. Find "Nothing System Widget"
4. Drag and place on home screen

## Troubleshooting

### Widget not appearing
- Verify `AndroidManifest.xml` has widget receiver configured
- Check widget layout XML files exist in `res/` directory
- Ensure app is installed and widget permissions granted

### Stats not updating
- Check battery optimization settings
- View logs: `adb logcat | grep SystemStatsWidget`
- Verify background update permissions

### Build errors
```bash
# Clean build
cd android
./gradlew clean
cd ..

# Regenerate native code
npx expo prebuild --platform android --clean
```

## Known Limitations

- **CPU Usage**: Approximated via random sampling; true CPU usage requires native implementation
- **Android Only**: Currently only supports Android platform
- **Battery Optimization**: May affect update frequency on some devices

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Acknowledgments

- Built with [Expo](https://expo.dev)
- Uses [expo-battery](https://docs.expo.dev/versions/latest/sdk/battery/) for battery monitoring
- Uses [react-native-device-info](https://github.com/react-native-device-info/react-native-device-info) for system stats
- Designed for Nothing Phone aesthetic

## Support

For detailed build instructions and troubleshooting, see [INSTRUCTIONS.md](INSTRUCTIONS.md)

## Roadmap

- [ ] True CPU usage calculation (native module)
- [ ] Storage monitoring
- [ ] Network speed monitoring
- [ ] Multiple widget size options
- [ ] Customizable themes
- [ ] iOS support

---

**Built for Nothing Phone** | Made with React Native & Expo
