package hasab.compiler.types

/**
 * Checker for generic type support.
 *
 * Handles:
 * - Type variable tracking
 * - Basic generic function signatures (e.g., `fn identity<T>(x: T) -> T`)
 * - Type parameter substitution in function calls
 * - Trait bound constraint validation
 */
public class GenericTypeChecker(private val diagnostics: DiagnosticCollector) {

    private var nextVarId = 0

    /**
     * Create a fresh type variable for a generic parameter.
     */
    public fun freshTypeVariable(name: String = "?${nextVarId++}", bounds: List<Type> = emptyList()): TypeVariable {
        return TypeVariable(nextVarId - 1, name, bounds)
    }

    /**
     * Substitute type variables in a function type based on call argument types.
     *
     * For example, if the function is `fn identity<T>(x: T) -> T` and the argument
     * is `42`, this returns `FunctionType([IntType], IntType)`.
     */
    public fun substituteFunctionType(
        fnType: FunctionType,
        argumentTypes: List<Type>,
    ): SubstitutionResult {
        val bindings = mutableMapOf<Int, Type>()

        if (fnType.parameterTypes.size != argumentTypes.size) {
            return SubstitutionResult.Failure("Argument count mismatch")
        }

        for ((paramType, argType) in fnType.parameterTypes.zip(argumentTypes)) {
            val result = bindTypeVariable(paramType, argType, bindings)
            if (result is SubstitutionResult.Failure) return result
        }

        val resolvedParams = fnType.parameterTypes.map { substituteInType(it, bindings) }
        val resolvedReturn = substituteInType(fnType.returnType, bindings)

        return SubstitutionResult.Success(FunctionType(resolvedParams, resolvedReturn))
    }

    /**
     * Validate that a concrete type satisfies all trait bounds of a type variable.
     * Returns true if all bounds are satisfied.
     */
    public fun validateBounds(
        typeVariable: TypeVariable,
        concreteType: Type,
        range: hasab.compiler.frontend.lexer.SourceRange,
        fileName: String,
    ): Boolean {
        if (typeVariable.bounds.isEmpty()) return true
        if (concreteType is UnknownType || concreteType is TypeVariable) return true

        var allSatisfied = true
        for (bound in typeVariable.bounds) {
            if (!satisfiesBound(concreteType, bound)) {
                diagnostics.report(
                    TypeDiagnosticCode.GENERIC_CONSTRAINT,
                    "Type '${concreteType.displayName}' does not satisfy bound '${bound.displayName}' for '${typeVariable.name}'",
                    range,
                    fileName,
                    expectedType = bound,
                    foundType = concreteType,
                    suggestion = "Ensure '${concreteType.displayName}' implements '${bound.displayName}'",
                )
                allSatisfied = false
            }
        }
        return allSatisfied
    }

    /**
     * Check if [actualType] satisfies the given [bound] type.
     * For trait bounds, this is structural — checks that the type has the required methods.
     */
    private fun satisfiesBound(actualType: Type, bound: Type): Boolean {
        if (actualType == bound) return true
        if (bound is TraitType && actualType is StructType) {
            for (requiredMethod in bound.methods) {
                val hasField = actualType.fields.any { it.name == requiredMethod.name }
                if (!hasField) return false
            }
            return true
        }
        return false
    }

    private fun bindTypeVariable(
        pattern: Type,
        actual: Type,
        bindings: MutableMap<Int, Type>,
    ): SubstitutionResult {
        return when (pattern) {
            is TypeVariable -> {
                val existing = bindings[pattern.id]
                if (existing != null) {
                    if (existing == actual) SubstitutionResult.Success(actual)
                    else SubstitutionResult.Failure(
                        "Type variable '${pattern.name}' cannot be both '${existing.displayName}' and '${actual.displayName}'"
                    )
                } else {
                    bindings[pattern.id] = actual
                    SubstitutionResult.Success(actual)
                }
            }
            is ArrayType -> if (actual is ArrayType) {
                bindTypeVariable(pattern.elementType, actual.elementType, bindings)
            } else SubstitutionResult.Failure("Expected array type, got '${actual.displayName}'")
            is PointerType -> if (actual is PointerType) {
                bindTypeVariable(pattern.elementType, actual.elementType, bindings)
            } else SubstitutionResult.Failure("Expected pointer type, got '${actual.displayName}'")
            is OptionalType -> if (actual is OptionalType) {
                bindTypeVariable(pattern.elementType, actual.elementType, bindings)
            } else if (actual is NilLiteralType) {
                SubstitutionResult.Success(NilLiteralType)
            } else SubstitutionResult.Failure("Expected optional type, got '${actual.displayName}'")
            else -> if (pattern == actual || pattern is UnknownType) {
                SubstitutionResult.Success(actual)
            } else SubstitutionResult.Failure(
                "Expected '${pattern.displayName}', got '${actual.displayName}'"
            )
        }
    }

    private fun substituteInType(type: Type, bindings: Map<Int, Type>): Type = when (type) {
        is TypeVariable -> bindings[type.id] ?: type
        is ArrayType -> ArrayType(substituteInType(type.elementType, bindings))
        is PointerType -> PointerType(substituteInType(type.elementType, bindings))
        is OptionalType -> OptionalType(substituteInType(type.elementType, bindings))
        is FunctionType -> FunctionType(
            type.parameterTypes.map { substituteInType(it, bindings) },
            substituteInType(type.returnType, bindings),
        )
        else -> type
    }

    /**
     * Result of type substitution.
     */
    public sealed interface SubstitutionResult {
        public data class Success(val resolvedType: Type) : SubstitutionResult
        public data class Failure(val message: String) : SubstitutionResult
    }
}
