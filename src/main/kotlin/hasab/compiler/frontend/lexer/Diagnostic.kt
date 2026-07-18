package hasab.compiler.frontend.lexer

public enum class DiagnosticSeverity {
    WARNING,
    ERROR,
}

public data class Diagnostic(
    public val severity: DiagnosticSeverity,
    public val message: String,
    public val range: SourceRange,
    public val fileName: String,
    public val hint: String? = null,
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$fileName:${range.start}: ${severity.name.lowercase()}: $message")
        if (hint != null) {
            sb.append("\n  hint: $hint")
        }
        return sb.toString()
    }
}
