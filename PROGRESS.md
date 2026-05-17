# AndroText Progress Tracker

## Sprint 1: Code Syntax Highlighting

**Status**: COMPLETED

### Subtasks

| #  | Task                                           | Status      | Notes |
|----|------------------------------------------------|-------------|-------|
| 1.1| Curate TextMate grammars                       | Done        | 28 grammar files downloaded from VS Code, textmate bundles, and community repos |
| 1.2| Bundle grammar files in assets                 | Done        | All grammars in `app/src/main/assets/textmate/grammars/` |
| 1.3| Initialize LanguageRegistry on startup         | Done        | `MainActivity.initLanguageRegistry()` registers all languages on IO thread |
| 1.4| Wire TextMateLanguageService to editor         | Done        | `EditorHost.applyLanguage()` resolves language from filename and sets on editor |
| 1.5| Complete extension-to-language mapping         | Done        | `LanguageDefinitions` has 28 languages with ~80 file extensions mapped |
| 1.6| Handle tab switching language re-application   | Done        | Language re-applied when `fileName` changes during tab switch |
| 1.7| Test across file types                         | Done        | Build passes, all tests pass |

### Supported Languages

| Language | Scope | Extensions | Source |
|----------|-------|------------|--------|
| JSON | source.json | json | VS Code |
| HTML | text.html.basic | html, htm, svg | VS Code |
| CSS | source.css | css | VS Code |
| JavaScript | source.js | js, mjs, cjs | VS Code |
| TypeScript | source.ts | ts, tsx | VS Code |
| Java | source.java | java | VS Code |
| Kotlin | source.kotlin | kt, kts | vscode-kotlin (mathiasfrohlich) |
| C | source.c | c, h | VS Code |
| C++ | source.cpp | cpp, hpp, cc, cxx, hxx | VS Code |
| Python | source.python | py, pyw | VS Code (MagicPython) |
| Go | source.go | go | VS Code |
| Rust | source.rust | rs | dustypomerleau/rust-syntax |
| PHP | source.php | php, phtml | VS Code |
| Ruby | source.ruby | rb, erb, rake, gemspec | textmate/ruby.tmbundle |
| Swift | source.swift | swift | textmate bundle |
| C# | source.cs | cs | dotnet/csharp-tmLanguage |
| Dart | source.dart | dart | Dart-Code/Dart-Code |
| Shell/Bash | source.shell | sh, bash, zsh, fish | VS Code |
| SQL | source.sql | sql | textmate/sql.tmbundle |
| XML | text.xml | xml, xsd, xsl, xslt, svg | VS Code |
| YAML | source.yaml | yaml, yml | VS Code |
| TOML | source.toml | toml | textmate/toml.tmbundle |
| Markdown | text.html.markdown | md, markdown, mdown, mkd | VS Code |
| Diff | source.diff | diff, patch | VS Code |
| Lua | source.lua | lua | textmate/lua.tmbundle |
| R | source.r | r, R | textmate/r.tmbundle |
| Groovy/Gradle | source.groovy | groovy, gradle | textmate/groovy.tmbundle |
| LaTeX | text.tex.latex | tex, latex, lhs | textmate/latex.tmbundle |

### Key Files Changed

- `app/src/main/kotlin/com/androtext/app/MainActivity.kt` — added language registry init
- `app/src/main/kotlin/com/androtext/app/ui/screens/EditorHost.kt` — added fileName param and language application
- `core/lang/src/main/kotlin/com/androtext/core/lang/LanguageRegistry.kt` — added registerAllLanguages(), extension mapping
- `core/lang/src/main/kotlin/com/androtext/core/lang/LanguageDefinitions.kt` — NEW: grammar definition registry
- `core/lang/build.gradle.kts` — changed Sora deps to `api` for transitive visibility
- `core/render/src/main/kotlin/com/androtext/core/render/SoraEditorHost.kt` — added setLanguage() method
- `app/src/main/assets/textmate/grammars/` — NEW: 28 grammar files
- `app/src/main/assets/textmate/themes/solarized-dark.json` — NEW: default TextMate theme

### Known Issues

- Grammar loading happens on a background thread; if a file is opened very quickly after app launch, the language might not be applied yet. Subsequent tab switches will correctly apply the language.
- No language configuration files (for comment toggling, bracket matching) bundled yet. Only grammars.

---

## Sprint 2: Theme System

**Status**: COMPLETED

### Subtasks

