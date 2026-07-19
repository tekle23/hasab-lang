# HASAB Language Server — API Reference

Complete API documentation for the HASAB Language Server Protocol (LSP) implementation.

---

## Table of Contents

1. [Server Entry Point](#server-entry-point)
2. [Server Core](#server-core)
3. [Document Management](#document-management)
4. [Feature Engines](#feature-engines)
5. [Infrastructure](#infrastructure)
6. [Extension Points](#extension-points)
7. [Configuration](#configuration)

---

## Server Entry Point

### `HasabLspLauncher`

**Package:** `hasab.lsp`

Launches the LSP server over stdio.

```kotlin
// Main entry point
HasabLspLauncher.main(args)

// Or programmatic launch
val launcher = HasabLspLauncher()
launcher.launchStdio()  // Uses System.in/out
launcher.launch(input, output)  // Custom streams
```

---

## Server Core

### `HasabLanguageServer`

**Implements:** `org.eclipse.lsp4j.services.LanguageServer`

The central LSP server coordinating all services.

| Method | Returns | Description |
|--------|---------|-------------|
| `initialize(InitializeParams)` | `InitializeResult` | Server initialization with capabilities |
| `shutdown()` | `CompletableFuture<Any>` | Graceful shutdown |
| `exit()` | `Unit` | Terminate process |
| `getTextDocumentService()` | `HasabTextDocumentService` | Text document operations |
| `getWorkspaceService()` | `HasabWorkspaceService` | Workspace operations |
| `getWorkspaceIndex()` | `WorkspaceIndex` | Symbol index |
| `getPerformanceMetrics()` | `PerformanceMetrics` | Performance data |

**Capabilities registered:**
- Text document sync (Full)
- Completion (with resolve)
- Hover
- Go to Definition
- Find References
- Document Highlight
- Document Symbol
- Signature Help
- Formatting (full, range, on-type)
- Code Actions
- Rename (with prepare)
- Workspace Symbols
- Execute Commands

### `HasabTextDocumentService`

**Extends:** `org.eclipse.lsp4j.services.TextDocumentService`

Handles all text document LSP requests.

| Method | Signature |
|--------|-----------|
| `didOpen(params)` | Opens and indexes a document |
| `didChange(params)` | Updates document content, re-analyzes |
| `didClose(params)` | Removes document from index |
| `didSave(params)` | Re-publishes diagnostics |
| `completion(params)` | Returns completion items |
| `hover(params)` | Returns hover information |
| `definition(params)` | Returns go-to-definition location |
| `references(params)` | Returns all references |
| `documentHighlight(params)` | Returns highlighted references |
| `documentSymbol(params)` | Returns document symbols |
| `signatureHelp(params)` | Returns function signature help |
| `formatting(params)` | Returns formatting edits |
| `rangeFormatting(params)` | Returns range formatting edits |
| `onTypeFormatting(params)` | Returns on-type formatting edits |
| `codeAction(params)` | Returns code actions |
| `rename(params)` | Returns workspace rename edit |

### `HasabWorkspaceService`

**Extends:** `org.eclipse.lsp4j.services.WorkspaceService`

Handles workspace-level operations.

| Method | Description |
|--------|-------------|
| `didChangeWorkspaceFolders` | Tracks workspace folder changes |
| `didChangeConfiguration` | Handles configuration changes |
| `didChangeWatchedFiles` | Handles external file changes |
| `symbol(params)` | Workspace-wide symbol search |
| `executeCommand(params)` | Executes server commands |

---

## Document Management

### `DocumentState`

**Package:** `hasab.lsp`

Manages the state of a single open document, including incremental analysis caching.

```kotlin
class DocumentState(uri: String, languageId: String, initialVersion: Int)
```

**Properties:**

| Property | Type | Description |
|----------|------|-------------|
| `uri` | `String` | Document URI |
| `languageId` | `String` | Language identifier (`"hasab"`) |
| `version` | `Int` | Document version |
| `content` | `String` | Current source text |
| `fileName` | `String` | Extracted file name |
| `lexerResult` | `LexerResult?` | Cached lexer output |
| `parseResult` | `ParseResult?` | Cached parse result |
| `semanticModel` | `SemanticModel?` | Cached semantic analysis |
| `typeCheckResult` | `TypeCheckResult?` | Cached type check result |

**Methods:**

| Method | Returns | Description |
|--------|---------|-------------|
| `updateContent(content, version)` | `Unit` | Update source text |
| `parse()` | `ParseResult` | Lex and parse |
| `analyzeSemantics()` | `SemanticModel` | Run semantic analysis |
| `typeCheck()` | `TypeCheckResult` | Run type checking |
| `fullAnalysis()` | `FullAnalysisResult` | All three passes |
| `invalidate()` | `Unit` | Clear all caches |

### `WorkspaceIndex`

**Package:** `hasab.lsp`

Workspace-wide symbol index for cross-file operations.

```kotlin
class WorkspaceIndex
```

**Methods:**

| Method | Returns | Description |
|--------|---------|-------------|
| `addDocument(state)` | `Unit` | Index a document |
| `removeDocument(uri)` | `Unit` | Remove from index |
| `updateDocument(state)` | `Unit` | Re-index a document |
| `getDocument(uri)` | `DocumentState?` | Get document by URI |
| `getAllDocuments()` | `Map<String, DocumentState>` | All open documents |
| `getSymbolsForFile(uri)` | `List<IndexedSymbol>` | Symbols in one file |
| `getAllSymbols()` | `List<IndexedSymbol>` | All indexed symbols |
| `getSymbolsByName(name)` | `List<IndexedSymbol>` | Find by name |
| `getSymbolsByKind(kind)` | `List<IndexedSymbol>` | Filter by kind |
| `searchSymbols(query)` | `List<IndexedSymbol>` | Fuzzy search |
| `findUsages(name)` | `List<UsageLocation>` | All usages of a name |

**Data classes:**

```kotlin
data class IndexedSymbol(
    val name: String,
    val kind: SymbolKind,
    val range: SourceRange,
    val uri: String,
    val fileName: String,
    val docComment: String?,
    val isPublic: Boolean,
)

data class UsageLocation(
    val uri: String,
    val fileName: String,
    val range: SourceRange,
)
```

---

## Feature Engines

### DiagnosticEngine

Converts HASAB compiler diagnostics to LSP format.

```kotlin
class DiagnosticEngine {
    fun computeDiagnostics(state: DocumentState): List<Diagnostic>
}
```

**Sources mapped:**
- `hasab-parser` — Lexer/parser errors
- `hasab-semantic` — Semantic analysis errors/warnings
- `hasab-typechecker` — Type checking errors

**Quick fix data:** Diagnostics from semantic analysis may include fix data in the `data` field for code actions.

### CompletionEngine

Provides auto-completion items.

```kotlin
class CompletionEngine(workspaceIndex: WorkspaceIndex) {
    fun computeCompletions(state: DocumentState, position: Position): List<CompletionItem>
}
```

**Completion categories:**
- **Keywords** (30 HASAB keywords) — `fn`, `let`, `if`, `struct`, etc.
- **Snippets** (8 templates) — `fn-main`, `if-else`, `for-in`, etc.
- **Built-in functions** — `print`, `len`, `typeof`, `to_string`, etc.
- **Symbols** — Functions, variables, types from symbol table and workspace index
- **Field access** — Dot-triggered field completions

### HoverEngine

Provides hover information on symbols.

```kotlin
class HoverEngine {
    fun computeHover(state: DocumentState, position: Position): Hover?
}
```

**Hover content includes:**
- Symbol kind and name
- Documentation from `///` doc comments
- Visibility, file location
- Type annotations (for variables/parameters)

### DefinitionEngine

Go-to-definition for symbols.

```kotlin
class DefinitionEngine(workspaceIndex: WorkspaceIndex) {
    fun findDefinition(state: DocumentState, position: Position): Location?
}
```

**Resolves:** Function calls, variable references, struct/enum names, trait names, type aliases.

### ReferenceEngine

Find all references to a symbol.

```kotlin
class ReferenceEngine(workspaceIndex: WorkspaceIndex) {
    fun findReferences(
        state: DocumentState,
        position: Position,
        includeDeclaration: Boolean
    ): List<Location>
}
```

### RenameEngine

Rename a symbol across all open files.

```kotlin
class RenameEngine(workspaceIndex: WorkspaceIndex) {
    fun prepareRename(state: DocumentState, position: Position): Boolean
    fun computeRename(state: DocumentState, position: Position, newName: String): WorkspaceEdit?
}
```

### SignatureEngine

Function signature help at call sites.

```kotlin
class SignatureEngine {
    fun computeSignatureHelp(state: DocumentState, position: Position): SignatureHelp?
}
```

### FormattingEngine

Code formatting (delegates to `HasabFormatter`).

```kotlin
class FormattingEngine {
    fun formatDocument(state: DocumentState): List<TextEdit>
    fun formatRange(state: DocumentState, range: Range): List<TextEdit>
    fun formatOnType(state: DocumentState, position: Position, ch: String): List<TextEdit>
}
```

### CodeActionEngine

Quick fixes and refactoring actions.

```kotlin
class CodeActionEngine(formattingEngine: FormattingEngine) {
    fun computeCodeActions(
        state: DocumentState,
        range: Range,
        diagnostics: List<Diagnostic>
    ): List<CodeAction>
}
```

**Code action types:**
- Quick fixes from diagnostic fix data
- Declare undeclared variables
- Extract to function
- Organize imports

### DocumentHighlightEngine

Highlights all occurrences of a symbol in the current document.

```kotlin
class DocumentHighlightEngine {
    fun computeHighlights(state: DocumentState, position: Position): List<DocumentHighlight>
}
```

**Highlight kinds:** `Read`, `Write`, `Text`

### WorkspaceSymbolEngine

Workspace-wide symbol search.

```kotlin
class WorkspaceSymbolEngine(workspaceIndex: WorkspaceIndex) {
    fun searchSymbols(query: String): List<SymbolInformation>
    fun getSymbolsByKind(kind: SymbolKind): List<SymbolInformation>
}
```

---

## Infrastructure

### LspLogger

Thread-safe logging with level filtering and callbacks.

```kotlin
class LspLogger(prefix: String, minLevel: Level) {
    enum class Level { DEBUG, INFO, WARNING, ERROR }

    var onLog: ((LogEntry) -> Unit)?

    fun debug(message: String, throwable: Throwable? = null)
    fun info(message: String, throwable: Throwable? = null)
    fun warn(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable? = null)
    fun getRecentLogs(count: Int = 50): List<LogEntry>
    fun getLogsByLevel(level: Level): List<LogEntry>
    fun clear()
}
```

### PerformanceMetrics

Timing and counter metrics for LSP operations.

```kotlin
class PerformanceMetrics {
    fun incrementCounter(name: String)
    fun recordTiming(name: String, durationNs: Long)
    inline fun <T> measure(name: String, block: () -> T): T
    fun getCounter(name: String): Long
    fun getAverageTimingNs(name: String): Double
    fun getCallCount(name: String): Long
    fun getP95TimingNs(name: String): Long
    fun snapshot(): Map<String, MetricSnapshot>
    fun reset()
}
```

**Tracked metrics:**
- `diagnostics` — Diagnostic computation time
- `completion` — Completion generation time
- `hover` — Hover computation time
- `definition` — Go-to-definition time
- `references` — Find references time
- `highlight` — Document highlight time
- `documentSymbol` — Document symbol time
- `signatureHelp` — Signature help time
- `formatting` — Full document formatting time
- `rangeFormatting` — Range formatting time
- `onTypeFormatting` — On-type formatting time
- `codeAction` — Code action computation time
- `rename` — Rename computation time
- `workspaceSymbol` — Workspace symbol search time

---

## Extension Points

### AI-Assisted Features

The architecture supports future AI integration through:

1. **Custom CodeActionEngine extensions** — Add AI-powered code actions (generate doc comments, suggest implementations, refactor suggestions)

2. **CompletionEngine hooks** — Add AI-completion providers:
   ```kotlin
   interface AiCompletionProvider {
       fun suggestCompletions(context: CompletionContext): List<CompletionItem>
   }
   ```

3. **DiagnosticEngine extensions** — Add AI-powered diagnostics (code smell detection, performance hints)

4. **HoverEngine extensions** — Add AI explanations in hover content

5. **Custom commands** — Register via `HasabWorkspaceService.executeCommand`:
   - `hasab.ai.explain` — AI explanation of selected code
   - `hasab.ai.refactor` — AI-assisted refactoring
   - `hasab.ai.testgen` — AI test generation
   - `hasab.ai.docgen` — AI documentation generation

### Logging Hooks

```kotlin
val logger = LspLogger("HasabLSP")
logger.onLog = { entry ->
    // Send to external logging service
    sendToTelemetry(entry)
}
```

### Custom Diagnostics

```kotlin
class CustomDiagnosticEngine : DiagnosticEngine() {
    override fun computeDiagnostics(state: DocumentState): List<Diagnostic> {
        val base = super.computeDiagnostics(state)
        return base + customAnalysis(state)
    }
}
```

---

## Configuration

### `build.gradle.kts`

```kotlin
dependencies {
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.21.0")
}
```

### Editor Configuration (VS Code `.vscode/settings.json`)

```json
{
    "server.path": "path/to/hasab-lsp",
    "hasab.diagnostics.enabled": true,
    "hasab.completion.triggerCharacters": [".", "(", ":"]
}
```

### Server Launch (stdio)

```
java -jar hasab-lsp.jar
```
