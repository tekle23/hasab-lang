package hasab.lsp.completion

import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Position
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

public class CompletionEngineTest {

    private fun createEngine(source: String): Pair<CompletionEngine, DocumentState> {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent(source, 1)
        index.addDocument(state)
        return Pair(CompletionEngine(index), state)
    }

    @Test
    public fun `keyword completions include fn let if`() {
        val (engine, state) = createEngine("")
        val completions = engine.computeCompletions(state, Position(0, 0))
        val labels = completions.map { it.label }
        assertTrue("fn" in labels, "Should contain 'fn' keyword")
        assertTrue("let" in labels, "Should contain 'let' keyword")
        assertTrue("if" in labels, "Should contain 'if' keyword")
        assertTrue("struct" in labels, "Should contain 'struct' keyword")
    }

    @Test
    public fun `completions return non-empty list`() {
        val (engine, state) = createEngine("fn main() {\n  \n}")
        val completions = engine.computeCompletions(state, Position(1, 2))
        assertTrue(completions.isNotEmpty(), "Should return completions")
    }

    @Test
    public fun `snippet completions available`() {
        val (engine, state) = createEngine("")
        val completions = engine.computeCompletions(state, Position(0, 0))
        val snippetItems = completions.filter {
            it.insertTextFormat == org.eclipse.lsp4j.InsertTextFormat.Snippet
        }
        assertTrue(snippetItems.isNotEmpty(), "Should have snippet completions")
    }

    @Test
    public fun `empty document returns keyword completions`() {
        val (engine, state) = createEngine("")
        val completions = engine.computeCompletions(state, Position(0, 0))
        assertTrue(completions.isNotEmpty())
    }

    @Test
    public fun `built-in function completions available`() {
        val (engine, state) = createEngine("")
        val completions = engine.computeCompletions(state, Position(0, 0))
        val labels = completions.map { it.label }
        assertTrue("print" in labels, "Should contain 'print' built-in")
        assertTrue("len" in labels, "Should contain 'len' built-in")
    }
}
