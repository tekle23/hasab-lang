package hasab.compiler.frontend.ast

// ── Statement AST Nodes ────────────────────────────────────────

public sealed interface Stmt : AstNode

public data class ExprStmt(
    val expression: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt

public data class ReturnStmt(
    val value: Expr?,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt

public data class BreakStmt(
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt

public data class ContinueStmt(
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt

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
) : Stmt

public data class IfStmt(
    val condition: Expr,
    val thenBranch: Block,
    val elseBranch: Stmt?,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt

public data class WhileStmt(
    val condition: Expr,
    val body: Block,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt

public data class ForStmt(
    val variable: String,
    val iterable: Expr,
    val body: Block,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt

public data class Block(
    val statements: List<Stmt>,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Stmt
