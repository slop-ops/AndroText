# AndroText

A lightweight, open-source text editor for Android. Built with a custom piece-table text engine, Sora Editor rendering, and a Solarized Dark Compose UI.

## Features

- **Piece-table text engine** — Efficiently handles files of any size; edits are stored as append-only fragments rather than copying the entire buffer
- **Incremental line index** — Binary-search-based line lookups that update incrementally on every insert/delete
- **Operation-based undo/redo** — `Insert`, `Delete`, `Replace`, and `Compound` (transaction-grouped) operations
- **NIO file I/O** — 64 KB chunked reads/writes with BOM detection (UTF-8, UTF-16LE, UTF-16BE) and atomic file saves
- **Sora Editor rendering** — Viewport virtualization with configurable overscan, decoration overlays (selection, search, errors, warnings)
- **TextMate syntax support** — Language registry with pluggable grammars, incremental tokenizer that only re-tokenizes dirty lines
- **Compose UI** — Jetpack Compose + Material 3 with a Solarized Dark color scheme applied to both the app chrome and the editor
- **Multi-tab editing** — Open multiple files simultaneously in a scrollable tab bar; each tab has its own buffer, modified indicator (`●`), and undo history; re-opening a file focuses its existing tab instead of duplicating
- **Search & replace** — Powered by Sora Editor's `EditorSearcher`; real-time match highlighting, match count (`3/15`), case-sensitive and regex toggles, navigate next/previous, replace single or replace all
- **Keyboard shortcuts** — gnome-text-editor style shortcuts via `Ctrl` key (see table below); works with hardware keyboards and Chrome OS
- **Recent files** — Persisted via SharedPreferences; up to 20 recent files tracked with display name and timestamp
- **Configurable editor** — Font size (8–32 sp), tab width (2–8), line numbers, word wrap, and current-line highlighting
- **Edge-to-edge** — Uses `enableEdgeToEdge()` with `adjustResize` soft-input mode

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+S` | Save file |
| `Ctrl+O` | Open file (system picker) |
| `Ctrl+F` | Find |
| `Ctrl+H` | Find & Replace |
| `Ctrl+G` | Find next match |
| `Ctrl+Shift+G` | Find previous match |
| `Ctrl+W` | Close current tab |
| `Ctrl+=` | Zoom in (font +1 sp) |
| `Ctrl+-` | Zoom out (font −1 sp) |
| `Ctrl+0` | Reset zoom (14 sp) |
| `Escape` | Close search bar |
| `Ctrl+Z` | Undo (handled by Sora Editor) |
| `Ctrl+Shift+Z` | Redo (handled by Sora Editor) |

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  app (Android Application)                                      │
│  MainActivity · EditorViewModel · Compose screens               │
│  (Editor / OpenFile / Settings / Welcome / SearchBar)           │
├─────────────┬─────────────────────┬─────────────────────────────┤
│ core:render │ core:lang           │ core:text-buffer             │
│ (Android)   │ (Android)           │ (Pure JVM)                  │
│             │                     │                             │
│ SoraEditor  │ TextMateLang        │ PieceTableBuffer            │
│ Host        │ Service             │ IncrementalLineIndex        │
│ TextRenderer│ LanguageRegistry    │ UndoRedoStackImpl           │
│ ViewportMgr │ IncrementalTokenizer│ FileLoader / FileWriter     │
│ EditorConfig│ TokenStore          │ TextBuffer interface        │
│ Decorations │ TokenProvider       │                             │
└─────────────┴─────────────────────┴─────────────────────────────┘
```

### Module details

| Module | Type | Description |
|--------|------|-------------|
| `core:text-buffer` | Kotlin JVM | Piece table buffer, line index, undo/redo, NIO file I/O — no Android dependencies |
| `core:render` | Android Library | Sora Editor `CodeEditor` wrapper, text renderer, viewport manager, decoration system, search delegation |
| `core:lang` | Android Library | TextMate grammar registry, token provider, incremental tokenizer, token store |
| `app` | Android App | Compose UI (Material 3), multi-tab `EditorViewModel`, SAF-based file open/save, search & replace UI, keyboard shortcuts, settings, navigation |

## Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.3.10 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 9.3 |
| Jetpack Compose BOM | 2025.05.01 |
| Material 3 | (via Compose BOM) |
| Sora Editor BOM | 0.24.5 |
| language-textmate | (via Sora BOM) |
| JUnit 5 | 5.12.2 |
| kotlinx-coroutines | 1.10.2 |
| compileSdk / targetSdk | 36 |
| minSdk | 21 |
| Java target | 17 |

## Build

```bash
./gradlew assembleDebug                  # Debug APK → app/build/outputs/apk/debug/
./gradlew :core:text-buffer:test         # Run JVM unit tests
```

Requires `ANDROID_HOME` pointing to an Android SDK with platform 36 and build-tools 35+.

## Screens

| Screen | Description |
|--------|-------------|
| **Welcome** | Shown when no file is open. "Open a file" card and recent files list |
| **Editor** | Top app bar (file name, modified indicator, save/open/find icons), scrollable tab bar, search bar (when active), full-screen Sora editor |
| **Open File** | SAF file picker (`*/*` MIME) + recent files list with tap-to-reopen |
| **Settings** | Font size slider, line numbers toggle, tab width slider, word wrap toggle, current-line highlight toggle |
| **Search Bar** | Inline bar below tabs — find input with match count, prev/next, case-sensitive (`Aa`) and regex (`.*`) toggles, replace mode (`⇄`) with Replace/Replace All buttons |

## Project Structure

```
AndroText/
├── app/
│   └── src/main/
│       ├── kotlin/com/androtext/app/
│       │   ├── MainActivity.kt               # dispatchKeyEvent for shortcuts, SAF launcher
│       │   └── ui/
│       │       ├── navigation/Screen.kt
│       │       ├── screens/
│       │       │   ├── EditorScreen.kt       # Editor + tab bar + Welcome screens
│       │       │   ├── EditorHost.kt          # AndroidView bridge to SoraEditorHost
│       │       │   ├── SearchBar.kt           # Find/replace UI composable
│       │       │   ├── OpenFileScreen.kt
│       │       │   └── SettingsScreen.kt
│       │       ├── theme/Theme.kt             # Solarized Dark MaterialTheme
│       │       └── viewmodel/
│       │           ├── EditorViewModel.kt     # Multi-tab state, search state, editor config
│       │           └── EditorTab.kt           # Per-tab: uri, buffer, modified flag
│       ├── res/
│       └── AndroidManifest.xml
├── core/
│   ├── text-buffer/src/
│   │   ├── main/.../buffer/
│   │   │   ├── TextBuffer.kt               # Interface + TextChangeEvent
│   │   │   ├── PieceTableBuffer.kt         # Piece table implementation
│   │   │   ├── LineIndex.kt                # Line index interface
│   │   │   ├── IncrementalLineIndex.kt     # Incremental line tracking
│   │   │   ├── UndoRedoStack.kt            # Undo/redo interface + EditOperation types
│   │   │   ├── UndoRedoStackImpl.kt        # Stack impl with transactions
│   │   │   └── FileIO.kt                   # FileLoader + FileWriter (NIO, chunked)
│   │   └── test/.../buffer/
│   │       ├── PieceTableBufferTest.kt
│   │       ├── IncrementalLineIndexTest.kt
│   │       ├── UndoRedoStackTest.kt
│   │       └── FileIOTest.kt
│   ├── render/src/main/.../render/
│   │   ├── SoraEditorHost.kt               # CodeEditor wrapper + search delegation
│   │   ├── SoraTextRenderer.kt             # Decoration drawing on canvas
│   │   ├── SoraViewportManager.kt          # Viewport change detection
│   │   ├── TextRenderer.kt                 # Renderer + Decoration interfaces
│   │   ├── ViewportManager.kt              # Viewport interface
│   │   ├── EditorConfig.kt                 # Data class for editor settings
│   │   └── DecorationProviderImpl.kt       # Decoration collection
│   └── lang/src/main/.../lang/
│       ├── LanguageService.kt              # Token/TokenType definitions, interfaces
│       ├── TextMateLanguageService.kt      # Creates Sora TextMateLanguage
│       ├── TextMateTokenProvider.kt        # Line-level tokenizer with scope classification
│       ├── IncrementalTokenizer.kt         # Dirty-line tracking + re-tokenization
│       ├── TokenStore.kt                   # Token store interface
│       └── LanguageRegistry.kt             # Singleton: grammar/theme loading
├── gradle/
│   └── libs.versions.toml                  # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── LICENSE                                 # GPL-3.0
```

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
