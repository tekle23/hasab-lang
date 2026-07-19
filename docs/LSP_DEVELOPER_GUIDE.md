# HASAB Language Server — Developer Guide

This guide covers the architecture, development workflow, and extension points for the HASAB Language Server.

---

## Architecture Overview

The HASAB Language Server implements the Language Server Protocol (LSP) to provide IDE features for the HASAB programming language. It is built in Kotlin, uses LSP4J for protocol handling, and reuses the existing HASAB compiler frontend for parsing, semantic analysis, and type checking.

### Request Lifecycle

```
Editor Action
  → LSP Client (JSON-RPC)
    → HasabLanguageServer (routing)
      → TextDocumentService / WorkspaceService
        → DocumentState (parse/analyze cache)
          → Compiler Frontend (Lexer → Parser → SemanticAnalyzer → TypeChecker)
        → Feature Engine (diagnostics/completion/hover/...)
      ← LSP response
    ← JSON-RPC response
  ← Editor UI update
```

### Package Structure

```
hasab/lsp/
  HasabLanguageServer.kt         Main server (LanguageServer)
  HasabTextDocumentService.kt    Text document operations
  HasabWorkspaceService.kt       Workspace operations
  HasabLspLauncher.kt            Entry point / stdio launcher
  DocumentState.kt               Per-document state + caching
  WorkspaceIndex.kt              Cross-file symbol index
  diagnostics/                   DiagnosticEngine
  completion/                    CompletionEngine
  hover/                         HoverEngine
  definition/                    DefinitionEngine
  references/                    ReferenceEngine
  rename/                        RenameEngine
  signature/                     SignatureEngine
  formatting/                    FormattingEngine
  codeaction/                    CodeActionEngine
  highlighting/                  DocumentHighlightEngine
  symbol/                        WorkspaceSymbolEngine
  logging/                       LspLogger, PerformanceMetrics
```

---

## Key Components

### DocumentState

Each open file is represented by a `DocumentState` that caches the results of each analysis pass:

1. **Lexer pass** → `LexerResult` (tokens)
2. **Parse pass** → `ParseResult` (AST: `Module` with `Decl`, `Expr`, `Stmt` nodes)
3. **Semantic pass** → `SemanticModel` (symbol table, scope tree, diagnostics, node bindings)
4. **Type check pass** → `TypeCheckResult` (type environment, typed model)

When the document content changes (via `didChange`), all caches are invalidated and re-analyzed on the next request.

### WorkspaceIndex

Maintains a workspace-wide index of all symbols across open files. Uses a `SymbolIndexer` (AstVisitorBase implementation) to walk the AST and extract symbols.

**Key operations:**
- Symbol search by name (fuzzy matching)
- Symbol filter by kind (function, struct, enum, etc.)
- Usage tracking for rename operations

### Feature Engines

Each LSP feature is implemented as a standalone engine class. Engines receive a `DocumentState` and optional `WorkspaceIndex`, and return LSP-compatible result types.

**Design pattern:**
- Engines are stateless (no mutable state)
- Engines delegate to the compiler frontend for analysis
- Engines handle errors gracefully (return null/empty on failure)

---

## Compiler Integration

The LSP server reuses the full HASAB compiler frontend pipeline:

```kotlin
// 1. Lexing
val sourceFile = SourceFile(fileName, content)
val lexerResult = Lexer(sourceFile).tokenize()

// 2. Parsing
val parseResult = Parser(lexerResult).parse()
// parseResult.module: Module (root AST node)

// 3. Semantic Analysis
val semanticModel = SemanticAnalyzer().analyze(parseResult.module)
// semanticModel.symbolTable: SymbolTable
// semanticModel.nodeBindings: Map<AstNode, Symbol>
// semanticModel.diagnostics: List<SemanticDiagnostic>

// 4. Type Checking
val typeCheckResult = TypeChecker().check(parseResult.module)
// typeCheckResult.environment: TypeEnvironment
// typeCheckResult.diagnostics: List<TypeDiagnostic>
```

