package hasab.compiler.types

import hasab.compiler.frontend.ast.Expr
import hasab.compiler.frontend.ast.IdentifierExpr
import hasab.compiler.frontend.ast.NilLiteralExpr

/**
 * Enforces null safety rules in the HASAB type system.
 *
 * Rules:
 * - `nil` cannot be assigned to a non-optional type.
 * - A `nil` literal used in a condition must be in an optional context.
 * - Nullable types must be explicitly declared with `?`.
 */
public object NullSafetyChecker {

    /**
     * Check that a `nil` literal is not assigned to a non-optional type.
     */
    public fun checkNilAssignment(
        valueExpr: Expr,
        targetType: Type,
        diagnostics: DiagnosticCollector,
    ) {
        if (valueExpr is NilLiteralExpr && targetType !is OptionalType) {
            diagnostics.report(
                TypeDiagnosticCode.NULL_SAFETY,
                "Cannot assign 'nil' to non-optional type '${targetType.displayName}'",
                valueExpr.range(),
                valueExpr.fileName,
                expectedType = targetType,
                foundType = NilLiteralType,
                hint = "Use '${targetType.displayName}?' to make the type optional",
            )
        }
    }

    /**
     * Check that an if/while condition has a bool-compatible type.
     * Nil checks are allowed via optional unwrapping.
     */
    public fun checkCondition(
        conditionExpr: Expr,
        conditionType: Type,
        diagnostics: DiagnosticCollector,
    ) {
        if (conditionType == BoolType) return
        if (conditionType == UnknownType) return
        if (conditionType is NilLiteralType) {
            diagnostics.report(
                TypeDiagnosticCode.NULL_SAFETY,
                "Cannot use 'nil' directly as a condition",
                conditionExpr.range(),
                conditionExpr.fileName,
                hint = "Use an explicit null check: 'x != nil' or 'if (x?)'",
            )
            return
        }
        diagnostics.report(
            TypeDiagnosticCode.LOGIC_TYPE_ERROR,
            "Condition must be 'bool', got '${conditionType.displayName}'",
            conditionExpr.range(),
            conditionExpr.fileName,
            expectedType = BoolType,
            foundType = conditionType,
            hint = "Convert the condition to 'bool' or use an optional check",
        )
    }

    /**
     * Check that a let statement with nil init has a type annotation.
     */
    public fun checkNilWithoutAnnotation(
        initializerType: Type,
        hasTypeAnnotation: Boolean,
        range: hasab.compiler.frontend.lexer.SourceRange,
        fileName: String,
        diagnostics: DiagnosticCollector,
    ) {
        if (initializerType == NilLiteralType && !hasTypeAnnotation) {
            diagnostics.report(
                TypeDiagnosticCode.CANNOT_INFER,
                "Cannot infer type from 'nil' without type annotation",
                range,
                fileName,
                hint = "Add a type annotation: 'let x: Type = nil'",
            )
        }
    }

    /**
     * Check that a return value is compatible with the function's return type
     * considering null safety.
     */
    public fun checkReturnType(
        valueType: Type,
        expectedType: Type,
        range: hasab.compiler.frontend.lexer.SourceRange,
        fileName: String,
        diagnostics: DiagnosticCollector,
    ) {
        if (valueType == UnknownType || expectedType == UnknownType) return

        if (valueType is NilLiteralType && expectedType is OptionalType) return

        if (!areTypesCompatible(valueType, expectedType)) {
            diagnostics.report(
                TypeDiagnosticCode.RETURN_TYPE_MISMATCH,
                "Expected return type '${expectedType.displayName}', got '${valueType.displayName}'",
                range,
                fileName,
                expectedType = expectedType,
                foundType = valueType,
                hint = "Function returns '${expectedType.displayName}'",
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
}
