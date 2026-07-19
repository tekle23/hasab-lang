package hasab.lsp.codeaction

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.AstVisitorBase
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.IdentifierExpr
import hasab.compiler.frontend.ast.Module
import hasab.compiler.frontend.ast.UseDecl
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.semantic.DiagnosticCode
import hasab.lsp.DocumentState
import hasab.lsp.formatting.FormattingEngine
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionKind
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit

public class CodeActionEngine(
    private val formattingEngine: FormattingEngine,
) {

    public fun computeCodeActions(
        state: DocumentState,
        range: Range,
        diagnostics: List<Diagnostic>,
    ): List<CodeAction> {
        val result = mutableListOf<CodeAction>()

        for (diagnostic in diagnostics) {
            result.addAll(fixActionsForDiagnostic(state, diagnostic))
        }

        result.addAll(extractActions(state, range))
        result.addAll(organizeImportsActions(state))

        return result
    }

    private fun fixActionsForDiagnostic(state: DocumentState, diagnostic: Diagnostic): List<CodeAction> {
        val actions = mutableListOf<CodeAction>()

        val data = diagnostic.data as? Map<*, *> ?: return actions
        val fixDescription = data["fixDescription"] as? String ?: return actions
        val fixReplacement = data["fixReplacement"] as? String ?: return actions
        val fixStartOffset = (data["fixStartOffset"] as? Number)?.toInt() ?: return actions
        val fixEndOffset = (data["fixEndOffset"] as? Number)?.toInt() ?: return actions

        val start = offsetToPosition(state.content, fixStartOffset)
        val end = offsetToPosition(state.content, fixEndOffset)

        if (start != null && end != null) {
            actions.add(
                CodeAction().apply {
                    title = fixDescription
                    kind = CodeActionKind.QuickFix
                    diagnostics = listOf(diagnostic)
                    edit = WorkspaceEdit(
                        mapOf(
                            state.uri to listOf(
                                TextEdit(Range(start, end), fixReplacement),
                            )
                        )
                    )
                }
            )
        }

        val code = data["diagnosticId"] as? String
        if (code != null) {
            when {
                code.contains("UNDEFINED_VARIABLE") || code.contains("HSB2001") -> {
                    val name = extractUndeclaredName(state, diagnostic.range)
                    if (name != null) {
                        actions.add(
                            CodeAction().apply {
                                title = "Declare variable '$name'"
                                kind = CodeActionKind.QuickFix
                                diagnostics = listOf(diagnostic)
                                edit = WorkspaceEdit(
                                    mapOf(
                                        state.uri to listOf(
                                            TextEdit(
                                                diagnostic.range,
                                                "let $name = TODO()\n",
                                            )
                                        )
                                    )
                                )
                            }
                        )
                    }
                }
                code.contains("DUPLICATE_DECLARATION") || code.contains("HSB2002") -> {
                    actions.add(
                        CodeAction().apply {
                            title = "Rename to avoid duplicate"
                            kind = CodeActionKind.QuickFix
                            diagnostics = listOf(diagnostic)
                        }
                    )
                }
            }
        }

        return actions
    }

    private fun extractActions(state: DocumentState, range: Range): List<CodeAction> {
        val actions = mutableListOf<CodeAction>()

        if (range.start.line == range.end.line) return actions

        val content = state.content
        val lines = content.lines()
        val selectedText = lines.subList(
            range.start.line.coerceIn(0, lines.size - 1),
            (range.end.line + 1).coerceIn(0, lines.size),
        ).joinToString("\n").trim()

        if (selectedText.isNotEmpty()) {
            actions.add(
                CodeAction().apply {
                    title = "Extract to function"
                    kind = CodeActionKind.RefactorExtract
                    edit = WorkspaceEdit(
                        mapOf(
                            state.uri to listOf(
                                TextEdit(
                                    range,
                                    "fn extracted() {\n$selectedText\n}\n",
                                )
                            )
                        )
                    )
                }
            )
        }

        return actions
    }

    private fun organizeImportsActions(state: DocumentState): List<CodeAction> {
        val actions = mutableListOf<CodeAction>()
        try {
            val parsed = state.parseResult ?: state.parse()
            val module = parsed.module

            val imports = mutableListOf<UseDecl>()
            val nonImports = mutableListOf<AstNode>()

            for (decl in module.declarations) {
                if (decl is UseDecl) imports.add(decl) else nonImports.add(decl)
            }

            if (imports.size <= 1) return actions

            val sortedImports = imports.sortedBy { it.path.joinToString(".") }
            val currentOrder = imports.map { it.path.joinToString(".") }
            val sortedOrder = sortedImports.map { it.path.joinToString(".") }

            if (currentOrder == sortedOrder) return actions

            val importLines = sortedImports.joinToString("\n") { "use ${it.path.joinToString("::")};" }

            actions.add(
                CodeAction().apply {
                    title = "Organize imports"
                    kind = CodeActionKind.SourceOrganizeImports
                    edit = WorkspaceEdit(
                        mapOf(
                            state.uri to listOf(
                                TextEdit(
                                    Range(Position(0, 0), Position(imports.last().line, 0)),
                                    "$importLines\n\n",
                                )
                            )
                        )
                    )
                }
            )
        } catch (_: Exception) {}

        return actions
    }

    private fun extractUndeclaredName(state: DocumentState, range: Range): String? {
        val content = state.content
        val lines = content.lines()
        val line = lines.getOrNull(range.start.line) ?: return null
        val col = range.start.character
        if (col >= line.length) return null
        val rest = line.substring(col)
        val match = Regex("^[a-zA-Z_][a-zA-Z0-9_]*").find(rest) ?: return null
        return match.value
    }

    private fun offsetToPosition(content: String, offset: Int): Position? {
        var line = 0
        var col = 0
        for (i in 0 until offset.coerceAtMost(content.length)) {
            if (content[i] == '\n') {
                line++
                col = 0
            } else {
                col++
            }
        }
        return Position(line, col)
    }
}
