package hasab.compiler.frontend.ast

// -- Expression AST Nodes --

public sealed interface Expr : AstNode

public data class IntegerLiteralExpr(
    val value: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = emptyList()
}

public data class FloatLiteralExpr(
    val value: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = emptyList()
}

public data class StringLiteralExpr(
    val value: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = emptyList()
}

public data class CharLiteralExpr(
    val value: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = emptyList()
}

public data class BoolLiteralExpr(
    val value: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = emptyList()
}

public data class NilLiteralExpr(
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = emptyList()
}

public data class IdentifierExpr(
    val name: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = emptyList()
}

public data class BinaryExpr(
    val left: Expr,
    val operator: String,
    val right: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(left, right)
}

public data class UnaryExpr(
    val operator: String,
    val operand: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(operand)
}

public data class CallExpr(
    val callee: Expr,
    val arguments: List<Expr>,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(callee) + arguments
}

public data class IndexExpr(
    val callee: Expr,
    val index: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(callee, index)
}

public data class FieldAccessExpr(
    val callee: Expr,
    val fieldName: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(callee)
}

public data class SafeFieldAccessExpr(
    val callee: Expr,
    val fieldName: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(callee)
}

public data class NullAssertExpr(
    val operand: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(operand)
}

public data class ParenExpr(
    val inner: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(inner)
}

public data class ArrayLiteralExpr(
    val elements: List<Expr>,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = elements
}

public data class ArrayInitExpr(
    val elementType: TypeNode?,
    val size: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOfNotNull(elementType, size)
}

public data class IfExpr(
    val condition: Expr,
    val thenBranch: Expr,
    val elseBranch: Expr?,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOfNotNull(condition, thenBranch, elseBranch)
}

public data class AssignmentExpr(
    val target: Expr,
    val value: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(target, value)
}

public data class CompoundAssignmentExpr(
    val target: Expr,
    val operator: String,
    val value: Expr,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Expr {
    override fun children(): List<AstNode> = listOf(target, value)
}
