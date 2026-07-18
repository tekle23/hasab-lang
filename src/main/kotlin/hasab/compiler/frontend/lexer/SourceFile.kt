package hasab.compiler.frontend.lexer

public data class SourceFile(
    public val name: String,
    public val content: String,
) {
    public val length: Int get() = content.length

    public fun isEmpty(): Boolean = content.isEmpty()

    public fun charAt(index: Int): Char {
        require(index in content.indices) { "Index $index out of bounds [0, ${content.length})" }
        return content[index]
    }

    public fun substring(start: Int, end: Int): String = content.substring(start, end)
}
