package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*

/**
 * Abstract AST visitor for semantic analysis passes.
 *
 * Provides a structured way to walk the AST during semantic analysis.
 * Each pass extends this visitor and overrides only the methods it needs.
 *
 * The visitor walks declarations, statements, and expressions in a structured
 * manner, calling the appropriate visit method at each node.
 */
public abstract class SemanticVisitor {

    /**
     * Visit a module (entry point).
     */
    public open fun visitModule(module: Module) {
        for (decl in module.declarations) {
            visitDecl(decl)
        }
    }

    /**
     * Visit a declaration.
     */
    public open fun visitDecl(decl: Decl) {
        when (decl) {
            is FnDecl -> visitFnDecl(decl)
            is StructDecl -> visitStructDecl(decl)
            is EnumDecl -> visitEnumDecl(decl)
            is TraitDecl -> visitTraitDecl(decl)
            is TypeAliasDecl -> visitTypeAliasDecl(decl)
            is ImplDecl -> visitImplDecl(decl)
            is ModDecl -> visitModDecl(decl)
            is UseDecl -> visitUseDecl(decl)
            is PubDecl -> visitPubDecl(decl)
        }
    }

    // ---- Declaration visitors ----

    public open fun visitFnDecl(node: FnDecl) {
        node.returnType?.let { visitTypeNode(it) }
        for (param in node.parameters) {
            param.type?.let { visitTypeNode(it) }
        }
        node.body?.let { visitBlock(it) }
    }

    public open fun visitStructDecl(node: StructDecl) {
        for (field in node.fields) {
            visitTypeNode(field.type)
        }
    }

    public open fun visitEnumDecl(node: EnumDecl) {
        for (variant in node.variants) {
            for (field in variant.fields) {
                visitTypeNode(field.type)
            }
        }
    }

    public open fun visitTraitDecl(node: TraitDecl) {
        for (method in node.methods) {
            visitFnDecl(method)
        }
    }

    public open fun visitTypeAliasDecl(node: TypeAliasDecl) {
        visitTypeNode(node.target)
    }

    public open fun visitImplDecl(node: ImplDecl) {
        visitTypeNode(node.targetType)
        for (method in node.methods) {
            visitFnDecl(method)
        }
    }

    public open fun visitModDecl(node: ModDecl) {
        node.body?.let { decls ->
            for (decl in decls) {
                visitDecl(decl)
            }
        }
    }

    public open fun visitUseDecl(node: UseDecl) {}

    public open fun visitPubDecl(node: PubDecl) {
        visitDecl(node.inner)
    }

    // ---- Statement visitors ----

    public open fun visitBlock(block: Block) {
        for (stmt in block.statements) {
            visitStatement(stmt)
        }
    }

    public open fun visitStatement(stmt: Stmt) {
        when (stmt) {
            is ExprStmt -> visitExpr(stmt.expression)
            is ReturnStmt -> stmt.value?.let { visitExpr(it) }
            is BreakStmt -> {}
            is ContinueStmt -> {}
            is LetStmt -> {
                stmt.typeAnnotation?.let { visitTypeNode(it) }
                visitExpr(stmt.initializer)
            }
            is IfStmt -> {
                visitExpr(stmt.condition)
                visitBlock(stmt.thenBranch)
                stmt.elseBranch?.let { visitStatement(it) }
            }
            is WhileStmt -> {
                visitExpr(stmt.condition)
                visitBlock(stmt.body)
            }
            is ForStmt -> {
                visitExpr(stmt.iterable)
                visitBlock(stmt.body)
            }
            is Block -> visitBlock(stmt)
        }
    }

    // ---- Expression visitors ----

    public open fun visitExpr(expr: Expr) {
        when (expr) {
            is IntegerLiteralExpr -> {}
            is FloatLiteralExpr -> {}
            is StringLiteralExpr -> {}
            is CharLiteralExpr -> {}
            is BoolLiteralExpr -> {}
            is NilLiteralExpr -> {}
            is IdentifierExpr -> visitIdentifierExpr(expr)
            is BinaryExpr -> {
                visitExpr(expr.left)
                visitExpr(expr.right)
            }
            is UnaryExpr -> visitExpr(expr.operand)
            is CallExpr -> {
                visitExpr(expr.callee)
                for (arg in expr.arguments) {
                    visitExpr(arg)
                }
            }
            is IndexExpr -> {
                visitExpr(expr.callee)
                visitExpr(expr.index)
            }
            is FieldAccessExpr -> visitExpr(expr.callee)
            is SafeFieldAccessExpr -> visitExpr(expr.callee)
            is NullAssertExpr -> visitExpr(expr.operand)
            is ParenExpr -> visitExpr(expr.inner)
            is ArrayLiteralExpr -> {
                for (elem in expr.elements) {
                    visitExpr(elem)
                }
            }
            is ArrayInitExpr -> {
                expr.elementType?.let { visitTypeNode(it) }
                visitExpr(expr.size)
            }
            is IfExpr -> {
                visitExpr(expr.condition)
                visitExpr(expr.thenBranch)
                expr.elseBranch?.let { visitExpr(it) }
            }
            is AssignmentExpr -> {
                visitExpr(expr.target)
                visitExpr(expr.value)
            }
            is CompoundAssignmentExpr -> {
                visitExpr(expr.target)
                visitExpr(expr.value)
            }
        }
    }

    public open fun visitIdentifierExpr(expr: IdentifierExpr) {}

    // ---- Type visitors ----

    public open fun visitTypeNode(node: TypeNode) {
        when (node) {
            is IdentifierType -> {}
            is QualifiedType -> {}
            is ArrayType -> visitTypeNode(node.elementType)
            is PointerType -> visitTypeNode(node.elementType)
            is OptionalType -> visitTypeNode(node.elementType)
            is FunctionType -> {
                for (param in node.parameterTypes) {
                    visitTypeNode(param)
                }
                visitTypeNode(node.returnType)
            }
            is VoidType -> {}
        }
    }
}
