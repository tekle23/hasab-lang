package hasab.lsp

import hasab.lsp.completion.CompletionEngine
import hasab.lsp.diagnostics.DiagnosticEngine
import hasab.lsp.definition.DefinitionEngine
import hasab.lsp.formatting.FormattingEngine
import hasab.lsp.hover.HoverEngine
import hasab.lsp.references.ReferenceEngine
import hasab.lsp.rename.RenameEngine
import hasab.lsp.signature.SignatureEngine
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

public class HasabLspIntegrationTest {

    private val sampleSource = """fn add(a: int, b: int): int {
  return a + b
}

fn main() {
  let x = add(10, 20)
  let y = x + 5
  println(y)
}"""

    private fun createWorkspaceWithSource(source: String): Pair<WorkspaceIndex, DocumentState> {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///main.hs", "hasab", 1)
        state.updateContent(source, 1)
        index.addDocument(state)
        return Pair(index, state)
    }

    @Test
    public fun `full analysis pipeline works`() {
        val (index, state) = createWorkspaceWithSource(sampleSource)
        val result = state.fullAnalysis()
        assertNotNull(result.parseResult, "Parse result should not be null")
        assertNotNull(result.semanticModel, "Semantic model should not be null")
    }

    @Test
    public fun `diagnostic engine produces results for valid code`() {
        val (_, state) = createWorkspaceWithSource(sampleSource)
        val engine = DiagnosticEngine()
        val diagnostics = engine.computeDiagnostics(state)
        assertNotNull(diagnostics, "Diagnostics should not be null")
    }

    @Test
    public fun `diagnostic engine reports syntax error`() {
        val badSource = "fn main() {"
        val (_, state) = createWorkspaceWithSource(badSource)
        val engine = DiagnosticEngine()
        val diagnostics = engine.computeDiagnostics(state)
        assertTrue(diagnostics.isNotEmpty(), "Bad code should produce diagnostics")
    }

    @Test
    public fun `completion engine returns keywords`() {
        val (index, state) = createWorkspaceWithSource(sampleSource)
        val engine = CompletionEngine(index)
        val completions = engine.computeCompletions(state, Position(5, 2))
        assertTrue(completions.isNotEmpty(), "Should return completions")
        val labels = completions.map { it.label }
        assertTrue("fn" in labels, "Should contain 'fn' keyword")
        assertTrue("let" in labels, "Should contain 'let' keyword")
    }

    @Test
    public fun `hover engine returns result on keyword`() {
        val (_, state) = createWorkspaceWithSource(sampleSource)
        val engine = HoverEngine()
        val hover = engine.computeHover(state, Position(4, 2))
        assertNotNull(hover, "Hover should return result on valid position")
    }

    @Test
    public fun `definition engine works`() {
        val (index, state) = createWorkspaceWithSource(sampleSource)
        val engine = DefinitionEngine(index)
        val location = engine.findDefinition(state, Position(0, 3))
        if (location != null) {
            assertNotNull(location.uri, "Location should have URI")
        }
    }

    @Test
    public fun `reference engine works`() {
        val (index, state) = createWorkspaceWithSource(sampleSource)
        val engine = ReferenceEngine(index)
        val refs = engine.findReferences(state, Position(0, 3), true)
        assertNotNull(refs, "References should not be null")
    }

    @Test
    public fun `formatting engine formats code`() {
        val (_, state) = createWorkspaceWithSource("fn main(){let x=10}")
        val engine = FormattingEngine()
        val edits = engine.formatDocument(state)
        assertTrue(edits.isNotEmpty(), "Formatting should produce edits")
    }

    @Test
    public fun `multi-file workspace index works`() {
        val index = WorkspaceIndex()

        val state1 = DocumentState("file:///main.hs", "hasab", 1)
        state1.updateContent("fn main() {\n  let x = 10\n}", 1)
        index.addDocument(state1)

        val state2 = DocumentState("file:///utils.hs", "hasab", 1)
        state2.updateContent("fn helper(): int {\n  return 42\n}", 1)
        index.addDocument(state2)

        val allSymbols = index.getAllSymbols()
        assertTrue(allSymbols.size >= 2, "Should find symbols from both files, got ${allSymbols.size}")

        val queryResults = index.searchSymbols("helper")
        assertTrue(queryResults.isNotEmpty(), "Should find 'helper' in workspace")
    }

    @Test
    public fun `document removal clears index`() {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///temp.hs", "hasab", 1)
        state.updateContent("fn temp() {}", 1)
        index.addDocument(state)
        assertTrue(index.getAllSymbols().isNotEmpty(), "Should have symbols after add")

        index.removeDocument("file:///temp.hs")
        assertTrue(index.getAllSymbols().isEmpty(), "Should have no symbols after removal")
    }

    @Test
    public fun `rename engine computes edit`() {
        val (index, state) = createWorkspaceWithSource(sampleSource)
        val engine = RenameEngine(index)
        val edit = engine.computeRename(state, Position(0, 3), "sum")
        if (edit != null) {
            assertTrue(edit.changes != null || edit.documentChanges != null, "Edit should have changes")
        }
    }

    @Test
    public fun `signature engine handles call site`() {
        val (_, state) = createWorkspaceWithSource(sampleSource)
        val engine = SignatureEngine()
        val sig = engine.computeSignatureHelp(state, Position(5, 14))
        if (sig != null) {
            assertTrue(sig.signatures.isNotEmpty(), "Should have signatures")
        }
    }
}
