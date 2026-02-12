# OpenText Browser

A lightweight Android web browser designed to enable text selection and copying on any website, bypassing restrictions that prevent text selection.

## Features

- **Full Web Browsing**: Browse any website using the native Android WebView
- **Text Selection Enabled**: Long-press to select text on any webpage
- **Copy to Clipboard**: Copy selected text with a single tap
- **Navigation Controls**: Back, forward, and refresh functionality
- **Progress Indicator**: Visual loading progress bar
- **URL Auto-Complete**: Automatically adds https:// to URLs
- **Search Support**: Enter search queries directly in the URL bar

## How It Works

The browser uses JavaScript injection to override website restrictions that prevent text selection and copying. When a page loads, the app injects CSS and JavaScript to:

1. Force `user-select: text` on all elements
2. Remove event listeners that block text selection
3. Enable context menus on all elements

## Building the App

### Prerequisites

- Android Studio (or command-line tools)
- Android SDK (API level 24 or higher)
- Java Development Kit (JDK) 8 or higher

### Build Steps

1. Open the project in Android Studio:
   ```
   File > Open > /path/to/OpenTextBrowser
   ```

2. Wait for Gradle to sync dependencies

3. Build the debug APK:
   ```
   Build > Build Bundle(s) / APK(s) > Build APK(s)
   ```

### Command Line Build

If you have the Android SDK installed, you can build from the command line:

```bash
cd OpenTextBrowser
./gradlew assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Installing

1. Transfer the APK to your Android device
2. Enable "Install from unknown sources" in Settings
3. Open the APK file and install

## Usage

1. **Enter URL**: Type a URL or search query in the top bar and press Go
2. **Navigate**: Use the back, forward, and refresh buttons at the bottom
3. **Select Text**: Long-press on any text to select it
4. **Copy Text**: Tap the copy button or use the system copy option

## Project Structure

```
OpenTextBrowser/
├── app/
│   ├── src/main/
│   │   ├── java/com/opentext/browser/
│   │   │   └── MainActivity.kt          # Main browser activity
│   │   ├── res/
│   │   │   ├── layout/                  # UI layouts
│   │   │   ├── drawable/                # Icons and graphics
│   │   │   ├── values/                 # Colors, strings, themes
│   │   │   └── mipmap/                  # App icons
│   │   └── AndroidManifest.xml          # App manifest
│   └── build.gradle                     # App build configuration
├── build.gradle                         # Project build configuration
├── settings.gradle                      # Project settings
├── gradle.properties                    # Gradle properties
└── gradle/wrapper/                      # Gradle wrapper files
```

## Permissions

- **INTERNET**: Required for web browsing functionality
- **ACCESS_NETWORK_STATE**: For checking network connectivity

## Requirements

- Android 7.0 (API level 24) or higher
- No special permissions required beyond internet access

## License

This project is provided as-is for educational and personal use.
