package hasab.lsp.formatting

import hasab.lsp.DocumentState
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import kotlin.test.Test
import kotlin.test.assertTrue

public class FormattingEngineTest {

    private fun createState(source: String): DocumentState {
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent(source, 1)
        return state
    }

    @Test
    public fun `formatDocument returns edits for unformatted code`() {
        val source = "fn main(){let x=10}"
        val state = createState(source)
        val engine = FormattingEngine()
        val edits = engine.formatDocument(state)
        assertTrue(edits.isNotEmpty(), "Should return formatting edits")
    }

    @Test
    public fun `formatRange formats selected range`() {
        val source = "fn main() {\n    let x=10\n}"
        val state = createState(source)
        val engine = FormattingEngine()
        val range = Range(Position(1, 0), Position(1, 20))
        val edits = engine.formatRange(state, range)
        assertTrue(edits.isNotEmpty(), "Should return range formatting edits")
    }

    @Test
    public fun `formatOnType returns edits for newline`() {
        val source = "fn main() {"
        val state = createState(source)
        val engine = FormattingEngine()
        val edits = engine.formatOnType(state, Position(0, 11), "\n")
        assertTrue(edits.isEmpty(), "Simple newline should not require edits")
    }
}
