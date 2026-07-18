package hasab.compiler.frontend.lexer

public class Lexer(
    private val source: SourceFile,
    private val config: LexerConfig = LexerConfig.DEFAULT,
) {
    private val input: String = source.content
    private var pos: Int = 0
    private var line: Int = 1
    private var column: Int = 1
    private val diagnostics: MutableList<Diagnostic> = mutableListOf()

    public fun tokenize(): LexerResult {
        val tokens = mutableListOf<Token>()
        while (pos < input.length) {
            val token = nextToken()
            if (token.type != TokenType.Eof) {
                tokens.add(token)
            }
        }
        val eofToken = Token(
            type = TokenType.Eof,
            lexeme = "",
            fileName = source.name,
            line = line,
            column = column,
            startOffset = pos,
            endOffset = pos,
        )
        tokens.add(eofToken)
        return LexerResult(tokens, diagnostics.toList())
    }

    private fun nextToken(): Token {
        skipWhitespaceAndComments()
        if (pos >= input.length) {
            return makeToken(TokenType.Eof, "")
        }

        val startLine = line
        val startCol = column
        val startOffset = pos
        val ch = peek()

        // String literal
        if (ch == '"') return scanString(startLine, startCol, startOffset)

        // Character literal
        if (ch == '\'') return scanChar(startLine, startCol, startOffset)

        // Number literal
        if (ch.isDigit()) return scanNumber(startLine, startCol, startOffset)

        // Identifier or keyword (includes Amharic / Ethiopic identifiers)
        if (ch.isIdentifierStart()) return scanIdentifierOrKeyword(startLine, startCol, startOffset)

        // Operators and delimiters
        return scanOperatorOrDelimiter(startLine, startCol, startOffset)
    }

    // ── Whitespace & Comments ───────────────────────────────────

    private fun skipWhitespaceAndComments() {
        while (pos < input.length) {
            val ch = peek()
            when {
                ch.isWhitespace() -> advance()
                // Single-line comment
                input.startsWith(config.singleLineCommentPrefix, pos) -> skipSingleLineComment()
                // Multi-line comment
                input.startsWith(config.multiLineCommentOpen, pos) -> skipMultiLineComment()
                else -> return
            }
        }
    }

    private fun skipSingleLineComment() {
        pos += config.singleLineCommentPrefix.length
        column += config.singleLineCommentPrefix.length
        while (pos < input.length && peek() != '\n') {
            advance()
        }
    }

    private fun skipMultiLineComment() {
        val open = config.multiLineCommentOpen
        val close = config.multiLineCommentClose
        val startLine = line
        val startCol = column

        pos += open.length
        column += open.length
        var depth = 1

        while (pos < input.length && depth > 0) {
            when {
                input.startsWith(close, pos) -> {
                    pos += close.length
                    column += close.length
                    depth--
                }
                input.startsWith(config.multiLineCommentOpen, pos) -> {
                    pos += config.multiLineCommentOpen.length
                    column += config.multiLineCommentOpen.length
                    depth++
                }
                peek() == '\n' -> {
                    line++
                    column = 1
                    pos++
                }
                else -> advance()
            }
        }

        if (depth > 0) {
            addDiagnostic(
                DiagnosticSeverity.ERROR,
                "Unterminated block comment",
                SourceRange(SourcePosition(startLine, startCol, pos), SourcePosition(line, column, pos)),
                hint = "Close with */",
            )
        }
    }

    // ── String Literal ──────────────────────────────────────────

    private fun scanString(startLine: Int, startCol: Int, startOffset: Int): Token {
        advance() // skip opening "
        val sb = StringBuilder()

        while (pos < input.length && peek() != '"') {
            if (peek() == '\n') {
                addDiagnostic(
                    DiagnosticSeverity.ERROR,
                    "Unterminated string literal",
                    SourceRange(SourcePosition(startLine, startCol, startOffset), SourcePosition(line, column, pos)),
                    hint = "Close the string with a double quote: \"",
                )
                return makeToken(TokenType.StringLiteral, sb.toString(), startLine, startCol, startOffset)
            }
            if (peek() == '\\') {
                advance()
                if (pos >= input.length) break
                val escaped = scanEscapeSequence()
                sb.append(escaped)
            } else {
                sb.append(advance())
            }
        }

        if (pos >= input.length) {
            addDiagnostic(
                DiagnosticSeverity.ERROR,
                "Unterminated string literal",
                SourceRange(SourcePosition(startLine, startCol, startOffset), SourcePosition(line, column, pos)),
                hint = "Close the string with a double quote: \"",
            )
            return makeToken(TokenType.StringLiteral, sb.toString(), startLine, startCol, startOffset)
        }

        advance() // skip closing "
        if (sb.length > config.maxStringLiteralLength) {
            addDiagnostic(
                DiagnosticSeverity.ERROR,
                "String literal exceeds maximum length of ${config.maxStringLiteralLength} characters",
                SourceRange(SourcePosition(startLine, startCol, startOffset), SourcePosition(line, column, pos)),
            )
        }

        return makeToken(TokenType.StringLiteral, sb.toString(), startLine, startCol, startOffset)
    }

    // ── Character Literal ───────────────────────────────────────

    private fun scanChar(startLine: Int, startCol: Int, startOffset: Int): Token {
        advance() // skip opening '
        val sb = StringBuilder()

        if (pos >= input.length || peek() == '\n') {
            addDiagnostic(
                DiagnosticSeverity.ERROR,
                "Unterminated character literal",
                SourceRange(SourcePosition(startLine, startCol, startOffset), SourcePosition(line, column, pos)),
                hint = "Close the character literal with a single quote: '",
            )
            return makeToken(TokenType.CharacterLiteral, sb.toString(), startLine, startCol, startOffset)
        }

        if (peek() == '\\') {
            advance()
            if (pos < input.length) {
                sb.append(scanEscapeSequence())
            }
        } else {
            sb.append(advance())
        }

        if (pos < input.length && peek() == '\'') {
            advance() // skip closing '
        } else {
            addDiagnostic(
                DiagnosticSeverity.ERROR,
                "Unterminated character literal",
                SourceRange(SourcePosition(startLine, startCol, startOffset), SourcePosition(line, column, pos)),
                hint = "Close the character literal with a single quote: '",
            )
        }

        return makeToken(TokenType.CharacterLiteral, sb.toString(), startLine, startCol, startOffset)
    }

    private fun scanEscapeSequence(): Char {
        if (pos >= input.length) return '\\'
        val ch = advance()
        return when (ch) {
            'n' -> '\n'
            't' -> '\t'
            'r' -> '\r'
            '\\' -> '\\'
            '\'' -> '\''
            '"' -> '"'
            '0' -> '\u0000'
            else -> {
                addDiagnostic(
                    DiagnosticSeverity.WARNING,
                    "Unknown escape sequence: \\$ch",
                    SourceRange(SourcePosition(line, column - 2, pos - 2), SourcePosition(line, column, pos)),
                )
                ch
            }
        }
    }

    // ── Number Literals ─────────────────────────────────────────

    private fun scanNumber(startLine: Int, startCol: Int, startOffset: Int): Token {
        var isFloat = false
        scanDigits()
        if (pos < input.length && peek() == '.' &&
            pos + 1 < input.length && input[pos + 1].isDigit()
        ) {
            isFloat = true
            advance() // skip .
            scanDigits()
        }
        // Scientific notation: e.g., 1.5e10, 1e5
        if (pos < input.length && (peek() == 'e' || peek() == 'E')) {
            isFloat = true
            advance()
            if (pos < input.length && (peek() == '+' || peek() == '-')) {
                advance()
            }
            scanDigits()
        }
        val lexeme = input.substring(startOffset, pos)
        val type = if (isFloat) TokenType.FloatLiteral else TokenType.IntegerLiteral
        return makeToken(type, lexeme, startLine, startCol, startOffset)
    }

    private fun scanDigits() {
        while (pos < input.length && peek().isDigit()) {
            advance()
        }
    }

    // ── Identifiers & Keywords ──────────────────────────────────

    private fun scanIdentifierOrKeyword(startLine: Int, startCol: Int, startOffset: Int): Token {
        while (pos < input.length && peek().isIdentifierPart()) {
            advance()
        }
        val lexeme = input.substring(startOffset, pos)
        if (lexeme == "_") {
            return makeToken(TokenType.Underscore, lexeme, startLine, startCol, startOffset)
        }
        val keyword = Keyword.lookup(lexeme)
        return if (keyword != null) {
            makeToken(TokenType.Keyword(keyword), lexeme, startLine, startCol, startOffset)
        } else {
            makeToken(TokenType.Identifier, lexeme, startLine, startCol, startOffset)
        }
    }

    // ── Operators & Delimiters ──────────────────────────────────

    private fun scanOperatorOrDelimiter(startLine: Int, startCol: Int, startOffset: Int): Token {
        val ch = advance()

        when (ch) {
            '+' -> if (match('=')) return makeToken(TokenType.PlusAssign, "+=", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Plus, "+", startLine, startCol, startOffset)
            '-' -> if (match('>')) return makeToken(TokenType.Arrow, "->", startLine, startCol, startOffset)
                   else if (match('=')) return makeToken(TokenType.MinusAssign, "-=", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Minus, "-", startLine, startCol, startOffset)
            '*' -> if (match('=')) return makeToken(TokenType.StarAssign, "*=", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Star, "*", startLine, startCol, startOffset)
            '/' -> if (match('=')) return makeToken(TokenType.SlashAssign, "/=", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Slash, "/", startLine, startCol, startOffset)
            '%' -> if (match('=')) return makeToken(TokenType.PercentAssign, "%=", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Percent, "%", startLine, startCol, startOffset)
            '=' -> if (match('=')) return makeToken(TokenType.Equal, "==", startLine, startCol, startOffset)
                   else if (match('>')) return makeToken(TokenType.FatArrow, "=>", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Assign, "=", startLine, startCol, startOffset)
            '!' -> if (match('=')) return makeToken(TokenType.NotEqual, "!=", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Not, "!", startLine, startCol, startOffset)
            '<' -> if (match('=')) return makeToken(TokenType.LessEqual, "<=", startLine, startCol, startOffset)
                   else if (match('<')) return makeToken(TokenType.ShiftLeft, "<<", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Less, "<", startLine, startCol, startOffset)
            '>' -> if (match('=')) return makeToken(TokenType.GreaterEqual, ">=", startLine, startCol, startOffset)
                   else if (match('>')) return makeToken(TokenType.ShiftRight, ">>", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Greater, ">", startLine, startCol, startOffset)
            '&' -> if (match('&')) return makeToken(TokenType.And, "&&", startLine, startCol, startOffset)
                   else return makeToken(TokenType.BitwiseAnd, "&", startLine, startCol, startOffset)
            '|' -> if (match('|')) return makeToken(TokenType.Or, "||", startLine, startCol, startOffset)
                   else return makeToken(TokenType.BitwiseOr, "|", startLine, startCol, startOffset)
            '^' -> return makeToken(TokenType.BitwiseXor, "^", startLine, startCol, startOffset)
            '~' -> return makeToken(TokenType.BitwiseNot, "~", startLine, startCol, startOffset)
            '.' -> if (match('.')) {
                       if (match('=')) return makeToken(TokenType.RangeInclusive, "..=", startLine, startCol, startOffset)
                       else return makeToken(TokenType.RangeExclusive, "..", startLine, startCol, startOffset)
                   } else return makeToken(TokenType.Dot, ".", startLine, startCol, startOffset)
            ':' -> if (match(':')) return makeToken(TokenType.DoubleColon, "::", startLine, startCol, startOffset)
                   else return makeToken(TokenType.Colon, ":", startLine, startCol, startOffset)
            '(' -> return makeToken(TokenType.LeftParen, "(", startLine, startCol, startOffset)
            ')' -> return makeToken(TokenType.RightParen, ")", startLine, startCol, startOffset)
            '{' -> return makeToken(TokenType.LeftBrace, "{", startLine, startCol, startOffset)
            '}' -> return makeToken(TokenType.RightBrace, "}", startLine, startCol, startOffset)
            '[' -> return makeToken(TokenType.LeftBracket, "[", startLine, startCol, startOffset)
            ']' -> return makeToken(TokenType.RightBracket, "]", startLine, startCol, startOffset)
            ';' -> return makeToken(TokenType.Semicolon, ";", startLine, startCol, startOffset)
            ',' -> return makeToken(TokenType.Comma, ",", startLine, startCol, startOffset)
            '_' -> return makeToken(TokenType.Underscore, "_", startLine, startCol, startOffset)
            else -> {
                addDiagnostic(
                    DiagnosticSeverity.ERROR,
                    "Unexpected character: '$ch'",
                    SourceRange(SourcePosition(startLine, startCol, startOffset), SourcePosition(line, column, pos)),
                    hint = "Remove this character or replace it with a valid token",
                )
                return makeToken(TokenType.Identifier, ch.toString(), startLine, startCol, startOffset)
            }
        }
    }

    // ── Char classification helpers ─────────────────────────────

    private fun Char.isWhitespace(): Boolean = this == ' ' || this == '\t' || this == '\r' || this == '\n'

    private fun Char.isDigit(): Boolean = this in '0'..'9'

    private fun Char.isIdentifierStart(): Boolean {
        if (this in 'a'..'z' || this in 'A'..'Z' || this == '_') return true
        if (config.allowUnicodeInIdentifiers) {
            return isEthiopicChar() || java.lang.Character.isLetter(this.code)
        }
        return false
    }

    private fun Char.isIdentifierPart(): Boolean {
        if (isIdentifierStart()) return true
        if (this in '0'..'9') return true
        if (config.allowUnicodeInIdentifiers && isEthiopicChar()) return true
        return false
    }

    private fun Char.isEthiopicChar(): Boolean {
        val code = this.code
        return code in 0x1200..0x137F ||  // Ethiopic
               code in 0x1380..0x139F ||  // Ethiopic Supplement
               code in 0x2D80..0x2DDF ||  // Ethiopic Extended
               code in 0xAB00..0xAB2F     // Ethiopic Extended-A
    }

    // ── Cursor helpers ──────────────────────────────────────────

    private fun peek(): Char = input[pos]

    private fun advance(): Char {
        val ch = input[pos]
        pos++
        if (ch == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return ch
    }

    private fun match(expected: Char): Boolean {
        if (pos < input.length && input[pos] == expected) {
            advance()
            return true
        }
        return false
    }

    // ── Token factory ───────────────────────────────────────────

    private fun makeToken(
        type: TokenType,
        lexeme: String,
        startLine: Int = line,
        startCol: Int = column,
        startOffset: Int = pos,
    ): Token = Token(
        type = type,
        lexeme = lexeme,
        fileName = source.name,
        line = startLine,
        column = startCol,
        startOffset = startOffset,
        endOffset = pos,
    )

    // ── Diagnostics ─────────────────────────────────────────────

    private fun addDiagnostic(
        severity: DiagnosticSeverity,
        message: String,
        range: SourceRange,
        hint: String? = null,
    ) {
        diagnostics.add(
            Diagnostic(
                severity = severity,
                message = message,
                range = range,
                fileName = source.name,
                hint = hint,
            )
        )
    }
}
