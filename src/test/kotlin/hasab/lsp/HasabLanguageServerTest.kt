package hasab.lsp

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

public class HasabLanguageServerTest {

    @Test
    public fun `getWorkspaceIndex returns index`() {
        val server = HasabLanguageServer()
        assertNotNull(server.getWorkspaceIndex(), "Workspace index should not be null")
    }

    @Test
    public fun `getPerformanceMetrics returns metrics`() {
        val server = HasabLanguageServer()
        assertNotNull(server.getPerformanceMetrics(), "Performance metrics should not be null")
    }

    @Test
    public fun `getTextDocumentService returns service`() {
        val server = HasabLanguageServer()
        val service = server.getTextDocumentService()
        assertNotNull(service, "Text document service should not be null")
    }

    @Test
    public fun `getWorkspaceService returns service`() {
        val server = HasabLanguageServer()
        val service = server.getWorkspaceService()
        assertNotNull(service, "Workspace service should not be null")
    }

    @Test
    public fun `index and metrics are accessible`() {
        val server = HasabLanguageServer()
        val index = server.getWorkspaceIndex()
        val metrics = server.getPerformanceMetrics()
        assertNotNull(index)
        assertNotNull(metrics)
        assertTrue(index.getAllSymbols().isEmpty(), "New index should be empty")
    }
}
