package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DeclarationParserTest {

    private fun parseDecl(code: String): Decl {
        val source = SourceFile("test.hasab", code)
        val result = Lexer(source).tokenize()
        val stream = TokenStream(result.tokens)
        val diagnostics = mutableListOf<ParserDiagnostic>()
        return DeclarationParser(stream, diagnostics).parseDeclaration()
    }

    private fun parseDecls(code: String): List<Decl> {
        val source = SourceFile("test.hasab", code)
        val result = Lexer(source).tokenize()
        val stream = TokenStream(result.tokens)
        val diagnostics = mutableListOf<ParserDiagnostic>()
        val decls = mutableListOf<Decl>()
        while (!stream.isAtEnd()) {
            decls.add(DeclarationParser(stream, diagnostics).parseDeclaration())
        }
        return decls
    }

    @Test
    fun `function declaration`() {
        val d = parseDecl("fn add(x: int, y: int) -> int { return x + y; }")
        assertIs<FnDecl>(d)
        assertEquals("add", d.name)
        assertEquals(2, d.parameters.size)
        assertNotNull(d.returnType)
        assertNotNull(d.body)
    }

    @Test
    fun `function with no params`() {
        val d = parseDecl("fn main() { }")
        assertIs<FnDecl>(d)
        assertEquals(0, d.parameters.size)
    }

    @Test
    fun `function with mutable param`() {
        val d = parseDecl("fn modify(mut x: int) { x = 10; }")
        assertIs<FnDecl>(d)
        assertTrue(d.parameters[0].isMutable)
    }

    @Test
    fun `function no return type`() {
        val d = parseDecl("fn noop() { }")
        assertIs<FnDecl>(d)
        assertEquals(null, d.returnType)
    }

    @Test
    fun `struct declaration`() {
        val d = parseDecl("struct Point { x: int, y: int }")
        assertIs<StructDecl>(d)
        assertEquals("Point", d.name)
        assertEquals(2, d.fields.size)
    }

    @Test
    fun `struct with mutable field`() {
        val d = parseDecl("struct Counter { mut value: int }")
        assertIs<StructDecl>(d)
        assertTrue(d.fields[0].isMutable)
    }

    @Test
    fun `struct empty`() {
        val d = parseDecl("struct Empty { }")
        assertIs<StructDecl>(d)
        assertEquals(0, d.fields.size)
    }

    @Test
    fun `enum declaration`() {
        val d = parseDecl("enum Color { Red, Green, Blue }")
        assertIs<EnumDecl>(d)
        assertEquals("Color", d.name)
        assertEquals(3, d.variants.size)
    }

    @Test
    fun `enum with fields`() {
        val d = parseDecl("enum Result { Ok(int), Err(string) }")
        assertIs<EnumDecl>(d)
        assertEquals(2, d.variants.size)
        assertEquals(1, d.variants[0].fields.size)
    }

    @Test
    fun `impl declaration`() {
        val d = parseDecl("impl Point { fn new(x: int, y: int) -> Point { } }")
        assertIs<ImplDecl>(d)
        assertEquals(1, d.methods.size)
    }

    @Test
    fun `trait declaration`() {
        val d = parseDecl("trait Drawable { fn draw(); }")
        assertIs<TraitDecl>(d)
        assertEquals("Drawable", d.name)
        assertEquals(1, d.methods.size)
    }

    @Test
    fun `type alias declaration`() {
        val d = parseDecl("type IntList = [int]")
        assertIs<TypeAliasDecl>(d)
        assertEquals("IntList", d.name)
    }

    @Test
    fun `use declaration`() {
        val d = parseDecl("use std::io")
        assertIs<UseDecl>(d)
        assertEquals(listOf("std", "io"), d.path)
    }

    @Test
    fun `mod declaration with body`() {
        val d = parseDecl("mod math { fn add(x: int, y: int) -> int { } }")
        assertIs<ModDecl>(d)
        assertEquals("math", d.name)
        assertNotNull(d.body)
    }

    @Test
    fun `mod declaration without body`() {
        val d = parseDecl("mod utils;")
        assertIs<ModDecl>(d)
        assertEquals(null, d.body)
    }

    @Test
    fun `pub function`() {
        val d = parseDecl("pub::fn main() { }")
        assertIs<PubDecl>(d)
        assertIs<FnDecl>(d.inner)
    }

    @Test
    fun `pub struct`() {
        val d = parseDecl("pub::struct Vec { data: [int] }")
        assertIs<PubDecl>(d)
        assertIs<StructDecl>(d.inner)
    }

    @Test
    fun `function with no body`() {
        val d = parseDecl("fn add(x: int, y: int) -> int")
        assertIs<FnDecl>(d)
        assertEquals(null, d.body)
    }

    @Test
    fun `struct with complex field types`() {
        val d = parseDecl("struct Matrix { data: [[float]], rows: int }")
        assertIs<StructDecl>(d)
        assertEquals(2, d.fields.size)
        assertIs<ArrayType>((d.fields[0].type as ArrayType).elementType)
    }
}
