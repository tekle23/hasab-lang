package hasab.compiler.semantic

import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SymbolTableTest {

    private fun range(start: Int = 0, end: Int = 5) = SourceRange(
        SourcePosition(1, 1, start),
        SourcePosition(1, 1, end),
    )

    private fun varSym(name: String) = VariableSymbol(
        name = name,
        visibility = Visibility.MODULE_LOCAL,
        range = range(),
        fileName = "test.hb",
    )

    private fun fnSym(name: String) = FunctionSymbol(
        name = name,
        visibility = Visibility.MODULE_LOCAL,
        range = range(),
        fileName = "test.hb",
    )

    private fun structSym(name: String) = StructSymbol(
        name = name,
        visibility = Visibility.MODULE_LOCAL,
        range = range(),
        fileName = "test.hb",
    )

    // ---- Basic operations ----

    @Test
    fun `empty table has no symbols`() {
        assertTrue(SymbolTable.EMPTY.allVisibleSymbols().isEmpty())
    }

    @Test
    fun `define and lookup a symbol`() {
        val table = SymbolTable.EMPTY.define(varSym("x"))
        val found = table.lookup("x")
        assertNotNull(found)
        assertEquals("x", found.name)
        assertTrue(found is VariableSymbol)
    }

    @Test
    fun `lookup returns null for undefined symbol`() {
        assertNull(SymbolTable.EMPTY.lookup("undefined"))
    }

    @Test
    fun `lookupCurrent returns null for parent scope symbols`() {
        val parent = SymbolTable.EMPTY.define(varSym("parent_sym"))
        val child = parent.enterScope()
        assertNull(child.lookupCurrent("parent_sym"))
    }

    @Test
    fun `lookup walks up to parent scope`() {
        val parent = SymbolTable.EMPTY.define(varSym("parent_sym"))
        val child = parent.enterScope()
        val found = child.lookup("parent_sym")
        assertNotNull(found)
        assertEquals("parent_sym", found.name)
    }

    @Test
    fun `has returns true for defined symbol`() {
        val table = SymbolTable.EMPTY.define(varSym("x"))
        assertTrue(table.has("x"))
    }

    @Test
    fun `hasCurrent only checks current scope`() {
        val parent = SymbolTable.EMPTY.define(varSym("x"))
        val child = parent.enterScope().define(varSym("y"))
        assertTrue(child.hasCurrent("y"))
        assertTrue(child.has("x")) // inherited from parent
    }

    @Test
    fun `currentSymbols only returns current scope`() {
        val parent = SymbolTable.EMPTY.define(varSym("x"))
        val child = parent.enterScope().define(varSym("y"))
        val current = child.currentSymbols()
        assertEquals(1, current.size)
        assertTrue("y" in current)
        assertFalse("x" in current)
    }

    @Test
    fun `allVisibleSymbols merges parent and child`() {
        val parent = SymbolTable.EMPTY.define(varSym("x")).define(varSym("y"))
        val child = parent.enterScope().define(varSym("z"))
        val all = child.allVisibleSymbols()
        assertEquals(3, all.size)
        assertTrue("x" in all)
        assertTrue("y" in all)
        assertTrue("z" in all)
    }

    // ---- Scope nesting ----

    @Test
    fun `three-level nesting works`() {
        val l0 = SymbolTable.EMPTY.define(varSym("global"))
        val l1 = l0.enterScope().define(varSym("level1"))
        val l2 = l1.enterScope().define(varSym("level2"))

        assertEquals("level2", l2.lookup("level2")?.name)
        assertEquals("level1", l2.lookup("level1")?.name)
        assertEquals("global", l2.lookup("global")?.name)
        assertNull(l2.lookup("nonexistent"))
    }

    @Test
    fun `depth is correct`() {
        val l0 = SymbolTable.EMPTY
        val l1 = l0.enterScope()
        val l2 = l1.enterScope()
        assertEquals(0, l0.depth())
        assertEquals(1, l1.depth())
        assertEquals(2, l2.depth())
    }

    // ---- Immutability ----

    @Test
    fun `define returns new table, does not mutate original`() {
        val original = SymbolTable.EMPTY.define(varSym("x"))
        val modified = original.define(varSym("y"))
        assertNull(original.lookup("y"))
        assertNotNull(modified.lookup("y"))
        assertNotNull(modified.lookup("x"))
    }

    @Test
    fun `defineAll works correctly`() {
        val table = SymbolTable.EMPTY.defineAll(
            varSym("a"), varSym("b"), varSym("c"),
        )
        assertNotNull(table.lookup("a"))
        assertNotNull(table.lookup("b"))
        assertNotNull(table.lookup("c"))
        assertNull(table.lookup("d"))
    }

    // ---- Different symbol kinds ----

    @Test
    fun `multiple kinds can coexist`() {
        var table = SymbolTable.EMPTY
        table = table.define(varSym("x"))
        table = table.define(fnSym("f"))
        table = table.define(structSym("S"))
        table = table.define(EnumSymbol("E", Visibility.MODULE_LOCAL, range(), "test.hb"))

        assertTrue(table.lookup("x") is VariableSymbol)
        assertTrue(table.lookup("f") is FunctionSymbol)
        assertTrue(table.lookup("S") is StructSymbol)
        assertTrue(table.lookup("E") is EnumSymbol)
    }

    // ---- parentScope ----

    @Test
    fun `parentScope returns parent`() {
        val parent = SymbolTable.EMPTY
        val child = parent.enterScope()
        assertEquals(parent, child.parentScope())
        assertNull(parent.parentScope())
    }

    // ---- Visibility ----

    @Test
    fun `public symbol is visible`() {
        val table = SymbolTable.EMPTY.define(
            VariableSymbol("pub_fn", Visibility.PUBLIC, range(), "test.hb"),
        )
        assertEquals(Visibility.PUBLIC, table.lookup("pub_fn")?.visibility)
    }
}
