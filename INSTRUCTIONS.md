# Building Nothing System Widget with Claude CLI in VS Code

This guide will help you build and run the Nothing System Widget project using Claude CLI directly in VS Code.

## Prerequisites

Before starting, ensure you have:

1. **VS Code** installed
2. **Node.js** (v16 or higher) and npm
3. **Claude CLI** installed and configured
4. **Android Studio** with Android SDK
5. **Java Development Kit (JDK)** 11 or higher
6. **Git** (optional, for version control)

## Part 1: Initial Setup

### Step 1: Install Claude CLI

If you haven't installed Claude CLI yet:

```bash
# Install Claude CLI globally
npm install -g @anthropic-ai/claude-cli

# Verify installation
claude --version
```

### Step 2: Configure Claude CLI

```bash
# Set your API key (get it from https://console.anthropic.com/)
claude config set apiKey YOUR_API_KEY_HERE

# Verify configuration
claude config list
```

### Step 3: Extract and Open Project

```bash
# Navigate to where you extracted the zip file
cd /path/to/nothing-widget

# Open in VS Code
code .
```

## Part 2: Install Dependencies

### Step 1: Open VS Code Terminal

In VS Code, press `` Ctrl + ` `` (or `Cmd + ` on Mac) to open the integrated terminal.

### Step 2: Install Node Dependencies

```bash
# Install all npm dependencies
npm install

# Install additional required dependencies
npm install expo-battery expo-device react-native-device-info expo-build-properties
```

### Step 3: Verify package.json

Use Claude CLI to verify all dependencies are correct:

```bash
claude "Check my package.json and ensure all dependencies for the Nothing widget are correctly installed"
```

## Part 3: Configure Android Environment

### Step 1: Set Environment Variables

**Windows:**
```bash
# Add these to your system environment variables
setx ANDROID_HOME "C:\Users\YourUsername\AppData\Local\Android\Sdk"
setx JAVA_HOME "C:\Program Files\Java\jdk-11.0.x"
```

**macOS/Linux:**
```bash
# Add these to your ~/.bashrc or ~/.zshrc
export ANDROID_HOME=$HOME/Library/Android/sdk
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.x.jdk/Contents/Home
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

Then reload:
```bash
source ~/.bashrc  # or source ~/.zshrc
```

### Step 2: Verify Android Setup

```bash
# Check Android SDK
adb version

# List available devices/emulators
adb devices
```

## Part 4: Generate Native Android Code

### Step 1: Prebuild the Project

```bash
# Generate native Android project files
npx expo prebuild --platform android --clean
```

This will create the full Android project structure in the `android/` directory.

### Step 2: Verify Android Files with Claude CLI

```bash
claude "Verify that all Android widget files are correctly placed in the android directory structure. Check SystemStatsWidget.java, widget layouts, and AndroidManifest.xml"
```

## Part 5: Fix Common Issues with Claude CLI

### Issue 1: Missing Android Files

If Android native files are missing after prebuild:

```bash
claude "The android/app/src/main directory is missing widget files. Please ensure SystemStatsWidget.java, widget_system_stats.xml layout and config, and widget_background.xml are in the correct locations"
```

### Issue 2: Gradle Configuration

If you get Gradle errors:

```bash
claude "I'm getting Gradle build errors. Please check my android/app/build.gradle and android/build.gradle files and fix any configuration issues"
```

### Issue 3: Package Name Conflicts

```bash
claude "Fix package name conflicts in my Android project. The package should be com.nothing.systemwidget throughout all files"
```

## Part 6: Build the Project

### Method 1: Using Expo CLI (Recommended for Development)

```bash
# Start the Metro bundler
npx expo start

# In another terminal, run on Android
npx expo run:android
```

### Method 2: Direct Android Build

```bash
# Navigate to android directory
cd android

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Return to project root
cd ..
```

### Method 3: Using EAS Build (For Production)

```bash
# Install EAS CLI
npm install -g eas-cli

# Login to Expo
eas login

# Configure EAS
eas build:configure

# Build preview APK
eas build --platform android --profile preview

# Build production AAB
eas build --platform android --profile production
```

## Part 7: Troubleshooting with Claude CLI

