package hasab.lsp.signature

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.AstVisitorBase
import hasab.compiler.frontend.ast.CallExpr
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.FunctionParam
import hasab.compiler.frontend.ast.Module
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.Symbol
import hasab.compiler.semantic.SymbolKind
import hasab.lsp.DocumentState
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.ParameterInformation
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.SignatureInformation
import org.eclipse.lsp4j.jsonrpc.messages.Either

public class SignatureEngine {

    public fun computeSignatureHelp(
        state: DocumentState,
        position: Position,
    ): SignatureHelp? {
        try {
            val parsed = state.parseResult ?: state.parse()
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val module = parsed.module

            val callExpr = findCallExpression(module, position) ?: return null
            val callee = callExpr.callee

            val symbol = if (callee is hasab.compiler.frontend.ast.IdentifierExpr) {
                semanticModel.lookupSymbol(callee.name)
            } else null

            val signatures = mutableListOf<SignatureInformation>()

            if (symbol != null && symbol.kind == SymbolKind.FUNCTION) {
                val fnSymbol = symbol as hasab.compiler.semantic.FunctionSymbol
                val sig = SignatureInformation().apply {
                    label = "${fnSymbol.name}(${fnSymbol.parameterCount} params)"
                    documentation = Either.forLeft(fnSymbol.docComment ?: fnSymbol.name)
                }
                signatures.add(sig)
            }

            val declNode = if (callee is hasab.compiler.frontend.ast.IdentifierExpr) {
                findFunctionDeclaration(module, callee.name)
            } else null

            if (declNode != null) {
                val sig = buildSignatureFromDecl(declNode)
                if (signatures.none { it.label == sig.label }) {
                    signatures.add(sig)
                }
            }

            if (signatures.isEmpty()) return null

            val activeParameter = countCommasBeforePosition(state.content, position)

            return SignatureHelp().apply {
                this.signatures = signatures
                this.activeSignature = 0
                this.activeParameter = activeParameter
            }
        } catch (_: Exception) {
            return null
        }
    }

    private fun buildSignatureFromDecl(decl: FnDecl): SignatureInformation {
        val params = decl.parameters.map { param ->
            val paramInfo = ParameterInformation().apply {
                label = Either.forLeft("${param.name}: ${param.type?.let { typeNodeToString(it) } ?: "any"}")
                documentation = Either.forLeft(param.docComment ?: param.name)
            }
            paramInfo
        }

        val returnType = decl.returnType?.let { typeNodeToString(it) } ?: "void"
        val label = "${decl.name}(${params.joinToString(", ") { (it.label as? Either<*, *>)?.left as? String ?: "" }}): $returnType"

        return SignatureInformation().apply {
            this.label = label
            this.documentation = Either.forLeft(decl.docComment ?: "Function ${decl.name}")
            this.parameters = params
        }
    }

    private fun typeNodeToString(typeNode: AstNode): String {
        return when (typeNode) {
            is hasab.compiler.frontend.ast.IdentifierType -> typeNode.name
            is hasab.compiler.frontend.ast.QualifiedType -> typeNode.path.joinToString("::")
            is hasab.compiler.frontend.ast.ArrayType -> "[${typeNodeToString(typeNode.elementType)}]"
            is hasab.compiler.frontend.ast.PointerType -> "*${typeNodeToString(typeNode.elementType)}"
            is hasab.compiler.frontend.ast.OptionalType -> "${typeNodeToString(typeNode.elementType)}?"
            is hasab.compiler.frontend.ast.FunctionType -> {
                val params = typeNode.parameterTypes.joinToString(", ") { typeNodeToString(it) }
                "($params) -> ${typeNodeToString(typeNode.returnType)}"
            }
            is hasab.compiler.frontend.ast.VoidType -> "void"
            else -> "any"
        }
    }

    private fun findCallExpression(module: Module, position: Position): CallExpr? {
        var best: CallExpr? = null
        var bestDist = Int.MAX_VALUE

        fun walk(node: AstNode) {
            if (node is CallExpr) {
                val callEndLine = node.endOffset
                val targetOffset = position.line * 1000 + position.character
                val dist = kotlin.math.abs(node.endOffset - targetOffset)
                if (dist < bestDist) {
                    bestDist = dist
                    best = node
                }
            }
            for (child in node.children()) walk(child)
        }

        walk(module)
        return best
    }

    private fun findFunctionDeclaration(module: Module, name: String): FnDecl? {
        for (decl in module.declarations) {
            if (decl is FnDecl && decl.name == name) return decl
        }
        return null
    }

    private fun countCommasBeforePosition(content: String, position: Position): Int {
        var line = 0
        var col = 0
        var depth = 0
        var commas = 0
        var inTargetParen = false

        for (ch in content) {
            if (line == position.line && col == position.character) break

            when (ch) {
                '(' -> {
                    depth++
                    if (depth == 1) inTargetParen = true
                }
                ')' -> {
                    if (depth == 1) inTargetParen = false
                    depth--
                }
                ',' -> {
                    if (inTargetParen && depth == 1) commas++
                }
                '\n' -> {
                    line++
                    col = 0
                    continue
                }
            }
            col++
        }

        return commas
    }
}
