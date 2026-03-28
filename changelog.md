# 2026-03-28 00:00 — Remove Expo/React Native, convert to pure Android project

## Changes
- `android/settings.gradle` — Rewrote to standard Android `pluginManagement` + `dependencyResolutionManagement` blocks; removed all Node/RN/Expo references
- `android/build.gradle` — Rewrote to use `plugins {}` block with AGP 8.3.2 and Kotlin 1.9.23; removed React Native Gradle plugin and npm-resolved repositories
- `android/app/build.gradle` — Rewrote to plain Android app build; removed `com.facebook.react` plugin, `getRNVersion()`, `react {}` block, and all RN/Expo dependencies; added `androidx.appcompat:appcompat:1.6.1` and `androidx.core:core-ktx:1.12.0`
- `android/gradle.properties` — Stripped to four essential properties; removed all RN/Expo/Hermes/GIF/webp flags
- `android/app/src/main/AndroidManifest.xml` — Removed `MainApplication`, Expo meta-data, `MainActivity`, `DevSettingsActivity`, deep-link queries block, and unused permissions (INTERNET, READ/WRITE_EXTERNAL_STORAGE, SYSTEM_ALERT_WINDOW, VIBRATE); kept only widget-required permissions
- `android/app/src/main/res/values/styles.xml` — Simplified to minimal `AppTheme`; removed `ResetEditText` and `Theme.App.SplashScreen`
- `android/app/src/main/res/values/colors.xml` — Removed `splashscreen_background`, `colorPrimary`, `colorPrimaryDark`; kept widget colors only
- `.gitignore` — Removed Node/Expo entries; kept Android build artifacts and `personal/`
- `README.md` — Removed EAS Build option and `npm install` step from build instructions
- `INSTRUCTIONS.md` — Rewrote entirely; removed Node.js, npm, Expo CLI prerequisites and all Expo-specific commands; now Gradle-only

## Deleted Files
- `App.tsx` — Empty Expo entry point, not needed
- `package.json` — Node dependencies, not needed
- `package-lock.json` — npm lockfile, not needed
- `tsconfig.json` — TypeScript config for RN layer, not needed
- `app.json` — Expo project config, not needed
- `eas.json` — EAS Build profiles, not needed
- `android/react-settings-plugin/` — Expo Gradle plugin stub, not needed
- `android/app/src/debug/AndroidManifest.xml` — RN dev-mode manifest, not needed
- `android/app/src/main/java/com/nothing/systemwidget/MainActivity.kt` — React Native activity, not needed
- `android/app/src/main/java/com/nothing/systemwidget/MainApplication.kt` — React Native application class, not needed
- `android/app/src/main/res/drawable/rn_edit_text_material.xml` — RN UI drawable, not needed
- `android/app/src/main/res/drawable/splashscreen.xml` — Expo splash screen drawable, not needed
- `node_modules/` — npm packages, not needed
