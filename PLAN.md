# AndroText Feature Plan: Markdown, Highlighting, Multi-Cursor & Themes

## Architecture Summary

AndroText is a Kotlin/Compose Android code editor built on top of **Sora Editor**
(`io.github.rosemoe:editor` BOM 0.24.5). Key facts:

- Sora is a `View`-based editor wrapped in Compose via `AndroidView`
- Sora has built-in TextMate language support via `language-textmate` —
  infrastructure exists in `LanguageRegistry`/`TextMateLanguageService` but
  **no grammars or themes are bundled**
- Theme is hardcoded to Solarized Dark (both Compose UI and Sora
  `EditorColorScheme`)
- No multi-cursor support exists — cursor/selection is entirely Sora's built-in
  single-cursor model
- Settings are ephemeral (DataStore dependency declared but unused)
- The decoration system (`SoraTextRenderer`) can draw custom overlays on the
  Canvas

---

## Sprint 1: Code Syntax Highlighting

**Goal**: Bundle TextMate grammars and wire them up so all supported file types
get syntax highlighting.

### Subtasks

| #  | Task                                          | Details                                                                                                                                                                                                                           |
|----|-----------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1.1| **Curate TextMate grammars**                  | Source MIT-licensed TextMate grammars (from VS Code built-ins or textmate bundles) for: Kotlin, Java, Python, JavaScript, TypeScript, C, C++, HTML, CSS, JSON, XML, YAML, Shell/Bash, Markdown, Go, Rust, SQL, PHP, Ruby, Swift, C#, Dart, Lua, R, TOML, Diff, LaTeX, Gradle |
| 1.2| **Bundle grammar files**                      | Place all grammar JSON files in `app/src/main/assets/textmate/grammars/`. Create a `manifest.json` mapping each grammar to its file extensions and scope names                                                                    |
| 1.3| **Initialize LanguageRegistry on startup**    | In `MainActivity.onCreate()` or a `CompositionLocal`, call `LanguageRegistry.registerLanguage()` for each grammar found in assets. Use `context.assets.list()` to enumerate files                                                 |
| 1.4| **Wire TextMateLanguageService to editor**    | When a file is opened (in `EditorHost` or `EditorViewModel`), call `LanguageRegistry.getLanguageForFile(uri)` → `TextMateLanguageService.createSoraLanguage()` → `codeEditor.editorLanguage = language`. Apply language on tab creation and tab switch |
| 1.5| **Complete extension-to-language mapping**    | Fill in `LanguageRegistry.getLanguageForFile()` to handle all registered extensions. Add fallback for unknown types (plain text)                                                                                                  |
| 1.6| **Handle tab switching**                      | In `EditorHost`, when `fileVersion` changes (tab switch), re-apply the correct language to the `CodeEditor`                                                                                                                       |
| 1.7| **Test across file types**                    | Verify highlighting works for `.kt`, `.py`, `.js`, `.json`, `.xml`, `.html`, `.css`, `.md`, `.yaml`, `.sh`, `.c`, `.cpp`, `.java`, `.go`, `.rs`, `.sql`                                                                           |
| 1.8| **Update PROGRESS.md**                        | Document sprint 1 completion, list supported languages, note any known issues                                                                                                                                                     |

### Key Files to Modify

- `app/src/main/kotlin/com/androtext/app/MainActivity.kt` — init languages
- `app/src/main/kotlin/com/androtext/app/ui/screens/EditorHost.kt` — apply language
- `app/src/main/kotlin/com/androtext/app/ui/viewmodel/EditorViewModel.kt` — language state
- `core/lang/src/main/kotlin/com/androtext/core/lang/LanguageRegistry.kt` — complete mapping
- `core/lang/src/main/kotlin/com/androtext/core/lang/TextMateLanguageService.kt` — ensure factory works
- `app/src/main/assets/textmate/grammars/` — new directory with grammar files

### Dependencies

- No new dependencies — `language-textmate` is already included via Sora BOM

---

## Sprint 2: Theme System

**Goal**: Support 8-10 popular editor themes with both Sora editor and Compose
UI synchronization, persisted to DataStore.

### Subtasks

