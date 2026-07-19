package hasab.compiler.hir

/**
 * Recursive default implementation of [HirVisitor].
 *
 * Every visit method recursively walks [HirNode.hirChildren].
 * Override only the methods you need; all others recurse by default.
 */
public open class HirVisitorBase<out T>(private val default: T) : HirVisitor<T> {

    override fun visitModule(node: HirModule): T {
        for (decl in node.declarations) visitDecl(decl)
        return default
    }

    public fun visitDecl(node: HirDecl): T = when (node) {
        is HirFnDecl -> visitFnDecl(node)
        is HirStructDecl -> visitStructDecl(node)
        is HirEnumDecl -> visitEnumDecl(node)
        is HirTraitDecl -> visitTraitDecl(node)
        is HirTypeAliasDecl -> visitTypeAlias(node)
        is HirImplDecl -> visitImplDecl(node)
        is HirUseDecl -> visitUseDecl(node)
    }

    override fun visitFnDecl(node: HirFnDecl): T {
        node.body?.let { visitBlock(it) }
        return default
    }

    override fun visitStructDecl(node: HirStructDecl): T = default
    override fun visitEnumDecl(node: HirEnumDecl): T = default
    override fun visitTraitDecl(node: HirTraitDecl): T = default
    override fun visitTypeAlias(node: HirTypeAliasDecl): T = default
    override fun visitImplDecl(node: HirImplDecl): T {
        for (method in node.methods) visitFnDecl(method)
        return default
    }

    override fun visitUseDecl(node: HirUseDecl): T = default

    // ---- Expressions ----

    public fun visitExpr(node: HirExpr): T = when (node) {
        is HirIntLiteral -> visitIntLiteral(node)
        is HirFloatLiteral -> visitFloatLiteral(node)
        is HirStringLiteral -> visitStringLiteral(node)
        is HirCharLiteral -> visitCharLiteral(node)
        is HirBoolLiteral -> visitBoolLiteral(node)
        is HirNilLiteral -> visitNilLiteral(node)
        is HirIdentifier -> visitIdentifier(node)
        is HirBinary -> visitBinary(node)
        is HirUnary -> visitUnary(node)
        is HirCall -> visitCall(node)
        is HirIndex -> visitIndex(node)
        is HirFieldAccess -> visitFieldAccess(node)
        is HirSafeFieldAccess -> visitSafeFieldAccess(node)
        is HirNullAssert -> visitNullAssert(node)
        is HirArrayLiteral -> visitArrayLiteral(node)
        is HirArrayInit -> visitArrayInit(node)
        is HirIfExpr -> visitIfExpr(node)
        is HirAssignment -> visitAssignment(node)
        is HirCompoundAssignment -> visitCompoundAssignment(node)
    }

    override fun visitIntLiteral(node: HirIntLiteral): T = default
    override fun visitFloatLiteral(node: HirFloatLiteral): T = default
    override fun visitStringLiteral(node: HirStringLiteral): T = default
    override fun visitCharLiteral(node: HirCharLiteral): T = default
    override fun visitBoolLiteral(node: HirBoolLiteral): T = default
    override fun visitNilLiteral(node: HirNilLiteral): T = default
    override fun visitIdentifier(node: HirIdentifier): T = default

    override fun visitBinary(node: HirBinary): T {
        visitExpr(node.left)
        visitExpr(node.right)
        return default
    }

    override fun visitUnary(node: HirUnary): T {
        visitExpr(node.operand)
        return default
    }

    override fun visitCall(node: HirCall): T {
        visitExpr(node.callee)
        for (arg in node.arguments) visitExpr(arg)
        return default
    }

    override fun visitIndex(node: HirIndex): T {
        visitExpr(node.callee)
        visitExpr(node.index)
        return default
    }

    override fun visitFieldAccess(node: HirFieldAccess): T {
        visitExpr(node.callee)
        return default
    }

    override fun visitSafeFieldAccess(node: HirSafeFieldAccess): T {
        visitExpr(node.callee)
        return default
    }

    override fun visitNullAssert(node: HirNullAssert): T {
        visitExpr(node.operand)
        return default
    }

    override fun visitArrayLiteral(node: HirArrayLiteral): T {
        for (elem in node.elements) visitExpr(elem)
        return default
    }

    override fun visitArrayInit(node: HirArrayInit): T {
        visitExpr(node.size)
        return default
    }

    override fun visitIfExpr(node: HirIfExpr): T {
        visitExpr(node.condition)
        visitExpr(node.thenBranch)
        node.elseBranch?.let { visitExpr(it) }
        return default
    }

    override fun visitAssignment(node: HirAssignment): T {
        visitExpr(node.target)
        visitExpr(node.value)
        return default
    }

    override fun visitCompoundAssignment(node: HirCompoundAssignment): T {
        visitExpr(node.target)
        visitExpr(node.value)
        return default
    }

    // ---- Statements ----

    public fun visitStmt(node: HirStmt): T = when (node) {
        is HirBlock -> visitBlock(node)
        is HirExprStmt -> visitExprStmt(node)
        is HirReturnStmt -> visitReturnStmt(node)
        is HirLetStmt -> visitLetStmt(node)
        is HirIfStmt -> visitIfStmt(node)
        is HirWhileStmt -> visitWhileStmt(node)
        is HirForStmt -> visitForStmt(node)
        is HirBreakStmt -> visitBreakStmt(node)
        is HirContinueStmt -> visitContinueStmt(node)
    }

    override fun visitBlock(node: HirBlock): T {
        for (stmt in node.statements) visitStmt(stmt)
        return default
    }

    override fun visitExprStmt(node: HirExprStmt): T {
        visitExpr(node.expression)
        return default
    }

    override fun visitReturnStmt(node: HirReturnStmt): T {
        node.value?.let { visitExpr(it) }
        return default
    }

    override fun visitLetStmt(node: HirLetStmt): T {
        visitExpr(node.initializer)
        return default
    }

    override fun visitIfStmt(node: HirIfStmt): T {
        visitExpr(node.condition)
        visitBlock(node.thenBranch)
        node.elseBranch?.let { visitStmt(it) }
        return default
    }

    override fun visitWhileStmt(node: HirWhileStmt): T {
        visitExpr(node.condition)
        visitBlock(node.body)
        return default
    }

    override fun visitForStmt(node: HirForStmt): T {
        visitExpr(node.iterable)
        visitBlock(node.body)
        return default
    }

    override fun visitBreakStmt(node: HirBreakStmt): T = default
    override fun visitContinueStmt(node: HirContinueStmt): T = default
}
