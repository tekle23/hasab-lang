package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.Token
import hasab.compiler.frontend.lexer.TokenType
import hasab.compiler.frontend.ast.*

public class StatementParser(
    private val stream: TokenStream,
    private val diagnostics: MutableList<ParserDiagnostic>,
) {
    private val exprParser = ExpressionParser(stream, diagnostics)
    private val typeParser = TypeParser(stream, diagnostics)

    public fun parseStatement(): Stmt {
        val token = stream.peek()

        return when {
            stream.isAtKeyword("return") -> parseReturnStmt()
            stream.isAtKeyword("break") -> parseBreakStmt()
            stream.isAtKeyword("continue") -> parseContinueStmt()
            stream.isAtKeyword("if") -> parseIfStmt()
            stream.isAtKeyword("while") -> parseWhileStmt()
            stream.isAtKeyword("for") -> parseForStmt()
            stream.isAtKeyword("let") || stream.isAtKeyword("mut") -> parseLetStmt()
            stream.isAtKeyword("fn") -> {
                val decl = DeclarationParser(stream, diagnostics).parseFnDecl(isPublic = false)
                ExprStmt(
                    expression = IdentifierExpr(decl.name, decl.fileName, decl.line, decl.column, decl.startOffset, decl.endOffset),
                    fileName = decl.fileName, line = decl.line, column = decl.column,
                    startOffset = decl.startOffset, endOffset = decl.endOffset,
                )
            }
            stream.isAtKeyword("struct") || stream.isAtKeyword("enum") || stream.isAtKeyword("impl") ||
            stream.isAtKeyword("trait") || stream.isAtKeyword("type") || stream.isAtKeyword("mod") ||
            stream.isAtKeyword("use") -> {
                val decl = DeclarationParser(stream, diagnostics).parseDeclaration(isPublic = false)
                ExprStmt(
                    expression = IdentifierExpr("(decl)", decl.fileName, decl.line, decl.column, decl.startOffset, decl.endOffset),
                    fileName = decl.fileName, line = decl.line, column = decl.column,
                    startOffset = decl.startOffset, endOffset = decl.endOffset,
                )
            }
            stream.isAt(TokenType.LeftBrace) -> parseBlock()
            else -> parseExpressionStatement()
        }
    }

    public fun parseBlock(): Block {
        val token = stream.expect(TokenType.LeftBrace)
        val stmts = mutableListOf<Stmt>()

        while (!stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
            try {
                stmts.add(parseStatement())
            } catch (e: ParseException) {
                diagnostics.add(
                    ParserDiagnostic(
                        severity = DiagnosticSeverity.ERROR,
                        message = e.message ?: "Parse error",
                        fileName = e.token.fileName,
                        line = e.token.line,
                        column = e.token.column,
                        startOffset = e.token.startOffset,
                        endOffset = e.token.endOffset,
                    )
                )
                recoverToStatementEnd()
            }
        }

        val endToken = stream.expect(TokenType.RightBrace)
        return Block(
            statements = stmts,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = endToken.endOffset,
        )
    }

    private fun parseReturnStmt(): ReturnStmt {
        val token = stream.expectKeyword("return")
        var value: Expr? = null
        if (!stream.isAt(TokenType.Semicolon) && !stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
            value = exprParser.parseExpression()
        }
        expectSemicolon()
        return ReturnStmt(
            value = value,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseBreakStmt(): BreakStmt {
        val token = stream.expectKeyword("break")
        expectSemicolon()
        return BreakStmt(
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseContinueStmt(): ContinueStmt {
        val token = stream.expectKeyword("continue")
        expectSemicolon()
        return ContinueStmt(
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseIfStmt(): IfStmt {
        val token = stream.expectKeyword("if")
        val condition = exprParser.parseExpression()
        val thenBranch = parseBlock()

        var elseBranch: Stmt? = null
        if (stream.isAtKeyword("else")) {
            stream.advance()
            if (stream.isAtKeyword("if")) {
                elseBranch = parseIfStmt()
            } else {
                elseBranch = parseBlock()
            }
        }

        return IfStmt(
            condition = condition,
            thenBranch = thenBranch,
            elseBranch = elseBranch,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = (elseBranch ?: thenBranch).endOffset,
        )
    }

    private fun parseWhileStmt(): WhileStmt {
        val token = stream.expectKeyword("while")
        val condition = exprParser.parseExpression()
        val body = parseBlock()

        return WhileStmt(
            condition = condition,
            body = body,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = body.endOffset,
        )
    }

    private fun parseForStmt(): ForStmt {
        val token = stream.expectKeyword("for")
        stream.expect(TokenType.LeftParen)
        val variable = stream.expect(TokenType.Identifier).lexeme
        stream.expect(TokenType.Colon)
        val iterable = exprParser.parseExpression()
        stream.expect(TokenType.RightParen)
        val body = parseBlock()

        return ForStmt(
            variable = variable,
            iterable = iterable,
            body = body,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = body.endOffset,
        )
    }

    private fun parseLetStmt(): LetStmt {
        val mutToken = stream.matchKeyword("mut")
        var isMutable = mutToken != null

        val letToken: Token
        if (isMutable && !stream.isAtKeyword("let")) {
            letToken = mutToken!!
        } else {
            letToken = stream.expectKeyword("let")
            if (!isMutable) {
                val mutAfterLet = stream.matchKeyword("mut")
                if (mutAfterLet != null) isMutable = true
            }
        }

        val nameToken = stream.expect(TokenType.Identifier)

        var typeAnnotation: TypeNode? = null
        if (stream.isAt(TokenType.Colon)) {
            stream.advance()
            typeAnnotation = typeParser.parseType()
        }

        stream.expect(TokenType.Assign)
        val initializer = exprParser.parseExpression()
        expectSemicolon()

        return LetStmt(
            name = nameToken.lexeme,
            typeAnnotation = typeAnnotation,
            initializer = initializer,
            isMutable = isMutable,
            fileName = letToken.fileName, line = letToken.line, column = letToken.column,
            startOffset = letToken.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseExpressionStatement(): ExprStmt {
        val expr = exprParser.parseExpression()
        expectSemicolon()
        return ExprStmt(
            expression = expr,
            fileName = expr.fileName, line = expr.line, column = expr.column,
            startOffset = expr.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun expectSemicolon() {
        if (!stream.isAt(TokenType.Semicolon) && !stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
            reportError(
                "Expected ';' after statement",
                stream.peek(),
                hint = "Add a semicolon ';' at the end of the statement",
            )
        }
        if (stream.isAt(TokenType.Semicolon)) {
            stream.advance()
        }
    }

    private fun recoverToStatementEnd() {
        while (!stream.isAt(TokenType.Semicolon) &&
            !stream.isAt(TokenType.RightBrace) &&
            !stream.isAt(TokenType.RightParen) &&
            !stream.isAtEnd()
        ) {
            stream.advance()
        }
        if (stream.isAt(TokenType.Semicolon)) {
            stream.advance()
        }
    }

    private fun reportError(message: String, token: Token, hint: String? = null) {
        diagnostics.add(
            ParserDiagnostic(
                severity = DiagnosticSeverity.ERROR,
                message = message,
                fileName = token.fileName,
                line = token.line,
                column = token.column,
                startOffset = token.startOffset,
                endOffset = token.endOffset,
                hint = hint,
            )
        )
    }
}