| #   | Task                                           | Details                                                                                                                                                                                                                              |
|-----|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 2.1 | **Create theme data model**                    | `EditorTheme` data class: `id: String`, `name: String`, `isDark: Boolean`, `assetPath: String`, `previewColors: Map<String, Int>`. Create `ThemeRegistry` singleton to manage available themes                                       |
| 2.2 | **Curate TextMate theme files**                | Source MIT-licensed TextMate theme JSONs for: Monokai, One Dark Pro, Dracula, Nord, GitHub Dark, GitHub Light, Solarized Dark, Solarized Light, Catppuccin Mocha, Tokyonight Night                                                   |
| 2.3 | **Bundle theme files**                         | Place in `app/src/main/assets/textmate/themes/`. Create a `theme_index.json` with metadata (name, id, isDark, accent color)                                                                                                          |
| 2.4 | **Build Sora EditorColorScheme from TextMate** | Parse TextMate theme JSON, extract `tokenColors` and `colors`. Map to Sora's `EditorColorScheme` slots (WHOLE_BACKGROUND, TEXT_NORMAL, LINE_NUMBER, KEYWORD, COMMENT, LITERAL, FUNCTION_NAME, etc.)                                 |
| 2.5 | **Build Compose Material 3 ColorScheme**       | Extract background, foreground, primary, secondary, surface colors from the TextMate theme. Map to Material 3 `ColorScheme` (dark or light variant). Create a `composeColorSchemeFromTheme()` function                              |
| 2.6 | **Apply theme to both layers**                 | When theme changes: (a) apply `EditorColorScheme` to `CodeEditor`, (b) recompose `AndroTextTheme` with the matching Material 3 colors. Expose current theme via `CompositionLocal` or ViewModel state                                |
| 2.7 | **Theme selection UI**                         | Add a "Theme" section in `SettingsScreen`. Show theme cards with name + color preview strip. Selected theme gets a checkmark or border highlight                                                                                    |
| 2.8 | **Persist theme with DataStore**               | Initialize `DataStore<Preferences>` in `MainActivity`. Save selected theme ID. Load on startup before rendering. Replace the hardcoded Solarized Dark default                                                                        |
| 2.9 | **Remove hardcoded theme**                     | Replace `applySolarizedDarkTheme()` in `SoraEditorHost` with dynamic theme application from `ThemeRegistry`. Remove hardcoded colors from `Theme.kt` in favor of theme-derived colors                                                |
| 2.10| **Test all themes**                            | Verify each theme renders correctly in the editor and the surrounding UI. Check contrast ratios and readability                                                                                                                      |
| 2.11| **Update PROGRESS.md**                         | Document sprint 2 completion, list available themes, note the DataStore integration                                                                                                                                                  |

### Key Files to Modify

- `app/src/main/kotlin/com/androtext/app/ui/theme/Theme.kt` — dynamic theming
- `app/src/main/kotlin/com/androtext/app/ui/screens/SettingsScreen.kt` — theme picker
- `core/render/src/main/kotlin/com/androtext/core/render/SoraEditorHost.kt` — remove hardcoded theme
- `core/render/src/main/kotlin/com/androtext/core/render/EditorConfig.kt` — add theme field
- `app/src/main/kotlin/com/androtext/app/ui/viewmodel/EditorViewModel.kt` — theme state
- `app/src/main/kotlin/com/androtext/app/MainActivity.kt` — DataStore init
- New: `core/render/.../ThemeRegistry.kt`, `core/render/.../EditorTheme.kt`

### New Dependencies

- Already declared: `androidx.datastore:datastore-preferences` (just needs activation)

---

## Sprint 3: Markdown Preview — Foundation

**Goal**: Add Markwon-based markdown preview with a toggle between editor and
preview mode.

### Subtasks

