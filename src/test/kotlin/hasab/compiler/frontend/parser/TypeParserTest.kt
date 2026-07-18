package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TypeParserTest {

    private fun parseType(code: String): TypeNode {
        val source = SourceFile("test.hasab", code)
        val result = Lexer(source).tokenize()
        val stream = TokenStream(result.tokens)
        val diagnostics = mutableListOf<ParserDiagnostic>()
        return TypeParser(stream, diagnostics).parseType()
    }

    @Test
    fun `simple identifier type`() {
        val t = parseType("int")
        assertIs<IdentifierType>(t)
        assertEquals("int", t.name)
    }

    @Test
    fun `string type`() {
        val t = parseType("string")
        assertIs<IdentifierType>(t)
        assertEquals("string", t.name)
    }

    @Test
    fun `bool type`() {
        val t = parseType("bool")
        assertIs<IdentifierType>(t)
        assertEquals("bool", t.name)
    }

    @Test
    fun `void type`() {
        val t = parseType("void")
        assertIs<VoidType>(t)
    }

    @Test
    fun `array type`() {
        val t = parseType("[int]")
        assertIs<ArrayType>(t)
        assertIs<IdentifierType>(t.elementType)
        assertEquals("int", (t.elementType as IdentifierType).name)
    }

    @Test
    fun `nested array type`() {
        val t = parseType("[[string]]")
        assertIs<ArrayType>(t)
        assertIs<ArrayType>(t.elementType)
    }

    @Test
    fun `pointer type`() {
        val t = parseType("*int")
        assertIs<PointerType>(t)
        assertIs<IdentifierType>(t.elementType)
    }

    @Test
    fun `function type with no params`() {
        val t = parseType("fn() -> int")
        assertIs<FunctionType>(t)
        assertEquals(0, t.parameterTypes.size)
        assertIs<IdentifierType>(t.returnType)
        assertEquals("int", (t.returnType as IdentifierType).name)
    }

    @Test
    fun `function type with params`() {
        val t = parseType("fn(int, string) -> bool")
        assertIs<FunctionType>(t)
        assertEquals(2, t.parameterTypes.size)
    }

    @Test
    fun `function type with no return`() {
        val t = parseType("fn(int)")
        assertIs<FunctionType>(t)
        assertIs<VoidType>(t.returnType)
    }

    @Test
    fun `qualified type`() {
        val t = parseType("std::io::Writer")
        assertIs<QualifiedType>(t)
        assertEquals(listOf("std", "io", "Writer"), t.path)
    }

    @Test
    fun `single segment qualified type`() {
        val t = parseType("mymodule")
        assertIs<IdentifierType>(t)
        assertEquals("mymodule", t.name)
    }

    @Test
    fun `pointer to array type`() {
        val t = parseType("*[int]")
        assertIs<PointerType>(t)
        assertIs<ArrayType>(t.elementType)
    }

    @Test
    fun `ethiopic type name`() {
        val t = parseType("ሰላም")
        assertIs<IdentifierType>(t)
        assertEquals("ሰላም", t.name)
    }
}
