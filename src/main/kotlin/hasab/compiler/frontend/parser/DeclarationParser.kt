package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.Token
import hasab.compiler.frontend.lexer.TokenType
import hasab.compiler.frontend.ast.*

public class DeclarationParser(
    private val stream: TokenStream,
    private val diagnostics: MutableList<ParserDiagnostic>,
) {
    private val stmtParser = StatementParser(stream, diagnostics)
    private val typeParser = TypeParser(stream, diagnostics)

    public fun parseDeclaration(isPublic: Boolean = false): Decl {
        if (isPublic && stream.isAt(TokenType.Colon) && stream.peekAt(1).type == TokenType.Colon) {
            stream.advance()
            stream.advance()
        }

        val token = stream.peek()

        if (token.type is TokenType.Keyword) {
            val keyword = (token.type as TokenType.Keyword).keyword
            return when (keyword.tokenString) {
                "pub" -> parsePubDecl()
                "fn" -> parseFnDecl(isPublic)
                "struct" -> parseStructDecl(isPublic)
                "enum" -> parseEnumDecl(isPublic)
                "impl" -> parseImplDecl()
                "trait" -> parseTraitDecl(isPublic)
                "type" -> parseTypeAlias(isPublic)
                "mod" -> parseModDecl(isPublic)
                "use" -> parseUseDecl(isPublic)
                else -> {
                    reportError("Unexpected keyword at declaration level: ${keyword.tokenString}", token)
                    val t = stream.advance()
                    FnDecl(
                        name = t.lexeme, parameters = emptyList(), returnType = null, body = null,
                        isPublic = isPublic,
                        fileName = t.fileName, line = t.line, column = t.column,
                        startOffset = t.startOffset, endOffset = t.endOffset,
                    )
                }
            }
        }

        reportError("Expected a declaration, got ${token.type}", token)
        val t = stream.advance()
        return FnDecl(
            name = t.lexeme, parameters = emptyList(), returnType = null, body = null,
            isPublic = isPublic,
            fileName = t.fileName, line = t.line, column = t.column,
            startOffset = t.startOffset, endOffset = t.endOffset,
        )
    }

    public fun parseFnDecl(isPublic: Boolean): FnDecl {
        val token = stream.expectKeyword("fn")
        val nameToken = if (stream.peek().type is TokenType.Keyword) stream.advance() else stream.expect(TokenType.Identifier)
        val originalName = nameToken.lexeme
        val name = normalizeEntryPointName(originalName)
        val params = parseParameterList()

        var returnType: TypeNode? = null
        if (stream.isAt(TokenType.Arrow)) {
            stream.advance()
            returnType = typeParser.parseType()
        }

        val body = if (stream.isAt(TokenType.LeftBrace)) {
            stmtParser.parseBlock()
        } else {
            if (stream.isAt(TokenType.Semicolon)) stream.advance()
            null
        }

        return FnDecl(
            name = name,
            originalName = originalName,
            parameters = params,
            returnType = returnType,
            body = body,
            isPublic = isPublic,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = when {
                body != null -> body.endOffset
                returnType != null -> returnType.endOffset
                else -> nameToken.endOffset
            },
        )
    }

    private fun parseParameterList(): List<FunctionParam> {
        stream.expect(TokenType.LeftParen)
        val params = mutableListOf<FunctionParam>()

        if (!stream.isAt(TokenType.RightParen)) {
            params.add(parseParameter())
            while (stream.isAt(TokenType.Comma)) {
                stream.advance()
                if (stream.isAt(TokenType.RightParen)) break
                params.add(parseParameter())
            }
        }

        stream.expect(TokenType.RightParen)
        return params
    }

    private fun parseParameter(): FunctionParam {
        val mutToken = stream.matchKeyword("mut")
        val isMutable = mutToken != null
        val nameToken = stream.expect(TokenType.Identifier)

        if (nameToken.lexeme == "self") {
            return FunctionParam(
                name = "self",
                type = null,
                isMutable = isMutable,
                fileName = nameToken.fileName,
                line = nameToken.line,
                column = nameToken.column,
                startOffset = nameToken.startOffset,
                endOffset = nameToken.endOffset,
            )
        }

        stream.expect(TokenType.Colon)
        val type = typeParser.parseType()

        return FunctionParam(
            name = nameToken.lexeme,
            type = type,
            isMutable = isMutable,
            fileName = (mutToken ?: nameToken).fileName,
            line = (mutToken ?: nameToken).line,
            column = (mutToken ?: nameToken).column,
            startOffset = (mutToken ?: nameToken).startOffset,
            endOffset = type.endOffset,
        )
    }

    private fun parseStructDecl(isPublic: Boolean): StructDecl {
        val token = stream.expectKeyword("struct")
        val nameToken = stream.expect(TokenType.Identifier)

        val fields = if (stream.isAt(TokenType.LeftBrace)) {
            stream.advance()
            val list = mutableListOf<StructField>()
            while (!stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
                list.add(parseStructField())
                if (stream.isAt(TokenType.Comma)) stream.advance()
            }
            stream.expect(TokenType.RightBrace)
            list
        } else {
            reportError("Expected '{' after struct name", stream.peek())
            emptyList()
        }

        return StructDecl(
            name = nameToken.lexeme,
            fields = fields,
            isPublic = isPublic,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseStructField(): StructField {
        val mutToken = stream.matchKeyword("mut")
        val isMutable = mutToken != null
        val nameToken = stream.expect(TokenType.Identifier)
        stream.expect(TokenType.Colon)
        val type = typeParser.parseType()

        return StructField(
            name = nameToken.lexeme,
            type = type,
            isMutable = isMutable,
            fileName = (mutToken ?: nameToken).fileName,
            line = (mutToken ?: nameToken).line,
            column = (mutToken ?: nameToken).column,
            startOffset = (mutToken ?: nameToken).startOffset,
            endOffset = type.endOffset,
        )
    }

    private fun parseEnumDecl(isPublic: Boolean): EnumDecl {
        val token = stream.expectKeyword("enum")
        val nameToken = stream.expect(TokenType.Identifier)

        val variants = if (stream.isAt(TokenType.LeftBrace)) {
            stream.advance()
            val list = mutableListOf<EnumVariant>()
            while (!stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
                list.add(parseEnumVariant())
                if (stream.isAt(TokenType.Comma)) stream.advance()
            }
            stream.expect(TokenType.RightBrace)
            list
        } else {
            reportError("Expected '{' after enum name", stream.peek())
            emptyList()
        }

        return EnumDecl(
            name = nameToken.lexeme,
            variants = variants,
            isPublic = isPublic,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseEnumVariant(): EnumVariant {
        val nameToken = stream.expect(TokenType.Identifier)

        val fields = if (stream.isAt(TokenType.LeftParen)) {
            stream.advance()
            val list = mutableListOf<StructField>()
            while (!stream.isAt(TokenType.RightParen) && !stream.isAtEnd()) {
                val fieldToken = stream.peek()
                if (stream.peekAt(1).type == TokenType.Colon) {
                    list.add(parseStructField())
                } else {
                    val type = typeParser.parseType()
                    list.add(
                        StructField(
                            name = "_${list.size}",
                            type = type,
                            isMutable = false,
                            fileName = fieldToken.fileName,
                            line = fieldToken.line,
                            column = fieldToken.column,
                            startOffset = fieldToken.startOffset,
                            endOffset = type.endOffset,
                        )
                    )
                }
                if (stream.isAt(TokenType.Comma)) stream.advance()
            }
            stream.expect(TokenType.RightParen)
            list
        } else {
            emptyList()
        }

        return EnumVariant(
            name = nameToken.lexeme,
            fields = fields,
            fileName = nameToken.fileName,
            line = nameToken.line, column = nameToken.column,
            startOffset = nameToken.startOffset,
            endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseImplDecl(): ImplDecl {
        val token = stream.expectKeyword("impl")
        val targetType = typeParser.parseType()

        val methods = if (stream.isAt(TokenType.LeftBrace)) {
            stream.advance()
            val list = mutableListOf<FnDecl>()
            while (!stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
                if (stream.isAtKeyword("pub")) {
                    stream.advance()
                }
                list.add(parseFnDecl(isPublic = false))
            }
            stream.expect(TokenType.RightBrace)
            list
        } else {
            reportError("Expected '{' after impl type", stream.peek())
            emptyList()
        }

        return ImplDecl(
            targetType = targetType,
            methods = methods,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseTraitDecl(isPublic: Boolean): TraitDecl {
        val token = stream.expectKeyword("trait")
        val nameToken = stream.expect(TokenType.Identifier)

        val methods = if (stream.isAt(TokenType.LeftBrace)) {
            stream.advance()
            val list = mutableListOf<FnDecl>()
            while (!stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
                list.add(parseFnDecl(isPublic = false))
            }
            stream.expect(TokenType.RightBrace)
            list
        } else {
            reportError("Expected '{' after trait name", stream.peek())
            emptyList()
        }

        return TraitDecl(
            name = nameToken.lexeme,
            methods = methods,
            isPublic = isPublic,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseTypeAlias(isPublic: Boolean): TypeAliasDecl {
        val token = stream.expectKeyword("type")
        val nameToken = stream.expect(TokenType.Identifier)
        stream.expect(TokenType.Assign)
        val target = typeParser.parseType()
        if (stream.isAt(TokenType.Semicolon)) stream.advance()

        return TypeAliasDecl(
            name = nameToken.lexeme,
            target = target,
            isPublic = isPublic,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = target.endOffset,
        )
    }

    private fun parseModDecl(isPublic: Boolean): ModDecl {
        val token = stream.expectKeyword("mod")
        val nameToken = stream.expect(TokenType.Identifier)

        val body = if (stream.isAt(TokenType.LeftBrace)) {
            stream.advance()
            val decls = mutableListOf<Decl>()
            while (!stream.isAt(TokenType.RightBrace) && !stream.isAtEnd()) {
                decls.add(parseDeclaration())
            }
            stream.expect(TokenType.RightBrace)
            decls as List<Decl>
        } else {
            if (stream.isAt(TokenType.Semicolon)) stream.advance()
            null
        }

        return ModDecl(
            name = nameToken.lexeme,
            body = body,
            isPublic = isPublic,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parseUseDecl(isPublic: Boolean): UseDecl {
        val token = stream.expectKeyword("use")
        val path = mutableListOf<String>()
        path.add(stream.expect(TokenType.Identifier).lexeme)

        while (stream.isAt(TokenType.DoubleColon)) {
            stream.advance()
            path.add(stream.expect(TokenType.Identifier).lexeme)
        }

        if (stream.isAt(TokenType.Semicolon)) stream.advance()

        return UseDecl(
            path = path,
            isPublic = isPublic,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parsePubDecl(): PubDecl {
        val token = stream.expectKeyword("pub")
        stream.expect(TokenType.DoubleColon)
        val inner = parseDeclaration(isPublic = true)

        return PubDecl(
            inner = inner,
            fileName = token.fileName, line = token.line, column = token.column,
            startOffset = token.startOffset, endOffset = inner.endOffset,
        )
    }

    private fun normalizeEntryPointName(name: String): String {
        return if (name == "ዋና") "main" else name
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
