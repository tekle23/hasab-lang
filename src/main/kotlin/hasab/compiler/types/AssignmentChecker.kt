package hasab.compiler.types

import hasab.compiler.frontend.ast.Expr
import hasab.compiler.frontend.ast.IdentifierExpr

/**
 * Validates assignments: mutability checks and type compatibility.
 */
public object AssignmentChecker {

    /**
     * Check that an assignment to [targetExpr] of [targetType] with [valueType] is valid.
     *
     * Checks:
     * 1. The target is mutable (not a `let` variable).
     * 2. The value type is compatible with the target type.
     */
    public fun checkAssignment(
        targetExpr: Expr,
        valueExpr: Expr,
        targetType: Type,
        valueType: Type,
        env: TypeEnvironment,
        diagnostics: DiagnosticCollector,
    ) {
        if (targetExpr is IdentifierExpr && targetExpr.name !in BUILTIN_TYPE_NAMES) {
            diagnostics.report(
                TypeDiagnosticCode.MUTABILITY_VIOLATION,
                "Cannot assign to immutable variable '${targetExpr.name}'",
                targetExpr.range(),
                targetExpr.fileName,
                hint = "Declare with 'mut' to allow reassignment",
            )
        }
        checkTypeCompatibility(targetType, valueType, targetExpr.range(), targetExpr.fileName, diagnostics)
    }

    /**
     * Check that a compound assignment (+=, -=, etc.) is valid.
     */
    public fun checkCompoundAssignment(
        targetExpr: Expr,
        operator: String,
        targetType: Type,
        valueType: Type,
        diagnostics: DiagnosticCollector,
    ) {
        if (targetExpr is IdentifierExpr && targetExpr.name !in BUILTIN_TYPE_NAMES) {
            diagnostics.report(
                TypeDiagnosticCode.MUTABILITY_VIOLATION,
                "Cannot assign to immutable variable '${targetExpr.name}'",
                targetExpr.range(),
                targetExpr.fileName,
                hint = "Declare with 'mut' to allow reassignment",
            )
        }
        checkTypeCompatibility(targetType, valueType, targetExpr.range(), targetExpr.fileName, diagnostics)
    }

    private fun checkTypeCompatibility(
        targetType: Type,
        valueType: Type,
        range: hasab.compiler.frontend.lexer.SourceRange,
        fileName: String,
        diagnostics: DiagnosticCollector,
    ) {
        if (targetType == UnknownType || valueType == UnknownType) return

        if (!areTypesCompatible(valueType, targetType)) {
            diagnostics.report(
                TypeDiagnosticCode.CANNOT_ASSIGN_TYPE,
                "Cannot assign '${valueType.displayName}' to '${targetType.displayName}'",
                range,
                fileName,
                expectedType = targetType,
                foundType = valueType,
            )
        }
    }

    private fun areTypesCompatible(source: Type, target: Type): Boolean {
        if (source.isAssignableTo(target)) return true
        if (NumericPromotionRules.canPromote(source, target)) return true
        if (target is OptionalType) return source is NilLiteralType || source.isAssignableTo(target.elementType)
        if (source is OptionalType) return source.elementType.isAssignableTo(target)
        return false
    }

    private val BUILTIN_TYPE_NAMES = setOf("int", "float", "string", "bool", "char", "void")
}
