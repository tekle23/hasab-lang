package hasab.compiler.frontend.lexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LexerTest {

    private fun tokenize(input: String, fileName: String = "test.hasab"): LexerResult {
        val source = SourceFile(fileName, input)
        return Lexer(source).tokenize()
    }

    private fun tokenTypes(input: String): List<TokenType> {
        return tokenize(input).tokens.map { it.type }
    }

    private fun assertTokenAt(
        result: LexerResult,
        index: Int,
        expectedType: TokenType,
        expectedLexeme: String,
        expectedLine: Int = 1,
        expectedColumn: Int = 1,
    ) {
        val token = result.tokens[index]
        assertEquals(expectedType, token.type, "Token type mismatch at index $index")
        assertEquals(expectedLexeme, token.lexeme, "Lexeme mismatch at index $index")
        assertEquals(expectedLine, token.line, "Line mismatch at index $index")
        assertEquals(expectedColumn, token.column, "Column mismatch at index $index")
        assertEquals("test.hasab", token.fileName)
    }

    private fun assertKeyword(token: Token, expectedLatin: String) {
        assertIs<TokenType.Keyword>(token.type)
        assertEquals(expectedLatin, (token.type as TokenType.Keyword).keyword.latin)
    }

    private fun isKeyword(tokenType: TokenType): Boolean = tokenType is TokenType.Keyword

    // ── Identifiers ─────────────────────────────────────────────

    @Test
    fun `simple identifier`() {
        val result = tokenize("foo")
        assertTokenAt(result, 0, TokenType.Identifier, "foo")
    }

    @Test
    fun `identifier with underscore`() {
        val result = tokenize("_bar_baz")
        assertTokenAt(result, 0, TokenType.Identifier, "_bar_baz")
    }

    @Test
    fun `identifier with digits`() {
        val result = tokenize("x1y2z3")
        assertTokenAt(result, 0, TokenType.Identifier, "x1y2z3")
    }

    // ── Keywords ────────────────────────────────────────────────

    @Test
    fun `latin keyword fn`() {
        val result = tokenize("fn")
        assertKeyword(result.tokens[0], "fn")
    }

    @Test
    fun `latin keyword let`() {
        val result = tokenize("let")
        assertKeyword(result.tokens[0], "let")
    }

    @Test
    fun `latin keyword true`() {
        val result = tokenize("true")
        assertIs<TokenType.Keyword>(result.tokens[0].type)
    }

    @Test
    fun `latin keyword false`() {
        val result = tokenize("false")
        assertIs<TokenType.Keyword>(result.tokens[0].type)
    }

    @Test
    fun `all Latin keywords are recognized`() {
        val keywords = listOf(
            "fn", "let", "mut", "if", "else", "while", "for",
            "return", "break", "continue", "true", "false", "nil",
            "struct", "enum", "impl", "trait", "pub", "mod", "use",
            "as", "type", "class", "new", "this", "super", "static",
            "void", "int", "float", "string", "bool", "char", "import", "package",
        )
        for (kw in keywords) {
            val result = tokenize(kw)
            assertEquals(2, result.tokens.size, "Expected keyword + EOF for '$kw'")
            assertIs<TokenType.Keyword>(result.tokens[0].type, "Expected Keyword type for '$kw'")
        }
    }

    // ── Amharic / Ethiopic Identifiers and Keywords ──────────────

    @Test
    fun `ethiopic identifier`() {
        val result = tokenize("ሰላም")
        assertTokenAt(result, 0, TokenType.Identifier, "ሰላም")
    }

    @Test
    fun `amharic keyword`() {
        val result = tokenize("ተግባር")
        assertIs<TokenType.Keyword>(result.tokens[0].type)
    }

    @Test
    fun `mixed latin and ethiopic in source`() {
        val result = tokenize("fn ሰላም = 42")
        assertTrue(isKeyword(result.tokens[0].type))  // fn
        assertEquals(TokenType.Identifier, result.tokens[1].type) // ሰላም
        assertEquals(TokenType.Assign, result.tokens[2].type)     // =
        assertEquals(TokenType.IntegerLiteral, result.tokens[3].type) // 42
    }

    @Test
    fun `ethiopic let keyword`() {
        val result = tokenize("ለ")
        assertKeyword(result.tokens[0], "let")
    }

    // ── Integer Literals ────────────────────────────────────────

    @Test
    fun `simple integer`() {
        val result = tokenize("42")
        assertTokenAt(result, 0, TokenType.IntegerLiteral, "42")
    }

    @Test
    fun `zero`() {
        val result = tokenize("0")
        assertTokenAt(result, 0, TokenType.IntegerLiteral, "0")
    }

    @Test
    fun `large integer`() {
        val result = tokenize("999999999")
        assertTokenAt(result, 0, TokenType.IntegerLiteral, "999999999")
    }

    @Test
    fun `integer followed by paren`() {
        val result = tokenize("42()")
        assertEquals(TokenType.IntegerLiteral, result.tokens[0].type)
        assertEquals(TokenType.LeftParen, result.tokens[1].type)
        assertEquals(TokenType.RightParen, result.tokens[2].type)
    }

    // ── Float Literals ──────────────────────────────────────────

    @Test
    fun `simple float`() {
        val result = tokenize("3.14")
        assertTokenAt(result, 0, TokenType.FloatLiteral, "3.14")
    }

    @Test
    fun `float with scientific notation`() {
        val result = tokenize("1.5e10")
        assertTokenAt(result, 0, TokenType.FloatLiteral, "1.5e10")
    }

    @Test
    fun `integer with scientific notation`() {
        val result = tokenize("1e5")
        assertTokenAt(result, 0, TokenType.FloatLiteral, "1e5")
    }

    @Test
    fun `float with positive exponent`() {
        val result = tokenize("2.0e+3")
        assertTokenAt(result, 0, TokenType.FloatLiteral, "2.0e+3")
    }

    @Test
    fun `float with negative exponent`() {
        val result = tokenize("2.0e-3")
        assertTokenAt(result, 0, TokenType.FloatLiteral, "2.0e-3")
    }

    // ── String Literals ─────────────────────────────────────────

    @Test
    fun `simple string`() {
        val result = tokenize("\"hello\"")
        assertTokenAt(result, 0, TokenType.StringLiteral, "hello")
    }

    @Test
    fun `empty string`() {
        val result = tokenize("\"\"")
        assertTokenAt(result, 0, TokenType.StringLiteral, "")
    }

    @Test
    fun `string with escape sequences`() {
        val result = tokenize("\"hello\\nworld\"")
        assertEquals(TokenType.StringLiteral, result.tokens[0].type)
        assertEquals("hello\nworld", result.tokens[0].lexeme)
    }

    @Test
    fun `string with tab escape`() {
        val result = tokenize("\"a\\tb\"")
        assertEquals("a\tb", result.tokens[0].lexeme)
    }

    @Test
    fun `string with escaped quote`() {
        val result = tokenize("\"say \\\"hi\\\"\"")
        assertEquals("say \"hi\"", result.tokens[0].lexeme)
    }

    @Test
    fun `string with escaped backslash`() {
        val result = tokenize("\"a\\\\b\"")
        assertEquals("a\\b", result.tokens[0].lexeme)
    }

    @Test
    fun `unterminated string`() {
        val result = tokenize("\"hello")
        assertTrue(result.hasErrors)
        assertEquals(TokenType.StringLiteral, result.tokens[0].type)
    }

    @Test
    fun `unterminated string with newline`() {
        val result = tokenize("\"hello\nworld\"")
        assertTrue(result.hasErrors)
        assertEquals(TokenType.StringLiteral, result.tokens[0].type)
    }

    // ── Character Literals ──────────────────────────────────────

    @Test
    fun `simple char`() {
        val result = tokenize("'a'")
        assertTokenAt(result, 0, TokenType.CharacterLiteral, "a")
    }

    @Test
    fun `char with escape`() {
        val result = tokenize("'\\n'")
        assertEquals(TokenType.CharacterLiteral, result.tokens[0].type)
        assertEquals("\n", result.tokens[0].lexeme)
    }

    @Test
    fun `unterminated char`() {
        val result = tokenize("'a")
        assertTrue(result.hasErrors)
    }

    // ── Operators ───────────────────────────────────────────────

    @Test
    fun `all single-character operators`() {
        val operators = mapOf(
            "+" to TokenType.Plus,
            "-" to TokenType.Minus,
            "*" to TokenType.Star,
            "/" to TokenType.Slash,
            "%" to TokenType.Percent,
            "=" to TokenType.Assign,
            "!" to TokenType.Not,
            "<" to TokenType.Less,
            ">" to TokenType.Greater,
            "&" to TokenType.BitwiseAnd,
            "|" to TokenType.BitwiseOr,
            "^" to TokenType.BitwiseXor,
            "~" to TokenType.BitwiseNot,
        )
        for ((op, expectedType) in operators) {
            val result = tokenize(op)
            assertEquals(expectedType, result.tokens[0].type, "Operator '$op' mismatch")
        }
    }

    @Test
    fun `comparison operators`() {
        val result = tokenize("== != <= >=")
        assertEquals(TokenType.Equal, result.tokens[0].type)
        assertEquals(TokenType.NotEqual, result.tokens[1].type)
        assertEquals(TokenType.LessEqual, result.tokens[2].type)
        assertEquals(TokenType.GreaterEqual, result.tokens[3].type)
    }

    @Test
    fun `logical operators`() {
        val result = tokenize("&& ||")
        assertEquals(TokenType.And, result.tokens[0].type)
        assertEquals(TokenType.Or, result.tokens[1].type)
    }

    @Test
    fun `assignment operators`() {
        val result = tokenize("+= -= *= /= %=")
        assertEquals(TokenType.PlusAssign, result.tokens[0].type)
        assertEquals(TokenType.MinusAssign, result.tokens[1].type)
        assertEquals(TokenType.StarAssign, result.tokens[2].type)
        assertEquals(TokenType.SlashAssign, result.tokens[3].type)
        assertEquals(TokenType.PercentAssign, result.tokens[4].type)
    }

    @Test
    fun `arrow and fat arrow`() {
        val result = tokenize("-> =>")
        assertEquals(TokenType.Arrow, result.tokens[0].type)
        assertEquals(TokenType.FatArrow, result.tokens[1].type)
    }

    @Test
    fun `double colon`() {
        val result = tokenize("::")
        assertEquals(TokenType.DoubleColon, result.tokens[0].type)
    }

    @Test
    fun `dot and range operators`() {
        val result = tokenize(". .. ..=")
        assertEquals(TokenType.Dot, result.tokens[0].type)
        assertEquals(TokenType.RangeExclusive, result.tokens[1].type)
        assertEquals(TokenType.RangeInclusive, result.tokens[2].type)
    }

    @Test
    fun `shift operators`() {
        val result = tokenize("<< >>")
        assertEquals(TokenType.ShiftLeft, result.tokens[0].type)
        assertEquals(TokenType.ShiftRight, result.tokens[1].type)
    }

    // ── Delimiters ──────────────────────────────────────────────

    @Test
    fun `all delimiters`() {
        val delimiters = mapOf(
            "(" to TokenType.LeftParen,
            ")" to TokenType.RightParen,
            "{" to TokenType.LeftBrace,
            "}" to TokenType.RightBrace,
            "[" to TokenType.LeftBracket,
            "]" to TokenType.RightBracket,
            ";" to TokenType.Semicolon,
            "," to TokenType.Comma,
            ":" to TokenType.Colon,
            "_" to TokenType.Underscore,
        )
        for ((delim, expectedType) in delimiters) {
            val result = tokenize(delim)
            assertEquals(expectedType, result.tokens[0].type, "Delimiter '$delim' mismatch")
        }
    }

    // ── Comments ────────────────────────────────────────────────

    @Test
    fun `single-line comment`() {
        val result = tokenize("// this is a comment\n42")
        assertEquals(TokenType.IntegerLiteral, result.tokens[0].type)
        assertEquals("42", result.tokens[0].lexeme)
        assertFalse(result.hasErrors)
    }

    @Test
    fun `single-line comment at end of file`() {
        val result = tokenize("// comment")
        assertEquals(TokenType.Eof, result.tokens[0].type)
    }

    @Test
    fun `multi-line comment`() {
        val result = tokenize("/* block\ncomment */\n42")
        assertEquals(TokenType.IntegerLiteral, result.tokens[0].type)
        assertEquals("42", result.tokens[0].lexeme)
    }

    @Test
    fun `nested multi-line comments`() {
        val result = tokenize("/* outer /* inner */ outer */\n42")
        assertEquals(TokenType.IntegerLiteral, result.tokens[0].type)
        assertEquals("42", result.tokens[0].lexeme)
    }

    @Test
    fun `unterminated multi-line comment`() {
        val result = tokenize("/* unterminated")
        assertTrue(result.hasErrors)
    }

    @Test
    fun `comment between tokens`() {
        val result = tokenize("1 /* + */ + 2")
        assertEquals(TokenType.IntegerLiteral, result.tokens[0].type)
        assertEquals(TokenType.Plus, result.tokens[1].type)
        assertEquals(TokenType.IntegerLiteral, result.tokens[2].type)
    }

    // ── Whitespace ──────────────────────────────────────────────

    @Test
    fun `multiple spaces`() {
        val result = tokenize("  foo   bar  ")
        assertEquals(TokenType.Identifier, result.tokens[0].type)
        assertEquals("foo", result.tokens[0].lexeme)
        assertEquals(TokenType.Identifier, result.tokens[1].type)
        assertEquals("bar", result.tokens[1].lexeme)
    }

    @Test
    fun `tabs and carriage returns`() {
        val result = tokenize("\tfoo\r\nbar")
        assertEquals(TokenType.Identifier, result.tokens[0].type)
        assertEquals("foo", result.tokens[0].lexeme)
        assertEquals(TokenType.Identifier, result.tokens[1].type)
        assertEquals("bar", result.tokens[1].lexeme)
    }

    // ── Line and Column Tracking ────────────────────────────────

    @Test
    fun `multiline source positions`() {
        val result = tokenize("foo\n  bar\n    baz")
        assertTokenAt(result, 0, TokenType.Identifier, "foo", 1, 1)
        assertTokenAt(result, 1, TokenType.Identifier, "bar", 2, 3)
        assertTokenAt(result, 2, TokenType.Identifier, "baz", 3, 5)
    }

    @Test
    fun `column tracking with operators`() {
        val result = tokenize("a + b")
        assertTokenAt(result, 0, TokenType.Identifier, "a", 1, 1)
        assertTokenAt(result, 1, TokenType.Plus, "+", 1, 3)
        assertTokenAt(result, 2, TokenType.Identifier, "b", 1, 5)
    }

    // ── Offset Tracking ─────────────────────────────────────────

    @Test
    fun `offset tracking`() {
        val result = tokenize("foo bar")
        assertEquals(0, result.tokens[0].startOffset)
        assertEquals(3, result.tokens[0].endOffset)
        assertEquals(4, result.tokens[1].startOffset)
        assertEquals(7, result.tokens[1].endOffset)
    }

    // ── EOF Token ───────────────────────────────────────────────

    @Test
    fun `empty source produces only EOF`() {
        val result = tokenize("")
        assertEquals(1, result.tokens.size)
        assertEquals(TokenType.Eof, result.tokens[0].type)
        assertEquals("", result.tokens[0].lexeme)
    }

    @Test
    fun `EOF token is always last`() {
        val result = tokenize("a b c")
        assertEquals(TokenType.Eof, result.tokens.last().type)
    }

    // ── File Name ───────────────────────────────────────────────

    @Test
    fun `file name propagation`() {
        val result = tokenize("x", "myfile.hasab")
        assertEquals("myfile.hasab", result.tokens[0].fileName)
    }

    // ── Complex Expressions ─────────────────────────────────────

    @Test
    fun `function declaration`() {
        val result = tokenize("fn add(x, y) { return x + y; }")
        assertTrue(isKeyword(result.tokenTypes[0]))   // fn
        assertEquals(TokenType.Identifier, result.tokenTypes[1]) // add
        assertEquals(TokenType.LeftParen, result.tokenTypes[2])
        assertEquals(TokenType.Identifier, result.tokenTypes[3]) // x
        assertEquals(TokenType.Comma, result.tokenTypes[4])
        assertEquals(TokenType.Identifier, result.tokenTypes[5]) // y
        assertEquals(TokenType.RightParen, result.tokenTypes[6])
        assertEquals(TokenType.LeftBrace, result.tokenTypes[7])
        assertTrue(isKeyword(result.tokenTypes[8]))   // return
        assertEquals(TokenType.Identifier, result.tokenTypes[9]) // x
        assertEquals(TokenType.Plus, result.tokenTypes[10])
        assertEquals(TokenType.Identifier, result.tokenTypes[11]) // y
        assertEquals(TokenType.Semicolon, result.tokenTypes[12])
        assertEquals(TokenType.RightBrace, result.tokenTypes[13])
        assertEquals(TokenType.Eof, result.tokenTypes[14])
        assertFalse(result.hasErrors)
    }

    @Test
    fun `let binding with integer`() {
        val result = tokenize("let x = 42;")
        assertTrue(isKeyword(result.tokenTypes[0]))       // let
        assertEquals(TokenType.Identifier, result.tokenTypes[1])
        assertEquals(TokenType.Assign, result.tokenTypes[2])
        assertEquals(TokenType.IntegerLiteral, result.tokenTypes[3])
        assertEquals(TokenType.Semicolon, result.tokenTypes[4])
        assertEquals(TokenType.Eof, result.tokenTypes[5])
    }

    @Test
    fun `if else expression`() {
        val result = tokenize("if (x > 0) { y } else { z }")
        val types = result.tokenTypes
        assertTrue(isKeyword(types[0]))  // if
        assertEquals(TokenType.LeftParen, types[1]) // (
        assertEquals(TokenType.Identifier, types[2])// x
        assertEquals(TokenType.Greater, types[3])   // >
        assertEquals(TokenType.IntegerLiteral, types[4]) // 0
        assertEquals(TokenType.RightParen, types[5])// )
        assertEquals(TokenType.LeftBrace, types[6]) // {
        assertEquals(TokenType.Identifier, types[7])// y
        assertEquals(TokenType.RightBrace, types[8])// }
        assertTrue(isKeyword(types[9]))   // else
        assertEquals(TokenType.LeftBrace, types[10])// {
        assertEquals(TokenType.Identifier, types[11])// z
        assertEquals(TokenType.RightBrace, types[12])// }
    }

    @Test
    fun `while loop`() {
        val result = tokenize("while (x < 10) { x += 1; }")
        assertTrue(isKeyword(result.tokenTypes[0])) // while
        assertEquals(0, result.diagnostics.size)
    }

    @Test
    fun `string interpolation style`() {
        val result = tokenize("\"hello world\"")
        assertEquals(TokenType.StringLiteral, result.tokens[0].type)
        assertEquals("hello world", result.tokens[0].lexeme)
    }

    // ── Diagnostics ─────────────────────────────────────────────

    @Test
    fun `unexpected character produces diagnostic`() {
        val result = tokenize("@")
        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { "Unexpected character" in it.message })
    }

    @Test
    fun `unknown escape sequence produces warning`() {
        val result = tokenize("\"\\z\"")
        assertTrue(result.warnings.isNotEmpty())
    }

    // ── Unicode and Ethiopic comprehensive ──────────────────────

    @Test
    fun `full Ethiopic identifier`() {
        val result = tokenize("ማስሰር")
        assertTokenAt(result, 0, TokenType.Identifier, "ማስሰር")
    }

    @Test
    fun `ethiopic in multiline source`() {
        val result = tokenize("fn ስርጭት = 10\nlet መጠን = 20")
        assertFalse(result.hasErrors)
        assertTrue(isKeyword(result.tokens[0].type))  // fn
        assertEquals(TokenType.Identifier, result.tokens[1].type) // ስርጭት
        assertEquals(TokenType.Assign, result.tokens[2].type)
        assertEquals(TokenType.IntegerLiteral, result.tokens[3].type) // 10
        assertTrue(isKeyword(result.tokens[4].type)) // let
        assertEquals(TokenType.Identifier, result.tokens[5].type) // መጠን
    }

    @Test
    fun `mixed scripts in source`() {
        val result = tokenize("fn foo() { let ተግባር = \"hello\"; }")
        assertFalse(result.hasErrors)
    }

    // ── Source Range ─────────────────────────────────────────────

    @Test
    fun `token range is correct`() {
        val result = tokenize("hello")
        val token = result.tokens[0]
        assertEquals(0, token.range.start.offset)
        assertEquals(5, token.range.end.offset)
        assertEquals(5, token.range.length)
    }

    // ── Edge Cases ──────────────────────────────────────────────

    @Test
    fun `consecutive operators without spaces`() {
        val result = tokenize("a==b")
        assertEquals(TokenType.Identifier, result.tokens[0].type)
        assertEquals(TokenType.Equal, result.tokens[1].type)
        assertEquals(TokenType.Identifier, result.tokens[2].type)
    }

    @Test
    fun `dot not followed by digit becomes dot operator`() {
        val result = tokenize("a.b")
        assertEquals(TokenType.Identifier, result.tokens[0].type)
        assertEquals(TokenType.Dot, result.tokens[1].type)
        assertEquals(TokenType.Identifier, result.tokens[2].type)
    }

    @Test
    fun `double colon for path`() {
        val result = tokenize("std::io")
        assertEquals(TokenType.Identifier, result.tokens[0].type)
        assertEquals("std", result.tokens[0].lexeme)
        assertEquals(TokenType.DoubleColon, result.tokens[1].type)
        assertEquals(TokenType.Identifier, result.tokens[2].type)
        assertEquals("io", result.tokens[2].lexeme)
    }

    // ── Safe navigation (?.) and null assertion (!!) ──────────────

    @Test
    fun `question dot token`() {
        val result = tokenize("obj?.field")
        assertTokenAt(result, 0, TokenType.Identifier, "obj", expectedColumn = 1)
        assertTokenAt(result, 1, TokenType.QuestionDot, "?.", expectedColumn = 4)
        assertTokenAt(result, 2, TokenType.Identifier, "field", expectedColumn = 6)
    }

    @Test
    fun `bang bang token`() {
        val result = tokenize("x!!")
        assertTokenAt(result, 0, TokenType.Identifier, "x", expectedColumn = 1)
        assertTokenAt(result, 1, TokenType.BangBang, "!!", expectedColumn = 2)
    }

    @Test
    fun `question dot in chain`() {
        val result = tokenize("a?.b?.c")
        assertTokenAt(result, 0, TokenType.Identifier, "a", expectedColumn = 1)
        assertTokenAt(result, 1, TokenType.QuestionDot, "?.", expectedColumn = 2)
        assertTokenAt(result, 2, TokenType.Identifier, "b", expectedColumn = 4)
        assertTokenAt(result, 3, TokenType.QuestionDot, "?.", expectedColumn = 5)
        assertTokenAt(result, 4, TokenType.Identifier, "c", expectedColumn = 7)
    }

    @Test
    fun `bang bang after field access`() {
        val result = tokenize("obj.field!!")
        assertEquals(TokenType.Identifier, result.tokens[0].type)
        assertEquals(TokenType.Dot, result.tokens[1].type)
        assertEquals(TokenType.Identifier, result.tokens[2].type)
        assertEquals(TokenType.BangBang, result.tokens[3].type)
    }

    @Test
    fun `lone question mark produces question token`() {
        val result = tokenize("?")
        assertEquals(TokenType.Question, result.tokens[0].type)
        assertEquals("?", result.tokens[0].lexeme)
    }

    @Test
    fun `question dot not confused with range`() {
        val result = tokenize("a?.b..c")
        assertEquals(TokenType.Identifier, result.tokens[0].type)
        assertEquals(TokenType.QuestionDot, result.tokens[1].type)
        assertEquals(TokenType.Identifier, result.tokens[2].type)
        assertEquals(TokenType.RangeExclusive, result.tokens[3].type)
        assertEquals(TokenType.Identifier, result.tokens[4].type)
    }
}
