package hasab.lsp.diagnostics

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.semantic.SemanticDiagnostic
import hasab.lsp.DocumentState
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity as LspSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.jsonrpc.messages.Either

public class DiagnosticEngine {

    public fun computeDiagnostics(state: DocumentState): List<Diagnostic> {
        val result = mutableListOf<Diagnostic>()

        try {
            val fullAnalysis = state.fullAnalysis()

            for (parseDiagnostic in fullAnalysis.parseResult.diagnostics) {
                result.add(convertParseDiagnostic(parseDiagnostic))
            }

            for (semanticDiagnostic in fullAnalysis.semanticModel.diagnostics) {
                result.add(convertSemanticDiagnostic(semanticDiagnostic))
            }

            for (typeDiagnostic in fullAnalysis.typeCheckResult.diagnostics) {
                result.add(convertTypeDiagnostic(typeDiagnostic))
            }
        } catch (e: Exception) {
            result.add(
                Diagnostic().apply {
                    range = Range(Position(0, 0), Position(0, 0))
                    severity = LspSeverity.Error
                    message = "Analysis failed: ${e.message}"
                    source = "hasab-lsp"
                }
            )
        }

        return result
    }

    private fun convertParseDiagnostic(diagnostic: hasab.compiler.frontend.lexer.Diagnostic): Diagnostic {
        return Diagnostic().apply {
            range = sourceRangeToLspRange(diagnostic.range)
            severity = when (diagnostic.severity) {
                DiagnosticSeverity.ERROR -> LspSeverity.Error
                DiagnosticSeverity.WARNING -> LspSeverity.Warning
            }
            message = diagnostic.message
            source = "hasab-parser"
        }
    }

    private fun convertSemanticDiagnostic(diagnostic: SemanticDiagnostic): Diagnostic {
        return Diagnostic().apply {
            range = sourceRangeToLspRange(diagnostic.range)
            severity = when (diagnostic.severity) {
                DiagnosticSeverity.ERROR -> LspSeverity.Error
                DiagnosticSeverity.WARNING -> LspSeverity.Warning
            }
            message = diagnostic.message + (diagnostic.hint?.let { "\nHint: $it" } ?: "")
            source = "hasab-semantic"
            code = Either.forLeft(diagnostic.code.code)
            diagnostic.fix?.let { fix ->
                data = mapOf(
                    "diagnosticId" to diagnostic.code.name,
                    "fixDescription" to fix.description,
                    "fixReplacement" to fix.replacement,
                    "fixStartOffset" to fix.startOffset,
                    "fixEndOffset" to fix.endOffset,
                )
            }
        }
    }

    private fun convertTypeDiagnostic(diagnostic: hasab.compiler.types.TypeDiagnostic): Diagnostic {
        return Diagnostic().apply {
            range = sourceRangeToLspRange(diagnostic.range)
            severity = when (diagnostic.severity) {
                DiagnosticSeverity.ERROR -> LspSeverity.Error
                DiagnosticSeverity.WARNING -> LspSeverity.Warning
            }
            message = diagnostic.message
            source = "hasab-typechecker"
            code = Either.forLeft(diagnostic.code.name)
        }
    }

    private fun sourceRangeToLspRange(range: SourceRange): Range {
        return Range(
            Position(range.start.line - 1, range.start.column - 1),
            Position(range.end.line - 1, range.end.column - 1),
        )
    }
}
