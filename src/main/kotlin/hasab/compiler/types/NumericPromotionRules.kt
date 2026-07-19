package hasab.compiler.types

/**
 * Defines the numeric promotion and type compatibility rules for HASAB.
 *
 * Rules:
 * - int → float (widening promotion)
 * - int → string (stringification)
 * - nil → T? (null literal is assignable to any optional)
 * - T? → T (nullable to non-nullable, warning)
 * - T → T? (non-nullable to nullable, always ok)
 * - [T] → [U] if T → U (covariant array assignment)
 */
public object NumericPromotionRules {

    /**
     * Check if [source] can be implicitly promoted to [target].
     *
     * Only numeric widening promotions are allowed:
     * - int → float
     */
    public fun canPromote(source: Type, target: Type): Boolean {
        if (source == target) return true
        return source is IntType && target is FloatType
    }

    /**
     * Apply numeric promotion: returns the promoted type, or null if no promotion applies.
     */
    public fun promote(source: Type, target: Type): Type? {
        if (source == target) return source
        if (source is IntType && target is FloatType) return FloatType
        return null
    }

    /**
     * Check if two types are compatible for binary operations.
     *
     * Returns the result type of the operation, or null if incompatible.
     */
    public fun binaryResultType(op: String, left: Type, right: Type): Type? = when (op) {
        "+", "-", "*", "/", "%" -> arithmeticResultType(left, right)
        "==", "!=" -> if (areComparable(left, right)) BoolType else null
        "<", ">", "<=", ">=" -> if (areOrderable(left, right)) BoolType else null
        "&&", "||" -> if (left == BoolType && right == BoolType) BoolType else null
        "&", "|", "^", "<<", ">>" -> if (left == IntType && right == IntType) IntType else null
        ".." -> if (left == IntType && right == IntType) ArrayType(IntType) else null
        "..=" -> if (left == IntType && right == IntType) ArrayType(IntType) else null
        else -> null
    }

    /**
     * Determine the result type of an arithmetic operation.
     */
    private fun arithmeticResultType(left: Type, right: Type): Type? = when {
        left == IntType && right == IntType -> IntType
        left == FloatType && right == FloatType -> FloatType
        left == IntType && right == FloatType -> FloatType
        left == FloatType && right == IntType -> FloatType
        left == StringType && right == StringType -> StringType
        else -> null
    }

    /**
     * Check if two types can be compared.
     */
    public fun areComparable(left: Type, right: Type): Boolean {
        if (left == right) return true
        if (left is IntType && right is FloatType) return true
        if (left is FloatType && right is IntType) return true
        return false
    }

    /**
     * Check if two types support ordering (<, >, <=, >=).
     */
    public fun areOrderable(left: Type, right: Type): Boolean = areComparable(left, right)

    /**
     * Check if a unary operator is valid for the given type.
     */
    public fun unaryResultType(op: String, operand: Type): Type? = when (op) {
        "-", "+" -> if (operand == IntType || operand == FloatType) operand else null
        "!" -> if (operand == BoolType) BoolType else null
        "*" -> when (operand) {
            is PointerType -> operand.elementType
            else -> null
        }
        "&" -> PointerType(operand)
        "~" -> if (operand == IntType) IntType else null
        else -> null
    }

    /**
     * Get the common type for if-expression branches.
     *
     * If both branches are compatible, returns the more specific type.
     * If incompatible, returns the first type (error will be reported by caller).
     */
    public fun commonBranchType(thenType: Type, elseType: Type): Type {
        if (thenType == elseType) return thenType
        if (areComparable(thenType, elseType)) {
            return when {
                thenType is IntType && elseType is FloatType -> FloatType
                thenType is FloatType && elseType is IntType -> FloatType
                else -> thenType
            }
        }
        return thenType
    }
}
