package hasab.compiler.types

import hasab.compiler.frontend.ast.Module

/**
 * Public API entry point for type checking.
 *
 * Delegates to [TypeCheckerEngine] for the actual work.
 * Backward-compatible — [check] returns a [TypeCheckResult] just as before.
 *
 * Usage:
 * ```kotlin
 * val typeChecker = TypeChecker()
 * val result = typeChecker.check(module)
 * if (result.hasErrors) { ... }
 * ```
 */
public class TypeChecker {

    private val engine = TypeCheckerEngine()

    /**
     * Type-check a module and return a [TypeCheckResult].
     */
    public fun check(module: Module): TypeCheckResult {
        val model = engine.check(module)
        return TypeCheckResult(
            diagnostics = model.diagnostics,
            environment = model.environment,
            typedModel = model,
        )
    }
}

/**
 * Result of type checking a module.
 *
 * @param diagnostics all type-checking diagnostics
 * @param environment the final type environment with all bindings
 * @param typedModel the full typed semantic model with per-node type info
 */
public data class TypeCheckResult(
    val diagnostics: List<TypeDiagnostic>,
    val environment: TypeEnvironment = TypeEnvironment.root(),
    val typedModel: TypedSemanticModel = TypedSemanticModel.empty(),
) {
    val hasErrors: Boolean get() = typedModel.hasErrors
    val errors: List<TypeDiagnostic> get() = typedModel.errors
    val warnings: List<TypeDiagnostic> get() = typedModel.warnings
}
