package hasab.compiler.hir

import hasab.compiler.types.Type
import hasab.compiler.types.IntType
import hasab.compiler.types.FloatType
import hasab.compiler.types.StringType
import hasab.compiler.types.CharType
import hasab.compiler.types.BoolType
import hasab.compiler.types.VoidType
import hasab.compiler.types.NilLiteralType

// ---- HIR Expressions (all typed) ----

public sealed interface HirExpr : HirNode

public data class HirIntLiteral(val value: String) : HirExpr {
    override val type: Type get() = IntType
}

public data class HirFloatLiteral(val value: String) : HirExpr {
    override val type: Type get() = FloatType
}

public data class HirStringLiteral(val value: String) : HirExpr {
    override val type: Type get() = StringType
}

public data class HirCharLiteral(val value: String) : HirExpr {
    override val type: Type get() = CharType
}

public data class HirBoolLiteral(val value: Boolean) : HirExpr {
    override val type: Type get() = BoolType
}

public data class HirNilLiteral(override val type: Type) : HirExpr

public data class HirIdentifier(
    val name: String,
    override val type: Type,
) : HirExpr

public data class HirBinary(
    val left: HirExpr,
    val operator: String,
    val right: HirExpr,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(left, right)
}

public data class HirUnary(
    val operator: String,
    val operand: HirExpr,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(operand)
}

public data class HirCall(
    val callee: HirExpr,
    val arguments: List<HirExpr>,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(callee) + arguments
}

public data class HirIndex(
    val callee: HirExpr,
    val index: HirExpr,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(callee, index)
}

public data class HirFieldAccess(
    val callee: HirExpr,
    val fieldName: String,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(callee)
}

public data class HirSafeFieldAccess(
    val callee: HirExpr,
    val fieldName: String,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(callee)
}

public data class HirNullAssert(
    val operand: HirExpr,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(operand)
}

public data class HirArrayLiteral(
    val elements: List<HirExpr>,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = elements
}

public data class HirArrayInit(
    val size: HirExpr,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(size)
}

public data class HirIfExpr(
    val condition: HirExpr,
    val thenBranch: HirExpr,
    val elseBranch: HirExpr?,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOfNotNull(condition, thenBranch, elseBranch)
}

public data class HirAssignment(
    val target: HirExpr,
    val value: HirExpr,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(target, value)
}

public data class HirCompoundAssignment(
    val target: HirExpr,
    val operator: String,
    val value: HirExpr,
    override val type: Type,
) : HirExpr {
    override val hirChildren: List<HirNode> get() = listOf(target, value)
}
