package hasab.compiler.hir.cfg

import hasab.compiler.types.Type
import kotlin.jvm.JvmInline

/**
 * A unique identifier for a basic block within a CFG function.
 */
@JvmInline
public value class BlockId(public val value: Int) {
    override fun toString(): String = "bb$value"
}

/**
 * A unique identifier for a virtual register within a CFG function.
 */
public data class Register(public val name: String, public val type: Type) {
    override fun toString(): String = name
}

/**
 * An operand used in CFG instructions — either a register, a constant, or a function parameter.
 */
public sealed interface Operand {
    public val type: Type
}

public data class RegisterOperand(public val register: Register) : Operand {
    override val type: Type get() = register.type
    override fun toString(): String = register.name
}

public data class ConstOperand(public val value: Any?, override val type: Type) : Operand {
    override fun toString(): String = when (value) {
        is String -> "\"$value\""
        is Char -> "'$value'"
        null -> "nil"
        else -> value.toString()
    }
}

public data class ParamOperand(public val name: String, override val type: Type) : Operand {
    override fun toString(): String = name
}

/**
 * Sealed hierarchy of all CFG instructions.
 *
 * Instructions are categorized as:
 * - **Value instructions**: produce a result in a target register
 * - **Terminator instructions**: end a basic block (exactly one per block)
 * - **Memory instructions**: store values (no result register)
 */
public sealed interface HirInstruction {
    public val comment: String? get() = null
}

// ---- Value instructions (produce a target register) ----

public data class AssignInstr(
    public val target: Register,
    public val value: Operand,
    override val comment: String? = null,
) : HirInstruction

public data class BinaryOpInstr(
    public val target: Register,
    public val operator: String,
    public val left: Operand,
    public val right: Operand,
    override val comment: String? = null,
) : HirInstruction

public data class UnaryOpInstr(
    public val target: Register,
    public val operator: String,
    public val operand: Operand,
    override val comment: String? = null,
) : HirInstruction

public data class CallInstr(
    public val target: Register?,
    public val calleeName: String,
    public val calleeType: Type,
    public val arguments: List<Operand>,
    override val comment: String? = null,
) : HirInstruction

public data class LoadFieldInstr(
    public val target: Register,
    public val base: Operand,
    public val fieldName: String,
    public val fieldType: Type,
    override val comment: String? = null,
) : HirInstruction

public data class LoadIndexInstr(
    public val target: Register,
    public val base: Operand,
    public val index: Operand,
    public val elementType: Type,
    override val comment: String? = null,
) : HirInstruction

public data class ArrayLiteralInstr(
    public val target: Register,
    public val elements: List<Operand>,
    public val arrayType: Type,
    override val comment: String? = null,
) : HirInstruction

public data class ArrayInitInstr(
    public val target: Register,
    public val size: Operand,
    public val arrayType: Type,
    override val comment: String? = null,
) : HirInstruction

public data class PhiInstr(
    public val target: Register,
    public val sources: List<Pair<BlockId, Operand>>,
    override val comment: String? = null,
) : HirInstruction

public data class CastInstr(
    public val target: Register,
    public val source: Operand,
    public val toType: Type,
    override val comment: String? = null,
) : HirInstruction

public data class NullCheckInstr(
    public val target: Register,
    public val source: Operand,
    override val comment: String? = null,
) : HirInstruction

public data class NullAssertInstr(
    public val target: Register,
    public val source: Operand,
    override val comment: String? = null,
) : HirInstruction

// ---- Terminator instructions (end a basic block) ----

public data class ReturnInstr(
    public val value: Operand?,
    override val comment: String? = null,
) : HirInstruction

public data class BranchInstr(
    public val condition: Operand,
    public val trueBlock: BlockId,
    public val falseBlock: BlockId,
    override val comment: String? = null,
) : HirInstruction

public data class JumpInstr(
    public val target: BlockId,
    override val comment: String? = null,
) : HirInstruction

public data class SwitchInstr(
    public val subject: Operand,
    public val cases: List<Pair<Any, BlockId>>,
    public val defaultBlock: BlockId,
    override val comment: String? = null,
) : HirInstruction

// ---- Memory instructions (no result register) ----

public data class StoreFieldInstr(
    public val base: Operand,
    public val fieldName: String,
    public val value: Operand,
    override val comment: String? = null,
) : HirInstruction

public data class StoreIndexInstr(
    public val base: Operand,
    public val index: Operand,
    public val value: Operand,
    override val comment: String? = null,
) : HirInstruction

/**
 * Utility to check if an instruction is a terminator.
 */
public fun HirInstruction.isTerminator(): Boolean = this is ReturnInstr ||
    this is BranchInstr ||
    this is JumpInstr ||
    this is SwitchInstr

/**
 * Utility to get the target register of a value instruction, if any.
 */
public fun HirInstruction.targetRegister(): Register? = when (this) {
    is AssignInstr -> target
    is BinaryOpInstr -> target
    is UnaryOpInstr -> target
    is CallInstr -> target
    is LoadFieldInstr -> target
    is LoadIndexInstr -> target
    is ArrayLiteralInstr -> target
    is ArrayInitInstr -> target
    is PhiInstr -> target
    is CastInstr -> target
    is NullCheckInstr -> target
    is NullAssertInstr -> target
    is ReturnInstr, is BranchInstr, is JumpInstr, is SwitchInstr -> null
    is StoreFieldInstr, is StoreIndexInstr -> null
}
