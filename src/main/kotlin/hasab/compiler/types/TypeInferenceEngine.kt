package hasab.compiler.types

import hasab.compiler.frontend.ast.*

/**
 * Infers the [Type] of AST expressions.
 *
 * Stateless — operates on the provided [TypeEnvironment] and [TypedSemanticModel].
 * Each method returns the inferred type and optionally records it in the model.
 */
public class TypeInferenceEngine(
    private val model: TypedSemanticModel,
    private val env: TypeEnvironment,
    private val diagnostics: DiagnosticCollector,
) {
    private var typeVarCounter = 0

    /**
     * Infer the type of an expression.
     */
    internal fun infer(expr: Expr): Type = when (expr) {
        is IntegerLiteralExpr -> IntType
        is FloatLiteralExpr -> FloatType
        is StringLiteralExpr -> StringType
        is CharLiteralExpr -> CharType
        is BoolLiteralExpr -> BoolType
        is NilLiteralExpr -> NilLiteralType
        is IdentifierExpr -> inferIdentifier(expr)
        is BinaryExpr -> inferBinary(expr)
        is UnaryExpr -> inferUnary(expr)
        is CallExpr -> inferCall(expr)
        is IndexExpr -> inferIndex(expr)
        is FieldAccessExpr -> inferFieldAccess(expr)
        is SafeFieldAccessExpr -> inferSafeFieldAccess(expr)
        is NullAssertExpr -> inferNullAssert(expr)
        is ParenExpr -> infer(expr.inner)
        is ArrayLiteralExpr -> inferArrayLiteral(expr)
        is ArrayInitExpr -> inferArrayInit(expr)
        is IfExpr -> inferIfExpr(expr)
        is AssignmentExpr -> inferAssignment(expr)
        is CompoundAssignmentExpr -> inferCompoundAssignment(expr)
    }

    private fun inferIdentifier(expr: IdentifierExpr): Type {
        return env.lookup(expr.name) ?: run {
            diagnostics.report(
                TypeDiagnosticCode.UNDEFINED_VARIABLE,
                "Undefined variable '${expr.name}'",
                expr.range(),
                expr.fileName,
                hint = "Declare the variable before use, or check for typos",
            )
            UnknownType
        }
    }

    private fun inferBinary(expr: BinaryExpr): Type {
        val leftType = infer(expr.left)
        val rightType = infer(expr.right)
        val resultType = NumericPromotionRules.binaryResultType(expr.operator, leftType, rightType)

        return if (resultType != null) {
            resultType
        } else {
            diagnostics.report(
                TypeDiagnosticCode.ARITH_TYPE_ERROR,
                "Cannot apply '${expr.operator}' to '${leftType.displayName}' and '${rightType.displayName}'",
                expr.range(),
                expr.fileName,
                expectedType = leftType,
                foundType = rightType,
            )
            UnknownType
        }
    }

    private fun inferUnary(expr: UnaryExpr): Type {
        val operandType = infer(expr.operand)
        val resultType = NumericPromotionRules.unaryResultType(expr.operator, operandType)

        return if (resultType != null) {
            resultType
        } else {
            diagnostics.report(
                TypeDiagnosticCode.UNARY_TYPE_ERROR,
                "Cannot apply '${expr.operator}' to '${operandType.displayName}'",
                expr.range(),
                expr.fileName,
                foundType = operandType,
            )
            UnknownType
        }
    }

    private fun inferCall(expr: CallExpr): Type {
        val calleeType = infer(expr.callee)
        return FunctionCallChecker.checkCall(expr, calleeType, diagnostics)
    }

    private fun inferIndex(expr: IndexExpr): Type {
        val calleeType = infer(expr.callee)
        val indexType = infer(expr.index)

        if (indexType != IntType && indexType != UnknownType) {
            diagnostics.report(
                TypeDiagnosticCode.TYPE_MISMATCH,
                "Index must be 'int', got '${indexType.displayName}'",
                expr.index.range(),
                expr.index.fileName,
                expectedType = IntType,
                foundType = indexType,
            )
        }

        return when (calleeType) {
            is ArrayType -> OptionalType(calleeType.elementType)
            is StringType -> CharType
            else -> {
                diagnostics.report(
                    TypeDiagnosticCode.NOT_INDEXABLE,
                    "Type '${calleeType.displayName}' is not indexable",
                    expr.range(),
                    expr.fileName,
                    foundType = calleeType,
                )
                UnknownType
            }
        }
    }

    private fun inferFieldAccess(expr: FieldAccessExpr): Type {
        val calleeType = infer(expr.callee)
        return FieldAccessChecker.checkFieldAccess(expr, calleeType, diagnostics)
    }

    private fun inferSafeFieldAccess(expr: SafeFieldAccessExpr): Type {
        val calleeType = infer(expr.callee)
        val unwrappedType = when (calleeType) {
            is OptionalType -> calleeType.elementType
            is PointerType -> calleeType.elementType
            else -> calleeType
        }
        return when (unwrappedType) {
            is StructType -> {
                val field = unwrappedType.fieldByName(expr.fieldName)
                if (field != null) OptionalType(field.type) else UnknownType
            }
            else -> UnknownType
        }
    }

    private fun inferNullAssert(expr: NullAssertExpr): Type {
        val operandType = infer(expr.operand)
        return when (operandType) {
            is OptionalType -> operandType.elementType
            else -> operandType
        }
    }

    private fun inferArrayLiteral(expr: ArrayLiteralExpr): Type {
        if (expr.elements.isEmpty()) return ArrayType(TypeVariable(newTypeVarId()))
        val elementType = infer(expr.elements[0])
        for (i in 1 until expr.elements.size) {
            val elemType = infer(expr.elements[i])
            if (!areTypesCompatible(elemType, elementType)) {
                diagnostics.report(
                    TypeDiagnosticCode.TYPE_MISMATCH,
                    "Array element ${i + 1}: expected '${elementType.displayName}', got '${elemType.displayName}'",
                    expr.elements[i].range(),
                    expr.fileName,
                    expectedType = elementType,
                    foundType = elemType,
                )
            }
        }
        return ArrayType(elementType)
    }

    private fun inferArrayInit(expr: ArrayInitExpr): Type {
        val sizeType = infer(expr.size)
        if (sizeType != IntType && sizeType != UnknownType) {
            diagnostics.report(
                TypeDiagnosticCode.TYPE_MISMATCH,
                "Array size must be 'int', got '${sizeType.displayName}'",
                expr.size.range(),
                expr.fileName,
                expectedType = IntType,
                foundType = sizeType,
            )
        }
        val elementType = expr.elementType?.let { TypeResolver.resolve(it, env) } ?: TypeVariable(newTypeVarId())
        return ArrayType(elementType)
    }

    private fun inferIfExpr(expr: IfExpr): Type {
        val condType = infer(expr.condition)
        NullSafetyChecker.checkCondition(expr.condition, condType, diagnostics)

        val thenType = infer(expr.thenBranch)
        val elseType = expr.elseBranch?.let { infer(it) }
        return when {
            elseType == null -> OptionalType(thenType)
            areTypesCompatible(thenType, elseType) -> NumericPromotionRules.commonBranchType(thenType, elseType)
            areTypesCompatible(elseType, thenType) -> NumericPromotionRules.commonBranchType(elseType, thenType)
            else -> {
                diagnostics.report(
                    TypeDiagnosticCode.TYPE_MISMATCH,
                    "If branches have incompatible types: '${thenType.displayName}' vs '${elseType.displayName}'",
                    expr.range(),
                    expr.fileName,
                    expectedType = thenType,
                    foundType = elseType,
                )
                thenType
            }
        }
    }

    private fun inferAssignment(expr: AssignmentExpr): Type {
        val targetType = infer(expr.target)
        val valueType = infer(expr.value)
        AssignmentChecker.checkAssignment(expr.target, expr.value, targetType, valueType, env, diagnostics)
        return targetType
    }

    private fun inferCompoundAssignment(expr: CompoundAssignmentExpr): Type {
        val targetType = infer(expr.target)
        val valueType = infer(expr.value)
        AssignmentChecker.checkAssignment(expr.target, expr.value, targetType, valueType, env, diagnostics)
        return targetType
    }

    private fun newTypeVarId(): Int = typeVarCounter++

    private fun areTypesCompatible(source: Type, target: Type): Boolean =
        NumericPromotionRules.canPromote(source, target) || source.isAssignableTo(target) ||
            (target is OptionalType && (source is NilLiteralType || source.isAssignableTo(target.elementType))) ||
            (source is OptionalType && source.elementType.isAssignableTo(target))
}
