package hasab.compiler.frontend.ast

// -- Module AST Node --

public data class Module(
    val name: String?,
    val declarations: List<Decl>,
    override val fileName: String,
    override val startOffset: Int = 0,
    override val endOffset: Int = 0,
    override val docComment: String? = null,
) : AstNode {
    override val line: Int get() = 1
    override val column: Int get() = 1

    override fun children(): List<AstNode> = declarations
}
