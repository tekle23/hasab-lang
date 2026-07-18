package hasab.compiler.types

import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TypeResolverTest {

    private fun makeResolver(builtins: Boolean = true): Pair<TypeEnvironment, TypeResolver> {
        val env = TypeEnvironment()
        if (builtins) BuiltinTypes.registerBuiltins(env)
        return env to TypeResolver(env)
    }

    private fun id(name: String): IdentifierType {
        return IdentifierType(name, "test.hasab", 1, 1, 0, name.length)
    }

    @Test
    fun `resolve builtin int`() {
        val (_, resolver) = makeResolver()
        val result = resolver.resolve(id("int"))
        assertEquals(ResolvedType.IntType, result)
    }

    @Test
    fun `resolve builtin float`() {
        val (_, resolver) = makeResolver()
        val result = resolver.resolve(id("float"))
        assertEquals(ResolvedType.FloatType, result)
    }

    @Test
    fun `resolve builtin string`() {
        val (_, resolver) = makeResolver()
        val result = resolver.resolve(id("string"))
        assertEquals(ResolvedType.StringType, result)
    }

    @Test
    fun `resolve builtin bool`() {
        val (_, resolver) = makeResolver()
        val result = resolver.resolve(id("bool"))
        assertEquals(ResolvedType.BoolType, result)
    }

    @Test
    fun `resolve builtin char`() {
        val (_, resolver) = makeResolver()
        val result = resolver.resolve(id("char"))
        assertEquals(ResolvedType.CharType, result)
    }

    @Test
    fun `resolve void type`() {
        val (env, resolver) = makeResolver()
        val node = VoidType("test.hasab", 1, 1, 0, 4)
        val result = resolver.resolve(node)
        assertEquals(ResolvedType.VoidType, result)
    }

    @Test
    fun `resolve unknown type produces error`() {
        val (_, resolver) = makeResolver()
        val result = resolver.resolve(id("Foo"))
        assertIs<ResolvedType.ErrorType>(result)
        assertTrue(resolver.errors().isNotEmpty())
    }

    @Test
    fun `resolve user-defined struct type`() {
        val (env, resolver) = makeResolver()
        val fields = linkedMapOf<String, ResolvedType>("x" to ResolvedType.IntType, "y" to ResolvedType.IntType)
        env.define(StructSymbol("Point", fields))

        val result = resolver.resolve(id("Point"))
        assertIs<ResolvedType.StructType>(result)
        assertEquals("Point", result.name)
    }

    @Test
    fun `resolve array type`() {
        val (_, resolver) = makeResolver()
        val node = hasab.compiler.frontend.ast.ArrayType(id("int"), "test.hasab", 1, 1, 0, 5)
        val result = resolver.resolve(node)
        assertIs<ResolvedType.ArrayType>(result)
        assertEquals(ResolvedType.IntType, result.elementType)
    }

    @Test
    fun `resolve pointer type`() {
        val (_, resolver) = makeResolver()
        val node = PointerType(id("float"), "test.hasab", 1, 1, 0, 7)
        val result = resolver.resolve(node)
        assertIs<ResolvedType.PointerType>(result)
        assertEquals(ResolvedType.FloatType, result.elementType)
    }

    @Test
    fun `resolve optional type`() {
        val (_, resolver) = makeResolver()
        val node = OptionalType(id("int"), "test.hasab", 1, 1, 0, 5)
        val result = resolver.resolve(node)
        assertIs<ResolvedType.OptionalType>(result)
        assertEquals(ResolvedType.IntType, result.elementType)
    }

    @Test
    fun `resolve function type`() {
        val (_, resolver) = makeResolver()
        val node = hasab.compiler.frontend.ast.FunctionType(
            listOf(id("int"), id("string")),
            id("bool"),
            "test.hasab", 1, 1, 0, 20,
        )
        val result = resolver.resolve(node)
        assertIs<ResolvedType.FunctionType>(result)
        assertEquals(2, result.parameterTypes.size)
        assertEquals(ResolvedType.IntType, result.parameterTypes[0])
        assertEquals(ResolvedType.StringType, result.parameterTypes[1])
        assertEquals(ResolvedType.BoolType, result.returnType)
    }

    @Test
    fun `resolve nested array type`() {
        val (_, resolver) = makeResolver()
        val innerArray = hasab.compiler.frontend.ast.ArrayType(id("int"), "test.hasab", 1, 1, 0, 5)
        val outerArray = hasab.compiler.frontend.ast.ArrayType(innerArray, "test.hasab", 1, 1, 0, 7)
        val result = resolver.resolve(outerArray)
        assertIs<ResolvedType.ArrayType>(result)
        assertIs<ResolvedType.ArrayType>(result.elementType)
    }
}
