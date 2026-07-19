package hasab.compiler.semantic

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourceRange

/**
 * Unique diagnostic codes for semantic analysis errors.
 * Each code maps to a specific class of semantic issue.
 *
 * Code format: HSB2xxx (semantic), HSB1xxx (reserved for parser/lexer).
 */
public enum class DiagnosticCode(public val code: String, public val defaultMessage: String) {
    UNDEFINED_VARIABLE("HSB2001", "Undefined variable"),
    DUPLICATE_DECLARATION("HSB2002", "Duplicate declaration"),
    UNDEFINED_TYPE("HSB2003", "Undefined type"),
    UNDEFINED_FUNCTION("HSB2004", "Undefined function"),
    WRONG_ARGUMENT_COUNT("HSB2005", "Wrong number of arguments"),
    TYPE_MISMATCH("HSB2006", "Type mismatch"),
    NOT_CALLABLE("HSB2007", "Expression is not callable"),
    NOT_INDEXABLE("HSB2008", "Expression is not indexable"),
    NO_SUCH_FIELD("HSB2009", "No such field"),
    CANNOT_ASSIGN("HSB2010", "Cannot assign to expression"),
    MUTABILITY_VIOLATION("HSB2011", "Mutability violation"),
    VISIBILITY_ERROR("HSB2012", "Visibility error"),
    UNUSED_VARIABLE("HSB2013", "Unused variable"),
    UNREACHABLE_CODE("HSB2014", "Unreachable code"),
    BREAK_OUTSIDE_LOOP("HSB2015", "'break' outside of loop"),
    CONTINUE_OUTSIDE_LOOP("HSB2016", "'continue' outside of loop"),
    RETURN_TYPE_MISMATCH("HSB2017", "Return type mismatch"),
    MISSING_RETURN("HSB2018", "Missing return value"),
    LOOP_CONDITION_NOT_BOOL("HSB2019", "Loop condition must be 'bool'"),
    CANNOT_ITERATE("HSB2020", "Cannot iterate over expression"),
    DUPLICATE_PARAMETER("HSB2021", "Duplicate parameter name"),
    DUPLICATE_FIELD("HSB2022", "Duplicate field name"),
    DUPLICATE_VARIANT("HSB2023", "Duplicate variant name"),
    EMPTY_MODULE_NAME("HSB2024", "Module name is empty"),
    UNRESOLVED_IMPORT("HSB2025", "Unresolved import"),
    UNRESOLVED_MODULE("HSB2026", "Unresolved module"),
    CIRCULAR_MODULE_DEPENDENCY("HSB2027", "Circular module dependency"),
    SELF_OUTSIDE_IMPL("HSB2028", "'self' used outside of impl block"),
    INVALID_OVERRIDE("HSB2029", "Invalid method override"),
    TRAIT_NOT_SATISFIED("HSB2030", "Trait not fully satisfied"),
    CANNOT_INFER_NIL("HSB2031", "Cannot infer type from nil"),
    RANGE_TYPE_ERROR("HSB2032", "Range operands must be 'int'"),
    BITWISE_TYPE_ERROR("HSB2033", "Bitwise operator requires 'int'"),
    LOGIC_TYPE_ERROR("HSB2034", "Logic operator requires 'bool'"),
    ARITH_TYPE_ERROR("HSB2035", "Arithmetic type mismatch"),
    COMPARE_TYPE_ERROR("HSB2036", "Cannot compare incompatible types"),
    UNARY_TYPE_ERROR("HSB2037", "Unary operator type error"),
    FIELD_ACCESS_ON_NON_STRUCT("HSB2038", "Field access on non-struct type"),
    IMPL_TARGET_NOT_STRUCT("HSB2039", "Impl target is not a struct type"),
    MODULE_NOT_FOUND("HSB2040", "Module not found"),
}

/**
 * A suggested fix for a diagnostic.
 */
public data class DiagnosticFix(
    public val description: String,
    public val startOffset: Int,
    public val endOffset: Int,
    public val replacement: String,
)

/**
 * A single semantic diagnostic with code, location, message, and optional fix.
 */
public data class SemanticDiagnostic(
    val code: DiagnosticCode,
    val severity: DiagnosticSeverity,
    val message: String,
    val range: SourceRange,
    val fileName: String,
    val hint: String? = null,
    val fix: DiagnosticFix? = null,
    val didYouMean: String? = null,
) {
    public fun format(): String {
        val sb = StringBuilder()
        sb.append("$fileName:${range.start.line}:${range.start.column}: ")
        sb.append(if (severity == DiagnosticSeverity.ERROR) "error" else "warning")
        sb.append("[${code.code}]: $message")
        hint?.let { sb.append("\n  hint: $it") }
        didYouMean?.let { sb.append("\n  did you mean: '$it'?") }
        fix?.let { sb.append("\n  fix: ${it.description}") }
        return sb.toString()
    }
}

/**
 * Compute Levenshtein distance between two strings.
 */
public fun levenshteinDistance(a: String, b: String): Int {
    val aLen = a.length
    val bLen = b.length
    val dp = Array(aLen + 1) { IntArray(bLen + 1) }
    for (i in 0..aLen) dp[i][0] = i
    for (j in 0..bLen) dp[0][j] = j
    for (i in 1..aLen) {
        for (j in 1..bLen) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + cost,
            )
        }
    }
    return dp[aLen][bLen]
}

/**
 * Find the closest matching name from a list of candidates.
 * Returns null if the best match is too far away (distance > 2 or > 50% of name length).
 */
public fun didYouMean(name: String, candidates: Collection<String>): String? {
    if (candidates.isEmpty()) return null
    val threshold = maxOf(2, name.length / 2)
    return candidates
        .filter { it != name }
        .map { it to levenshteinDistance(name.lowercase(), it.lowercase()) }
        .filter { it.second <= threshold }
        .minByOrNull { it.second }
        ?.first
}

/**
 * Result of a complete semantic analysis pass.
 * Immutable and thread-safe by construction.
 */
public data class SemanticResult(
    val diagnostics: List<SemanticDiagnostic> = emptyList(),
    val symbolTable: SymbolTable = SymbolTable.EMPTY,
    val scopeTree: Scope? = null,
    val imports: Map<String, ResolvedImport> = emptyMap(),
    val moduleGraph: Map<String, ModuleInfo> = emptyMap(),
) {
    val errors: List<SemanticDiagnostic>
        get() = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }

    val warnings: List<SemanticDiagnostic>
        get() = diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }

    val hasErrors: Boolean
        get() = errors.isNotEmpty()

    val hasWarnings: Boolean
        get() = warnings.isNotEmpty()
}

/**
 * A resolved import mapping a use-path to a module.
 */
public data class ResolvedImport(
    val path: List<String>,
    val moduleName: String,
    val isWildcard: Boolean,
    val range: SourceRange,
    val fileName: String,
)

/**
 * Information about a module in the module graph.
 */
public data class ModuleInfo(
    val name: String,
    val fileName: String,
    val declarations: List<String>,
    val imports: List<String>,
    val isPublic: Boolean,
)
