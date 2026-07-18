package hasab.compiler.frontend.ast

// -- Statement AST Nodes --

public sealed interface Stmt : AstNode

public data class ExprStmt(
    val expression: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = listOf(expression)
}

public data class ReturnStmt(
    val value: Expr?,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = listOfNotNull(value)
}

public data class BreakStmt(
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = emptyList()
}

public data class ContinueStmt(
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = emptyList()
}

public data class LetStmt(
    val name: String,
    val typeAnnotation: TypeNode?,
    val initializer: Expr,
    val isMutable: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = listOfNotNull(typeAnnotation, initializer)
}

public data class IfStmt(
    val condition: Expr,
    val thenBranch: Block,
    val elseBranch: Stmt?,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = listOf(condition, thenBranch) + listOfNotNull(elseBranch)
}

public data class WhileStmt(
    val condition: Expr,
    val body: Block,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = listOf(condition, body)
}

public data class ForStmt(
    val variable: String,
    val iterable: Expr,
    val body: Block,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = listOf(iterable, body)
}

public data class Block(
    val statements: List<Stmt>,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Stmt {
    override fun children(): List<AstNode> = statements
}
