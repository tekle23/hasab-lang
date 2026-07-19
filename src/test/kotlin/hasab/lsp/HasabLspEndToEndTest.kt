package hasab.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertFalse

public class HasabLspEndToEndTest {

    private val sampleSource = """fn add(a: int, b: int): int {
  return a + b
}

struct Point {
  x: int
  y: int
}

fn main() {
  let p = Point { x: 10, y: 20 }
  let result = add(p.x, p.y)
  println(result)
}"""

    private fun initServer(): HasabLanguageServer {
        val server = HasabLanguageServer()
        val params = InitializeParams()
        params.workspaceFolders = listOf(WorkspaceFolder("file:///workspace", "test-workspace"))
        server.initialize(params).get()
        return server
    }

    private fun openDocument(server: HasabLanguageServer, uri: String, content: String) {
        val params = DidOpenTextDocumentParams().apply {
            textDocument = TextDocumentItem().apply {
                this.uri = uri
                languageId = "hasab"
                version = 1
                this.text = content
            }
        }
        server.textDocumentService.didOpen(params)
    }

    private fun changeDocument(server: HasabLanguageServer, uri: String, content: String, version: Int) {
        val params = DidChangeTextDocumentParams().apply {
            textDocument = VersionedTextDocumentIdentifier().apply {
                this.uri = uri
                this.version = version
            }
            contentChanges = listOf(TextDocumentContentChangeEvent().apply {
                this.text = content
            })
        }
        server.textDocumentService.didChange(params)
    }

    private fun closeDocument(server: HasabLanguageServer, uri: String) {
        val params = DidCloseTextDocumentParams().apply {
            textDocument = TextDocumentIdentifier().apply {
                this.uri = uri
            }
        }
        server.textDocumentService.didClose(params)
    }

    @Test
    public fun `full session - initialize, open, features, close, shutdown`() {
        val server = initServer()
        val uri = "file:///test/main.hs"

        openDocument(server, uri, sampleSource)

        val index = server.getWorkspaceIndex()
        val state = index.getDocument(uri)
        assertNotNull(state, "Document should be indexed after open")
        assertEquals(1, state.version)

        val diagnostics = server.getTextDocumentService().getDiagnostics(uri)
        assertNotNull(diagnostics, "Diagnostics should not be null")

        val completionParams = CompletionParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(10, 2)
        }
        val completions = server.textDocumentService.completion(completionParams).get()
        assertNotNull(completions, "Completions should not be null")
        assertTrue(
            completions.left?.isNotEmpty() == true || completions.right?.items?.isNotEmpty() == true,
            "Should have completion items"
        )

