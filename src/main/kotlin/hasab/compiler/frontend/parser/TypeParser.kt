package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.Token
import hasab.compiler.frontend.lexer.TokenType
import hasab.compiler.frontend.ast.*

public class TypeParser(
    private val stream: TokenStream,
    private val diagnostics: MutableList<ParserDiagnostic>,
) {

    public fun parseType(): TypeNode {
        val token = stream.peek()

        return when (token.type) {
            TokenType.LeftBracket -> parseArrayType()
            TokenType.Star -> parsePointerType()
            else -> {
                val base = parseBaseType()
                parseTypeSuffix(base)
            }
        }
    }

    private fun parseBaseType(): TypeNode {
        val token = stream.peek()

        return when (token.type) {
            is TokenType.Keyword -> {
                val keyword = (token.type as TokenType.Keyword).keyword
                when (keyword.tokenString) {
                    "void" -> {
                        val t = stream.advance()
                        VoidType(t.fileName, t.line, t.column, t.startOffset, t.endOffset)
                    }
                    "fn" -> parseFunctionType()
                    else -> {
                        val t = stream.advance()
                        IdentifierType(t.lexeme, t.fileName, t.line, t.column, t.startOffset, t.endOffset)
                    }
                }
            }
            TokenType.Identifier -> {
                val t = stream.advance()
                parseQualifiedType(t)
            }
            else -> {
                reportError("Expected a type, got ${token.type}", token)
                val t = stream.advance()
                IdentifierType(t.lexeme, t.fileName, t.line, t.column, t.startOffset, t.endOffset)
            }
        }
    }

    private fun parseQualifiedType(firstToken: Token): TypeNode {
        val parts = mutableListOf(firstToken.lexeme)
        var startOffset = firstToken.startOffset
        var endOffset = firstToken.endOffset
        val line = firstToken.line
        val column = firstToken.column
        val fileName = firstToken.fileName

        while (stream.isAt(TokenType.DoubleColon)) {
            stream.advance()
            val next = stream.expect(TokenType.Identifier)
            parts.add(next.lexeme)
            endOffset = next.endOffset
        }

        return if (parts.size == 1) {
            IdentifierType(parts[0], fileName, line, column, startOffset, endOffset)
        } else {
            QualifiedType(parts, fileName, line, column, startOffset, endOffset)
        }
    }

    private fun parseFunctionType(): TypeNode {
        val token = stream.expectKeyword("fn")
        val fnToken = token
        stream.expect(TokenType.LeftParen)

        val paramTypes = mutableListOf<TypeNode>()
        if (!stream.isAt(TokenType.RightParen)) {
            paramTypes.add(parseType())
            while (stream.isAt(TokenType.Comma)) {
                stream.advance()
                paramTypes.add(parseType())
            }
        }
        stream.expect(TokenType.RightParen)

        var returnType: TypeNode = VoidType(
            fnToken.fileName, fnToken.line, fnToken.column, fnToken.endOffset, fnToken.endOffset,
        )

        if (stream.isAt(TokenType.Arrow)) {
            stream.advance()
            returnType = parseType()
        }

        return FunctionType(
            parameterTypes = paramTypes,
            returnType = returnType,
            fileName = fnToken.fileName,
            line = fnToken.line,
            column = fnToken.column,
            startOffset = fnToken.startOffset,
            endOffset = returnType.endOffset,
        )
    }

    private fun parseArrayType(): TypeNode {
        val token = stream.expect(TokenType.LeftBracket)
        val elementType = parseType()
        stream.expect(TokenType.RightBracket)

        return ArrayType(
            elementType = elementType,
            fileName = token.fileName,
            line = token.line,
            column = token.column,
            startOffset = token.startOffset,
            endOffset = stream.previousToken().endOffset,
        )
    }

    private fun parsePointerType(): TypeNode {
        val token = stream.expect(TokenType.Star)
        val elementType = parseType()

        return PointerType(
            elementType = elementType,
            fileName = token.fileName,
            line = token.line,
            column = token.column,
            startOffset = token.startOffset,
            endOffset = elementType.endOffset,
        )
    }

    private fun parseTypeSuffix(base: TypeNode): TypeNode {
        if (stream.isAt(TokenType.Question)) {
            val token = stream.advance()
            return OptionalType(
                elementType = base,
                fileName = token.fileName,
                line = token.line,
                column = token.column,
                startOffset = base.startOffset,
                endOffset = token.endOffset,
            )
        }
        return base
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
