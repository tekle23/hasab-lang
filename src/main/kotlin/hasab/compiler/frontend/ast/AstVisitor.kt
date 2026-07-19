package hasab.compiler.frontend.ast

/**
 * Generic AST Visitor pattern.
 *
 * Every concrete AST node has a dedicated visit method.
 * Implementations can override only the methods they need.
 *
 * @param T the return type of each visit method
 */
public interface AstVisitor<out T> {
    // Module
    public fun visitModule(node: Module): T

    // Types
    public fun visitIdentifierType(node: IdentifierType): T
    public fun visitQualifiedType(node: QualifiedType): T
    public fun visitArrayType(node: ArrayType): T
    public fun visitPointerType(node: PointerType): T
    public fun visitOptionalType(node: OptionalType): T
    public fun visitFunctionType(node: FunctionType): T
    public fun visitVoidType(node: VoidType): T

    // Expressions
    public fun visitIntegerLiteral(node: IntegerLiteralExpr): T
    public fun visitFloatLiteral(node: FloatLiteralExpr): T
    public fun visitStringLiteral(node: StringLiteralExpr): T
    public fun visitCharLiteral(node: CharLiteralExpr): T
    public fun visitBoolLiteral(node: BoolLiteralExpr): T
    public fun visitNilLiteral(node: NilLiteralExpr): T
    public fun visitIdentifier(node: IdentifierExpr): T
    public fun visitBinary(node: BinaryExpr): T
    public fun visitUnary(node: UnaryExpr): T
    public fun visitCall(node: CallExpr): T
    public fun visitIndex(node: IndexExpr): T
    public fun visitFieldAccess(node: FieldAccessExpr): T
    public fun visitParen(node: ParenExpr): T
    public fun visitArrayLiteral(node: ArrayLiteralExpr): T
    public fun visitArrayInit(node: ArrayInitExpr): T
    public fun visitIfExpr(node: IfExpr): T
    public fun visitAssignment(node: AssignmentExpr): T
    public fun visitCompoundAssignment(node: CompoundAssignmentExpr): T

    // Statements
    public fun visitExprStmt(node: ExprStmt): T
    public fun visitReturn(node: ReturnStmt): T
    public fun visitBreak(node: BreakStmt): T
    public fun visitContinue(node: ContinueStmt): T
    public fun visitLet(node: LetStmt): T
    public fun visitIfStmt(node: IfStmt): T
    public fun visitWhile(node: WhileStmt): T
    public fun visitFor(node: ForStmt): T
    public fun visitBlock(node: Block): T

    // Declarations
    public fun visitFnDecl(node: FnDecl): T
    public fun visitStructDecl(node: StructDecl): T
    public fun visitEnumDecl(node: EnumDecl): T
    public fun visitImplDecl(node: ImplDecl): T
    public fun visitTraitDecl(node: TraitDecl): T
    public fun visitTypeAlias(node: TypeAliasDecl): T
    public fun visitModDecl(node: ModDecl): T
    public fun visitUseDecl(node: UseDecl): T
    public fun visitPubDecl(node: PubDecl): T
}

// ---- Dispatch helper ----

public fun <T> AstNode.accept(visitor: AstVisitor<T>): T = when (this) {
    is Module -> visitor.visitModule(this)
    is IdentifierType -> visitor.visitIdentifierType(this)
    is QualifiedType -> visitor.visitQualifiedType(this)
    is ArrayType -> visitor.visitArrayType(this)
    is PointerType -> visitor.visitPointerType(this)
    is OptionalType -> visitor.visitOptionalType(this)
    is FunctionType -> visitor.visitFunctionType(this)
    is VoidType -> visitor.visitVoidType(this)
    is IntegerLiteralExpr -> visitor.visitIntegerLiteral(this)
    is FloatLiteralExpr -> visitor.visitFloatLiteral(this)
    is StringLiteralExpr -> visitor.visitStringLiteral(this)
    is CharLiteralExpr -> visitor.visitCharLiteral(this)
    is BoolLiteralExpr -> visitor.visitBoolLiteral(this)
    is NilLiteralExpr -> visitor.visitNilLiteral(this)
    is IdentifierExpr -> visitor.visitIdentifier(this)
    is BinaryExpr -> visitor.visitBinary(this)
    is UnaryExpr -> visitor.visitUnary(this)
    is CallExpr -> visitor.visitCall(this)
    is IndexExpr -> visitor.visitIndex(this)
    is FieldAccessExpr -> visitor.visitFieldAccess(this)
    is SafeFieldAccessExpr -> visitor.visitFieldAccess(FieldAccessExpr(this.callee, this.fieldName, this.fileName, this.line, this.column, this.startOffset, this.endOffset))
    is NullAssertExpr -> visitor.visitParen(ParenExpr(this.operand, this.fileName, this.line, this.column, this.startOffset, this.endOffset))
    is ParenExpr -> visitor.visitParen(this)
    is ArrayLiteralExpr -> visitor.visitArrayLiteral(this)
    is ArrayInitExpr -> visitor.visitArrayInit(this)
    is IfExpr -> visitor.visitIfExpr(this)
    is AssignmentExpr -> visitor.visitAssignment(this)
    is CompoundAssignmentExpr -> visitor.visitCompoundAssignment(this)
    is ExprStmt -> visitor.visitExprStmt(this)
    is ReturnStmt -> visitor.visitReturn(this)
    is BreakStmt -> visitor.visitBreak(this)
    is ContinueStmt -> visitor.visitContinue(this)
    is LetStmt -> visitor.visitLet(this)
    is IfStmt -> visitor.visitIfStmt(this)
    is WhileStmt -> visitor.visitWhile(this)
    is ForStmt -> visitor.visitFor(this)
    is Block -> visitor.visitBlock(this)
    is FnDecl -> visitor.visitFnDecl(this)
    is StructDecl -> visitor.visitStructDecl(this)
    is EnumDecl -> visitor.visitEnumDecl(this)
    is ImplDecl -> visitor.visitImplDecl(this)
    is TraitDecl -> visitor.visitTraitDecl(this)
    is TypeAliasDecl -> visitor.visitTypeAlias(this)
    is ModDecl -> visitor.visitModDecl(this)
    is UseDecl -> visitor.visitUseDecl(this)
    is PubDecl -> visitor.visitPubDecl(this)
}
