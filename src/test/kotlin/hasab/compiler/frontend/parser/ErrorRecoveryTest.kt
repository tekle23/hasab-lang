package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorRecoveryTest {

    private fun parseModule(code: String): ParseResult {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        return Parser(lexerResult).parse()
    }

    @Test
    fun `unexpected token produces error`() {
        val r = parseModule("@")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { "Unexpected" in it.message })
    }

    @Test
    fun `missing closing brace`() {
        val r = parseModule("fn main() { let x = 1;")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `empty input`() {
        val r = parseModule("")
        assertTrue(r.module.declarations.isEmpty())
    }

    @Test
    fun `only comments`() {
        val r = parseModule("// just a comment\n/* block comment */")
        assertTrue(r.module.declarations.isEmpty())
    }

    @Test
    fun `malformed function recovered`() {
        val r = parseModule("fn 123 { fn good() { } }")
        assertTrue(r.hasErrors)
        assertTrue(r.module.declarations.size >= 1)
    }

    @Test
    fun `duplicate tokens handled`() {
        val r = parseModule("let let x = 1;")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `mixed valid and invalid`() {
        val r = parseModule("""
            fn good() { }
            @invalid
            fn also_good() { }
        """.trimIndent())
        assertTrue(r.hasErrors)
        assertTrue(r.module.declarations.size >= 2)
    }
}
