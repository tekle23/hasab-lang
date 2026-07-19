package hasab.lsp

import hasab.lsp.logging.LspLogger
import hasab.lsp.logging.PerformanceMetrics
import hasab.lsp.symbol.WorkspaceSymbolEngine
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

public class HasabWorkspaceService(
    private val workspaceIndex: WorkspaceIndex,
    private val logger: LspLogger,
    private val metrics: PerformanceMetrics,
) : WorkspaceService {

    private val symbolEngine = WorkspaceSymbolEngine(workspaceIndex)

    private val workspaceFolders = mutableListOf<WorkspaceFolder>()

    override fun didChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams) {
        for (added in params.event.added) {
            workspaceFolders.add(added)
            logger.info("Workspace folder added: ${added.uri}")
        }
        for (removed in params.event.removed) {
            workspaceFolders.removeAll { it.uri == removed.uri }
            logger.info("Workspace folder removed: ${removed.uri}")
        }
    }

    override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
        logger.info("Configuration changed")
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        for (change in params.changes) {
            when (change.type) {
                org.eclipse.lsp4j.FileChangeType.Created -> {
                    logger.info("File created: ${change.uri}")
                }
                org.eclipse.lsp4j.FileChangeType.Changed -> {
                    val state = workspaceIndex.getDocument(change.uri)
                    if (state != null) {
                        state.invalidate()
                        workspaceIndex.updateDocument(state)
                    }
                }
                org.eclipse.lsp4j.FileChangeType.Deleted -> {
                    logger.info("File deleted: ${change.uri}")
                    workspaceIndex.removeDocument(change.uri)
                }
            }
        }
    }

    override fun symbol(params: WorkspaceSymbolParams): CompletableFuture<Either<List<SymbolInformation>, List<org.eclipse.lsp4j.WorkspaceSymbol>>> {
        return CompletableFuture.supplyAsync {
            metrics.measure("workspaceSymbol") {
                Either.forLeft(symbolEngine.searchSymbols(params.query))
            }
        }
    }

    override fun executeCommand(params: ExecuteCommandParams): CompletableFuture<Any> {
        return CompletableFuture.supplyAsync {
            when (params.command) {
                "hasab.restartServer" -> {
                    logger.info("Restart command received")
                    "Server restart requested"
                }
                "hasab.showOutput" -> {
                    logger.info("Show output command received")
                    "Output shown"
                }
                else -> {
                    logger.warn("Unknown command: ${params.command}")
                    "Unknown command: ${params.command}"
                }
            }
        }
    }

    public fun getWorkspaceFolders(): List<WorkspaceFolder> = workspaceFolders.toList()
}
