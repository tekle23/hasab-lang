package hasab.compiler.frontend.ast

/**
 * Base visitor that recursively walks the entire AST tree.
 *
 * Each visit method calls [visit] on child nodes, giving subclasses
 * a convenient way to perform tree-wide operations without
 * manually handling every node type.
 *
 * @param T the return type (defaults to [Unit])
 */
public open class AstVisitorBase<out T : Any?>(
    private val default: @UnsafeVariance T,
) : AstVisitor<T> {

    // The recursive entry point for any node.
    public open fun visit(node: AstNode): T = node.accept(this)

    // Module
    override fun visitModule(node: Module): T {
        node.declarations.forEach { visit(it) }
        return default
    }

    // Types
    override fun visitIdentifierType(node: IdentifierType): T = default
    override fun visitQualifiedType(node: QualifiedType): T = default
    override fun visitArrayType(node: ArrayType): T = run { node.elementType.accept(this); default }
    override fun visitPointerType(node: PointerType): T = run { node.elementType.accept(this); default }
    override fun visitOptionalType(node: OptionalType): T = run { node.elementType.accept(this); default }
    override fun visitFunctionType(node: FunctionType): T = run {
        node.parameterTypes.forEach { it.accept(this) }
        node.returnType.accept(this)
        default
    }
    override fun visitVoidType(node: VoidType): T = default

    // Expressions
    override fun visitIntegerLiteral(node: IntegerLiteralExpr): T = default
    override fun visitFloatLiteral(node: FloatLiteralExpr): T = default
    override fun visitStringLiteral(node: StringLiteralExpr): T = default
    override fun visitCharLiteral(node: CharLiteralExpr): T = default
    override fun visitBoolLiteral(node: BoolLiteralExpr): T = default
    override fun visitNilLiteral(node: NilLiteralExpr): T = default
    override fun visitIdentifier(node: IdentifierExpr): T = default

    override fun visitBinary(node: BinaryExpr): T = run {
        node.left.accept(this)
        node.right.accept(this)
        default
    }

    override fun visitUnary(node: UnaryExpr): T = run {
        node.operand.accept(this)
        default
    }

    override fun visitCall(node: CallExpr): T = run {
        node.callee.accept(this)
        node.arguments.forEach { it.accept(this) }
        default
    }

    override fun visitIndex(node: IndexExpr): T = run {
        node.callee.accept(this)
        node.index.accept(this)
        default
    }

    override fun visitFieldAccess(node: FieldAccessExpr): T = run {
        node.callee.accept(this)
        default
    }

    override fun visitParen(node: ParenExpr): T = run {
        node.inner.accept(this)
        default
    }

    override fun visitArrayLiteral(node: ArrayLiteralExpr): T = run {
        node.elements.forEach { it.accept(this) }
        default
    }

    override fun visitArrayInit(node: ArrayInitExpr): T = run {
        node.elementType?.accept(this)
        node.size.accept(this)
        default
    }

    override fun visitIfExpr(node: IfExpr): T = run {
        node.condition.accept(this)
        node.thenBranch.accept(this)
        node.elseBranch?.accept(this)
        default
    }

    override fun visitAssignment(node: AssignmentExpr): T = run {
        node.target.accept(this)
        node.value.accept(this)
        default
    }

    override fun visitCompoundAssignment(node: CompoundAssignmentExpr): T = run {
        node.target.accept(this)
        node.value.accept(this)
        default
    }

    // Statements
    override fun visitExprStmt(node: ExprStmt): T = run {
        node.expression.accept(this)
        default
    }

    override fun visitReturn(node: ReturnStmt): T = run {
        node.value?.accept(this)
        default
    }

    override fun visitBreak(node: BreakStmt): T = default
    override fun visitContinue(node: ContinueStmt): T = default

    override fun visitLet(node: LetStmt): T = run {
        node.typeAnnotation?.accept(this)
        node.initializer.accept(this)
        default
    }

    override fun visitIfStmt(node: IfStmt): T = run {
        node.condition.accept(this)
        node.thenBranch.accept(this)
        (node.elseBranch as? Stmt)?.accept(this)
        default
    }

    override fun visitWhile(node: WhileStmt): T = run {
        node.condition.accept(this)
        node.body.accept(this)
        default
    }

    override fun visitFor(node: ForStmt): T = run {
        node.iterable.accept(this)
        node.body.accept(this)
        default
    }

    override fun visitBlock(node: Block): T = run {
        node.statements.forEach { it.accept(this) }
        default
    }

    // Declarations
    override fun visitFnDecl(node: FnDecl): T = run {
        node.parameters.forEach { it.type?.accept(this) }
        node.returnType?.accept(this)
        node.body?.accept(this)
        default
    }

    override fun visitStructDecl(node: StructDecl): T = run {
        node.fields.forEach { it.type.accept(this) }
        default
    }

    override fun visitEnumDecl(node: EnumDecl): T = run {
        node.variants.forEach { v -> v.fields.forEach { it.type.accept(this) } }
        default
    }

    override fun visitImplDecl(node: ImplDecl): T = run {
        node.targetType.accept(this)
        node.methods.forEach { visitFnDecl(it) }
        default
    }

    override fun visitTraitDecl(node: TraitDecl): T = run {
        node.methods.forEach { visitFnDecl(it) }
        default
    }

    override fun visitTypeAlias(node: TypeAliasDecl): T = run {
        node.target.accept(this)
        default
    }

    override fun visitModDecl(node: ModDecl): T = run {
        node.body?.forEach { visit(it) }
        default
    }

    override fun visitUseDecl(node: UseDecl): T = default

    override fun visitPubDecl(node: PubDecl): T = run {
        visitDecl(node.inner)
        default
    }

    private fun visitDecl(node: Decl): T = when (node) {
        is FnDecl -> visitFnDecl(node)
        is StructDecl -> visitStructDecl(node)
        is EnumDecl -> visitEnumDecl(node)
        is ImplDecl -> visitImplDecl(node)
        is TraitDecl -> visitTraitDecl(node)
        is TypeAliasDecl -> visitTypeAlias(node)
        is ModDecl -> visitModDecl(node)
        is UseDecl -> visitUseDecl(node)
        is PubDecl -> visitPubDecl(node)
    }
}
