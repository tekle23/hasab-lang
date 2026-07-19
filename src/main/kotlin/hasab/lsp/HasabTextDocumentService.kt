package hasab.lsp

import hasab.lsp.codeaction.CodeActionEngine
import hasab.lsp.completion.CompletionEngine
import hasab.lsp.diagnostics.DiagnosticEngine
import hasab.lsp.formatting.FormattingEngine
import hasab.lsp.highlighting.DocumentHighlightEngine
import hasab.lsp.hover.HoverEngine
import hasab.lsp.definition.DefinitionEngine
import hasab.lsp.logging.LspLogger
import hasab.lsp.logging.PerformanceMetrics
import hasab.lsp.references.ReferenceEngine
import hasab.lsp.rename.RenameEngine
import hasab.lsp.signature.SignatureEngine
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

public class HasabTextDocumentService(
    private val workspaceIndex: WorkspaceIndex,
    private val logger: LspLogger,
    private val metrics: PerformanceMetrics,
) : TextDocumentService {

    private val diagnosticEngine = DiagnosticEngine()
    private val completionEngine = CompletionEngine(workspaceIndex)
    private val hoverEngine = HoverEngine()
    private val definitionEngine = DefinitionEngine(workspaceIndex)
    private val referenceEngine = ReferenceEngine(workspaceIndex)
    private val renameEngine = RenameEngine(workspaceIndex)
    private val signatureEngine = SignatureEngine()
    private val formattingEngine = FormattingEngine()
    private val codeActionEngine = CodeActionEngine(formattingEngine)
    private val highlightEngine = DocumentHighlightEngine()

    @Volatile
    public var client: org.eclipse.lsp4j.services.LanguageClient? = null

    public fun getDiagnostics(uri: String): List<Diagnostic> {
        val state = workspaceIndex.getDocument(uri) ?: return emptyList()
        return metrics.measure("diagnostics") {
            diagnosticEngine.computeDiagnostics(state)
        }
    }

    override fun didOpen(params: org.eclipse.lsp4j.DidOpenTextDocumentParams) {
        val uri = params.textDocument.uri
        val content = params.textDocument.text
        val version = params.textDocument.version
        val languageId = params.textDocument.languageId

        logger.info("Document opened: $uri (v$version)")

        val state = DocumentState(uri, languageId, version)
        state.updateContent(content, version)
        workspaceIndex.addDocument(state)

        publishDiagnostics(uri)
    }

    override fun didChange(params: org.eclipse.lsp4j.DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        val version = params.textDocument.version

        val state = workspaceIndex.getDocument(uri) ?: return
        val oldContent = state.content

        val fullText = params.contentChanges.firstOrNull()?.text
        if (fullText != null) {
            val changeType = state.detectChangeType(oldContent, fullText)
            val (startLine, endLine) = state.getChangeRange(oldContent, fullText)
            state.recordChange(startLine, endLine, changeType)
            state.updateContent(fullText, version)

            when (changeType) {
                ChangeType.NON_SEMANTIC -> {
                    logger.info("Non-semantic change at lines $startLine-$endLine, skipping re-analysis")
                    return
                }
                ChangeType.SEMANTIC -> {
                    state.invalidate()
                }
                ChangeType.STRUCTURAL -> {
                    state.invalidate()
                }
            }
        } else {
            state.invalidate()
        }

        workspaceIndex.updateDocument(state)
        publishDiagnostics(uri)
    }

    override fun didClose(params: org.eclipse.lsp4j.DidCloseTextDocumentParams) {
        val uri = params.textDocument.uri
        logger.info("Document closed: $uri")
        workspaceIndex.removeDocument(uri)
        client?.publishDiagnostics(org.eclipse.lsp4j.PublishDiagnosticsParams(uri, emptyList()))
    }

    override fun didSave(params: org.eclipse.lsp4j.DidSaveTextDocumentParams) {
        val uri = params.textDocument.uri
        logger.info("Document saved: $uri")
        publishDiagnostics(uri)
    }

    private fun publishDiagnostics(uri: String) {
        val diagnostics = getDiagnostics(uri)
        client?.publishDiagnostics(org.eclipse.lsp4j.PublishDiagnosticsParams(uri, diagnostics))
    }

    override fun completion(position: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("completion") {
                val state = workspaceIndex.getDocument(position.textDocument.uri)
                    ?: return@supplyAsync Either.forLeft(emptyList<CompletionItem>())
                val items = completionEngine.computeCompletions(state, position.position)
                Either.forLeft(items)
            }
        }
    }

    override fun resolveCompletionItem(item: CompletionItem): CompletableFuture<CompletionItem> {
        return CompletableFuture.completedFuture(item)
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover> {
        return CompletableFuture.supplyAsync {
            metrics.measure("hover") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                state?.let { hoverEngine.computeHover(it, params.position) } ?: Hover()
            }
        }
    }

    override fun definition(params: DefinitionParams): CompletableFuture<Either<List<org.eclipse.lsp4j.Location>, List<org.eclipse.lsp4j.LocationLink>>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("definition") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                    ?: return@supplyAsync Either.forLeft(emptyList<org.eclipse.lsp4j.Location>())
                val location = definitionEngine.findDefinition(state, params.position)
                Either.forLeft(if (location != null) listOf(location) else emptyList())
            }
        }
    }

    override fun references(params: ReferenceParams): CompletableFuture<List<org.eclipse.lsp4j.Location>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("references") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                    ?: return@supplyAsync emptyList<org.eclipse.lsp4j.Location>()
                referenceEngine.findReferences(state, params.position, params.context.isIncludeDeclaration)
            }
        }
    }

    override fun documentHighlight(params: DocumentHighlightParams): CompletableFuture<List<DocumentHighlight>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("highlight") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                    ?: return@supplyAsync emptyList<DocumentHighlight>()
                highlightEngine.computeHighlights(state, params.position)
            }
        }
    }

    override fun documentSymbol(params: DocumentSymbolParams): CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("documentSymbol") {
                val symbols = workspaceIndex.getSymbolsForFile(params.textDocument.uri)
                symbols.map { sym: WorkspaceIndex.IndexedSymbol ->
                    Either.forRight<SymbolInformation, DocumentSymbol>(DocumentSymbol().apply {
                        name = sym.name
                        kind = when (sym.kind) {
                            hasab.compiler.semantic.SymbolKind.FUNCTION -> org.eclipse.lsp4j.SymbolKind.Function
                            hasab.compiler.semantic.SymbolKind.VARIABLE -> org.eclipse.lsp4j.SymbolKind.Variable
                            hasab.compiler.semantic.SymbolKind.STRUCT -> org.eclipse.lsp4j.SymbolKind.Struct
                            hasab.compiler.semantic.SymbolKind.ENUM -> org.eclipse.lsp4j.SymbolKind.Enum
                            hasab.compiler.semantic.SymbolKind.TRAIT -> org.eclipse.lsp4j.SymbolKind.Interface
                            hasab.compiler.semantic.SymbolKind.TYPE_ALIAS -> org.eclipse.lsp4j.SymbolKind.TypeParameter
                            hasab.compiler.semantic.SymbolKind.MODULE -> org.eclipse.lsp4j.SymbolKind.Module
                            hasab.compiler.semantic.SymbolKind.PARAMETER -> org.eclipse.lsp4j.SymbolKind.Variable
                            hasab.compiler.semantic.SymbolKind.FIELD -> org.eclipse.lsp4j.SymbolKind.Field
                            hasab.compiler.semantic.SymbolKind.VARIANT -> org.eclipse.lsp4j.SymbolKind.EnumMember
                        }
                        range = org.eclipse.lsp4j.Range(
                            org.eclipse.lsp4j.Position(sym.range.start.line - 1, sym.range.start.column - 1),
                            org.eclipse.lsp4j.Position(sym.range.end.line - 1, sym.range.end.column - 1),
                        )
                        selectionRange = range
                    })
                }
            }
        }
    }

    override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp> {
        return CompletableFuture.supplyAsync {
            metrics.measure("signatureHelp") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                state?.let { signatureEngine.computeSignatureHelp(it, params.position) } ?: SignatureHelp()
            }
        }
    }

    override fun formatting(params: org.eclipse.lsp4j.DocumentFormattingParams): CompletableFuture<List<TextEdit>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("formatting") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                    ?: return@supplyAsync emptyList<TextEdit>()
                formattingEngine.formatDocument(state)
            }
        }
    }

    override fun rangeFormatting(params: org.eclipse.lsp4j.DocumentRangeFormattingParams): CompletableFuture<List<TextEdit>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("rangeFormatting") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                    ?: return@supplyAsync emptyList<TextEdit>()
                formattingEngine.formatRange(state, params.range)
            }
        }
    }

    override fun onTypeFormatting(params: org.eclipse.lsp4j.DocumentOnTypeFormattingParams): CompletableFuture<List<TextEdit>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("onTypeFormatting") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                    ?: return@supplyAsync emptyList<TextEdit>()
                formattingEngine.formatOnType(state, params.position, params.ch)
            }
        }
    }

    override fun codeAction(params: CodeActionParams): CompletableFuture<List<Either<Command, CodeAction>>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("codeAction") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                    ?: return@supplyAsync emptyList<Either<Command, CodeAction>>()
                val actions = codeActionEngine.computeCodeActions(state, params.range, params.context.diagnostics)
                actions.map { Either.forRight<Command, CodeAction>(it) }
            }
        }
    }

    override fun rename(params: RenameParams): CompletableFuture<WorkspaceEdit> {
        return CompletableFuture.supplyAsync {
            metrics.measure("rename") {
                val state = workspaceIndex.getDocument(params.textDocument.uri)
                state?.let { renameEngine.computeRename(it, params.position, params.newName) } ?: WorkspaceEdit()
            }
        }
    }
}
