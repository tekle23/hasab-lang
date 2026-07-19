package hasab.lsp.highlighting

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.AstVisitorBase
import hasab.compiler.frontend.ast.IdentifierExpr
import hasab.compiler.frontend.ast.CallExpr
import hasab.compiler.frontend.ast.FieldAccessExpr
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.LetStmt
import hasab.compiler.frontend.ast.FunctionParam
import hasab.compiler.frontend.ast.Module
import hasab.compiler.frontend.ast.accept
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.semantic.SemanticModel
import hasab.lsp.DocumentState
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightKind
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

public class DocumentHighlightEngine {

    public fun computeHighlights(
        state: DocumentState,
        position: Position,
    ): List<DocumentHighlight> {
        try {
            val parsed = state.parseResult ?: state.parse()
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val module = parsed.module

            val node = findNodeAtPosition(module, position) ?: return emptyList()
            val name = extractName(node) ?: return emptyList()

            val results = mutableListOf<DocumentHighlight>()
            val finder = HighlightFinder(name)

            for (decl in module.declarations) {
                decl.accept(finder)
            }

            for (range in finder.readReferences) {
                results.add(DocumentHighlight(range, DocumentHighlightKind.Read))
            }

            for (range in finder.writeReferences) {
                results.add(DocumentHighlight(range, DocumentHighlightKind.Write))
            }

            if (results.isEmpty() && name.isNotEmpty()) {
                val finder2 = HighlightFinder(name)
                for (decl in module.declarations) {
                    decl.accept(finder2)
                }
                for (range in finder2.allReferences) {
                    results.add(DocumentHighlight(range, DocumentHighlightKind.Text))
                }
            }

            return results
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun extractName(node: AstNode): String? {
        return when (node) {
            is IdentifierExpr -> node.name
            is FnDecl -> node.name
            is LetStmt -> node.name
            is FunctionParam -> node.name
            is FieldAccessExpr -> node.fieldName
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

    private class HighlightFinder(private val name: String) : AstVisitorBase<List<AstNode>>(emptyList()) {
        val readReferences = mutableListOf<Range>()
        val writeReferences = mutableListOf<Range>()
        val allReferences = mutableListOf<Range>()

        private fun toRange(range: SourceRange): Range {
            return Range(
                Position(range.start.line - 1, range.start.column - 1),
                Position(range.end.line - 1, range.end.column - 1),
            )
        }

        override fun visitIdentifier(node: IdentifierExpr): List<AstNode> {
            if (node.name == name) {
                val r = toRange(node.range())
                readReferences.add(r)
                allReferences.add(r)
            }
            return listOf(node)
        }

        override fun visitCall(node: CallExpr): List<AstNode> {
            val callee = node.callee
            if (callee is IdentifierExpr && callee.name == name) {
                val r = toRange(callee.range())
                readReferences.add(r)
                allReferences.add(r)
            }
            for (arg in node.arguments) arg.accept(this)
            return listOf(node)
        }

        override fun visitFieldAccess(node: FieldAccessExpr): List<AstNode> {
            if (node.fieldName == name) {
                val r = toRange(node.range())
                readReferences.add(r)
                allReferences.add(r)
            }
            node.callee.accept(this)
            return listOf(node)
        }

        override fun visitFnDecl(node: FnDecl): List<AstNode> {
            if (node.name == name) {
                val r = toRange(node.range())
                writeReferences.add(r)
                allReferences.add(r)
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitLet(node: LetStmt): List<AstNode> {
            if (node.name == name) {
                val r = toRange(node.range())
                writeReferences.add(r)
                allReferences.add(r)
            }
            node.initializer.accept(this)
            return listOf(node)
        }

        override fun visitStructDecl(node: hasab.compiler.frontend.ast.StructDecl): List<AstNode> {
            if (node.name == name) {
                val r = toRange(node.range())
                writeReferences.add(r)
                allReferences.add(r)
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitEnumDecl(node: hasab.compiler.frontend.ast.EnumDecl): List<AstNode> {
            if (node.name == name) {
                val r = toRange(node.range())
                writeReferences.add(r)
                allReferences.add(r)
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitTraitDecl(node: hasab.compiler.frontend.ast.TraitDecl): List<AstNode> {
            if (node.name == name) {
                val r = toRange(node.range())
                writeReferences.add(r)
                allReferences.add(r)
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }
    }
}
