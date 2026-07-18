package hasab.compiler.frontend.lexer

public data class LexerResult(
    public val tokens: List<Token>,
    public val diagnostics: List<Diagnostic>,
) {
    public val errors: List<Diagnostic>
        get() = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }

    public val warnings: List<Diagnostic>
        get() = diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }

    public val hasErrors: Boolean get() = errors.isNotEmpty()

    public val tokenTypes: List<TokenType> get() = tokens.map { it.type }

    public fun filterTokens(type: TokenType): List<Token> = tokens.filter { it.type == type }
}
