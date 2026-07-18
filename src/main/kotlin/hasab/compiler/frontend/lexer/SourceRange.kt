package hasab.compiler.frontend.lexer

public data class SourceRange(
    public val start: SourcePosition,
    public val end: SourcePosition,
) {
    init {
        require(end.offset >= start.offset) {
            "End offset (${end.offset}) must be >= start offset (${start.offset})"
        }
    }

    public val length: Int get() = end.offset - start.offset
}