| #   | Task                                           | Status      | Notes |
|-----|------------------------------------------------|-------------|-------|
| 2.1 | Create theme data model                        | Done        | `EditorTheme`, `ParsedThemeColors`, `ComposeThemeColors`, `TokenColorRule` data classes in `core/render/theme/` |
| 2.2 | Curate TextMate theme files                    | Done        | 10 themes: Solarized Dark, Monokai, One Dark Pro, Dracula, Nord, GitHub Dark, GitHub Light, Solarized Light, Catppuccin Mocha, Tokyonight Night |
| 2.3 | Bundle theme files in assets                   | Done        | All themes in `app/src/main/assets/textmate/themes/` with `theme_index.json` metadata |
| 2.4 | Build Sora EditorColorScheme from TextMate     | Done        | `TextMateThemeParser.parseEditorColorScheme()` maps theme JSON `colors` + `tokenColors` to Sora's `EditorColorScheme` slots |
| 2.5 | Build Compose Material 3 ColorScheme           | Done        | `TextMateThemeParser.parseComposeColors()` derives full Material 3 `ColorScheme` from theme JSON |
| 2.6 | Apply theme to both layers                     | Done        | Theme changes propagate to: (a) Sora `EditorColorScheme` for editor chrome, (b) Sora TextMate `ThemeRegistry` for token colors, (c) Compose `MaterialTheme` for UI |
| 2.7 | Theme selection UI                             | Done        | Theme cards in SettingsScreen with color preview swatches and checkmark for active theme |
| 2.8 | Persist theme with DataStore                   | Done        | `preferencesDataStore("settings")` saves/loads selected theme ID; activated previously-declared `androidx.datastore:preferences` dependency |
| 2.9 | Remove hardcoded theme                         | Done        | `applySolarizedDarkTheme()` replaced with generic `applyTheme(EditorColorScheme)`; hardcoded Solarized Dark colors removed from `Theme.kt` |
| 2.10| Test all themes                                | Done        | All 10 theme JSONs validated; build passes; no compiler warnings |

### Available Themes

| Theme | Type | Background | Accent |
|-------|------|------------|--------|
| Solarized Dark | Dark | #002B36 | #268BD2 |
| Monokai | Dark | #272822 | #F92672 |
| One Dark Pro | Dark | #282C34 | #C678DD |
| Dracula | Dark | #282A36 | #FF79C6 |
| Nord | Dark | #2E3440 | #81A1C1 |
| GitHub Dark | Dark | #0D1117 | #FF7B72 |
| GitHub Light | Light | #FFFFFF | #CF222E |
| Solarized Light | Light | #FDF6E3 | #268BD2 |
| Catppuccin Mocha | Dark | #1E1E2E | #CBA6F7 |
| Tokyonight Night | Dark | #1A1B26 | #BB9AF7 |

### Key Files Changed

- `core/render/src/main/kotlin/com/androtext/core/render/theme/EditorTheme.kt` — NEW: theme data model
- `core/render/src/main/kotlin/com/androtext/core/render/theme/TextMateThemeParser.kt` — NEW: parses TextMate theme JSON → EditorColorScheme + Compose ColorScheme
- `core/render/src/main/kotlin/com/androtext/core/render/theme/ThemeRegistry.kt` — NEW: manages available themes, caching, active theme state
- `core/render/src/main/kotlin/com/androtext/core/render/SoraEditorHost.kt` — replaced `applySolarizedDarkTheme()` with `applyTheme(EditorColorScheme)`
- `core/lang/src/main/kotlin/com/androtext/core/lang/LanguageRegistry.kt` — added `loadAllThemes()`, `setActiveTheme()` for Sora TextMate theme management
- `app/src/main/kotlin/com/androtext/app/ui/theme/Theme.kt` — dynamic theming with `AndroTextTheme(themeColors=...)`, `LocalThemeColors`
- `app/src/main/kotlin/com/androtext/app/ui/viewmodel/EditorViewModel.kt` — added theme state (`currentThemeId`, `currentComposeColors`), `selectTheme()`, DataStore persistence
- `app/src/main/kotlin/com/androtext/app/ui/screens/EditorHost.kt` — passes `themeId`, applies theme via `ThemeRegistry.getEditorColorScheme()`
- `app/src/main/kotlin/com/androtext/app/ui/screens/SettingsScreen.kt` — added Appearance section with theme selector cards
- `app/src/main/kotlin/com/androtext/app/MainActivity.kt` — calls `viewModel.initializeTheme()`, passes `currentComposeColors` to `AndroTextTheme`
- `app/src/main/assets/textmate/themes/` — 9 new theme JSON files + `theme_index.json`

