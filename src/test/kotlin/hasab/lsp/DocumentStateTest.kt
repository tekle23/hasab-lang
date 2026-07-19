package hasab.lsp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DocumentStateTest {

    @Test
    public fun `create document state with uri, languageId, version`() {
        val state = DocumentState("file:///test.hs", "hasab", 1)
        assertEquals("file:///test.hs", state.uri)
        assertEquals("hasab", state.languageId)
        assertEquals(1, state.version)
        assertEquals("", state.content)
        assertNull(state.lexerResult)
        assertNull(state.parseResult)
        assertNull(state.semanticModel)
        assertNull(state.typeCheckResult)
    }

    @Test
    public fun `updateContent changes content and version`() {
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent("fn main() { let x = 10; }", 2)
        assertEquals("fn main() { let x = 10; }", state.content)
        assertEquals(2, state.version)
    }

    @Test
    public fun `parse returns ParseResult`() {
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent("fn main() { let x = 10; }", 1)
        val result = state.parse()
        assertNotNull(result)
        assertNotNull(result.module)
        assertNotNull(result.diagnostics)
        assertNotNull(state.parseResult)
        assertNotNull(state.lexerResult)
    }

    @Test
    public fun `invalidate clears cached results`() {
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent("fn main() { let x = 10; }", 1)
        state.parse()
        assertNotNull(state.parseResult)
        assertNotNull(state.lexerResult)
        state.analyzeSemantics()
        assertNotNull(state.semanticModel)
        state.invalidate()
        assertNull(state.parseResult)
        assertNull(state.lexerResult)
        assertNull(state.semanticModel)
        assertNull(state.typeCheckResult)
    }

    @Test
    public fun `uriToFileName converts file URIs correctly`() {
        val unixUri = "file:///home/user/test.hs"
        val result = DocumentState.uriToFileName(unixUri)
        assertNotNull(result)
    }
}
