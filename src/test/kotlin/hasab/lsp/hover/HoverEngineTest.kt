package hasab.lsp.hover

import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Position
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

public class HoverEngineTest {

    private fun createEngine(source: String): Pair<HoverEngine, DocumentState> {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent(source, 1)
        index.addDocument(state)
        return Pair(HoverEngine(), state)
    }

    @Test
    public fun `hover on function name shows info`() {
        val source = "fn main() {\n  let x = 10\n}"
        val (engine, state) = createEngine(source)
        val hover = engine.computeHover(state, Position(0, 3))
        if (hover != null) {
            assertTrue(hover.contents != null, "Hover should have contents")
        }
    }

    @Test
    public fun `hover on empty space returns null`() {
        val source = "fn main() {\n\n}"
        val (engine, state) = createEngine(source)
        val hover = engine.computeHover(state, Position(1, 0))
        assertNull(hover, "Hover on empty space should be null")
    }

    @Test
    public fun `hover on variable declaration`() {
        val source = "fn main() {\n  let myVar = 10\n}"
        val (engine, state) = createEngine(source)
        val hover = engine.computeHover(state, Position(1, 8))
        if (hover != null) {
            assertTrue(hover.contents != null, "Hover on variable should have contents")
        }
    }
}
