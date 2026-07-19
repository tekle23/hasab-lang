package hasab.lsp

import hasab.compiler.semantic.SymbolKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkspaceIndexTest {

    private val testUri = "file:///test.hs"
    private val testSource = "fn main() { let x = 10; }"
    private val testUri2 = "file:///test2.hs"
    private val testSource2 = "fn helper() { let y = 20; }"

    private fun createState(uri: String, content: String): DocumentState {
        val state = DocumentState(uri, "hasab", 1)
        state.updateContent(content, 1)
        return state
    }

    @Test
    public fun `addDocument indexes symbols`() {
        val index = WorkspaceIndex()
        val state = createState(testUri, testSource)
        index.addDocument(state)
        val symbols = index.getSymbolsForFile(testUri)
        assertTrue(symbols.isNotEmpty())
        assertTrue(symbols.any { it.kind == SymbolKind.FUNCTION })
    }

    @Test
    public fun `removeDocument clears index`() {
        val index = WorkspaceIndex()
        val state = createState(testUri, testSource)
        index.addDocument(state)
        assertTrue(index.getSymbolsForFile(testUri).isNotEmpty())
        index.removeDocument(testUri)
        val symbols = index.getSymbolsForFile(testUri)
        assertEquals(0, symbols.size)
        assertNull(index.getDocument(testUri))
    }

    @Test
    public fun `updateDocument re-indexes`() {
        val index = WorkspaceIndex()
        val state1 = createState(testUri, testSource)
        index.addDocument(state1)
        val count1 = index.getSymbolsForFile(testUri).size
        val state2 = createState(testUri, "fn main() { let x = 10; }\nfn helper() { let y = 20; }")
        state2.updateContent("fn main() { let x = 10; }\nfn helper() { let y = 20; }", 2)
        index.updateDocument(state2)
        val count2 = index.getSymbolsForFile(testUri).size
        assertTrue(count2 >= count1)
    }

    @Test
    public fun `getSymbolsForFile returns file symbols`() {
        val index = WorkspaceIndex()
        val state = createState(testUri, testSource)
        index.addDocument(state)
        val symbols = index.getSymbolsForFile(testUri)
        assertNotNull(symbols)
        assertTrue(symbols.all { it.uri == testUri })
    }

    @Test
    public fun `getAllSymbols returns all symbols`() {
        val index = WorkspaceIndex()
        val state1 = createState(testUri, testSource)
        val state2 = createState(testUri2, testSource2)
        index.addDocument(state1)
        index.addDocument(state2)
        val allSymbols = index.getAllSymbols()
        assertTrue(allSymbols.isNotEmpty())
        assertTrue(allSymbols.any { it.uri == testUri })
        assertTrue(allSymbols.any { it.uri == testUri2 })
    }

    @Test
    public fun `searchSymbols finds by query`() {
        val index = WorkspaceIndex()
        val state = createState(testUri, "fn main() { let x = 10; }\nfn helper() { let y = 20; }")
        state.updateContent("fn main() { let x = 10; }\nfn helper() { let y = 20; }", 1)
        index.addDocument(state)
        val results = index.searchSymbols("main")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.name.contains("main", ignoreCase = true) })
    }

    @Test
    public fun `getSymbolsByKind filters by kind`() {
        val index = WorkspaceIndex()
        val state = createState(testUri, testSource)
        index.addDocument(state)
        val fnSymbols = index.getSymbolsByKind(SymbolKind.FUNCTION)
        val structSymbols = index.getSymbolsByKind(SymbolKind.STRUCT)
        assertTrue(fnSymbols.isNotEmpty())
        assertTrue(fnSymbols.all { it.kind == SymbolKind.FUNCTION })
        assertTrue(structSymbols.isEmpty())
    }
}
