package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.AstNode

public data class ParserDiagnostic(
    public val severity: DiagnosticSeverity,
    public val message: String,
    public val fileName: String,
    public val line: Int,
    public val column: Int,
    public val startOffset: Int,
    public val endOffset: Int,
    public val hint: String? = null,
) {
    public fun toDiagnostic(): Diagnostic = Diagnostic(
        severity = severity,
        message = message,
        range = SourceRange(
            SourcePosition(line, column, startOffset),
            SourcePosition(line, column, endOffset),
        ),
        fileName = fileName,
        hint = hint,
    )
}
