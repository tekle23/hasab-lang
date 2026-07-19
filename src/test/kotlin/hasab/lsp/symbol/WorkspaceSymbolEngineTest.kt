package hasab.lsp.symbol

import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

public class WorkspaceSymbolEngineTest {

    @Test
    public fun `searchSymbols finds by query`() {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent("fn main() {\n  let x = 10\n}", 1)
        index.addDocument(state)
        val engine = WorkspaceSymbolEngine(index)
        val results = engine.searchSymbols("main")
        assertTrue(results.isNotEmpty(), "Should find 'main' symbol")
    }

    @Test
    public fun `searchSymbols returns empty for no match`() {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent("fn main() {}", 1)
        index.addDocument(state)
        val engine = WorkspaceSymbolEngine(index)
        val results = engine.searchSymbols("zzz_nonexistent_zzz")
        assertEquals(0, results.size, "Should return empty for non-existent symbol")
    }

    @Test
    public fun `getSymbolsByKind filters correctly`() {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent("fn main() {\n  let x = 10\n}", 1)
        index.addDocument(state)
        val engine = WorkspaceSymbolEngine(index)
        val fns = engine.getSymbolsByKind(hasab.compiler.semantic.SymbolKind.FUNCTION)
        assertTrue(fns.isNotEmpty(), "Should find function symbols")
    }
}
