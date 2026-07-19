package hasab.compiler.hir

/**
 * Generic HIR Visitor pattern.
 *
 * Every concrete HIR node has a dedicated visit method.
 * Implementations can override only the methods they need.
 *
 * @param T the return type of each visit method
 */
public interface HirVisitor<out T> {
    public fun visitModule(node: HirModule): T

    // Declarations
    public fun visitFnDecl(node: HirFnDecl): T
    public fun visitStructDecl(node: HirStructDecl): T
    public fun visitEnumDecl(node: HirEnumDecl): T
    public fun visitTraitDecl(node: HirTraitDecl): T
    public fun visitTypeAlias(node: HirTypeAliasDecl): T
    public fun visitImplDecl(node: HirImplDecl): T
    public fun visitUseDecl(node: HirUseDecl): T

    // Expressions
    public fun visitIntLiteral(node: HirIntLiteral): T
    public fun visitFloatLiteral(node: HirFloatLiteral): T
    public fun visitStringLiteral(node: HirStringLiteral): T
    public fun visitCharLiteral(node: HirCharLiteral): T
    public fun visitBoolLiteral(node: HirBoolLiteral): T
    public fun visitNilLiteral(node: HirNilLiteral): T
    public fun visitIdentifier(node: HirIdentifier): T
    public fun visitBinary(node: HirBinary): T
    public fun visitUnary(node: HirUnary): T
    public fun visitCall(node: HirCall): T
    public fun visitIndex(node: HirIndex): T
    public fun visitFieldAccess(node: HirFieldAccess): T
    public fun visitSafeFieldAccess(node: HirSafeFieldAccess): T
    public fun visitNullAssert(node: HirNullAssert): T
    public fun visitArrayLiteral(node: HirArrayLiteral): T
    public fun visitArrayInit(node: HirArrayInit): T
    public fun visitIfExpr(node: HirIfExpr): T
    public fun visitAssignment(node: HirAssignment): T
    public fun visitCompoundAssignment(node: HirCompoundAssignment): T

    // Statements
    public fun visitBlock(node: HirBlock): T
    public fun visitExprStmt(node: HirExprStmt): T
    public fun visitReturnStmt(node: HirReturnStmt): T
    public fun visitLetStmt(node: HirLetStmt): T
    public fun visitIfStmt(node: HirIfStmt): T
    public fun visitWhileStmt(node: HirWhileStmt): T
    public fun visitForStmt(node: HirForStmt): T
    public fun visitBreakStmt(node: HirBreakStmt): T
    public fun visitContinueStmt(node: HirContinueStmt): T
}
