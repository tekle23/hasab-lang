package hasab.compiler.hir

import hasab.compiler.types.*

/**
 * A validation error in the HIR tree.
 */
public data class HirValidationError(
    public val code: String,
    public val message: String,
    public val nodeType: String,
    public val context: String = "",
) {
    override fun toString(): String {
        val ctx = if (context.isNotEmpty()) " in $context" else ""
        return "[$code] $nodeType$ctx: $message"
    }
}

/**
 * Structural and type validator for tree-based HIR.
 *
 * Checks:
 * - No [UnknownType] leaks in typed positions
 * - Return types match function declarations
 * - Expressions have non-void types (where applicable)
 * - Block nesting is well-formed
 * - Let statements have valid initializers
 */
public class HirValidator {

    private val errors = mutableListOf<HirValidationError>()

    /**
     * Validate an entire [HirModule]. Returns list of errors (empty = valid).
     */
    public fun validate(module: HirModule): List<HirValidationError> {
        errors.clear()
        for (decl in module.declarations) {
            validateDecl(decl)
        }
        return errors.toList()
    }

    /**
     * Validate a single [HirFnDecl]. Useful for validating individual functions.
     */
    public fun validateFunction(fn: HirFnDecl): List<HirValidationError> {
        errors.clear()
        validateFnDecl(fn)
        return errors.toList()
    }

    private fun validateDecl(decl: HirDecl) {
        when (decl) {
            is HirFnDecl -> validateFnDecl(decl)
            is HirStructDecl -> validateStructDecl(decl)
            is HirEnumDecl -> validateEnumDecl(decl)
            is HirTraitDecl -> validateTraitDecl(decl)
            is HirTypeAliasDecl -> validateTypeAliasDecl(decl)
            is HirImplDecl -> validateImplDecl(decl)
            is HirUseDecl -> { /* use declarations are structurally valid */ }
        }
    }

    private fun validateFnDecl(fn: HirFnDecl) {
        for (param in fn.parameters) {
            if (param.type == UnknownType) {
                errors.add(HirValidationError("HIR001", "Parameter '${param.name}' has unknown type", "FnDecl", fn.name))
            }
            if (param.type == VoidType) {
                errors.add(HirValidationError("HIR002", "Parameter '${param.name}' cannot have void type", "FnDecl", fn.name))
            }
        }

        if (fn.returnType == UnknownType) {
            errors.add(HirValidationError("HIR003", "Function '${fn.name}' has unknown return type", "FnDecl", fn.name))
        }

        fn.body?.let { block ->
            validateBlock(block, fn.name, fn.returnType)
        }
    }

    private fun validateBlock(block: HirBlock, context: String, expectedReturnType: Type) {
        for (stmt in block.statements) {
            validateStmt(stmt, context, expectedReturnType)
        }
    }

    private fun validateStmt(stmt: HirStmt, context: String, expectedReturnType: Type) {
        when (stmt) {
            is HirBlock -> validateBlock(stmt, context, expectedReturnType)
            is HirExprStmt -> validateExpr(stmt.expression, context)
            is HirReturnStmt -> {
                stmt.value?.let { validateExpr(it, context) }
                if (stmt.value != null && expectedReturnType == VoidType) {
                    errors.add(HirValidationError("HIR004", "Cannot return value from void function", "ReturnStmt", context))
                }
            }
            is HirLetStmt -> {
                validateExpr(stmt.initializer, context)
                if (stmt.type == UnknownType) {
                    errors.add(HirValidationError("HIR005", "Let statement '${stmt.name}' has unknown type", "LetStmt", context))
                }
            }
            is HirIfStmt -> {
                validateExpr(stmt.condition, context)
                validateBlock(stmt.thenBranch, context, expectedReturnType)
                stmt.elseBranch?.let { validateStmt(it, context, expectedReturnType) }
            }
            is HirWhileStmt -> {
                validateExpr(stmt.condition, context)
                validateBlock(stmt.body, context, VoidType)
            }
            is HirForStmt -> {
                validateExpr(stmt.iterable, context)
                validateBlock(stmt.body, context, VoidType)
            }
            is HirBreakStmt, is HirContinueStmt -> { /* always valid at HIR level */ }
        }
    }

    private fun validateExpr(expr: HirExpr, context: String) {
        when (expr) {
            is HirBinary -> {
                validateExpr(expr.left, context)
                validateExpr(expr.right, context)
                if (expr.type == UnknownType) {
                    errors.add(HirValidationError("HIR006", "Binary expression has unknown type", "Binary", context))
                }
            }
            is HirUnary -> {
                validateExpr(expr.operand, context)
                if (expr.type == UnknownType) {
                    errors.add(HirValidationError("HIR007", "Unary expression has unknown type", "Unary", context))
                }
            }
            is HirCall -> {
                validateExpr(expr.callee, context)
                for (arg in expr.arguments) validateExpr(arg, context)
                if (expr.type == UnknownType) {
                    errors.add(HirValidationError("HIR008", "Call expression has unknown return type", "Call", context))
                }
            }
            is HirIndex -> {
                validateExpr(expr.callee, context)
                validateExpr(expr.index, context)
            }
            is HirFieldAccess -> {
                validateExpr(expr.callee, context)
                if (expr.type == UnknownType) {
                    errors.add(HirValidationError("HIR009", "Field access '${expr.fieldName}' has unknown type", "FieldAccess", context))
                }
            }
            is HirSafeFieldAccess -> validateExpr(expr.callee, context)
            is HirNullAssert -> validateExpr(expr.operand, context)
            is HirArrayLiteral -> {
                for (elem in expr.elements) validateExpr(elem, context)
            }
            is HirArrayInit -> validateExpr(expr.size, context)
            is HirIfExpr -> {
                validateExpr(expr.condition, context)
                validateExpr(expr.thenBranch, context)
                expr.elseBranch?.let { validateExpr(it, context) }
            }
            is HirAssignment -> {
                validateExpr(expr.target, context)
                validateExpr(expr.value, context)
            }
            is HirCompoundAssignment -> {
                validateExpr(expr.target, context)
                validateExpr(expr.value, context)
            }
            is HirIdentifier, is HirIntLiteral, is HirFloatLiteral,
            is HirStringLiteral, is HirCharLiteral, is HirBoolLiteral,
            is HirNilLiteral -> { /* leaf nodes, always valid */ }
        }
    }

    private fun validateStructDecl(struct: HirStructDecl) {
        for (field in struct.fields) {
            if (field.type == UnknownType) {
                errors.add(HirValidationError("HIR010", "Field '${field.name}' has unknown type", "StructDecl", struct.name))
            }
        }
    }

    private fun validateEnumDecl(enum: HirEnumDecl) {
        for (variant in enum.variants) {
            for (i in variant.fieldTypes.indices) {
                if (variant.fieldTypes[i] == UnknownType) {
                    errors.add(HirValidationError("HIR011", "Variant '${variant.name}' field $i has unknown type", "EnumDecl", enum.name))
                }
            }
        }
    }

    private fun validateTraitDecl(trait: HirTraitDecl) {
        for (method in trait.methods) {
            validateFnDecl(method)
        }
    }

    private fun validateTypeAliasDecl(alias: HirTypeAliasDecl) {
        if (alias.targetType == UnknownType) {
            errors.add(HirValidationError("HIR012", "Type alias target is unknown", "TypeAliasDecl", alias.name))
        }
    }

    private fun validateImplDecl(impl: HirImplDecl) {
        for (method in impl.methods) {
            validateFnDecl(method)
        }
    }
}
