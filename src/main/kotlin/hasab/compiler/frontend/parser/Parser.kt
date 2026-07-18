package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.Module

public class Parser(
    private val lexerResult: LexerResult,
) {
    private val diagnostics: MutableList<ParserDiagnostic> = mutableListOf()

    public fun parse(): ParseResult {
        val stream = TokenStream(lexerResult.tokens)
        val moduleParser = ModuleParser(stream, diagnostics)
        val module = moduleParser.parseModule()

        val allDiagnostics = lexerResult.diagnostics.map { it } +
            diagnostics.map { it.toDiagnostic() }

        return ParseResult(
            module = module,
            diagnostics = allDiagnostics,
        )
    }
}

public data class ParseResult(
    val module: Module,
    val diagnostics: List<Diagnostic>,
) {
    val errors: List<Diagnostic>
        get() = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }

    val warnings: List<Diagnostic>
        get() = diagnostics.filter { it.severity == DiagnosticSeverity.WARNING }

    val hasErrors: Boolean get() = errors.isNotEmpty()
}
