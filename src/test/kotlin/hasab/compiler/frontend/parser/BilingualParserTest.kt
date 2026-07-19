package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Parser integration tests verifying that English and Amharic keyword forms
 * produce ASTs with the same structure.
 */
class BilingualParserTest {

    private fun parseModule(code: String): ParseResult {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        return Parser(lexerResult).parse()
    }

    private fun amharicFor(latin: String): String {
        val kw = Keyword.lookup(latin) ?: error("No keyword for '$latin'")
        return String(kw.amharicChars)
    }

    private fun amharicAltFor(latin: String): String {
        val kw = Keyword.ALL.first { it.latin == latin }
        return kw.amharicAlt ?: error("No alt Amharic for '$latin'")
    }

    private fun returnTypeIsInt(decl: FnDecl): Boolean {
        val rt = decl.returnType ?: return false
        return rt is IdentifierType && rt.name == "int"
    }

    // ── fn / ተግባር ─────────────────────────────────────────────

    @Test
    fun `latin fn parses as FnDecl`() {
        val r = parseModule("fn add(a: int, b: int) -> int { return a + b; }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertEquals("add", fn.name)
        assertEquals(2, fn.parameters.size)
        assertTrue(returnTypeIsInt(fn))
    }

    @Test
    fun `amharic fn parses as FnDecl`() {
        val amharic = amharicFor("fn")
        val r = parseModule("$amharic add(a: int, b: int) -> int { return a + b; }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertEquals("add", fn.name)
        assertEquals(2, fn.parameters.size)
        assertTrue(returnTypeIsInt(fn))
    }

    @Test
    fun `latin and amharic fn produce identical AST structure`() {
        val amharic = amharicFor("fn")
        val latinResult = parseModule("fn greet(name: string) -> string { return name; }")
        val amharicResult = parseModule("$amharic greet(name: string) -> string { return name; }")

        assertFalse(latinResult.hasErrors)
        assertFalse(amharicResult.hasErrors)

        val latinFn = assertIs<FnDecl>(latinResult.module.declarations[0])
        val amharicFn = assertIs<FnDecl>(amharicResult.module.declarations[0])

        assertEquals(latinFn.name, amharicFn.name)
        assertEquals(latinFn.parameters.size, amharicFn.parameters.size)
        assertEquals(latinFn.body!!.statements.size, amharicFn.body!!.statements.size)
    }

    // ── let / ለ / ይሁን ──────────────────────────────────────────

    @Test
    fun `latin let parses as LetStmt`() {
        val r = parseModule("fn main() { let x = 42; }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val letStmt = assertIs<LetStmt>(fn.body!!.statements[0])
        assertEquals("x", letStmt.name)
        assertFalse(letStmt.isMutable)
    }

    @Test
    fun `existing amharic let parses as LetStmt`() {
        val r = parseModule("fn main() { ${amharicFor("let")} x = 42; }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val letStmt = assertIs<LetStmt>(fn.body!!.statements[0])
        assertEquals("x", letStmt.name)
        assertFalse(letStmt.isMutable)
    }

    @Test
    fun `alt amharic let parses as LetStmt`() {
        val alt = amharicAltFor("let")
        val r = parseModule("fn main() { $alt x = 42; }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val letStmt = assertIs<LetStmt>(fn.body!!.statements[0])
        assertEquals("x", letStmt.name)
        assertFalse(letStmt.isMutable)
    }

    @Test
    fun `let mut produces mutable LetStmt for all three forms`() {
        val existingAmharic = amharicFor("let")
        val altAmharic = amharicAltFor("let")

        val latin = parseModule("fn main() { let mut x = 10; }")
        val existing = parseModule("fn main() { $existingAmharic mut x = 10; }")
        val alt = parseModule("fn main() { $altAmharic mut x = 10; }")

        assertFalse(latin.hasErrors)
        assertFalse(existing.hasErrors)
        assertFalse(alt.hasErrors)

        val latinLet = assertIs<LetStmt>((latin.module.declarations[0] as FnDecl).body!!.statements[0])
        val existingLet = assertIs<LetStmt>((existing.module.declarations[0] as FnDecl).body!!.statements[0])
        val altLet = assertIs<LetStmt>((alt.module.declarations[0] as FnDecl).body!!.statements[0])

        assertTrue(latinLet.isMutable)
        assertTrue(existingLet.isMutable)
        assertTrue(altLet.isMutable)
        assertEquals(latinLet.name, existingLet.name)
        assertEquals(latinLet.name, altLet.name)
    }

    @Test
    fun `let with type annotation for all forms`() {
        val existingAmharic = amharicFor("let")
        val altAmharic = amharicAltFor("let")

        val latin = parseModule("fn main() { let x: int = 42; }")
        val existing = parseModule("fn main() { $existingAmharic x: int = 42; }")
        val alt = parseModule("fn main() { $altAmharic x: int = 42; }")

        assertFalse(latin.hasErrors)
        assertFalse(existing.hasErrors)
        assertFalse(alt.hasErrors)

        val latinLet = assertIs<LetStmt>((latin.module.declarations[0] as FnDecl).body!!.statements[0])
        val existingLet = assertIs<LetStmt>((existing.module.declarations[0] as FnDecl).body!!.statements[0])
        val altLet = assertIs<LetStmt>((alt.module.declarations[0] as FnDecl).body!!.statements[0])

        assertTrue(latinLet.typeAnnotation != null)
        assertTrue(existingLet.typeAnnotation != null)
        assertTrue(altLet.typeAnnotation != null)
    }

    // ── println / ጻፍ ───────────────────────────────────────────
    // println is a recognized keyword in expression position.
    // Both forms parse without errors and produce identical ASTs.

    @Test
    fun `latin println call parses without errors`() {
        val r = parseModule("fn main() { println(\"hello\"); }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val exprStmt = assertIs<ExprStmt>(fn.body!!.statements[0])
        assertIs<CallExpr>(exprStmt.expression)
    }

    @Test
    fun `amharic println call parses without errors`() {
        val amharic = amharicFor("println")
        val r = parseModule("fn main() { $amharic(\"hello\"); }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val exprStmt = assertIs<ExprStmt>(fn.body!!.statements[0])
        assertIs<CallExpr>(exprStmt.expression)
    }

    @Test
    fun `println and amharic println produce identical AST`() {
        val amharic = amharicFor("println")
        val latin = parseModule("fn main() { println(\"hello\"); }")
        val amharicResult = parseModule("fn main() { $amharic(\"hello\"); }")

        assertFalse(latin.hasErrors)
        assertFalse(amharicResult.hasErrors)

        val latinFn = assertIs<FnDecl>(latin.module.declarations[0])
        val amharicFn = assertIs<FnDecl>(amharicResult.module.declarations[0])

        val latinCall = assertIs<CallExpr>((latinFn.body!!.statements[0] as ExprStmt).expression)
        val amharicCall = assertIs<CallExpr>((amharicFn.body!!.statements[0] as ExprStmt).expression)

        // Both must resolve to the same canonical identifier "println"
        val latinCallee = assertIs<IdentifierExpr>(latinCall.callee)
        val amharicCallee = assertIs<IdentifierExpr>(amharicCall.callee)
        assertEquals("println", latinCallee.name)
        assertEquals("println", amharicCallee.name)
        assertEquals(latinCallee.name, amharicCallee.name)

        assertEquals(latinCall.arguments.size, amharicCall.arguments.size)
    }

    // ── return / ተመለስ ────────────────────────────────────────

    @Test
    fun `latin return parses as ReturnStmt`() {
        val r = parseModule("fn main() { return 42; }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertIs<ReturnStmt>(fn.body!!.statements[0])
    }

    @Test
    fun `amharic return parses as ReturnStmt`() {
        val amharic = amharicFor("return")
        val r = parseModule("fn main() { $amharic 42; }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertIs<ReturnStmt>(fn.body!!.statements[0])
    }

    // ── Mixed English/Amharic in single program ─────────────────

    @Test
    fun `mixed keywords parse correctly`() {
        val altLet = amharicAltFor("let")
        val r = parseModule("""
            fn main() {
                $altLet x = 10;
                return x;
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertEquals(2, fn.body!!.statements.size)
        assertIs<LetStmt>(fn.body!!.statements[0])
        assertIs<ReturnStmt>(fn.body!!.statements[1])
    }

    @Test
    fun `fully amharic program parses correctly`() {
        val fnKw = amharicFor("fn")
        val altLet = amharicAltFor("let")
        val returnKw = amharicFor("return")

        val r = parseModule("""
            $fnKw main() {
                $altLet x = 10;
                $returnKw x;
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertEquals(2, fn.body!!.statements.size)
    }

    // ── if / ከመ ────────────────────────────────────────────────

    @Test
    fun `latin if parses as IfStmt`() {
        val r = parseModule("fn main() { if true { let x = 1; } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertIs<IfStmt>(fn.body!!.statements[0])
    }

    @Test
    fun `amharic if parses as IfStmt`() {
        val amharic = amharicFor("if")
        val r = parseModule("fn main() { $amharic true { let x = 1; } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertIs<IfStmt>(fn.body!!.statements[0])
    }

    // ── for / አምልኮ ────────────────────────────────────────────
    // Parser syntax: for (variable: iterable) { body }

    @Test
    fun `latin for parses as ForStmt`() {
        val r = parseModule("fn main() { for (i: 0..3) { } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertIs<ForStmt>(fn.body!!.statements[0])
    }

    @Test
    fun `amharic for parses as ForStmt`() {
        val amharic = amharicFor("for")
        val r = parseModule("fn main() { $amharic (i: 0..3) { } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        assertIs<ForStmt>(fn.body!!.statements[0])
    }

    // ── break / ውቅያና ──────────────────────────────────────────

    @Test
    fun `latin break parses as BreakStmt`() {
        val r = parseModule("fn main() { while true { break; } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val whileStmt = assertIs<WhileStmt>(fn.body!!.statements[0])
        assertIs<BreakStmt>(whileStmt.body.statements[0])
    }

    @Test
    fun `amharic break parses as BreakStmt`() {
        val amharic = amharicFor("break")
        val r = parseModule("fn main() { while true { $amharic; } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val whileStmt = assertIs<WhileStmt>(fn.body!!.statements[0])
        assertIs<BreakStmt>(whileStmt.body.statements[0])
    }

    // ── continue / ቀጥል ────────────────────────────────────────

    @Test
    fun `latin continue parses as ContinueStmt`() {
        val r = parseModule("fn main() { while true { continue; } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val whileStmt = assertIs<WhileStmt>(fn.body!!.statements[0])
        assertIs<ContinueStmt>(whileStmt.body.statements[0])
    }

    @Test
    fun `amharic continue parses as ContinueStmt`() {
        val amharic = amharicFor("continue")
        val r = parseModule("fn main() { while true { $amharic; } }")
        assertFalse(r.hasErrors)
        val fn = assertIs<FnDecl>(r.module.declarations[0])
        val whileStmt = assertIs<WhileStmt>(fn.body!!.statements[0])
        assertIs<ContinueStmt>(whileStmt.body.statements[0])
    }
}
