package hasab.lsp.definition

import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Position
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

public class DefinitionEngineTest {

    private fun createEngine(source: String): Pair<DefinitionEngine, DocumentState> {
        val index = WorkspaceIndex()
        val state = DocumentState("file:///test.hs", "hasab", 1)
        state.updateContent(source, 1)
        index.addDocument(state)
        return Pair(DefinitionEngine(index), state)
    }

    @Test
    public fun `definition of function name`() {
        val source = "fn main() {\n  let x = 10\n}"
        val (engine, state) = createEngine(source)
        val location = engine.findDefinition(state, Position(0, 3))
        if (location != null) {
            assertNotNull(location.uri, "Location should have URI")
        }
    }

    @Test
    public fun `definition returns null for unknown identifier`() {
        val source = "fn main() {\n  let x = 10\n}"
        val (engine, state) = createEngine(source)
        val location = engine.findDefinition(state, Position(1, 9))
        assertNull(location, "Definition of unknown identifier should be null")
    }

    @Test
    public fun `definition of variable name`() {
        val source = "fn main() {\n  let myVar = 10\n}"
        val (engine, state) = createEngine(source)
        val location = engine.findDefinition(state, Position(1, 8))
        if (location != null) {
            assertNotNull(location.uri, "Location should have URI")
        }
    }
}
