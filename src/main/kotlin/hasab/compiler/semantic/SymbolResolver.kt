package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.lexer.DiagnosticSeverity

/**
 * Resolves identifier references against the symbol table.
 * Reports undefined variables, functions, and types.
 * Also populates node bindings (AST node → symbol) for IDE tooling.
 */
public class SymbolResolver(
    private val table: SymbolTable,
    private val scopeManager: ScopeManager,
) {
    private val diagnostics: MutableList<SemanticDiagnostic> = mutableListOf()
    private val _nodeBindings: MutableMap<AstNode, Symbol> = mutableMapOf()

    public val nodeBindings: Map<AstNode, Symbol> get() = _nodeBindings.toMap()

    public fun resolve(module: Module): List<SemanticDiagnostic> {
        diagnostics.clear()
        _nodeBindings.clear()
        resolveDeclarations(module.declarations)
        return diagnostics.toList()
    }

    private fun report(code: DiagnosticCode, message: String, node: AstNode, hint: String? = null, didYouMean: String? = null) {
        diagnostics.add(SemanticDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = node.range(),
            fileName = node.fileName,
            hint = hint,
            didYouMean = didYouMean,
        ))
    }

    // ---- Declarations ----

    private fun resolveDeclarations(decls: List<Decl>) {
        for (decl in decls) {
            resolveDecl(decl)
        }
    }

    private fun resolveDecl(decl: Decl) {
        when (decl) {
            is FnDecl -> resolveFnDecl(decl)
            is StructDecl -> {} // types resolved in declaration collection
            is EnumDecl -> {}
            is TraitDecl -> resolveTraitDecl(decl)
            is TypeAliasDecl -> resolveTypeAliasDecl(decl)
            is ImplDecl -> resolveImplDecl(decl)
            is ModDecl -> {
                decl.body?.let { resolveDeclarations(it) }
            }
            is UseDecl -> {} // handled by ImportResolver
            is PubDecl -> resolveDecl(decl.inner)
        }
    }

    private fun resolveFnDecl(node: FnDecl) {
        node.returnType?.let { resolveTypeNode(it) }
        node.body?.let { resolveBlock(it) }
    }

    private fun resolveTraitDecl(node: TraitDecl) {
        for (method in node.methods) {
            method.returnType?.let { resolveTypeNode(it) }
            method.body?.let { resolveBlock(it) }
        }
    }

    private fun resolveTypeAliasDecl(node: TypeAliasDecl) {
        resolveTypeNode(node.target)
    }

    private fun resolveImplDecl(node: ImplDecl) {
        resolveTypeNode(node.targetType)
        for (method in node.methods) {
            resolveFnDecl(method)
        }
    }

    // ---- Statements ----

    private fun resolveBlock(block: Block) {
        for (stmt in block.statements) {
            resolveStatement(stmt)
        }
    }

    private fun resolveStatement(stmt: Stmt) {
        when (stmt) {
            is ExprStmt -> resolveExpr(stmt.expression)
            is ReturnStmt -> stmt.value?.let { resolveExpr(it) }
            is BreakStmt -> {}
            is ContinueStmt -> {}
            is LetStmt -> {
                stmt.typeAnnotation?.let { resolveTypeNode(it) }
                resolveExpr(stmt.initializer)
            }
            is IfStmt -> {
                resolveExpr(stmt.condition)
                resolveBlock(stmt.thenBranch)
                stmt.elseBranch?.let { resolveStatement(it) }
            }
            is WhileStmt -> {
                resolveExpr(stmt.condition)
                resolveBlock(stmt.body)
            }
            is ForStmt -> {
                resolveExpr(stmt.iterable)
                resolveBlock(stmt.body)
            }
            is Block -> resolveBlock(stmt)
        }
    }

    // ---- Expressions ----

    private fun resolveExpr(expr: Expr) {
        when (expr) {
            is IntegerLiteralExpr -> {}
            is FloatLiteralExpr -> {}
            is StringLiteralExpr -> {}
            is CharLiteralExpr -> {}
            is BoolLiteralExpr -> {}
            is NilLiteralExpr -> {}
            is IdentifierExpr -> resolveIdentifier(expr)
            is BinaryExpr -> {
                resolveExpr(expr.left)
                resolveExpr(expr.right)
            }
            is UnaryExpr -> resolveExpr(expr.operand)
            is CallExpr -> {
                resolveExpr(expr.callee)
                expr.arguments.forEach { resolveExpr(it) }
            }
            is IndexExpr -> {
                resolveExpr(expr.callee)
                resolveExpr(expr.index)
            }
            is FieldAccessExpr -> resolveExpr(expr.callee)
            is SafeFieldAccessExpr -> resolveExpr(expr.callee)
            is NullAssertExpr -> resolveExpr(expr.operand)
            is ParenExpr -> resolveExpr(expr.inner)
            is ArrayLiteralExpr -> expr.elements.forEach { resolveExpr(it) }
            is ArrayInitExpr -> {
                expr.elementType?.let { resolveTypeNode(it) }
                resolveExpr(expr.size)
            }
            is IfExpr -> {
                resolveExpr(expr.condition)
                resolveExpr(expr.thenBranch)
                expr.elseBranch?.let { resolveExpr(it) }
            }
            is AssignmentExpr -> {
                resolveExpr(expr.target)
                resolveExpr(expr.value)
            }
            is CompoundAssignmentExpr -> {
                resolveExpr(expr.target)
                resolveExpr(expr.value)
            }
        }
    }

    private fun resolveIdentifier(expr: IdentifierExpr) {
        val symbol = table.lookup(expr.name)
        if (symbol == null) {
            val suggestion = didYouMean(expr.name, table.allSymbolNames())
            report(
                DiagnosticCode.UNDEFINED_VARIABLE,
                "Undefined variable '${expr.name}'",
                expr,
                hint = suggestion?.let { "Did you mean '$it'?" } ?: "Did you forget to declare '${expr.name}'?",
                didYouMean = suggestion,
            )
        } else {
            _nodeBindings[expr] = symbol
        }
    }

    // ---- Type nodes ----

    private fun resolveTypeNode(node: TypeNode) {
        when (node) {
            is IdentifierType -> {
                val symbol = table.lookup(node.name)
                if (symbol == null) {
                    val builtin = isBuiltinType(node.name)
                    if (!builtin) {
                        val suggestion = didYouMean(node.name, table.allSymbolNames())
                        report(
                            DiagnosticCode.UNDEFINED_TYPE,
                            "Unknown type '${node.name}'",
                            node,
                            hint = suggestion?.let { "Did you mean '$it'?" } ?: "Did you forget to declare type '${node.name}'?",
                            didYouMean = suggestion,
                        )
                    }
                } else {
                    _nodeBindings[node] = symbol
                }
            }
            is QualifiedType -> {} // resolved by ImportResolver
            is ArrayType -> resolveTypeNode(node.elementType)
            is PointerType -> resolveTypeNode(node.elementType)
            is OptionalType -> resolveTypeNode(node.elementType)
            is FunctionType -> {
                node.parameterTypes.forEach { resolveTypeNode(it) }
                resolveTypeNode(node.returnType)
            }
            is VoidType -> {}
        }
    }

    private fun isBuiltinType(name: String): Boolean = when (name) {
        "int", "float", "string", "bool", "char", "void" -> true
        else -> false
    }
}
