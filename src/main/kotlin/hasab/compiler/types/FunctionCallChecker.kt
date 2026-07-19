package hasab.compiler.types

import hasab.compiler.frontend.ast.CallExpr

/**
 * Validates function call expressions: argument count, argument types, return type resolution.
 */
public object FunctionCallChecker {

    /**
     * Check a call expression with the given callee type.
     * Returns the resolved return type.
     */
    public fun checkCall(
        expr: CallExpr,
        calleeType: Type,
        diagnostics: DiagnosticCollector,
    ): Type {
        if (calleeType is UnknownType) return UnknownType

        if (calleeType !is FunctionType) {
            diagnostics.report(
                TypeDiagnosticCode.NOT_CALLABLE,
                "Expression of type '${calleeType.displayName}' is not callable",
                expr.range(),
                expr.fileName,
                hint = "Only functions can be called",
            )
            return UnknownType
        }

        checkArgumentCount(expr, calleeType, diagnostics)
        checkArgumentTypes(expr, calleeType, diagnostics)

        return calleeType.returnType
    }

    /**
     * Check that the number of arguments matches the function signature.
     */
    private fun checkArgumentCount(
        expr: CallExpr,
        fnType: FunctionType,
        diagnostics: DiagnosticCollector,
    ) {
        if (expr.arguments.size != fnType.parameterTypes.size) {
            diagnostics.report(
                TypeDiagnosticCode.WRONG_ARGUMENT_COUNT,
                "Expected ${fnType.parameterTypes.size} arguments, got ${expr.arguments.size}",
                expr.range(),
                expr.fileName,
                hint = "Function expects ${fnType.parameterTypes.size} arguments",
            )
        }
    }

    /**
     * Check that each argument type matches the corresponding parameter type.
     */
    private fun checkArgumentTypes(
        expr: CallExpr,
        fnType: FunctionType,
        diagnostics: DiagnosticCollector,
    ) {
        val count = minOf(expr.arguments.size, fnType.parameterTypes.size)
        for (i in 0 until count) {
            val argType = inferExprType(expr.arguments[i], fnType)
            val paramType = fnType.parameterTypes[i]
            if (!areTypesCompatible(argType, paramType)) {
                diagnostics.report(
                    TypeDiagnosticCode.ARGUMENT_TYPE_MISMATCH,
                    "Argument ${i + 1}: expected '${paramType.displayName}', got '${argType.displayName}'",
                    expr.arguments[i].range(),
                    expr.arguments[i].fileName,
                    expectedType = paramType,
                    foundType = argType,
                    suggestion = "Convert '${argType.displayName}' to '${paramType.displayName}' or change the parameter type",
                )
            }
        }
    }

    /**
     * Minimal expression type inference for argument checking.
     * Uses literal types and TypeVariables; delegates complex cases to UnknownType.
     */
    private fun inferExprType(expr: hasab.compiler.frontend.ast.Expr, context: FunctionType): Type = when (expr) {
        is hasab.compiler.frontend.ast.IntegerLiteralExpr -> IntType
        is hasab.compiler.frontend.ast.FloatLiteralExpr -> FloatType
        is hasab.compiler.frontend.ast.StringLiteralExpr -> StringType
        is hasab.compiler.frontend.ast.CharLiteralExpr -> CharType
        is hasab.compiler.frontend.ast.BoolLiteralExpr -> BoolType
        is hasab.compiler.frontend.ast.NilLiteralExpr -> NilLiteralType
        else -> UnknownType
    }

    private fun areTypesCompatible(source: Type, target: Type): Boolean {
        if (source.isAssignableTo(target)) return true
        if (NumericPromotionRules.canPromote(source, target)) return true
        if (target is OptionalType) return source is NilLiteralType || source.isAssignableTo(target.elementType)
        if (source is OptionalType) return source.elementType.isAssignableTo(target)
        return false
    }
}
