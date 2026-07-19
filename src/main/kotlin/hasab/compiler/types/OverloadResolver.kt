package hasab.compiler.types

/**
 * Resolves the best matching overload for a function call when multiple
 * function signatures exist for the same name.
 *
 * Selection algorithm:
 * 1. Exact match: all argument types match parameter types exactly.
 * 2. Promotion match: all argument types are compatible via numeric promotion.
 * 3. Coercion match: argument types are assignable with implicit conversions.
 * 4. If no unique match, report ambiguity.
 */
public object OverloadResolver {

    /**
     * Select the best matching [FunctionType] from a list of overloads.
     *
     * @param overloads available function signatures
     * @param argumentTypes types of the call arguments
     * @return the best match, or null if no match found / ambiguous
     */
    public fun resolve(overloads: List<FunctionType>, argumentTypes: List<Type>): ResolutionResult {
        if (overloads.isEmpty()) return ResolutionResult.NoMatch
        if (overloads.size == 1) {
            return if (matchesWithPromotion(overloads[0], argumentTypes)) {
                ResolutionResult.SingleMatch(overloads[0])
            } else {
                ResolutionResult.NoMatch
            }
        }

        val exactMatches = overloads.filter { matchesExact(it, argumentTypes) }
        if (exactMatches.size == 1) return ResolutionResult.SingleMatch(exactMatches[0])

        val promotionMatches = overloads.filter { matchesWithPromotion(it, argumentTypes) }
        return when {
            promotionMatches.size == 1 -> ResolutionResult.SingleMatch(promotionMatches[0])
            promotionMatches.size > 1 -> ResolutionResult.Ambiguous(promotionMatches)
            exactMatches.size > 1 -> ResolutionResult.Ambiguous(exactMatches)
            else -> ResolutionResult.NoMatch
        }
    }

    /**
     * Check if argument types exactly match parameter types.
     */
    private fun matchesExact(fn: FunctionType, argumentTypes: List<Type>): Boolean {
        if (fn.parameterTypes.size != argumentTypes.size) return false
        return fn.parameterTypes.zip(argumentTypes).all { (param, arg) -> param == arg }
    }

    /**
     * Check if argument types match via numeric promotion.
     */
    private fun matchesWithPromotion(fn: FunctionType, argumentTypes: List<Type>): Boolean {
        if (fn.parameterTypes.size != argumentTypes.size) return false
        return fn.parameterTypes.zip(argumentTypes).all { (param, arg) ->
            arg.isAssignableTo(param) || NumericPromotionRules.canPromote(arg, param)
        }
    }

    /**
     * Check if argument types match with any implicit conversion.
     */
    private fun matchesWithCoercion(fn: FunctionType, argumentTypes: List<Type>): Boolean {
        if (fn.parameterTypes.size != argumentTypes.size) return false
        return fn.parameterTypes.zip(argumentTypes).all { (param, arg) ->
            areTypesCompatible(arg, param)
        }
    }

    private fun areTypesCompatible(source: Type, target: Type): Boolean {
        if (source.isAssignableTo(target)) return true
        if (NumericPromotionRules.canPromote(source, target)) return true
        if (target is OptionalType) return source is NilLiteralType || source.isAssignableTo(target.elementType)
        return false
    }

    /**
     * Result of overload resolution.
     */
    public sealed interface ResolutionResult {
        public data class SingleMatch(val function: FunctionType) : ResolutionResult
        public data class Ambiguous(val candidates: List<FunctionType>) : ResolutionResult
        public data object NoMatch : ResolutionResult
    }
}
