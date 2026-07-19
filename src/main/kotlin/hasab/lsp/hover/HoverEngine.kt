package hasab.lsp.hover

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.AstVisitorBase
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.StructDecl
import hasab.compiler.frontend.ast.EnumDecl
import hasab.compiler.frontend.ast.TraitDecl
import hasab.compiler.frontend.ast.TypeAliasDecl
import hasab.compiler.frontend.ast.IdentifierExpr
import hasab.compiler.frontend.ast.CallExpr
import hasab.compiler.frontend.ast.FieldAccessExpr
import hasab.compiler.frontend.ast.LetStmt
import hasab.compiler.frontend.ast.FunctionParam
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.Symbol
import hasab.compiler.semantic.SymbolKind
import hasab.compiler.types.Type
import hasab.compiler.types.TypeChecker
import hasab.compiler.types.TypeCheckResult
import hasab.lsp.DocumentState
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.jsonrpc.messages.Either

public class HoverEngine {

    public fun computeHover(
        state: DocumentState,
        position: Position,
    ): Hover? {
        try {
            val parsed = state.parseResult ?: state.parse()
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val typeResult = state.typeCheckResult ?: state.typeCheck()
            val module = parsed.module

            val node = findNodeAtPosition(module, position) ?: return null

            val symbol = semanticModel.bindingFor(node)
            if (symbol != null) {
                return buildSymbolHover(symbol, node)
            }

            return buildNodeHover(node, typeResult)
        } catch (_: Exception) {
            return null
        }
    }

    private fun buildSymbolHover(symbol: Symbol, node: AstNode): Hover {
        val markdown = buildString {
            append("### ")
            append(symbol.kind.name.lowercase().replaceFirstChar { it.uppercase() })
            append(" `")
            append(symbol.name)
            appendLine("`")
            appendLine()

            if (symbol.docComment != null) {
                appendLine(symbol.docComment)
                appendLine()
            }

            append("**Kind:** ")
            appendLine(symbol.kind.name.lowercase())
            append("**Visibility:** ")
            appendLine(symbol.visibility.name.lowercase())
            append("**File:** ")
            appendLine(symbol.fileName)
            append("**Location:** ")
            appendLine("${symbol.range.start.line}:${symbol.range.start.column}")

            when (symbol) {
                is hasab.compiler.semantic.FunctionSymbol -> {
                    appendLine()
                    appendLine("---")
                    append("**Parameters:** ")
                    appendLine(symbol.parameterCount.toString())
                    if (symbol.isExtern) {
                        append("**External:** ")
                        appendLine("yes")
                    }
                }
                is hasab.compiler.semantic.VariableSymbol -> {
                    appendLine()
                    appendLine("---")
                    if (symbol.isMutable) {
                        append("**Mutable:** ")
                        appendLine("yes")
                    }
                    symbol.typeAnnotation?.let {
                        append("**Type:** `")
                        append(it)
                        appendLine("`")
                    }
                }
                is hasab.compiler.semantic.ParameterSymbol -> {
                    appendLine()
                    appendLine("---")
                    if (symbol.isMutable) {
                        append("**Mutable:** ")
                        appendLine("yes")
                    }
                    symbol.typeAnnotation?.let {
                        append("**Type:** `")
                        append(it)
                        appendLine("`")
                    }
                }
                else -> {}
            }
        }

        return Hover().apply {
            contents = Either.forRight(MarkupContent("markdown", markdown))
            range = Range(
                Position(node.line - 1, node.column - 1),
                Position(node.line - 1, node.column - 1 + (node.endOffset - node.startOffset).coerceAtLeast(1)),
            )
        }
    }

    private fun buildNodeHover(node: AstNode, typeResult: TypeCheckResult): Hover? {
        val typeName = when (node) {
            is IdentifierExpr -> "Identifier `${node.name}`"
            is FnDecl -> "Function `${node.name}`"
            is StructDecl -> "Struct `${node.name}`"
            is EnumDecl -> "Enum `${node.name}`"
            is TraitDecl -> "Trait `${node.name}`"
            is TypeAliasDecl -> "Type alias `${node.name}`"
            is LetStmt -> "Variable `${node.name}`"
            is FunctionParam -> "Parameter `${node.name}`"
            is FieldAccessExpr -> "Field `${node.fieldName}`"
            else -> return null
        }

        val markdown = buildString {
            appendLine(typeName)
            appendLine()
            append("**Location:** ")
            append("${node.fileName}:${node.line}:${node.column}")
        }

        return Hover().apply {
            contents = Either.forRight(MarkupContent("markdown", markdown))
            range = Range(
                Position(node.line - 1, node.column - 1),
                Position(node.line - 1, node.column - 1 + (node.endOffset - node.startOffset).coerceAtLeast(1)),
            )
        }
    }

    private fun findNodeAtPosition(module: AstNode, position: Position): AstNode? {
        val targetLine = position.line + 1
        val targetCol = position.character + 1
        var best: AstNode? = null
        var bestScore = Int.MAX_VALUE

        fun walk(node: AstNode) {
            val nodeLine = node.line
            val nodeCol = node.column
            val nodeEnd = node.endOffset

            if (nodeLine == targetLine) {
                val startDiff = kotlin.math.abs(nodeCol - targetCol)
                if (startDiff < bestScore) {
                    bestScore = startDiff
                    best = node
                }
            }

            for (child in node.children()) {
                walk(child)
            }
        }

        walk(module)
        return best
    }
}
