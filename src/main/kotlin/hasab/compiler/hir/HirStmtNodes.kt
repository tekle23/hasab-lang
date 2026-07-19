package hasab.compiler.hir

import hasab.compiler.types.Type
import hasab.compiler.types.VoidType

// ---- HIR Statements (all typed) ----

public sealed interface HirStmt : HirNode

public data class HirBlock(
    val statements: List<HirStmt>,
) : HirStmt {
    override val type: Type get() = VoidType
    override val hirChildren: List<HirNode> get() = statements
}

public data class HirExprStmt(
    val expression: HirExpr,
) : HirStmt {
    override val type: Type get() = expression.type
    override val hirChildren: List<HirNode> get() = listOf(expression)
}

public data class HirReturnStmt(
    val value: HirExpr?,
    override val type: Type,
) : HirStmt {
    override val hirChildren: List<HirNode> get() = listOfNotNull(value)
}

public data class HirLetStmt(
    val name: String,
    val initializer: HirExpr,
    val isMutable: Boolean,
    override val type: Type,
) : HirStmt {
    override val hirChildren: List<HirNode> get() = listOf(initializer)
}

public data class HirIfStmt(
    val condition: HirExpr,
    val thenBranch: HirBlock,
    val elseBranch: HirStmt?,
    override val type: Type,
) : HirStmt {
    override val hirChildren: List<HirNode> get() = listOf(condition, thenBranch) + listOfNotNull(elseBranch)
}

public data class HirWhileStmt(
    val condition: HirExpr,
    val body: HirBlock,
) : HirStmt {
    override val type: Type get() = VoidType
    override val hirChildren: List<HirNode> get() = listOf(condition, body)
}

public data class HirForStmt(
    val variable: String,
    val variableType: Type,
    val iterable: HirExpr,
    val body: HirBlock,
) : HirStmt {
    override val type: Type get() = VoidType
    override val hirChildren: List<HirNode> get() = listOf(iterable, body)
}

public class HirBreakStmt : HirStmt {
    override val type: Type get() = VoidType
    override fun equals(other: Any?): Boolean = other is HirBreakStmt
    override fun hashCode(): Int = HirBreakStmt::class.hashCode()
}

public class HirContinueStmt : HirStmt {
    override val type: Type get() = VoidType
    override fun equals(other: Any?): Boolean = other is HirContinueStmt
    override fun hashCode(): Int = HirContinueStmt::class.hashCode()
}
