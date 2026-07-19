package hasab.compiler.types

import hasab.compiler.frontend.ast.FieldAccessExpr

/**
 * Validates field access expressions on structs, enums, and pointers.
 */
public object FieldAccessChecker {

    /**
     * Check field access on a callee of the given [calleeType].
     * Returns the field type, or [UnknownType] if invalid.
     */
    public fun checkFieldAccess(
        expr: FieldAccessExpr,
        calleeType: Type,
        diagnostics: DiagnosticCollector,
    ): Type = when (calleeType) {
        is StructType -> {
            val field = calleeType.fieldByName(expr.fieldName)
            if (field != null) field.type
            else {
                diagnostics.report(
                    TypeDiagnosticCode.NO_SUCH_FIELD,
                    "Struct '${calleeType.name}' has no field '${expr.fieldName}'",
                    expr.range(),
                    expr.fileName,
                    hint = if (calleeType.fields.isNotEmpty()) {
                        "Available fields: ${calleeType.fields.joinToString(", ") { it.name }}"
                    } else null,
                    didYouMean = findClosestField(calleeType.fields.map { it.name }, expr.fieldName),
                )
                UnknownType
            }
        }
        is EnumType -> {
            val variant = calleeType.variantByName(expr.fieldName)
            if (variant != null) FunctionType(variant.fields, calleeType)
            else {
                diagnostics.report(
                    TypeDiagnosticCode.NO_SUCH_FIELD,
                    "Enum '${calleeType.name}' has no variant '${expr.fieldName}'",
                    expr.range(),
                    expr.fileName,
                    hint = if (calleeType.variants.isNotEmpty()) {
                        "Available variants: ${calleeType.variants.joinToString(", ") { it.name }}"
                    } else null,
                    didYouMean = findClosestField(calleeType.variants.map { it.name }, expr.fieldName),
                )
                UnknownType
            }
        }
        is PointerType -> {
            when (val innerType = calleeType.elementType) {
                is StructType -> {
                    val field = innerType.fieldByName(expr.fieldName)
                    if (field != null) field.type
                    else {
                        diagnostics.report(
                            TypeDiagnosticCode.NO_SUCH_FIELD,
                            "Struct '${innerType.name}' has no field '${expr.fieldName}'",
                            expr.range(),
                            expr.fileName,
                            hint = if (innerType.fields.isNotEmpty()) {
                                "Available fields: ${innerType.fields.joinToString(", ") { it.name }}"
                            } else null,
                            didYouMean = findClosestField(innerType.fields.map { it.name }, expr.fieldName),
                        )
                        UnknownType
                    }
                }
                else -> {
                    diagnostics.report(
                        TypeDiagnosticCode.NO_SUCH_FIELD,
                        "Cannot access fields on '${calleeType.displayName}'",
                        expr.range(),
                        expr.fileName,
                        hint = "Dereference the pointer first, or use a struct type",
                    )
                    UnknownType
                }
            }
        }
        else -> {
            diagnostics.report(
                TypeDiagnosticCode.NO_SUCH_FIELD,
                "Cannot access fields on '${calleeType.displayName}'",
                expr.range(),
                expr.fileName,
                hint = "Use a struct type to access fields",
            )
            UnknownType
        }
    }

    /**
     * Find the closest matching name using simple edit distance.
     */
    private fun findClosestField(candidates: List<String>, target: String): String? {
        if (candidates.isEmpty()) return null
        val scored = candidates.map { it to editDistance(it.lowercase(), target.lowercase()) }
            .sortedBy { it.second }
        val best = scored.first()
        return if (best.second <= 2) best.first else null
    }

    private fun editDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.length][b.length]
    }
}
