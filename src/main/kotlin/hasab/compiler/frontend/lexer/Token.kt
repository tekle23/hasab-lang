package hasab.compiler.frontend.lexer

public data class Token(
    public val type: TokenType,
    public val lexeme: String,
    public val fileName: String,
    public val line: Int,
    public val column: Int,
    public val startOffset: Int,
    public val endOffset: Int,
) {
    public val range: SourceRange
        get() = SourceRange(
            SourcePosition(line, column, startOffset),
            SourcePosition(line, column + lexeme.length, endOffset),
        )

    override fun toString(): String {
        val lexemePreview = if (lexeme.length > 40) lexeme.take(40) + "..." else lexeme
        return "Token($type, \"$lexemePreview\", $fileName:$line:$column)"
    }
}
