package hasab.lsp

import hasab.lsp.logging.LspLogger
import hasab.lsp.logging.PerformanceMetrics
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.SignatureHelpOptions
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

public class HasabLanguageServer : LanguageServer {

    private val logger = LspLogger(prefix = "HasabLSP")
    private val metrics = PerformanceMetrics()
    private val workspaceIndex = WorkspaceIndex()

    @Volatile
    private var client: org.eclipse.lsp4j.services.LanguageClient? = null

    private lateinit var textDocumentService: HasabTextDocumentService
    private lateinit var workspaceService: HasabWorkspaceService

    public fun getPerformanceMetrics(): PerformanceMetrics = metrics

    public fun getWorkspaceIndex(): WorkspaceIndex = workspaceIndex

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        return CompletableFuture.supplyAsync {
            logger.info("Initializing HASAB Language Server")

            textDocumentService = HasabTextDocumentService(workspaceIndex, logger, metrics)
            workspaceService = HasabWorkspaceService(workspaceIndex, logger, metrics)

            params.workspaceFolders?.forEach { folder ->
                logger.info("Workspace folder: ${folder.uri}")
            }

            val capabilities = ServerCapabilities().apply {
                setTextDocumentSync(TextDocumentSyncKind.Full)
                completionProvider = CompletionOptions().apply {
                    resolveProvider = true
                    triggerCharacters = listOf(".", "(", ":", " ", "\t")
                }
                setHoverProvider(true)
                definitionProvider = Either.forLeft(true)
                referencesProvider = Either.forLeft(true)
                documentHighlightProvider = Either.forLeft(true)
                documentSymbolProvider = Either.forLeft(true)
                signatureHelpProvider = SignatureHelpOptions().apply {
                    triggerCharacters = listOf("(", ",")
                }
                documentFormattingProvider = Either.forLeft(true)
                documentRangeFormattingProvider = Either.forLeft(true)
                codeActionProvider = Either.forLeft(true)
                renameProvider = Either.forLeft(true)
                workspaceSymbolProvider = Either.forLeft(true)
                executeCommandProvider = org.eclipse.lsp4j.ExecuteCommandOptions().apply {
                    commands = listOf("hasab.restartServer", "hasab.showOutput")
                }
            }

            logger.info("HASAB Language Server initialized successfully")
            InitializeResult(capabilities)
        }
    }

    override fun initialized(params: org.eclipse.lsp4j.InitializedParams) {
        logger.info("Client connected and initialized")
        client?.logMessage(org.eclipse.lsp4j.MessageParams(
            org.eclipse.lsp4j.MessageType.Info,
            "HASAB Language Server v1.0.0 connected"
        ))
    }

    override fun shutdown(): CompletableFuture<Any> {
        logger.info("Server shutting down")
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        logger.info("Server exiting")
        System.exit(0)
    }

    override fun getTextDocumentService(): HasabTextDocumentService {
        if (!::textDocumentService.isInitialized) {
            textDocumentService = HasabTextDocumentService(workspaceIndex, logger, metrics)
        }
        return textDocumentService
    }

    override fun getWorkspaceService(): HasabWorkspaceService {
        if (!::workspaceService.isInitialized) {
            workspaceService = HasabWorkspaceService(workspaceIndex, logger, metrics)
        }
        return workspaceService
    }

    public fun setClient(client: org.eclipse.lsp4j.services.LanguageClient) {
        this.client = client
        textDocumentService.client = client
    }
}
