package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StatementParserTest {

    private fun parseStmt(code: String): Stmt {
        val source = SourceFile("test.hasab", code)
        val result = Lexer(source).tokenize()
        val stream = TokenStream(result.tokens)
        val diagnostics = mutableListOf<ParserDiagnostic>()
        return StatementParser(stream, diagnostics).parseStatement()
    }

    private fun parseBlock(code: String): Block {
        val source = SourceFile("test.hasab", code)
        val result = Lexer(source).tokenize()
        val stream = TokenStream(result.tokens)
        val diagnostics = mutableListOf<ParserDiagnostic>()
        return StatementParser(stream, diagnostics).parseBlock()
    }

    @Test
    fun `expression statement`() {
        val s = parseStmt("42;")
        assertIs<ExprStmt>(s)
    }

    @Test
    fun `return statement with value`() {
        val s = parseStmt("return 42;")
        assertIs<ReturnStmt>(s)
        assertNotNull(s.value)
    }

    @Test
    fun `return statement without value`() {
        val s = parseStmt("return;")
        assertIs<ReturnStmt>(s)
        assertEquals(null, s.value)
    }

    @Test
    fun `break statement`() {
        val s = parseStmt("break;")
        assertIs<BreakStmt>(s)
    }

    @Test
    fun `continue statement`() {
        val s = parseStmt("continue;")
        assertIs<ContinueStmt>(s)
    }

    @Test
    fun `let statement`() {
        val s = parseStmt("let x = 42;")
        assertIs<LetStmt>(s)
        assertEquals("x", s.name)
        assertEquals(false, s.isMutable)
    }

    @Test
    fun `mut statement`() {
        val s = parseStmt("mut x = 42;")
        assertIs<LetStmt>(s)
        assertEquals(true, s.isMutable)
    }

    @Test
    fun `let with type annotation`() {
        val s = parseStmt("let x: int = 42;")
        assertIs<LetStmt>(s)
        assertNotNull(s.typeAnnotation)
    }

    @Test
    fun `if statement`() {
        val s = parseStmt("if true { return 1; }")
        assertIs<IfStmt>(s)
    }

    @Test
    fun `if else statement`() {
        val s = parseStmt("if true { return 1; } else { return 0; }")
        assertIs<IfStmt>(s)
        assertNotNull(s.elseBranch)
    }

    @Test
    fun `if else if statement`() {
        val s = parseStmt("if true { return 1; } else if false { return 0; }")
        assertIs<IfStmt>(s)
        assertIs<IfStmt>(s.elseBranch)
    }

    @Test
    fun `while statement`() {
        val s = parseStmt("while true { break; }")
        assertIs<WhileStmt>(s)
    }

    @Test
    fun `for statement`() {
        val s = parseStmt("for (i: 0..10) { }")
        assertIs<ForStmt>(s)
        assertEquals("i", s.variable)
    }

    @Test
    fun `block statement`() {
        val b = parseBlock("{ let x = 1; return x; }")
        assertIs<Block>(b)
        assertEquals(2, b.statements.size)
    }

    @Test
    fun `nested blocks`() {
        val b = parseBlock("{ { let x = 1; } }")
        assertIs<Block>(b)
        assertEquals(1, b.statements.size)
        assertIs<Block>(b.statements[0])
    }

    @Test
    fun `multiple statements in block`() {
        val b = parseBlock("{ let a = 1; let b = 2; let c = 3; }")
        assertEquals(3, b.statements.size)
    }

    @Test
    fun `complex expression statement`() {
        val s = parseStmt("foo(1 + 2, bar(x));")
        assertIs<ExprStmt>(s)
        assertIs<CallExpr>(s.expression)
    }
}
