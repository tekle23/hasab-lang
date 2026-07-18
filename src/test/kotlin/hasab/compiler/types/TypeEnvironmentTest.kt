package hasab.compiler.types

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TypeEnvironmentTest {

    @Test
    fun `define and lookup in same scope`() {
        val env = TypeEnvironment()
        env.define(VariableSymbol("x", ResolvedType.IntType, isMutable = false))

        val symbol = env.lookup("x")
        assertNotNull(symbol)
        assertIs<VariableSymbol>(symbol)
        assertEquals("x", symbol.name)
        assertEquals(ResolvedType.IntType, symbol.type)
        assertFalse(symbol.isMutable)
    }

    @Test
    fun `lookup returns null for undefined symbol`() {
        val env = TypeEnvironment()
        assertNull(env.lookup("nonexistent"))
    }

    @Test
    fun `child scope inherits parent symbols`() {
        val parent = TypeEnvironment()
        parent.define(VariableSymbol("x", ResolvedType.IntType, isMutable = false))

        val child = parent.enterScope()
        val symbol = child.lookup("x")
        assertNotNull(symbol)
        assertEquals(ResolvedType.IntType, symbol.type)
    }

    @Test
    fun `child scope shadows parent symbol`() {
        val parent = TypeEnvironment()
        parent.define(VariableSymbol("x", ResolvedType.IntType, isMutable = false))

        val child = parent.enterScope()
        child.define(VariableSymbol("x", ResolvedType.StringType, isMutable = false))

        val symbol = child.lookup("x")
        assertNotNull(symbol)
        assertEquals(ResolvedType.StringType, symbol.type)

        val parentSymbol = parent.lookup("x")
        assertNotNull(parentSymbol)
        assertEquals(ResolvedType.IntType, parentSymbol.type)
    }

    @Test
    fun `lookupCurrent only checks current scope`() {
        val env = TypeEnvironment()
        env.define(VariableSymbol("x", ResolvedType.IntType, isMutable = false))

        val child = env.enterScope()
        assertNull(child.lookupCurrent("x"))
        assertNotNull(child.lookup("x"))
    }

    @Test
    fun `hasCurrent returns true for defined symbol`() {
        val env = TypeEnvironment()
        assertFalse(env.hasCurrent("x"))
        env.define(VariableSymbol("x", ResolvedType.IntType, isMutable = false))
        assertTrue(env.hasCurrent("x"))
    }

    @Test
    fun `define duplicate throws`() {
        val env = TypeEnvironment()
        env.define(VariableSymbol("x", ResolvedType.IntType, isMutable = false))
        val ex = assertFailsWith<IllegalArgumentException> {
            env.define(VariableSymbol("x", ResolvedType.StringType, isMutable = false))
        }
        assertTrue(ex.message!!.contains("'x'"))
    }

    @Test
    fun `defineOrOverride replaces existing`() {
        val env = TypeEnvironment()
        env.define(VariableSymbol("x", ResolvedType.IntType, isMutable = false))
        env.defineOrOverride(VariableSymbol("x", ResolvedType.StringType, isMutable = false))
        val symbol = env.lookup("x")
        assertNotNull(symbol)
        assertEquals(ResolvedType.StringType, symbol.type)
    }

    @Test
    fun `parent returns null for root`() {
        val env = TypeEnvironment()
        assertNull(env.parent())
    }

    @Test
    fun `parent returns parent scope`() {
        val parent = TypeEnvironment()
        val child = parent.enterScope()
        assertEquals(parent, child.parent())
    }

    @Test
    fun `function symbol lookup`() {
        val env = TypeEnvironment()
        val fn = FunctionSymbol("add", listOf(ResolvedType.IntType, ResolvedType.IntType), ResolvedType.IntType)
        env.define(fn)

        val symbol = env.lookup("add")
        assertNotNull(symbol)
        assertIs<FunctionSymbol>(symbol)
        assertEquals(2, symbol.parameterTypes.size)
        assertEquals(ResolvedType.IntType, symbol.returnType)
    }

    @Test
    fun `struct symbol lookup`() {
        val env = TypeEnvironment()
        val fields = linkedMapOf<String, ResolvedType>("x" to ResolvedType.IntType, "y" to ResolvedType.IntType)
        env.define(StructSymbol("Point", fields))

        val symbol = env.lookup("Point")
        assertNotNull(symbol)
        assertIs<StructSymbol>(symbol)
        assertEquals(2, symbol.fields.size)
    }

    @Test
    fun `nested scopes 3 levels deep`() {
        val root = TypeEnvironment()
        root.define(VariableSymbol("a", ResolvedType.IntType, isMutable = false))

        val level1 = root.enterScope()
        level1.define(VariableSymbol("b", ResolvedType.StringType, isMutable = false))

        val level2 = level1.enterScope()
        level2.define(VariableSymbol("c", ResolvedType.BoolType, isMutable = false))

        assertNotNull(level2.lookup("a"))
        assertNotNull(level2.lookup("b"))
        assertNotNull(level2.lookup("c"))
        assertNull(level1.lookup("c"))
    }
}
