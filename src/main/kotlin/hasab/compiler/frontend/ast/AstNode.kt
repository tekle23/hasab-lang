package hasab.compiler.frontend.ast

import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.frontend.lexer.SourcePosition

public sealed interface AstNode {
    public val startOffset: Int
    public val endOffset: Int
    public val fileName: String
    public val line: Int
    public val column: Int

    public fun range(): SourceRange = SourceRange(
        SourcePosition(line, column, startOffset),
        SourcePosition(line, column, endOffset),
    )
}
