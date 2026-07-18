package hasab.compiler.frontend.lexer

public data class LexerConfig(
    val maxStringLiteralLength: Int = 65536,
    val maxCharLiteralLength: Int = 1,
    val allowUnicodeInStrings: Boolean = true,
    val allowUnicodeInIdentifiers: Boolean = true,
    val singleLineCommentPrefix: String = "//",
    val multiLineCommentOpen: String = "/*",
    val multiLineCommentClose: String = "*/",
) {
    public companion object {
        public val DEFAULT: LexerConfig = LexerConfig()
    }
}
