package hasab.lsp

import hasab.lsp.logging.LspLogger
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.concurrent.Future

public class HasabLspLauncher {

    public fun launch(input: InputStream, output: OutputStream): Future<*> {
        val server = HasabLanguageServer()

        val launcher = Launcher.createLauncher(
            server,
            LanguageClient::class.java,
            input,
            output,
        )

        val client = launcher.remoteProxy
        server.setClient(client)

        val executor = Executors.newSingleThreadExecutor { r ->
            Thread(r, "hasab-lsp-main").apply { isDaemon = true }
        }

        val future = launcher.startListening()

        return future
    }

    public fun launchStdio(): Future<*> {
        return launch(System.`in`, System.out)
    }

    public companion object {
        @JvmStatic
        public fun main(args: Array<String>) {
            val logger = LspLogger(prefix = "HasabLSP-main")
            logger.info("HASAB Language Server starting (stdio)")

            try {
                val launcher = HasabLspLauncher()
                launcher.launchStdio().get()
            } catch (e: Exception) {
                logger.error("Server failed", e)
                System.exit(1)
            }
        }
    }
}
