package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.TokenType
import hasab.compiler.frontend.ast.Decl
import hasab.compiler.frontend.ast.Module

public class ModuleParser(
    private val stream: TokenStream,
    private val diagnostics: MutableList<ParserDiagnostic>,
) {
    private val declParser = DeclarationParser(stream, diagnostics)

    public fun parseModule(): Module {
        val declarations = mutableListOf<Decl>()
        var moduleName: String? = null

        while (!stream.isAtEnd()) {
            try {
                if (stream.isAtKeyword("package") && moduleName == null) {
                    moduleName = parsePackageDecl()
                    continue
                }

                val isPublic = stream.isAtKeyword("pub")
                if (isPublic) {
                    stream.advance()
                    if (stream.isAt(TokenType.DoubleColon)) {
                        stream.advance()
                    }
                }

                declarations.add(declParser.parseDeclaration(isPublic = isPublic))
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
                recoverToTopLevel()
            }
        }

        val endOffset = if (stream.size > 0) stream.peekAt(-1).endOffset else 0
        return Module(
            name = moduleName,
            declarations = declarations,
            fileName = stream.peek().fileName,
            endOffset = endOffset,
        )
    }

    private fun parsePackageDecl(): String {
        stream.expectKeyword("package")
        val parts = mutableListOf<String>()
        parts.add(stream.expect(TokenType.Identifier).lexeme)

        while (stream.isAt(TokenType.DoubleColon)) {
            stream.advance()
            parts.add(stream.expect(TokenType.Identifier).lexeme)
        }

        if (stream.isAt(TokenType.Semicolon)) {
            stream.advance()
        }

        return parts.joinToString("::")
    }

    private fun recoverToTopLevel() {
        while (!stream.isAtEnd()) {
            when (stream.peek().type) {
                TokenType.Eof -> return
                TokenType.RightBrace -> {
                    stream.advance()
                    return
                }
                TokenType.Semicolon -> {
                    stream.advance()
                    return
                }
                is TokenType.Keyword -> {
                    val kw = (stream.peek().type as TokenType.Keyword).keyword.tokenString
                    if (kw in listOf("fn", "struct", "enum", "impl", "trait", "type", "mod", "use", "pub", "package")) {
                        return
                    }
                    stream.advance()
                }
                else -> stream.advance()
            }
        }
    }

}
