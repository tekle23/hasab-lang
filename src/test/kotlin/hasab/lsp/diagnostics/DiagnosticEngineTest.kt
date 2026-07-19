package hasab.lsp.diagnostics

import hasab.lsp.DocumentState
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DiagnosticEngineTest {

    private fun createState(code: String): DocumentState {
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent(code, 1)
        return state
    }

    @Test
    public fun `computeDiagnostics with valid code returns no errors`() {
        val state = createState("fn main() { let x = 10; }")
        val engine = DiagnosticEngine()
        val diagnostics = engine.computeDiagnostics(state)
        assertNotNull(diagnostics)
        val errors = diagnostics.filter { it.severity == org.eclipse.lsp4j.DiagnosticSeverity.Error }
        assertTrue(errors.isEmpty(), "Valid code should have no errors, but found: $errors")
    }

    @Test
    public fun `computeDiagnostics with syntax error returns parser diagnostic`() {
        val state = createState("fn {")
        val engine = DiagnosticEngine()
        val diagnostics = engine.computeDiagnostics(state)
        assertNotNull(diagnostics)
        assertTrue(diagnostics.isNotEmpty(), "Syntax error should produce diagnostics")
    }

    @Test
    public fun `computeDiagnostics returns analysis results`() {
        val state = createState("fn main() { let x = 10; }")
        val engine = DiagnosticEngine()
        val diagnostics = engine.computeDiagnostics(state)
        assertNotNull(diagnostics)
        assertNotNull(state.parseResult)
        assertNotNull(state.semanticModel)
        assertNotNull(state.typeCheckResult)
    }
}