| #  | Task                               | Details                                                                                                                                                                                                              |
|----|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 3.1| **Add Markwon dependencies**       | Add to `app/build.gradle.kts`: `io.noties.markwon:core`, `io.noties.markwon:image`, `io.noties.markwon:ext-tables`, `io.noties.markwon:ext-tasklist`, `io.noties.markwon:syntax-highlight` (for code blocks)       |
| 3.2| **Create MarkdownPreview composable** | New `MarkdownPreviewScreen.kt` with an `AndroidView` wrapping a `TextView`. Configure Markwon instance with all extensions. Apply markdown via `markwon.setMarkdown(textView, text)`                                |
| 3.3| **Theme the preview**              | Style the preview `TextView` to match the current editor theme (background, text, code block, link colors). Create `styleMarkdownPreview()` that reads from current theme                                            |
| 3.4| **Add preview toggle button**      | Add a preview/eye icon in the `EditorScreen` top app bar (visible only for `.md` files). Toggling switches between editor view and preview view                                                                      |
| 3.5| **Wire preview content**           | When preview is active, read current text from `EditorViewModel.currentText` (or the active tab's `PieceTableBuffer`) and pass to `MarkdownPreviewScreen`                                                           |
| 3.6| **MD syntax highlighting**         | Ensure the markdown TextMate grammar from Sprint 1 is applied to `.md` files. Markdown should look good both in raw edit and preview                                                                                 |
| 3.7| **Add preview mode preference**    | Add a setting for default markdown view (editor / preview). Persist with DataStore                                                                                                                                   |
| 3.8| **Test with various markdown**     | Test headings, bold/italic, lists, code blocks, tables, task lists, links, images, blockquotes, horizontal rules                                                                                                     |
| 3.9| **Update PROGRESS.md**             | Document sprint 3 completion, list markdown features supported                                                                                                                                                       |

### Key Files to Modify

- `app/build.gradle.kts` — add Markwon deps
- `app/src/main/kotlin/com/androtext/app/ui/screens/EditorScreen.kt` — preview toggle
- `app/src/main/kotlin/com/androtext/app/ui/viewmodel/EditorViewModel.kt` — preview state
- New: `app/src/main/kotlin/com/androtext/app/ui/screens/MarkdownPreviewScreen.kt`
- New: `app/src/main/kotlin/com/androtext/app/ui/markdown/MarkwonFactory.kt`

### New Dependencies

- `io.noties.markwon:core` (latest stable)
- `io.noties.markwon:image`
- `io.noties.markwon:ext-tables`
- `io.noties.markwon:ext-tasklist`
- `io.noties.markwon:syntax-highlight`

---

## Sprint 4: Markdown Split View & Toolbar

**Goal**: Add side-by-side editor+preview split view and a markdown formatting
toolbar.

### Subtasks

| #  | Task                          | Details                                                                                                                                                                                                     |
|----|-------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 4.1| **Create SplitView layout**   | New `MarkdownSplitView.kt` composable using `Row` with two panes. Left: `EditorHost`. Right: `MarkdownPreviewScreen`. `Modifier.weight()` for 50/50. Add draggable divider with `Modifier.pointerInput`    |
| 4.2| **Live preview updates**      | Add `snapshotFlow`/`LaunchedEffect` watching editor text (debounced 300ms) and re-rendering preview via `Markwon.setMarkdown()`                                                                             |
| 4.3| **View mode state**           | `MarkdownViewMode` enum: `EDITOR`, `PREVIEW`, `SPLIT`. Store in ViewModel. Segmented toggle buttons in toolbar: Edit \| Split \| Preview                                                                    |
| 4.4| **Responsive layout**         | Portrait/narrow → default to toggle mode. Landscape/wide → default to split. Configuration in settings                                                                                                      |
| 4.5| **Markdown formatting toolbar** | New `MarkdownToolbar.kt` shown above editor for `.md` files. Buttons: Bold, Italic, Heading, Link, Code, Code block, List, Ordered list, Task list, Blockquote, HR                                           |
| 4.6| **Wire toolbar to editor**    | Each button inserts/wraps text at cursor(s) via `codeEditor.commitText()` or `codeEditor.getText().insert()`                                                                                                 |
| 4.7| **Scroll sync (stretch)**     | Map editor line numbers to heading positions in preview. Bidirectional scroll on heading anchors. Best-effort                                                                                                 |
| 4.8| **Performance optimization**  | Debounce preview re-renders. Limit preview text length. Pagination for very large files                                                                                                                     |
| 4.9| **Update PROGRESS.md**        | Document sprint 4 completion, describe split view and toolbar features                                                                                                                                      |

### Key Files to Modify

- `app/src/main/kotlin/com/androtext/app/ui/screens/EditorScreen.kt` — integrate split view
- `app/src/main/kotlin/com/androtext/app/ui/viewmodel/EditorViewModel.kt` — view mode state
- `app/src/main/kotlin/com/androtext/app/ui/screens/SettingsScreen.kt` — markdown settings
- New: `app/src/main/kotlin/com/androtext/app/ui/screens/MarkdownSplitView.kt`
- New: `app/src/main/kotlin/com/androtext/app/ui/screens/MarkdownToolbar.kt`

---

## Sprint 5: Multi-Cursor — Foundation

**Goal**: Implement the core multi-cursor model, Alt+Click to add cursors, and
basic multi-cursor typing.

### Subtasks

| #   | Task                               | Details                                                                                                                                                                                                                      |
|-----|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 5.1 | **Create MultiCursorManager**      | New class in `core/render` maintaining a sorted list of `CursorInfo(position, selectionStart, selectionEnd)`. Primary cursor (index 0) is Sora's native cursor. Secondary cursors managed by us                              |
| 5.2 | **Custom InputConnection**         | Create `MultiCursorInputConnection` wrapping Sora's default `InputConnection`. Intercept `commitText()`, `deleteSurroundingText()`, `sendKeyEvent()` — apply to all cursors in reverse offset order                          |
| 5.3 | **Alt+Click gesture handler**      | In `SoraEditorHost`, override `onTouchEvent()`. Detect Alt+Click. Convert touch coords to line/col via `codeEditor.getPointLineColumn()`. Add cursor via `MultiCursorManager.addCursor()`                                   |
| 5.4 | **Multi-cursor rendering**         | Extend `SoraTextRenderer.render()` to draw additional carets and selections. For each secondary cursor draw a blinking caret line and selection highlight                                                                     |
| 5.5 | **Multi-cursor insert**            | When `commitText()` is called, iterate all cursors in reverse position order and insert at each. Adjust subsequent cursor positions after each insert                                                                        |
| 5.6 | **Multi-cursor delete/backspace**  | When `deleteSurroundingText()` is called, apply at all cursor positions. Handle edge cases (position 0, overlapping selections)                                                                                              |
| 5.7 | **Escape to clear**                | When Escape is pressed, clear all secondary cursors keeping only the primary                                                                                                                                                |
| 5.8 | **Cursor position sync**           | After each edit, recalculate cursor positions (they shift when text changes before them). Maintain invariants: sorted, no duplicates, no overlapping                                                                         |
| 5.9 | **Visual cursor animations**       | Animate cursor appearance (fade-in for new cursors). Different opacity/style for secondary vs primary                                                                                                                        |
| 5.10| **Update PROGRESS.md**             | Document sprint 5 completion, describe Alt+Click and basic multi-cursor editing                                                                                                                                              |

### Key Files to Modify

- `core/render/src/main/kotlin/com/androtext/core/render/SoraEditorHost.kt` — touch, InputConnection
- `core/render/src/main/kotlin/com/androtext/core/render/SoraTextRenderer.kt` — cursor rendering
- New: `core/render/src/main/kotlin/com/androtext/core/render/MultiCursorManager.kt`
- New: `core/render/src/main/kotlin/com/androtext/core/render/MultiCursorInputConnection.kt`
- New: `core/render/src/main/kotlin/com/androtext/core/render/CursorInfo.kt`

### Technical Challenges

- Sora's `InputConnection` is the main interaction point — wrapping correctly is critical
- Position recalculation after multi-cursor edits must be precise
- Sora's internal cursor state must be kept in sync with multi-cursor model

---

## Sprint 6: Multi-Cursor — Advanced Features

**Goal**: Add Ctrl+D (select next occurrence), block/column selection, and
multi-cursor copy/cut/paste.

### Subtasks

| #   | Task                               | Details                                                                                                                                                                                                                      |
|-----|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 6.1 | **Ctrl+D select-next-occurrence** | On Ctrl+D: (1) if nothing selected, select word at cursor. (2) find next occurrence of selected text. (3) add cursor with that text selected. (4) wrap around. Notify when no more occurrences                              |
| 6.2 | **Ctrl+Shift+L select-all**       | Find all occurrences of current selection/word and add a cursor at each                                                                                                                                                      |
| 6.3 | **Block/column selection mode**   | Override touch for Alt+Shift+drag. Track rectangular region by start/end line+column. Select text in rectangle. Add cursor at start of each line in block                                                                   |
| 6.4 | **Block selection rendering**     | Draw translucent rectangle highlight over block selection in `SoraTextRenderer`                                                                                                                                              |
| 6.5 | **Block typing**                  | In block mode, insert typed character at same column on every selected line                                                                                                                                                  |
| 6.6 | **Multi-cursor copy**             | On Ctrl+C with multiple cursors: copy all selections joined by newlines (or block as-is). Custom clipboard format                                                                                                            |
| 6.7 | **Multi-cursor cut**              | On Ctrl+X: copy all selections, then delete them in reverse order                                                                                                                                                            |
| 6.8 | **Multi-cursor paste**            | On Ctrl+V: if clipboard lines count matches cursor count, paste one line per cursor. Otherwise paste full clipboard at each cursor                                                                                          |
| 6.9 | **Undo/redo integration**         | Group all edits from a single multi-cursor operation into one undo step. Sora's undo stack should treat multi-cursor edit as atomic                                                                                          |
| 6.10| **Edge cases**                    | Handle: merging cursors at same position, cursors crossing during edits, empty selections, cap at ~100 cursors for performance                                                                                               |
| 6.11| **Keyboard shortcut overlay**     | Brief tooltip listing multi-cursor shortcuts: Alt+Click, Ctrl+D, Ctrl+Shift+L, Alt+Shift+Drag, Escape                                                                                                                       |
| 6.12| **Update PROGRESS.md**            | Document sprint 6 completion, list all multi-cursor features and shortcuts                                                                                                                                                   |

### Key Files to Modify

- `core/render/.../MultiCursorManager.kt` — occurrence search, block selection
- `core/render/.../MultiCursorInputConnection.kt` — copy/cut/paste
- `core/render/.../SoraEditorHost.kt` — key event handling
- `core/render/.../SoraTextRenderer.kt` — block selection rendering
- New: `core/render/.../BlockSelection.kt`

---

## Sprint 7: Integration, Polish & Settings Persistence

**Goal**: Final integration, settings persistence, performance, and polish.

### Subtasks

| #  | Task                         | Details                                                                                                                                                                                                      |
|----|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 7.1| **Persist all settings**     | Migrate all `EditorConfig` settings to DataStore. Load on startup, save on change                                                                                                                            |
| 7.2| **E2E integration test**     | Open each supported file type, verify highlighting. Switch themes. Open markdown, toggle preview/split. Use multi-cursor in each mode                                                                       |
| 7.3| **Performance profiling**    | Test with 10K+ line files. Profile grammar parsing, theme loading, markdown rendering, multi-cursor with 50+ cursors                                                                                        |
| 7.4| **Memory optimization**      | Lazy-load grammars, LRU cache. Lazy-load themes. Release markdown preview resources when not visible                                                                                                         |
| 7.5| **Loading states**           | Spinners/skeletons for: grammar loading, theme switching, markdown preview for large files                                                                                                                   |
| 7.6| **Error handling**           | Graceful fallbacks: grammar fail → plain text, theme corrupt → default, markdown fail → raw text with error                                                                                                  |
| 7.7| **Settings UI polish**       | Reorganize into sections: Editor (font, line numbers, tab width, wrap, highlight), Appearance (theme picker), Markdown (view mode). Section headers                                                          |
| 7.8| **Accessibility**            | TalkBack for theme picker and markdown toolbar. Color contrast in all themes. System font scaling                                                                                                            |
| 7.9| **Final PROGRESS.md**        | Complete status of all features. Known limitations. Future work                                                                                                                                             |

---

## Dependency Summary

| Sprint | New Dependencies                                                              |
|--------|-------------------------------------------------------------------------------|
| 1      | None (uses existing `language-textmate`)                                      |
| 2      | None (activates existing `datastore-preferences`)                             |
| 3      | `io.noties.markwon:core`, `image`, `ext-tables`, `ext-tasklist`, `syntax-highlight` |
| 4      | None                                                                          |
| 5      | None                                                                          |
| 6      | None                                                                          |
| 7      | None                                                                          |

## Risk Assessment

| Risk                                                         | Mitigation                                                                                  |
|--------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| TextMate grammars have licensing restrictions                | Use VS Code built-in grammars (MIT) or source from textmate GitHub org                      |
| Multi-cursor InputConnection may conflict with Sora          | Extensive testing with Sora 0.24.5. May need upstream contribution                          |
| Markwon + Compose may have performance issues                | Debounce updates, limit preview length, benchmark early                                     |
| Block selection touch handling may conflict with Sora        | Careful event interception order, may need to disable Sora selection temporarily            |
| Theme-to-Compose ColorScheme mapping may look poor           | Manual color curation for each theme's Material 3 mapping                                   |
