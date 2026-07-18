package hasab.compiler.frontend.lexer

public data class SourcePosition(
    public val line: Int,
    public val column: Int,
    public val offset: Int,
) {
    init {
        require(line >= 1) { "Line number must be >= 1, got $line" }
        require(column >= 1) { "Column number must be >= 1, got $column" }
        require(offset >= 0) { "Offset must be >= 0, got $offset" }
    }

    override fun toString(): String = "$line:$column"
}