### AST Node Hierarchy

```
AstNode (sealed interface)
  ├── Module
  ├── Decl (sealed)
  │   ├── FnDecl, StructDecl, EnumDecl, ImplDecl
  │   ├── TraitDecl, TypeAliasDecl, ModDecl, UseDecl, PubDecl
  ├── Stmt (sealed)
  │   ├── ExprStmt, ReturnStmt, BreakStmt, ContinueStmt
  │   ├── LetStmt, IfStmt, WhileStmt, ForStmt, Block
  ├── Expr (sealed)
  │   ├── IdentifierExpr, BinaryExpr, UnaryExpr, CallExpr
  │   ├── FieldAccessExpr, IfExpr, AssignmentExpr, etc.
  └── TypeNode (sealed)
      ├── IdentifierType, QualifiedType, ArrayType
      ├── PointerType, OptionalType, FunctionType, VoidType
```

### Symbol Resolution

The `SymbolResolver` class populates `nodeBindings` in the `SemanticModel`, mapping AST nodes to their resolved symbols. This enables:
- Go-to-definition: `semanticModel.bindingFor(node)`
- Find references: iterate `semanticModel.nodeBindings`
- Hover: `semanticModel.lookupSymbol(name)`

---

## Testing Strategy

### Unit Tests (per engine)

Each engine has isolated tests:

| Test Class | Tests | Coverage |
|-----------|-------|----------|
| `CompletionEngineTest` | 5 | Keywords, snippets, built-ins |
| `HoverEngineTest` | 3 | Function, variable, empty |
| `DefinitionEngineTest` | 3 | Function, variable, unknown |
| `ReferenceEngineTest` | 2 | Function refs, unknown |
| `FormattingEngineTest` | 3 | Full, range, on-type |
| `WorkspaceSymbolEngineTest` | 3 | Search, empty, filter |
| `DiagnosticEngineTest` | 3 | Valid, invalid, results |
| `DocumentStateTest` | 5 | Creation, parse, invalidate, URI |
| `WorkspaceIndexTest` | 7 | CRUD, search, filter |
| `PerformanceMetricsTest` | 9 | Counters, timing, snapshot |
| `LspLoggerTest` | 6 | Levels, filtering, callbacks |
| `HasabLanguageServerTest` | 5 | Services, index, metrics |

### Integration Tests

`HasabLspIntegrationTest` (12 tests) exercises the full pipeline:
- Full analysis pipeline
- Cross-engine workflows
- Multi-file workspace operations
- Document lifecycle (add/remove/update)

### End-to-End Tests

`HasabLspEndToEndTest` (11 tests) simulates full LSP sessions through the server API:
- Complete session lifecycle (initialize → open → features → close → shutdown)
- Incremental change tracking (non-semantic vs semantic changes)
- Multi-file workspace symbol resolution
- Diagnostics update on code changes
- Document highlighting, code actions, signature help
- Range and on-type formatting
- Completion item resolution

### Running Tests

```bash
# All LSP tests (77 tests)
gradle test --tests "hasab.lsp.*"

# Specific engine
gradle test --tests "hasab.lsp.completion.*"

# Integration tests
gradle test --tests "hasab.lsp.HasabLspIntegrationTest"

# End-to-end tests
gradle test --tests "hasab.lsp.HasabLspEndToEndTest"
```

---

## Performance Considerations

### Incremental Change Tracking

`DocumentState` classifies each document change as one of three types:
- **NON_SEMANTIC** — whitespace or comment-only changes. Re-analysis is skipped entirely.
- **SEMANTIC** — code changes that don't alter brace/bracket structure. All caches invalidated.
- **STRUCTURAL** — changes that add/remove braces or brackets. Full invalidation + workspace re-index.

