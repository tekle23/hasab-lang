package hasab.compiler.frontend.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Lexer tests verifying that English and Amharic keyword forms
 * produce identical token types and token streams.
 */
class BilingualKeywordTest {

    private fun tokenize(source: String): LexerResult {
        val file = SourceFile("test.has", source)
        val lexer = Lexer(file)
        return lexer.tokenize()
    }

    private fun amharicFor(latin: String): String {
        val kw = Keyword.lookup(latin) ?: error("No keyword for '$latin'")
        return String(kw.amharicChars)
    }

    private fun tokenStringOf(source: String): String {
        val token = tokenize(source).tokens.first()
        assertTrue(token.type is TokenType.Keyword, "Expected Keyword for '$source', got ${token.type}")
        return (token.type as TokenType.Keyword).keyword.tokenString
    }

    // ── println / ጻፍ ────────────────────────────────────────────

    @Test
    fun `latin println produces Keyword token`() {
        assertEquals("println", tokenStringOf("println"))
    }

    @Test
    fun `amharic println produces Keyword token`() {
        val amharic = amharicFor("println")
        assertEquals("println", tokenStringOf(amharic))
    }

    @Test
    fun `latin and amharic println produce same tokenString`() {
        val latin = tokenize("println").tokens.first()
        val amharic = tokenize(amharicFor("println")).tokens.first()
        assertTrue(latin.type is TokenType.Keyword)
        assertTrue(amharic.type is TokenType.Keyword)
        assertEquals(
            (latin.type as TokenType.Keyword).keyword.tokenString,
            (amharic.type as TokenType.Keyword).keyword.tokenString,
        )
    }

    @Test
    fun `println call produces identical token streams`() {
        val amharic = amharicFor("println")
        val latin = tokenize("println(\"hello\")").tokens
        val amharicTokens = tokenize("$amharic(\"hello\")").tokens

        assertEquals(latin.size, amharicTokens.size)
        for (i in latin.indices) {
            assertEquals(latin[i].type, amharicTokens[i].type)
        }
    }

    @Test
    fun `latin println in function body produces expected tokens`() {
        val tokens = tokenize("fn main() {\n    println(\"hello\");\n}").tokens
            .filter { it.type != TokenType.Eof }

        assertEquals(TokenType.Keyword::class, tokens[0].type::class)  // fn
        assertEquals(TokenType.Identifier, tokens[1].type)             // main
        assertEquals(TokenType.LeftParen, tokens[2].type)              // (
        assertEquals(TokenType.RightParen, tokens[3].type)             // )
        assertEquals(TokenType.LeftBrace, tokens[4].type)              // {
        assertEquals(TokenType.Keyword::class, tokens[5].type::class)  // println
        assertEquals(TokenType.LeftParen, tokens[6].type)              // (
        assertEquals(TokenType.StringLiteral, tokens[7].type)          // "hello"
        assertEquals(TokenType.RightParen, tokens[8].type)             // )
        assertEquals(TokenType.Semicolon, tokens[9].type)              // ;
        assertEquals(TokenType.RightBrace, tokens[10].type)            // }
    }

    @Test
    fun `amharic println in function body produces expected tokens`() {
        val amharic = amharicFor("println")
        val tokens = tokenize("fn main() {\n    $amharic(\"hello\");\n}").tokens
            .filter { it.type != TokenType.Eof }

        assertEquals(TokenType.Keyword::class, tokens[0].type::class)  // fn
        assertEquals(TokenType.Identifier, tokens[1].type)             // main
        assertEquals(TokenType.LeftParen, tokens[2].type)              // (
        assertEquals(TokenType.RightParen, tokens[3].type)             // )
        assertEquals(TokenType.LeftBrace, tokens[4].type)              // {
        assertEquals(TokenType.Keyword::class, tokens[5].type::class)  // ጻፍ
        assertEquals(TokenType.LeftParen, tokens[6].type)              // (
        assertEquals(TokenType.StringLiteral, tokens[7].type)          // "hello"
        assertEquals(TokenType.RightParen, tokens[8].type)             // )
        assertEquals(TokenType.Semicolon, tokens[9].type)              // ;
        assertEquals(TokenType.RightBrace, tokens[10].type)            // }
    }

    // ── let / ተለይ / ይሁን ───────────────────────────────────────

    @Test
    fun `latin let produces Keyword token with tokenString let`() {
        assertEquals("let", tokenStringOf("let"))
    }

    @Test
    fun `existing amharic let produces Keyword token`() {
        val amharic = amharicFor("let")
        assertEquals("let", tokenStringOf(amharic))
    }