### Check Build Errors

If build fails, use Claude CLI to diagnose:

```bash
# For Gradle errors
claude "I'm getting this Gradle error: [paste error here]. How do I fix it?"

# For React Native errors
claude "I'm getting this React Native error: [paste error here]. Please help me resolve it."

# For widget not appearing
claude "My Android widget isn't showing up in the widget list. Check my AndroidManifest.xml and widget configuration"
```

### Verify Widget Implementation

```bash
claude "Review my SystemStatsWidget.java file and ensure the widget will properly display battery, RAM, and CPU stats on the home screen"
```

### Fix Layout Issues

```bash
claude "My widget layout looks wrong. Please review android/app/src/main/res/layout/widget_system_stats.xml and fix any UI issues"
```

## Part 8: Testing the Widget

### Step 1: Install the App

After successful build, install on your device:

```bash
# Install via ADB
adb install android/app/build/outputs/apk/debug/app-debug.apk

# Or use Expo
npx expo run:android
```

### Step 2: Add Widget to Home Screen

1. Long-press on your Nothing Phone home screen
2. Tap "Widgets"
3. Scroll to find "Nothing System Widget"
4. Drag and drop it onto your home screen
5. Widget should show Battery, RAM, and CPU stats

### Step 3: Verify Widget Updates

```bash
claude "How can I verify that my widget is updating correctly? What should I check in the Android logcat?"
```

To view logs:
```bash
# View all logs
adb logcat

# Filter for widget logs
adb logcat | grep SystemStatsWidget

# Filter for errors only
adb logcat *:E
```

## Part 9: Common Claude CLI Commands for This Project

Here are useful Claude CLI commands while working on this project:

### Code Review
```bash
claude "Review my App.tsx and suggest improvements for better performance"
```

### Add Features
```bash
claude "Add a dark/light theme toggle to the main app"
```

```bash
claude "Add storage usage percentage to the widget"
```

### Fix Bugs
```bash
claude "The battery percentage isn't updating in the widget. Fix the SystemStatsWidget.java file"
```

### Optimize Performance
```bash
claude "Optimize the CPU usage calculation in SystemStatsWidget.java to be more efficient"
```

### Customize Design
```bash
claude "Change the widget background to use Nothing Phone's red accent color instead of cyan"
```

### Generate Additional Files
```bash
claude "Create app icons for the Nothing widget in different resolutions (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)"
```

## Part 10: Project Structure Verification

Use Claude CLI to verify your project structure:

```bash
claude "List and verify the complete project structure for the Nothing widget, ensuring all files are in the correct locations"
```

Expected structure:
```
nothing-widget/
├── App.tsx
├── app.json
├── package.json
├── .gitignore
├── README.md
├── INSTRUCTIONS.md (this file)
└── android/
    └── app/
        └── src/
            └── main/
                ├── AndroidManifest.xml
                ├── java/com/nothing/systemwidget/
                │   └── SystemStatsWidget.java
                └── res/
                    ├── drawable/
                    │   └── widget_background.xml
                    ├── layout/
                    │   └── widget_system_stats.xml
                    ├── values/
                    │   └── strings.xml
                    └── xml/
                        └── widget_system_stats.xml
```

## Part 11: Advanced Customization

### Customize Widget Colors

```bash
claude "Change all cyan colors (#00D9FF) in the widget to Nothing Phone's signature red color"
```

### Add More Metrics

```bash
claude "Add network speed monitoring to both the app and widget"
```

### Change Update Frequency

```bash
claude "Explain how to change the widget update frequency and the trade-offs with battery life"
```

### Add Widget Sizes

```bash
claude "Create multiple widget size configurations (small 2x2, medium 4x2, large 4x4) with different information density"
```

## Part 12: Debugging Tips

### Enable Debug Mode

```bash
# Enable React Native debug mode
npx react-native log-android

# Enable Expo debug mode
npx expo start --dev-client
```

### Check Widget Registration

```bash
claude "Verify that my widget is correctly registered in AndroidManifest.xml and can be detected by the Android system"
```

### Inspect Widget Layout

```bash
# Use Android Studio's Layout Inspector
# Open Android Studio > Tools > Layout Inspector
```

