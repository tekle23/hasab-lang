package hasab.compiler.semantic

import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScopeManagerTest {

    private fun range(start: Int = 0, end: Int = 10) = SourceRange(
        SourcePosition(1, 1, start),
        SourcePosition(1, 1, end),
    )

    @Test
    fun `global scope exists`() {
        val mgr = ScopeManager()
        assertEquals(ScopeKind.GLOBAL, mgr.globalScope().kind)
    }

    @Test
    fun `enter and exit scope`() {
        val mgr = ScopeManager()
        val fnScope = mgr.enterScope(ScopeKind.FUNCTION, "main", range(), "test.hb")
        assertEquals(ScopeKind.FUNCTION, fnScope.kind)
        assertEquals("main", fnScope.name)

        val exited = mgr.exitScope()
        assertEquals(fnScope, exited)
    }

    @Test
    fun `nested scopes have correct parents`() {
        val mgr = ScopeManager()
        val fnScope = mgr.enterScope(ScopeKind.FUNCTION, "main", range(), "test.hb")
        val blockScope = mgr.enterScope(ScopeKind.BLOCK, "block", range(), "test.hb")

        assertEquals(fnScope, blockScope.parent)
    }

    @Test
    fun `addSymbol adds to current scope`() {
        val mgr = ScopeManager()
        mgr.enterScope(ScopeKind.FUNCTION, "main", range(), "test.hb")
        mgr.addSymbol("x")

        assertTrue(mgr.currentScope().symbols.contains("x"))
    }

    @Test
    fun `isInsideLoop detects loop`() {
        val mgr = ScopeManager()
        assertFalse(mgr.isInsideLoop())

        mgr.enterScope(ScopeKind.FUNCTION, "main", range(), "test.hb")
        assertFalse(mgr.isInsideLoop())

        mgr.enterScope(ScopeKind.LOOP, "while", range(), "test.hb")
        assertTrue(mgr.isInsideLoop())

        mgr.exitScope()
        assertFalse(mgr.isInsideLoop())
    }

    @Test
    fun `isInsideImpl detects impl`() {
        val mgr = ScopeManager()
        assertFalse(mgr.isInsideImpl())

        mgr.enterScope(ScopeKind.IMPL, "impl", range(), "test.hb")
        assertTrue(mgr.isInsideImpl())

        mgr.exitScope()
        assertFalse(mgr.isInsideImpl())
    }

    @Test
    fun `findEnclosing finds correct scope`() {
        val mgr = ScopeManager()
        val fnScope = mgr.enterScope(ScopeKind.FUNCTION, "main", range(), "test.hb")
        mgr.enterScope(ScopeKind.BLOCK, "block", range(), "test.hb")

        assertEquals(fnScope, mgr.findEnclosing(ScopeKind.FUNCTION))
        assertNotNull(mgr.findEnclosing(ScopeKind.GLOBAL))
        assertNull(mgr.findEnclosing(ScopeKind.LOOP))
    }

    @Test
    fun `buildScopeTree returns current scope`() {
        val mgr = ScopeManager()
        mgr.enterScope(ScopeKind.MODULE, "test", range(), "test.hb")
        val tree = mgr.buildScopeTree()
        assertEquals(ScopeKind.MODULE, tree.kind)
        assertEquals("test", tree.name)
    }
}
