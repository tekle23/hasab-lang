package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.lexer.DiagnosticSeverity

/**
 * Checks visibility constraints on symbol access.
 * A symbol is accessible if:
 * - It is public, OR
 * - It is in the same module scope
 */
public class VisibilityChecker(
    private val table: SymbolTable,
) {
    private val diagnostics: MutableList<SemanticDiagnostic> = mutableListOf()

    public fun check(module: Module): List<SemanticDiagnostic> {
        diagnostics.clear()
        checkDeclarations(module.declarations, currentModule = module.name ?: module.fileName)
        return diagnostics.toList()
    }

    private fun report(code: DiagnosticCode, message: String, node: AstNode, hint: String? = null) {
        diagnostics.add(SemanticDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = node.range(),
            fileName = node.fileName,
            hint = hint,
        ))
    }

    private fun checkDeclarations(decls: List<Decl>, currentModule: String) {
        for (decl in decls) {
            checkDecl(decl, currentModule)
        }
    }

    private fun checkDecl(decl: Decl, currentModule: String) {
        when (decl) {
            is FnDecl -> checkFnDecl(decl, currentModule)
            is StructDecl -> {}
            is EnumDecl -> {}
            is TraitDecl -> checkTraitDecl(decl, currentModule)
            is TypeAliasDecl -> {}
            is ImplDecl -> checkImplDecl(decl, currentModule)
            is ModDecl -> {
                decl.body?.let {
                    val childModule = "$currentModule::${decl.name}"
                    checkDeclarations(it, childModule)
                }
            }
            is UseDecl -> {}
            is PubDecl -> checkDecl(decl.inner, currentModule)
        }
    }

    private fun checkFnDecl(node: FnDecl, currentModule: String) {
        node.body?.let { checkBlock(it, currentModule) }
    }

    private fun checkTraitDecl(node: TraitDecl, currentModule: String) {
        for (method in node.methods) {
            method.body?.let { checkBlock(it, currentModule) }
        }
    }

    private fun checkImplDecl(node: ImplDecl, currentModule: String) {
        for (method in node.methods) {
            method.body?.let { checkBlock(it, currentModule) }
        }
    }

    private fun checkBlock(block: Block, currentModule: String) {
        for (stmt in block.statements) {
            checkStatement(stmt, currentModule)
        }
    }

    private fun checkStatement(stmt: Stmt, currentModule: String) {
        when (stmt) {
            is ExprStmt -> checkExpr(stmt.expression, currentModule)
            is ReturnStmt -> stmt.value?.let { checkExpr(it, currentModule) }
            is BreakStmt -> {}
            is ContinueStmt -> {}
            is LetStmt -> checkExpr(stmt.initializer, currentModule)
            is IfStmt -> {
                checkExpr(stmt.condition, currentModule)
                checkBlock(stmt.thenBranch, currentModule)
                stmt.elseBranch?.let { checkStatement(it, currentModule) }
            }
            is WhileStmt -> {
                checkExpr(stmt.condition, currentModule)
                checkBlock(stmt.body, currentModule)
            }
            is ForStmt -> {
                checkExpr(stmt.iterable, currentModule)
                checkBlock(stmt.body, currentModule)
            }
            is Block -> checkBlock(stmt, currentModule)
        }
    }

    private fun checkExpr(expr: Expr, currentModule: String) {
        when (expr) {
            is IdentifierExpr -> {
                val symbol = table.lookup(expr.name)
                if (symbol != null && symbol.visibility == Visibility.MODULE_LOCAL) {
                    // Check if the symbol is in the same module
                    if (symbol.parentModule != null && symbol.parentModule != currentModule) {
                        report(
                            DiagnosticCode.VISIBILITY_ERROR,
                            "Symbol '${expr.name}' is not accessible from module '$currentModule'",
                            expr,
                            hint = "Declare '${expr.name}' as 'pub' to make it accessible",
                        )
                    }
                }
            }
            is BinaryExpr -> {
                checkExpr(expr.left, currentModule)
                checkExpr(expr.right, currentModule)
            }
            is UnaryExpr -> checkExpr(expr.operand, currentModule)
            is CallExpr -> {
                checkExpr(expr.callee, currentModule)
                expr.arguments.forEach { checkExpr(it, currentModule) }
            }
            is IndexExpr -> {
                checkExpr(expr.callee, currentModule)
                checkExpr(expr.index, currentModule)
            }
            is FieldAccessExpr -> checkExpr(expr.callee, currentModule)
            is ParenExpr -> checkExpr(expr.inner, currentModule)
            is ArrayLiteralExpr -> expr.elements.forEach { checkExpr(it, currentModule) }
            is ArrayInitExpr -> {
                expr.elementType?.let { /* type node, not an expr */ }
                checkExpr(expr.size, currentModule)
            }
            is IfExpr -> {
                checkExpr(expr.condition, currentModule)
                checkExpr(expr.thenBranch, currentModule)
                expr.elseBranch?.let { checkExpr(it, currentModule) }
            }
            is AssignmentExpr -> {
                checkExpr(expr.target, currentModule)
                checkExpr(expr.value, currentModule)
            }
            is CompoundAssignmentExpr -> {
                checkExpr(expr.target, currentModule)
                checkExpr(expr.value, currentModule)
            }
            else -> {} // literals
        }
    }
}