### Known Issues

- Theme change in settings re-applies editor chrome via EditorColorScheme and TextMate token colors via Sora's ThemeRegistry, but the TextMate language may need to be re-applied to the editor for token color changes to take effect in some edge cases.
- Compose ColorScheme derivation from TextMate themes uses heuristic brightness adjustments — some themes may benefit from manual color curation for better Material 3 harmony.
- DataStore preference loads asynchronously; there may be a brief flash of the default theme (Solarized Dark) before the saved theme is applied.

---

## Sprint 3: Markdown Preview — Foundation

**Status**: COMPLETED

### Subtasks

| #  | Task                               | Status      | Notes |
|----|------------------------------------|-------------|-------|
| 3.1| Add Markwon dependencies           | Done        | `markwon:core`, `image`, `ext-tables`, `ext-tasklist` added to version catalog and `app/build.gradle.kts` |
| 3.2| Create MarkdownPreviewScreen       | Done        | `MarkdownPreviewScreen.kt` wraps a `TextView` via `AndroidView` with Markwon rendering |
| 3.3| Create MarkwonFactory              | Done        | `MarkwonFactory.kt` creates a configured Markwon instance with Tables, TaskList, and Images plugins |
| 3.4| Theme the preview                  | Done        | Preview background, foreground, and accent colors derived from current theme's `ComposeThemeColors` |
| 3.5| Add preview toggle button          | Done        | Eye/Edit icon button in top app bar, visible only for `.md`/`.markdown`/`.mdown`/`.mkd` files |
| 3.6| Wire preview content               | Done        | `viewModel.getContent()` provides current editor text to `MarkdownPreviewScreen` |
| 3.7| MD syntax highlighting             | Done        | Markdown TextMate grammar from Sprint 1 applied to `.md` files in editor view |
| 3.8| Add preview mode preference        | Done        | `MarkdownViewMode` enum (EDITOR, PREVIEW) persisted via DataStore; setting in SettingsScreen under "Markdown" section |

### Markdown Features Supported

- Headings (h1–h6)
- Bold, italic, strikethrough
- Ordered and unordered lists
- Task lists (checkboxes)
- Tables (GFM)
- Links (clickable)
- Images (async loading via `ImagesPlugin`)
- Code spans and fenced code blocks
- Blockquotes
- Horizontal rules

### Key Files Changed

- `app/build.gradle.kts` — added Markwon dependencies
- `gradle/libs.versions.toml` — added `markwon = "4.6.1"` version and library entries
- `app/src/main/kotlin/com/androtext/app/ui/markdown/MarkwonFactory.kt` — NEW: configured Markwon singleton
- `app/src/main/kotlin/com/androtext/app/ui/screens/MarkdownPreviewScreen.kt` — NEW: composable wrapping Markwon-rendered TextView
- `app/src/main/kotlin/com/androtext/app/ui/screens/EditorScreen.kt` — added preview toggle button, conditional editor/preview display
- `app/src/main/kotlin/com/androtext/app/ui/viewmodel/EditorViewModel.kt` — added `isMarkdownPreview`, `isMarkdownFile`, `defaultMarkdownViewMode`, `MarkdownViewMode` enum, DataStore persistence
- `app/src/main/kotlin/com/androtext/app/ui/screens/SettingsScreen.kt` — added Markdown section with default view mode selector
- `app/src/main/kotlin/com/androtext/app/MainActivity.kt` — wired `previewContent` lambda with theme-derived colors

### Known Issues

- Code blocks in markdown preview are not syntax-highlighted (plain monospace text only). Adding `syntax-highlight` plugin requires prism4j annotation processing or the `prism4j-bundle` artifact, which was deferred to avoid kapt complexity.
- Markwon instance is cached per (bg, fg, accent) color tuple; theme changes invalidate and recreate it.
- No split view mode yet — preview replaces the editor (Sprint 4).
- Preview does not auto-scroll to match editor position.

---

## Sprint 4: Markdown Split View & Toolbar

**Status**: NOT STARTED

---

## Sprint 5: Multi-Cursor — Foundation

**Status**: NOT STARTED

---

## Sprint 6: Multi-Cursor — Advanced Features

**Status**: NOT STARTED

---

## Sprint 7: Integration, Polish & Settings Persistence

**Status**: NOT STARTED
