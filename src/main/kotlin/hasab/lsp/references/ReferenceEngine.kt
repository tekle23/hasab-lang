package hasab.lsp.references

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
import hasab.compiler.frontend.ast.Module
import hasab.compiler.frontend.ast.accept
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.Symbol
import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceContext

public class ReferenceEngine(
    private val workspaceIndex: WorkspaceIndex,
) {

    public fun findReferences(
        state: DocumentState,
        position: Position,
        includeDeclaration: Boolean = true,
    ): List<Location> {
        try {
            val parsed = state.parseResult ?: state.parse()
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val module = parsed.module

            val node = findNodeAtPosition(module, position) ?: return emptyList()
            val symbol = semanticModel.bindingFor(node)

            if (symbol != null) {
                return findSymbolReferences(symbol, state, includeDeclaration)
            }

            val name = extractName(node) ?: return emptyList()
            return findByNameReferences(module, name, state, includeDeclaration)
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun findSymbolReferences(
        symbol: Symbol,
        state: DocumentState,
        includeDeclaration: Boolean,
    ): List<Location> {
        val result = mutableListOf<Location>()

        if (includeDeclaration) {
            result.add(symbolToLocation(symbol, symbol.fileName))
        }

        val usages = workspaceIndex.findUsages(symbol.name)
        for (usage in usages) {
            result.add(Location(
                usage.uri,
                Range(
                    Position(usage.range.start.line - 1, usage.range.start.column - 1),
                    Position(usage.range.end.line - 1, usage.range.end.column - 1),
                )
            ))
        }

        return result
    }

    private fun findByNameReferences(
        module: Module,
        name: String,
        state: DocumentState,
        includeDeclaration: Boolean,
    ): List<Location> {
        val result = mutableListOf<Location>()
        val finder = ReferenceFinder(name)

        for (decl in module.declarations) {
            decl.accept(finder)
        }

        for (ref in finder.references) {
            result.add(Location(
                state.uri,
                Range(
                    Position(ref.start.line - 1, ref.start.column - 1),
                    Position(ref.end.line - 1, ref.end.column - 1),
                )
            ))
        }

        if (includeDeclaration) {
            val decl = findDeclarationForName(module, name)
            if (decl != null) {
                result.add(Location(
                    state.uri,
                    Range(
                        Position(decl.line - 1, decl.column - 1),
                        Position(decl.line - 1, decl.column - 1 + (decl.endOffset - decl.startOffset).coerceAtLeast(1)),
                    )
                ))
            }
        }

        return result
    }

    private fun findDeclarationForName(module: Module, name: String): AstNode? {
        for (decl in module.declarations) {
            when (decl) {
                is FnDecl -> if (decl.name == name) return decl
                is StructDecl -> if (decl.name == name) return decl
                is EnumDecl -> if (decl.name == name) return decl
                is TraitDecl -> if (decl.name == name) return decl
                is hasab.compiler.frontend.ast.TypeAliasDecl -> if (decl.name == name) return decl
                else -> {}
            }
        }
        return null
    }

    private fun symbolToLocation(symbol: Symbol, fileName: String): Location {
        val range = symbol.range
        return Location(
            fileName,
            Range(
                Position(range.start.line - 1, range.start.column - 1),
                Position(range.end.line - 1, range.end.column - 1),
            )
        )
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

    private class ReferenceFinder(private val name: String) : AstVisitorBase<List<AstNode>>(emptyList()) {
        val references = mutableListOf<SourceRange>()

        override fun visitIdentifier(node: IdentifierExpr): List<AstNode> {
            if (node.name == name) {
                references.add(node.range())
            }
            return listOf(node)
        }

        override fun visitCall(node: CallExpr): List<AstNode> {
            val callee = node.callee
            if (callee is IdentifierExpr && callee.name == name) {
                references.add(callee.range())
            }
            for (arg in node.arguments) {
                arg.accept(this)
            }
            return listOf(node)
        }

        override fun visitFieldAccess(node: FieldAccessExpr): List<AstNode> {
            if (node.fieldName == name) {
                references.add(node.range())
            }
            node.callee.accept(this)
            return listOf(node)
        }

        override fun visitFnDecl(node: FnDecl): List<AstNode> {
            if (node.name == name) {
                references.add(node.range())
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitLet(node: LetStmt): List<AstNode> {
            if (node.name == name) {
                references.add(node.range())
            }
            node.initializer.accept(this)
            return listOf(node)
        }

        override fun visitStructDecl(node: StructDecl): List<AstNode> {
            if (node.name == name) {
                references.add(node.range())
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitEnumDecl(node: EnumDecl): List<AstNode> {
            if (node.name == name) {
                references.add(node.range())
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }

        override fun visitTraitDecl(node: TraitDecl): List<AstNode> {
            if (node.name == name) {
                references.add(node.range())
            }
            for (child in node.children()) child.accept(this)
            return listOf(node)
        }
    }
}
