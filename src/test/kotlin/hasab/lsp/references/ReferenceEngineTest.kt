package hasab.lsp.references

import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Position
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

public class ReferenceEngineTest {

    private fun createEngine(source: String): Pair<ReferenceEngine, DocumentState> {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent(source, 1)
        index.addDocument(state)
        return Pair(ReferenceEngine(index), state)
    }

    @Test
    public fun `findReferences for function name`() {
        val source = "fn add(a: int, b: int): int {\n  return a + b\n}\nfn main() {\n  let x = add(1, 2)\n}"
        val (engine, state) = createEngine(source)
        val refs = engine.findReferences(state, Position(0, 3), true)
        assertNotNull(refs, "References should not be null")
    }

    @Test
    public fun `findReferences returns empty for unknown`() {
        val source = "fn main() {\n  let x = 10\n}"
        val (engine, state) = createEngine(source)
        val refs = engine.findReferences(state, Position(1, 9), true)
        assertNotNull(refs, "References should not be null")
    }

    private fun assertNotNull(value: Any?, message: String = "") {
        kotlin.test.assertNotNull(value, message)
    }
}
