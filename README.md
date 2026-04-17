# AndroText

A text editor for Android with large-file support, syntax highlighting, and a modern Compose UI.

## Features

- **Large file editing** — Piece table data structure handles files of any size efficiently
- **Syntax highlighting** — TextMate grammar support via Sora Editor
- **Incremental tokenization** — Only re-tokenizes changed lines
- **Operation-based undo/redo** — Memory-efficient edit history with transaction grouping
- **NIO file I/O** — Chunked reading/writing with BOM detection and atomic saves
- **Compose UI** — Material 3 design with light/dark theme

## Architecture

```
core:text-buffer (Kotlin JVM)   — Piece table, line index, undo/redo, file I/O
core:render    (Android Library) — Sora Editor rendering, viewport virtualization
core:lang      (Android Library) — TextMate language service, tokenization
app            (Android App)     — Compose UI, navigation, settings
```

## Tech Stack

- Kotlin 2.3.10, AGP 8.13.2, Gradle 9.3
- Jetpack Compose (BOM 2025.05.01) + Material 3
- Sora Editor (BOM 0.24.5) + language-textmate
- compileSdk 36, minSdk 21, Java 17 target

## Build

```bash
./gradlew assembleDebug        # Debug APK → app/build/outputs/apk/debug/
./gradlew :core:text-buffer:test  # Run JVM unit tests (68 tests)
```

Requires `ANDROID_HOME` pointing to an Android SDK with platform 36 and build-tools 35+.

## Project Structure

```
AndroText/
├── app/                    # Android application
├── core/
│   ├── text-buffer/        # Pure JVM text engine
│   ├── render/             # Sora Editor rendering layer
│   └── lang/               # Language & tokenization layer
├── gradle/
│   └── libs.versions.toml  # Version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

## License

MIT
