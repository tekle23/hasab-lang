package hasab.compiler.types

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TypeEnvironmentTest {

    @Test
    fun `define and lookup in same scope`() {
        val env = TypeEnvironment.root()
        val env2 = env.define("x", IntType)
        assertEquals(IntType, env2.lookup("x"))
    }

    @Test
    fun `lookup returns null for undefined symbol`() {
        val env = TypeEnvironment.root()
        assertNull(env.lookup("nonexistent"))
    }

    @Test
    fun `child scope inherits parent symbols`() {
        val parent = TypeEnvironment.root().define("x", IntType)
        val child = parent.enterScope()
        assertEquals(IntType, child.lookup("x"))
    }

    @Test
    fun `child scope shadows parent symbol`() {
        val parent = TypeEnvironment.root().define("x", IntType)
        val child = parent.enterScope().define("x", StringType)
        assertEquals(StringType, child.lookup("x"))
        assertEquals(IntType, parent.lookup("x"))
    }

    @Test
    fun `hasCurrent returns true for defined symbol`() {
        val env = TypeEnvironment.root().define("x", IntType)
        assertTrue(env.hasCurrent("x"))
        assertFalse(env.hasCurrent("y"))
    }

    @Test
    fun `has returns true for symbol in any scope`() {
        val env = TypeEnvironment.root().define("x", IntType)
        val child = env.enterScope()
        assertTrue(child.has("x"))
    }

    @Test
    fun `root has built-in types`() {
        val env = TypeEnvironment.root()
        assertEquals(IntType, env.lookup("int"))
        assertEquals(FloatType, env.lookup("float"))
        assertEquals(StringType, env.lookup("string"))
        assertEquals(BoolType, env.lookup("bool"))
        assertEquals(CharType, env.lookup("char"))
        assertEquals(VoidType, env.lookup("void"))
    }

    @Test
    fun `root has built-in functions`() {
        val env = TypeEnvironment.root()
        assertNotNull(env.lookup("println"))
        assertNotNull(env.lookup("print"))
        assertNotNull(env.lookup("len"))
    }

    @Test
    fun `refine updates existing binding`() {
        val env = TypeEnvironment.root().define("x", IntType)
        val refined = env.refine("x", FloatType)
        assertEquals(FloatType, refined.lookup("x"))
    }

    @Test
    fun `refine in child scope`() {
        val parent = TypeEnvironment.root().define("x", IntType)
        val child = parent.enterScope().define("y", StringType)
        val refined = child.refine("x", FloatType)
        assertEquals(FloatType, refined.lookup("x"))
        assertEquals(IntType, parent.lookup("x"))
    }

    @Test
    fun `nested scopes 3 levels deep`() {
        val root = TypeEnvironment.root().define("a", IntType)
        val level1 = root.enterScope().define("b", StringType)
        val level2 = level1.enterScope().define("c", BoolType)

        assertEquals(IntType, level2.lookup("a"))
        assertEquals(StringType, level2.lookup("b"))
        assertEquals(BoolType, level2.lookup("c"))
        assertNull(level1.lookup("c"))
    }
}