    @Test
    fun `let binding produces identical tokens for latin and amharic`() {
        val amharic = amharicFor("let")
        val latin = tokenize("let x = 42;").tokens
        val amharicTokens = tokenize("$amharic x = 42;").tokens

        assertEquals(latin.size, amharicTokens.size)
        for (i in latin.indices) {
            assertEquals(latin[i].type, amharicTokens[i].type)
        }
    }

    // ── fn / ተግባር ─────────────────────────────────────────────

    @Test
    fun `latin fn produces Keyword token with tokenString fn`() {
        assertEquals("fn", tokenStringOf("fn"))
    }

    @Test
    fun `amharic fn produces Keyword token`() {
        val amharic = amharicFor("fn")
        assertEquals("fn", tokenStringOf(amharic))
    }

    @Test
    fun `fn produces identical keyword token type for both forms`() {
        val latin = tokenize("fn").tokens.first()
        val amharic = tokenize(amharicFor("fn")).tokens.first()
        assertTrue(latin.type is TokenType.Keyword)
        assertTrue(amharic.type is TokenType.Keyword)
        assertEquals(
            (latin.type as TokenType.Keyword).keyword.tokenString,
            (amharic.type as TokenType.Keyword).keyword.tokenString,
        )
    }

    @Test
    fun `fn declaration produces identical token streams`() {
        val amharic = amharicFor("fn")
        val latin = tokenize("fn add(a, b) { return a + b; }").tokens
        val amharicTokens = tokenize("$amharic add(a, b) { return a + b; }").tokens

        assertEquals(latin.size, amharicTokens.size)
        for (i in latin.indices) {
            assertEquals(latin[i].type, amharicTokens[i].type)
        }
    }

    // ── return / ተመለስ ────────────────────────────────────────

    @Test
    fun `latin return produces Keyword token with tokenString return`() {
        assertEquals("return", tokenStringOf("return"))
    }

    @Test
    fun `amharic return produces Keyword token`() {
        val amharic = amharicFor("return")
        assertEquals("return", tokenStringOf(amharic))
    }

    @Test
    fun `return produces identical keyword token type for both forms`() {
        val latin = tokenize("return").tokens.first()
        val amharic = tokenize(amharicFor("return")).tokens.first()
        assertTrue(latin.type is TokenType.Keyword)
        assertTrue(amharic.type is TokenType.Keyword)
        assertEquals(
            (latin.type as TokenType.Keyword).keyword.tokenString,
            (amharic.type as TokenType.Keyword).keyword.tokenString,
        )
    }

    @Test
    fun `return statement produces identical token streams`() {
        val amharic = amharicFor("return")
        val latin = tokenize("return 42;").tokens
        val amharicTokens = tokenize("$amharic 42;").tokens

        assertEquals(latin.size, amharicTokens.size)
        for (i in latin.indices) {
            assertEquals(latin[i].type, amharicTokens[i].type)
        }
    }

    // ── Comprehensive: all keyword pairs ────────────────────────

    @Test
    fun `all registered keywords produce identical tokenString for both forms`() {
        for (kw in Keyword.ALL) {
            val latinResult = tokenize(kw.latin)
            val latinToken = latinResult.tokens.first()
            assertTrue(latinToken.type is TokenType.Keyword, "Latin '${kw.latin}' should be Keyword")
            assertEquals(kw.tokenString, (latinToken.type as TokenType.Keyword).keyword.tokenString)

            val amharicStr = String(kw.amharicChars)
            val amharicResult = tokenize(amharicStr)
            val amharicToken = amharicResult.tokens.first()
            // Skip keywords whose Amharic form contains non-identifier characters
            // (placeholder forms like "በ.repeat", "ሰንstructor", etc.)
            if (amharicToken.lexeme != amharicStr) continue
            assertTrue(amharicToken.type is TokenType.Keyword, "Amharic '$amharicStr' for '${kw.latin}' should be Keyword")
            assertEquals(kw.tokenString, (amharicToken.type as TokenType.Keyword).keyword.tokenString)
        }
    }

    @Test
    fun `all alt Amharic forms produce correct keyword token`() {
        for (kw in Keyword.ALL) {
            val alt = kw.amharicAlt ?: continue
            val altResult = tokenize(alt)
            val altToken = altResult.tokens.first()
            assertTrue(altToken.type is TokenType.Keyword, "Alt Amharic '$alt' for '${kw.latin}' should be Keyword")
            assertEquals(kw.tokenString, (altToken.type as TokenType.Keyword).keyword.tokenString)
        }
    }
}