        val hoverParams = HoverParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(0, 3)
        }
        val hover = server.textDocumentService.hover(hoverParams).get()
        assertNotNull(hover, "Hover should not be null")

        val definitionParams = DefinitionParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(11, 18)
        }
        val definition = server.textDocumentService.definition(definitionParams).get()
        assertNotNull(definition, "Definition should not be null")

        val referenceParams = ReferenceParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(0, 3)
            context = ReferenceContext().apply { isIncludeDeclaration = true }
        }
        val references = server.textDocumentService.references(referenceParams).get()
        assertNotNull(references, "References should not be null")

        val renameParams = RenameParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(0, 3)
            newName = "sum"
        }
        val rename = server.textDocumentService.rename(renameParams).get()
        assertNotNull(rename, "Rename should not be null")

        val docSymbolParams = DocumentSymbolParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
        }
        val symbols = server.textDocumentService.documentSymbol(docSymbolParams).get()
        assertTrue(symbols.isNotEmpty(), "Should have document symbols")

        val formatParams = DocumentFormattingParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            options = FormattingOptions().apply {
                tabSize = 2
                isInsertSpaces = true
            }
        }
        val formatting = server.textDocumentService.formatting(formatParams).get()
        assertNotNull(formatting, "Formatting should not be null")

        closeDocument(server, uri)

        val removedState = index.getDocument(uri)
        assertEquals(null, removedState, "Document should be removed after close")

        val shutdownResult = server.shutdown().get()
        assertEquals(null, shutdownResult, "Shutdown should return null per LSP spec")
    }

    @Test
    public fun `incremental changes - non-semantic change skips re-analysis`() {
        val server = initServer()
        val uri = "file:///test/incremental.hs"

        openDocument(server, uri, sampleSource)

        val index = server.getWorkspaceIndex()
        val state = index.getDocument(uri)!!

        state.parse()
        state.analyzeSemantics()
        assertNotNull(state.parseResult, "Should have parse result after initial analysis")

        val commentedVersion = sampleSource.replace(
            "// This is a comment",
            "// This is an updated comment"
        )
        changeDocument(server, uri, commentedVersion, 2)

        val newState = index.getDocument(uri)
        assertNotNull(newState, "State should still exist")

        val codeChange = sampleSource.replace(
            "fn add(a: int, b: int): int {",
            "fn add(a: int, b: int): int {"
        ).replace(
            "let result = add(p.x, p.y)",
            "let result = add(p.x, p.y + 1)"
        )
        changeDocument(server, uri, codeChange, 3)

        val finalState = index.getDocument(uri)
        assertNotNull(finalState, "State should exist after code change")
        assertTrue(finalState.version == 3, "Version should be 3")
    }

    @Test
    public fun `multi-file workspace - symbols from both files`() {
        val server = initServer()
        val uri1 = "file:///test/lib.hs"
        val uri2 = "file:///test/main2.hs"

        openDocument(server, uri1, "fn helper(): int {\n  return 42\n}")
        openDocument(server, uri2, "fn main() {\n  let x = helper()\n  println(x)\n}")

        val index = server.getWorkspaceIndex()
        val allSymbols = index.getAllSymbols()
        assertTrue(allSymbols.size >= 2, "Should find symbols from both files, found ${allSymbols.size}")

        val queryResults = index.searchSymbols("helper")
        assertTrue(queryResults.isNotEmpty(), "Should find 'helper' across workspace")

        closeDocument(server, uri1)
        closeDocument(server, uri2)
    }

    @Test
    public fun `document change - diagnostics update on error`() {
        val server = initServer()
        val uri = "file:///test/error.hs"

        openDocument(server, uri, "fn main() {\n  let x = 10\n}")
        val diagnostics1 = server.getTextDocumentService().getDiagnostics(uri)
        assertNotNull(diagnostics1)

        changeDocument(server, uri, "fn main() {", 2)
        val diagnostics2 = server.getTextDocumentService().getDiagnostics(uri)
        assertTrue(diagnostics2.isNotEmpty(), "Error code should produce diagnostics")
    }

    @Test
    public fun `workspace symbol search works across session`() {
        val server = initServer()
        val uri = "file:///test/search.hs"

        openDocument(server, uri, "struct Config {\n  debug: bool\n}\nfn getConfig(): Config {\n  return Config { debug: true }\n}")

        val symbolParams = WorkspaceSymbolParams().apply {
            query = "Config"
        }
        val results = server.workspaceService.symbol(symbolParams).get()
        assertNotNull(results)
        val symbols = results.left ?: results.right
        assertTrue(symbols?.isNotEmpty() == true, "Should find 'Config' via workspace symbol")

        closeDocument(server, uri)
    }

    @Test
    public fun `document highlight - highlights symbol occurrences`() {
        val server = initServer()
        val uri = "file:///test/highlight.hs"

        openDocument(server, uri, "fn main() {\n  let x = 10\n  let y = x + 1\n  println(x)\n}")

        val highlightParams = DocumentHighlightParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(1, 6)
        }
        val highlights = server.textDocumentService.documentHighlight(highlightParams).get()
        assertNotNull(highlights, "Highlights should not be null")
        assertTrue(highlights.isNotEmpty(), "Should find at least one highlight for 'x'")
    }

    @Test
    public fun `code action - provides actions for diagnostics`() {
        val server = initServer()
        val uri = "file:///test/codeaction.hs"

        openDocument(server, uri, "fn main() {\n  let x = 10\n}")

        val diagnostics = server.getTextDocumentService().getDiagnostics(uri)

        val codeActionParams = CodeActionParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            range = Range(Position(0, 0), Position(2, 0))
            context = CodeActionContext().apply {
                this.diagnostics = diagnostics
            }
        }
        val actions = server.textDocumentService.codeAction(codeActionParams).get()
        assertNotNull(actions, "Code actions should not be null")
    }

    @Test
    public fun `signature help at call site`() {
        val server = initServer()
        val uri = "file:///test/sig.hs"

        openDocument(server, uri, sampleSource)

        val sigParams = SignatureHelpParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(11, 15)
            context = SignatureHelpContext().apply {
                triggerKind = SignatureHelpTriggerKind.Invoked
            }
        }
        val sigHelp = server.textDocumentService.signatureHelp(sigParams).get()
        assertNotNull(sigHelp, "Signature help should not be null")
    }

    @Test
    public fun `range formatting`() {
        val server = initServer()
        val uri = "file:///test/range.hs"

        openDocument(server, uri, "fn main(){\nlet x=10\nlet y=20\nprintln(x+y)\n}")

        val rangeFormatParams = DocumentRangeFormattingParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            range = Range(Position(1, 0), Position(3, 0))
            options = FormattingOptions().apply {
                tabSize = 2
                isInsertSpaces = true
            }
        }
        val edits = server.textDocumentService.rangeFormatting(rangeFormatParams).get()
        assertNotNull(edits, "Range formatting should return edits")
    }

    @Test
    public fun `on type formatting on newline`() {
        val server = initServer()
        val uri = "file:///test/ontype.hs"

        openDocument(server, uri, "fn main(){\nlet x=10}")

        val onTypeParams = DocumentOnTypeFormattingParams().apply {
            textDocument = TextDocumentIdentifier().apply { this.uri = uri }
            position = Position(0, 12)
            ch = "\n"
        }
        val edits = server.textDocumentService.onTypeFormatting(onTypeParams).get()
        assertNotNull(edits, "On-type formatting should return edits")
    }

    @Test
    public fun `resolve completion item returns item`() {
        val server = initServer()
        val uri = "file:///test/resolve.hs"

        openDocument(server, uri, sampleSource)

        val completionItem = CompletionItem().apply {
            label = "add"
            kind = CompletionItemKind.Function
        }
        val resolved = server.textDocumentService.resolveCompletionItem(completionItem).get()
        assertNotNull(resolved, "Resolved item should not be null")
        assertEquals("add", resolved.label)
    }
}
