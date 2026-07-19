package hasab.lsp.rename

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.AstVisitorBase
import hasab.compiler.frontend.ast.IdentifierExpr
import hasab.compiler.frontend.ast.CallExpr
import hasab.compiler.frontend.ast.FieldAccessExpr
import hasab.compiler.frontend.ast.SafeFieldAccessExpr
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.LetStmt
import hasab.compiler.frontend.ast.FunctionParam
import hasab.compiler.frontend.ast.StructDecl
import hasab.compiler.frontend.ast.EnumDecl
import hasab.compiler.frontend.ast.TraitDecl
import hasab.compiler.frontend.ast.TypeAliasDecl
import hasab.compiler.frontend.ast.Module
import hasab.compiler.frontend.ast.accept
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.Symbol
import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit

public class RenameEngine(
    private val workspaceIndex: WorkspaceIndex,
) {

    public fun prepareRename(
        state: DocumentState,
        position: Position,
    ): Boolean {
        try {
            val parsed = state.parseResult ?: state.parse()
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val module = parsed.module

            val node = findNodeAtPosition(module, position) ?: return false
            val name = extractName(node) ?: return false

            return name.isNotEmpty() && name.all { it.isLetterOrDigit() || it == '_' }
        } catch (_: Exception) {
            return false
        }
    }

    public fun computeRename(
        state: DocumentState,
        position: Position,
        newName: String,
    ): WorkspaceEdit? {
        if (!newName.all { it.isLetterOrDigit() || it == '_' }) return null
        if (newName.isEmpty() || newName[0].isDigit()) return null

        try {
            val parsed = state.parseResult ?: state.parse()
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val module = parsed.module

            val node = findNodeAtPosition(module, position) ?: return null
            val oldName = extractName(node) ?: return null

            val edits = mutableListOf<TextEdit>()

            val fileEdits = findOccurrences(module, oldName, newName, state.uri)
            edits.addAll(fileEdits)

            for ((uri, docState) in workspaceIndex.getAllDocuments()) {
                if (uri == state.uri) continue
                try {
                    val otherParsed = docState.parseResult ?: docState.parse()
                    val otherEdits = findOccurrences(otherParsed.module, oldName, newName, uri)
                    edits.addAll(otherEdits)
                } catch (_: Exception) {}
            }

            val changes = mutableMapOf<String, List<TextEdit>>()
            changes[state.uri] = edits.filter { it.range.start.line >= 0 }

            val edit = WorkspaceEdit()
            edit.changes = changes
            return edit
        } catch (_: Exception) {
            return null
        }
    }

    private fun findOccurrences(module: Module, oldName: String, newName: String, uri: String): List<TextEdit> {
        val result = mutableListOf<TextEdit>()
        val finder = OccurrenceFinder(oldName)

        for (decl in module.declarations) {
            decl.accept(finder)
        }

        for (range in finder.occurrences) {
            result.add(TextEdit(
                Range(
                    Position(range.start.line - 1, range.start.column - 1),
                    Position(range.end.line - 1, range.end.column - 1),
                ),
                newName,
            ))
        }

        return result
    }

    private fun extractName(node: AstNode): String? {
        return when (node) {
            is IdentifierExpr -> node.name
            is FnDecl -> node.name
            is LetStmt -> node.name
            is FunctionParam -> node.name
            is StructDecl -> node.name
            is EnumDecl -> node.name
            is TraitDecl -> node.name
            is TypeAliasDecl -> node.name
            else -> null
        }
    }

    private fun findNodeAtPosition(module: AstNode, position: Position): AstNode? {
        val targetLine = position.line + 1
        val targetCol = position.character + 1
        var best: AstNode? = null
        var bestScore = Int.MAX_VALUE

        fun walk(node: AstNode) {
            val startDiff = kotlin.math.abs(node.column - targetCol)
            if (node.line == targetLine && startDiff < bestScore) {
                bestScore = startDiff
                best = node
            }
            for (child in node.children()) walk(child)
        }

        walk(module)
        return best
    }

    private class OccurrenceFinder(private val name: String) : AstVisitorBase<List<AstNode>>(emptyList()) {
        val occurrences = mutableListOf<SourceRange>()

        override fun visitIdentifier(node: IdentifierExpr): List<AstNode> {
            if (node.name == name) occurrences.add(node.range())
            return listOf(node)
        }

        override fun visitCall(node: CallExpr): List<AstNode> {
            val callee = node.callee
            if (callee is IdentifierExpr && callee.name == name) {
                occurrences.add(callee.range())
            }
            for (arg in node.arguments) arg.accept(this)
            return listOf(node)
        }

        override fun visitFieldAccess(node: FieldAccessExpr): List<AstNode> {
            if (node.fieldName == name) occurrences.add(node.range())
            node.callee.accept(this)
            return listOf(node)
        }

        override fun visitFnDecl(node: FnDecl): List<AstNode> {
            if (node.name == name) occurrences.add(node.range())
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitLet(node: LetStmt): List<AstNode> {
            if (node.name == name) occurrences.add(node.range())
            node.initializer.accept(this)
            return listOf(node)
        }

        override fun visitStructDecl(node: StructDecl): List<AstNode> {
            if (node.name == name) occurrences.add(node.range())
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitEnumDecl(node: EnumDecl): List<AstNode> {
            if (node.name == name) occurrences.add(node.range())
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitTraitDecl(node: TraitDecl): List<AstNode> {
            if (node.name == name) occurrences.add(node.range())
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitTypeAlias(node: TypeAliasDecl): List<AstNode> {
            if (node.name == name) occurrences.add(node.range())
            return listOf(node)
        }
    }
}
