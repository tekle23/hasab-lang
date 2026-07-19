package hasab.compiler.types

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourceRange

/**
 * Thread-safe collector for type-checking diagnostics.
 *
 * Used by all checker components to report errors and warnings
 * without needing direct access to the model.
 */
public class DiagnosticCollector {

    private val _diagnostics: MutableList<TypeDiagnostic> = mutableListOf()

    /**
     * Report a type error with full context.
     */
    public fun report(
        code: TypeDiagnosticCode,
        message: String,
        range: SourceRange,
        fileName: String,
        expectedType: Type? = null,
        foundType: Type? = null,
        suggestion: String? = null,
        hint: String? = null,
        didYouMean: String? = null,
        relatedLocation: SourceRange? = null,
    ) {
        _diagnostics.add(TypeDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = range,
            fileName = fileName,
            expectedType = expectedType,
            foundType = foundType,
            suggestion = suggestion,
            hint = hint,
            didYouMean = didYouMean,
            relatedLocation = relatedLocation,
        ))
    }

    /**
     * Report a type error using default message from the code.
     */
    public fun reportCode(
        code: TypeDiagnosticCode,
        range: SourceRange,
        fileName: String,
        expectedType: Type? = null,
        foundType: Type? = null,
        suggestion: String? = null,
    ) {
        val message = if (expectedType != null && foundType != null) {
            "${code.defaultMessage}: expected '${expectedType.displayName}', found '${foundType.displayName}'"
        } else {
            code.defaultMessage
        }
        report(code, message, range, fileName, expectedType, foundType, suggestion)
    }

    /**
     * Report a type warning.
     */
    public fun warn(
        code: TypeDiagnosticCode,
        message: String,
        range: SourceRange,
        fileName: String,
        hint: String? = null,
    ) {
        _diagnostics.add(TypeDiagnostic(
            code = code,
            severity = DiagnosticSeverity.WARNING,
            message = message,
            range = range,
            fileName = fileName,
            hint = hint,
        ))
    }

    /**
     * Get all collected diagnostics.
     */
    public fun diagnostics(): List<TypeDiagnostic> = _diagnostics.toList()

    /**
     * Check if any errors were reported.
     */
    public fun hasErrors(): Boolean = _diagnostics.any { it.severity == DiagnosticSeverity.ERROR }

    /**
     * Clear all collected diagnostics.
     */
    public fun clear() { _diagnostics.clear() }
}