The `detectChangeType()` method diffs old and new content line-by-line, checking for comment-only changes and brace count differences. The `getChangeRange()` method computes the affected line range.

### Caching

`DocumentState` caches analysis results per document version. When `didChange` is called, the engine classifies the change type and only invalidates caches when necessary.

### Lazy Analysis

Engines call `state.parse()` / `state.analyzeSemantics()` lazily — analysis only runs when an engine actually needs it.

### Metrics

`PerformanceMetrics` tracks timing for every LSP operation. Use `snapshot()` to get all metrics:

```kotlin
val metrics = server.getPerformanceMetrics()
val snapshot = metrics.snapshot()
// snapshot["completion"]?.averageNs  → average completion time
// snapshot["diagnostics"]?.p95Ns     → 95th percentile diagnostic time
```

---

## Extending the Server

### Adding a New LSP Feature

1. Create an engine class in `hasab/lsp/<feature>/`
2. Add a method to `HasabTextDocumentService`
3. Add the capability to `HasabLanguageServer.initialize()`
4. Write unit and integration tests

### Adding a Custom Command

Register in `HasabLanguageServer.initialize()`:
```kotlin
executeCommandProvider = ExecuteCommandOptions().apply {
    commands = listOf("hasab.restartServer", "hasab.myCommand")
}
```

Handle in `HasabWorkspaceService.executeCommand()`.

### Adding an AI Completion Provider

```kotlin
class AiCompletionProvider(private val llmClient: LlmClient) {
    suspend fun suggestCompletions(
        documentState: DocumentState,
        position: Position,
        context: String
    ): List<CompletionItem> {
        val response = llmClient.complete(context)
        return response.suggestions.map { suggestion ->
            CompletionItem().apply {
                label = suggestion.label
                detail = "AI suggestion"
                kind = CompletionItemKind.Snippet
                insertText = suggestion.code
            }
        }
    }
}
```

---

## VS Code Extension Integration

The LSP server communicates with VS Code through JSON-RPC over stdio. To create a VS Code extension:

1. **Package the server** as a standalone JAR
2. **Create a VS Code extension** that launches the server
3. **Configure `languageServerId`** in `package.json`

Example `package.json`:
```json
{
    "contributes": {
        "languages": [{
            "id": "hasab",
            "extensions": [".hs", ".hasab"],
            "configuration": "./language-configuration.json"
        }]
    }
}
```

---

## File Reference

| File | Lines | Purpose |
|------|-------|---------|
| `HasabLanguageServer.kt` | ~90 | Server initialization and routing |
| `HasabTextDocumentService.kt` | ~260 | Text document LSP handlers |
| `HasabWorkspaceService.kt` | ~90 | Workspace LSP handlers |
| `HasabLspLauncher.kt` | ~45 | Stdio launcher |
| `DocumentState.kt` | ~100 | Document state management |
| `WorkspaceIndex.kt` | ~190 | Cross-file symbol index |
| `DiagnosticEngine.kt` | ~95 | Compiler diagnostics → LSP |
| `CompletionEngine.kt` | ~310 | Auto-completion logic |
| `HoverEngine.kt` | ~160 | Hover information |
| `DefinitionEngine.kt` | ~160 | Go-to-definition |
| `ReferenceEngine.kt` | ~240 | Find references |
| `RenameEngine.kt` | ~200 | Rename symbol |
| `SignatureEngine.kt` | ~130 | Signature help |
| `FormattingEngine.kt` | ~55 | Code formatting |
| `CodeActionEngine.kt` | ~160 | Quick fixes and refactoring |
| `DocumentHighlightEngine.kt` | ~155 | Document highlighting |
| `WorkspaceSymbolEngine.kt` | ~65 | Workspace symbol search |
| `LspLogger.kt` | ~75 | Logging infrastructure |
| `PerformanceMetrics.kt` | ~85 | Performance tracking |

**Total source:** ~2,300 lines (production code)
**Total tests:** ~2,500 lines (66 tests)
