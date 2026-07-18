package hasab.compiler.types

import hasab.compiler.frontend.lexer.Diagnostic
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourceRange

public sealed interface TypeDiagnostic {
    public val message: String
    public val range: SourceRange
    public val fileName: String
    public val severity: DiagnosticSeverity
    public val hint: String?

    public fun toDiagnostic(): Diagnostic = Diagnostic(
        severity = severity,
        message = message,
        range = range,
        fileName = fileName,
        hint = hint,
    )
}

public data class TypeError(
    override val message: String,
    override val range: SourceRange,
    override val fileName: String,
    override val hint: String? = null,
) : TypeDiagnostic {
    override val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR
}

public data class TypeWarning(
    override val message: String,
    override val range: SourceRange,
    override val fileName: String,
    override val hint: String? = null,
) : TypeDiagnostic {
    override val severity: DiagnosticSeverity = DiagnosticSeverity.WARNING
}

public data class TypeCheckResult(
    val diagnostics: List<TypeDiagnostic>,
) {
    val hasErrors: Boolean get() = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }
    val errors: List<TypeDiagnostic> get() = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }
    val warnings: List<TypeDiagnostic> get() = diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }
}