### Monitor System Resources

```bash
# Monitor memory usage
adb shell dumpsys meminfo com.nothing.systemwidget

# Monitor battery usage
adb shell dumpsys battery

# Monitor CPU
adb shell top -m 10
```

## Part 13: Building for Release

### Step 1: Generate Keystore

```bash
# Generate release keystore
keytool -genkeypair -v -storetype PKCS12 -keystore nothing-widget-release.keystore -alias nothing-widget -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Configure Gradle for Release

Ask Claude CLI to help:
```bash
claude "Configure my android/app/build.gradle for release builds with signing configuration"
```

### Step 3: Build Release APK

```bash
cd android
./gradlew assembleRelease
cd ..
```

The APK will be at: `android/app/build/outputs/apk/release/app-release.apk`

## Part 14: Continuous Development with Claude CLI

### Watch Mode

Keep Claude CLI ready to help while developing:

```bash
# In one terminal, run the app
npx expo start

# In another terminal, keep Claude CLI ready
claude --interactive
```

### Quick Fixes

```bash
# Fix TypeScript errors
claude "Fix all TypeScript errors in App.tsx"

# Fix widget layout issues
claude "The widget text is overlapping. Fix the layout constraints in widget_system_stats.xml"

# Update dependencies
claude "Update all dependencies to their latest compatible versions and update package.json"
```

### Code Generation

```bash
# Generate new components
claude "Create a new component for displaying detailed CPU core usage in a modal"

# Generate tests
claude "Create Jest tests for the system stats data fetching functions"
```

## Part 15: Publishing

### Option 1: Direct APK Distribution

```bash
# Build signed release APK
cd android && ./gradlew assembleRelease

# Share the APK directly to Nothing Phone users
# File location: android/app/build/outputs/apk/release/app-release.apk
```

### Option 2: Google Play Store

```bash
# Build AAB (Android App Bundle)
cd android && ./gradlew bundleRelease

# Upload to Google Play Console
# File location: android/app/build/outputs/bundle/release/app-release.aab
```

Use Claude CLI for Play Store listing:
```bash
claude "Generate a Google Play Store description for the Nothing System Widget app, including key features and screenshots description"
```

## Part 16: Maintenance and Updates

### Update Widget Logic

```bash
claude "Update the CPU calculation in SystemStatsWidget.java to be more accurate using /proc/stat"
```

### Add New Features

```bash
claude "Add temperature monitoring to the widget if the device supports it"
```

### Fix Reported Issues

```bash
claude "Users report the widget stops updating after a few hours. Diagnose and fix the issue"
```

## Quick Reference Commands

```bash
# Start development
npm install
npx expo prebuild --platform android
npx expo run:android

# Clean build
cd android && ./gradlew clean && cd ..
npx expo prebuild --platform android --clean

# View logs
adb logcat | grep SystemStatsWidget

# Rebuild and install
npx expo run:android

# Ask Claude for help
claude "How do I [your question here]?"
```

## Support and Resources

- **Expo Documentation**: https://docs.expo.dev
- **React Native**: https://reactnative.dev
- **Android Widgets**: https://developer.android.com/guide/topics/appwidgets
- **Claude CLI**: https://docs.claude.com/en/docs/claude-code
- **Nothing Community**: https://nothing.community

## Final Checklist

Before considering the project complete, verify:

- [ ] App runs successfully on Android device
- [ ] Widget appears in widget picker
- [ ] Widget displays correct battery percentage
- [ ] Widget displays correct RAM usage
- [ ] Widget displays CPU approximation
- [ ] Widget updates automatically
- [ ] App shows real-time stats
- [ ] No crashes or errors in logcat
- [ ] UI matches Nothing Phone design
- [ ] All dependencies are properly installed

## Getting Help

If you encounter any issues, use Claude CLI:

```bash
# General help
claude "I'm stuck at [describe your issue]. What should I do?"

# Specific errors
claude "I'm getting this error: [paste full error]. How do I fix it?"

# Best practices
claude "What are the best practices for Android widget development with React Native?"
```

---

**Happy Building! 🚀**

For any questions or issues, use `claude --help` or ask Claude directly in your VS Code terminal.
