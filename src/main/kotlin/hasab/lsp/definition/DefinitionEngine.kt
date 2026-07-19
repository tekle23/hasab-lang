package hasab.lsp.definition

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.IdentifierExpr
import hasab.compiler.frontend.ast.CallExpr
import hasab.compiler.frontend.ast.FieldAccessExpr
import hasab.compiler.frontend.ast.LetStmt
import hasab.compiler.frontend.ast.FunctionParam
import hasab.compiler.frontend.ast.StructDecl
import hasab.compiler.frontend.ast.EnumDecl
import hasab.compiler.frontend.ast.TraitDecl
import hasab.compiler.frontend.ast.TypeAliasDecl
import hasab.compiler.frontend.ast.Module
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.Symbol
import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

public class DefinitionEngine(
    private val workspaceIndex: WorkspaceIndex,
) {

    public fun findDefinition(
        state: DocumentState,
        position: Position,
    ): Location? {
        try {
            val parsed = state.parseResult ?: state.parse()
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val module = parsed.module

            val node = findNodeAtPosition(module, position) ?: return null

            val symbol = semanticModel.bindingFor(node)
            if (symbol != null) {
                return symbolToLocation(symbol)
            }

            val definitionNode = findDeclarationNode(module, node)
            if (definitionNode != null) {
                return nodeToLocation(definitionNode, state.uri)
            }

            return null
        } catch (_: Exception) {
            return null
        }
    }

    private fun symbolToLocation(symbol: Symbol): Location {
        val range = symbol.range
        return Location(
            symbol.fileName,
            Range(
                Position(range.start.line - 1, range.start.column - 1),
                Position(range.end.line - 1, range.end.column - 1),
            )
        )
    }

    private fun nodeToLocation(node: AstNode, fallbackUri: String): Location {
        return Location(
            fallbackUri,
            Range(
                Position(node.line - 1, node.column - 1),
                Position(node.line - 1, node.column - 1 + (node.endOffset - node.startOffset).coerceAtLeast(1)),
            )
        )
    }

    private fun findDeclarationNode(module: Module, target: AstNode): AstNode? {
        when (target) {
            is IdentifierExpr -> {
                return findDeclarationByName(module, target.name)
            }
            is CallExpr -> {
                val callee = target.callee
                if (callee is IdentifierExpr) {
                    return findDeclarationByName(module, callee.name)
                }
            }
            is FieldAccessExpr -> {
                return findDeclarationByName(module, target.fieldName)
            }
            else -> {}
        }
        return null
    }

    private fun findDeclarationByName(module: Module, name: String): AstNode? {
        for (decl in module.declarations) {
            val result = findInDecl(decl, name)
            if (result != null) return result
        }
        return null
    }

    private fun findInDecl(decl: AstNode, name: String): AstNode? {
        return when (decl) {
            is FnDecl -> {
                if (decl.name == name) decl else {
                    for (param in decl.parameters) {
                        if (param.name == name) return decl
                    }
                    decl.body?.let { findInBlock(it, name) }
                }
            }
            is StructDecl -> {
                if (decl.name == name) decl else {
                    for (field in decl.fields) {
                        if (field.name == name) return decl
                    }
                    null
                }
            }
            is EnumDecl -> {
                if (decl.name == name) decl else {
                    for (variant in decl.variants) {
                        if (variant.name == name) return decl
                    }
                    null
                }
            }
            is TraitDecl -> {
                if (decl.name == name) decl else {
                    for (method in decl.methods) {
                        val result = findInDecl(method, name)
                        if (result != null) return result
                    }
                    null
                }
            }
            is hasab.compiler.frontend.ast.ImplDecl -> {
                for (method in decl.methods) {
                    val result = findInDecl(method, name)
                    if (result != null) return result
                }
                null
            }
            is TypeAliasDecl -> {
                if (decl.name == name) decl else null
            }
            is hasab.compiler.frontend.ast.ModDecl -> {
                if (decl.name == name) decl else {
                    for (inner in decl.body.orEmpty()) {
                        val result = findInDecl(inner, name)
                        if (result != null) return result
                    }
                    null
                }
            }
            is hasab.compiler.frontend.ast.PubDecl -> findInDecl(decl.inner, name)
            else -> null
        }
    }

    private fun findInBlock(block: hasab.compiler.frontend.ast.Block, name: String): AstNode? {
        for (stmt in block.statements) {
            when (stmt) {
                is hasab.compiler.frontend.ast.LetStmt -> {
                    if (stmt.name == name) return stmt
                }
                is hasab.compiler.frontend.ast.IfStmt -> {
                    val result = findInBlock(stmt.thenBranch, name)
                    if (result != null) return result
                    val elseBranch = stmt.elseBranch
                    if (elseBranch is hasab.compiler.frontend.ast.Block) {
                        val result2 = findInBlock(elseBranch, name)
                        if (result2 != null) return result2
                    }
                }
                is hasab.compiler.frontend.ast.WhileStmt -> {
                    val result = findInBlock(stmt.body, name)
                    if (result != null) return result
                }
                is hasab.compiler.frontend.ast.ForStmt -> {
                    val result = findInBlock(stmt.body, name)
                    if (result != null) return result
                }
                is hasab.compiler.frontend.ast.ExprStmt -> {}
                is hasab.compiler.frontend.ast.ReturnStmt -> {}
                is hasab.compiler.frontend.ast.BreakStmt -> {}
                is hasab.compiler.frontend.ast.ContinueStmt -> {}
                is hasab.compiler.frontend.ast.Block -> {
                    val result = findInBlock(stmt, name)
                    if (result != null) return result
                }
            }
        }
        return null
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
}
