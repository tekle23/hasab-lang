package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.Token
import hasab.compiler.frontend.lexer.TokenType
import hasab.compiler.frontend.ast.*

public class ExpressionParser(
    private val stream: TokenStream,
    private val diagnostics: MutableList<ParserDiagnostic>,
) {

    public fun parseExpression(): Expr = parseAssignment()

    public fun parseExpressionStatement(): Expr {
        return parseAssignment()
    }

    // ── Pratt-style precedence climbing ─────────────────────────

    private fun parseAssignment(): Expr {
        val expr = parseOr()

        if (stream.isAt(TokenType.Assign)) {
            val token = stream.advance()
            val value = parseAssignment()
            return AssignmentExpr(
                target = expr,
                value = value,
                fileName = token.fileName,
                line = token.line,
                column = token.column,
                startOffset = expr.startOffset,
                endOffset = value.endOffset,
            )
        }

        val compoundOps = mapOf(
            TokenType.PlusAssign to "+=",
            TokenType.MinusAssign to "-=",
            TokenType.StarAssign to "*=",
            TokenType.SlashAssign to "/=",
            TokenType.PercentAssign to "%=",
        )
        val opToken = stream.peek()
        val opStr = compoundOps[opToken.type]
        if (opStr != null) {
            stream.advance()
            val value = parseAssignment()
            return CompoundAssignmentExpr(
                target = expr,
                operator = opStr,
                value = value,
                fileName = opToken.fileName,
                line = opToken.line,
                column = opToken.column,
                startOffset = expr.startOffset,
                endOffset = value.endOffset,
            )
        }

        return expr
    }

    private fun parseOr(): Expr {
        var left = parseAnd()

        while (stream.isAt(TokenType.Or)) {
            val token = stream.advance()
            val right = parseAnd()
            left = BinaryExpr(
                left = left, operator = "||", right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseAnd(): Expr {
        var left = parseBitwiseOr()

        while (stream.isAt(TokenType.And)) {
            val token = stream.advance()
            val right = parseBitwiseOr()
            left = BinaryExpr(
                left = left, operator = "&&", right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseBitwiseOr(): Expr {
        var left = parseBitwiseXor()

        while (stream.isAt(TokenType.BitwiseOr)) {
            val token = stream.advance()
            val right = parseBitwiseXor()
            left = BinaryExpr(
                left = left, operator = "|", right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseBitwiseXor(): Expr {
        var left = parseBitwiseAnd()

        while (stream.isAt(TokenType.BitwiseXor)) {
            val token = stream.advance()
            val right = parseBitwiseAnd()
            left = BinaryExpr(
                left = left, operator = "^", right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseBitwiseAnd(): Expr {
        var left = parseEquality()

        while (stream.isAt(TokenType.BitwiseAnd)) {
            val token = stream.advance()
            val right = parseEquality()
            left = BinaryExpr(
                left = left, operator = "&", right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseEquality(): Expr {
        var left = parseComparison()

        while (stream.isAtAny(TokenType.Equal, TokenType.NotEqual)) {
            val token = stream.advance()
            val op = if (token.type == TokenType.Equal) "==" else "!="
            val right = parseComparison()
            left = BinaryExpr(
                left = left, operator = op, right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseComparison(): Expr {
        var left = parseRange()

        while (stream.isAtAny(TokenType.Less, TokenType.Greater, TokenType.LessEqual, TokenType.GreaterEqual)) {
            val token = stream.advance()
            val op = when (token.type) {
                TokenType.Less -> "<"
                TokenType.Greater -> ">"
                TokenType.LessEqual -> "<="
                TokenType.GreaterEqual -> ">="
                else -> "?"
            }
            val right = parseRange()
            left = BinaryExpr(
                left = left, operator = op, right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseRange(): Expr {
        var left = parseShift()

        while (stream.isAtAny(TokenType.RangeExclusive, TokenType.RangeInclusive)) {
            val token = stream.advance()
            val op = if (token.type == TokenType.RangeExclusive) ".." else "..="
            val right = parseShift()
            left = BinaryExpr(
                left = left, operator = op, right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseShift(): Expr {
        var left = parseAddSub()

        while (stream.isAtAny(TokenType.ShiftLeft, TokenType.ShiftRight)) {
            val token = stream.advance()
            val op = if (token.type == TokenType.ShiftLeft) "<<" else ">>"
            val right = parseAddSub()
            left = BinaryExpr(
                left = left, operator = op, right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseAddSub(): Expr {
        var left = parseMulDiv()

        while (stream.isAtAny(TokenType.Plus, TokenType.Minus)) {
            val token = stream.advance()
            val op = if (token.type == TokenType.Plus) "+" else "-"
            val right = parseMulDiv()
            left = BinaryExpr(
                left = left, operator = op, right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseMulDiv(): Expr {
        var left = parseUnary()

        while (stream.isAtAny(TokenType.Star, TokenType.Slash, TokenType.Percent)) {
            val token = stream.advance()
            val op = when (token.type) {
                TokenType.Star -> "*"
                TokenType.Slash -> "/"
                TokenType.Percent -> "%"
                else -> "?"
            }
            val right = parseUnary()
            left = BinaryExpr(
                left = left, operator = op, right = right,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = left.startOffset, endOffset = right.endOffset,
            )
        }
        return left
    }

    private fun parseUnary(): Expr {
        if (stream.isAtAny(TokenType.Minus, TokenType.Not, TokenType.BitwiseNot)) {
            val token = stream.advance()
            val op = when (token.type) {
                TokenType.Minus -> "-"
                TokenType.Not -> "!"
                TokenType.BitwiseNot -> "~"
                else -> "?"
            }
            val operand = parseUnary()
            return UnaryExpr(
                operator = op, operand = operand,
                fileName = token.fileName, line = token.line, column = token.column,
                startOffset = token.startOffset, endOffset = operand.endOffset,
            )
        }
        return parsePostfix()
    }

    private fun parsePostfix(): Expr {
        var expr = parsePrimary()

        while (true) {
            when {
                stream.isAt(TokenType.LeftParen) -> {
                    val args = parseArguments()
                    expr = CallExpr(
                        callee = expr, arguments = args,
                        fileName = expr.fileName, line = expr.line, column = expr.column,
                        startOffset = expr.startOffset, endOffset = stream.previousToken().endOffset,
                    )
                }
                stream.isAt(TokenType.LeftBracket) -> {
                    val token = stream.advance()
                    val index = parseExpression()
                    stream.expect(TokenType.RightBracket)
                    expr = IndexExpr(
                        callee = expr, index = index,
                        fileName = expr.fileName, line = expr.line, column = expr.column,
                        startOffset = expr.startOffset, endOffset = stream.previousToken().endOffset,
                    )
                }
                stream.isAt(TokenType.Dot) -> {
                    stream.advance()
                    val field = stream.expect(TokenType.Identifier)
                    expr = FieldAccessExpr(
                        callee = expr, fieldName = field.lexeme,
                        fileName = expr.fileName, line = expr.line, column = expr.column,
                        startOffset = expr.startOffset, endOffset = field.endOffset,
                    )
                }
                stream.isAt(TokenType.QuestionDot) -> {
                    stream.advance()
                    val field = stream.expect(TokenType.Identifier)
                    expr = SafeFieldAccessExpr(
                        callee = expr, fieldName = field.lexeme,
                        fileName = expr.fileName, line = expr.line, column = expr.column,
                        startOffset = expr.startOffset, endOffset = field.endOffset,
                    )
                }
                stream.isAt(TokenType.BangBang) -> {
                    val token = stream.advance()
                    expr = NullAssertExpr(
                        operand = expr,
                        fileName = expr.fileName, line = expr.line, column = expr.column,
                        startOffset = expr.startOffset, endOffset = token.endOffset,
                    )
                }
                else -> break
            }
        }

        return expr
    }

    private fun parseArguments(): List<Expr> {
        stream.expect(TokenType.LeftParen)
        val args = mutableListOf<Expr>()
        if (!stream.isAt(TokenType.RightParen)) {
            args.add(parseExpression())
            while (stream.isAt(TokenType.Comma)) {
                stream.advance()
                if (stream.isAt(TokenType.RightParen)) break
                args.add(parseExpression())
            }
        }
        stream.expect(TokenType.RightParen)
        return args
    }

    private fun parsePrimary(): Expr {
        val token = stream.peek()

        return when (token.type) {
            TokenType.IntegerLiteral -> {
                stream.advance()
                IntegerLiteralExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            TokenType.FloatLiteral -> {
                stream.advance()
                FloatLiteralExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            TokenType.StringLiteral -> {
                stream.advance()
                StringLiteralExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            TokenType.CharacterLiteral -> {
                stream.advance()
                CharLiteralExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            TokenType.Identifier -> {
                stream.advance()
                IdentifierExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            TokenType.LeftParen -> parseParenExpr()
            TokenType.LeftBracket -> parseArrayLiteral()
            TokenType.Dot -> parseArrayInit()
            is TokenType.Keyword -> parseKeywordExpr(token)
            TokenType.LeftBrace -> parseBlockAsExpr()
            else -> {
                reportError("Unexpected token in expression: ${token.type}", token)
                stream.advance()
                IntegerLiteralExpr("0", token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
        }
    }

    private fun parseKeywordExpr(token: Token): Expr {
        val keyword = (token.type as TokenType.Keyword).keyword
        return when (keyword.tokenString) {
            "true" -> {
                stream.advance()
                BoolLiteralExpr(true, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            "false" -> {
                stream.advance()
                BoolLiteralExpr(false, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            "nil" -> {
                stream.advance()
                NilLiteralExpr(token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            "if" -> parseIfExpr()
            "fn" -> {
                reportError("Lambda expressions are not yet supported in expression position", token)
                stream.advance()
                IdentifierExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            "this" -> {
                stream.advance()
                IdentifierExpr("this", token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            "new" -> {
                reportError("'new' keyword is not supported yet", token)
                stream.advance()
                IdentifierExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
            else -> {
                reportError("Unexpected keyword in expression: ${keyword.tokenString}", token)
                stream.advance()
                IdentifierExpr(token.lexeme, token.fileName, token.line, token.column, token.startOffset, token.endOffset)
            }
        }
    }

    private fun parseIfExpr(): Expr {
        val token = stream.expectKeyword("if")
        val condition = parseExpression()
        val thenBranch = parseBlockAsExpr()

        var elseBranch: Expr? = null
        if (stream.isAtKeyword("else")) {
            stream.advance()
            if (stream.isAtKeyword("if")) {
                elseBranch = parseIfExpr()
            } else {
                elseBranch = parseBlockAsExpr()
            }
        }

        return IfExpr(
            condition = condition,
            thenBranch = thenBranch,
            elseBranch = elseBranch,
            fileName = token.fileName,
            line = token.line,
            column = token.column,
            startOffset = token.startOffset,
            endOffset = (elseBranch ?: thenBranch).endOffset,
        )
    }

    private fun parseParenExpr(): Expr {
        val token = stream.expect(TokenType.LeftParen)
        val inner = parseExpression()
        stream.expect(TokenType.RightParen)
        return ParenExpr(
            inner = inner,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseArrayLiteral(): Expr {
        val token = stream.expect(TokenType.LeftBracket)
        val elements = mutableListOf<Expr>()
        if (!stream.isAt(TokenType.RightBracket)) {
            elements.add(parseExpression())
            while (stream.isAt(TokenType.Comma)) {
                stream.advance()
                if (stream.isAt(TokenType.RightBracket)) break
                elements.add(parseExpression())
            }
        }
        stream.expect(TokenType.RightBracket)
        return ArrayLiteralExpr(
            elements = elements,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseArrayInit(): Expr {
        val token = stream.expect(TokenType.Dot)
        stream.expect(TokenType.LeftBracket)
        val size = parseExpression()
        stream.expect(TokenType.RightBracket)
        return ArrayInitExpr(
            elementType = null, size = size,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseBlockAsExpr(): Expr {
        val token = stream.expect(TokenType.LeftBrace)
        val stmts = mutableListOf<Stmt>()
        val stmtParser = StatementParser(stream, diagnostics)
        while (!stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
            stmts.add(stmtParser.parseStatement())
        }
        stream.expect(TokenType.RightBrace)
        val lastExpr = stmts.lastOrNull() as? ExprStmt
        return lastExpr?.expression ?: NilLiteralExpr(
            token.fileName, token.line, token.column, token.startOffset, token.endOffset,
        )
    }

    private fun reportError(message: String, token: Token) {
        diagnostics.add(
            ParserDiagnostic(
                severity = DiagnosticSeverity.ERROR,
                message = message,
                fileName = token.fileName,
                line = token.line,
                column = token.column,
                startOffset = token.startOffset,
                endOffset = token.endOffset,
            )
        )
    }
}
