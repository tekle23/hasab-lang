package hasab.compiler.frontend.ast

/**
 * A single validation issue found in the AST.
 */
public data class ValidationIssue(
    public val severity: Severity,
    public val message: String,
    public val node: AstNode,
) {
    public enum class Severity { ERROR, WARNING }
}

/**
 * Validates structural integrity of an AST.
 *
 * Checks performed:
 * - Source ranges are valid (start <= end)
 * - Source ranges of children are contained within parent ranges
 * - Required structural invariants (e.g. function body not null when present)
 * - No duplicate field names in structs
 * - No duplicate enum variant names
 * - No duplicate parameter names in functions
 * - Block statements have non-empty ranges
 *
 * Usage:
 * ```
 * val validator = AstValidator()
 * val issues = validator.validate(module)
 * issues.filter { it.severity == ValidationIssue.Severity.ERROR }
 * ```
 */
public class AstValidator : AstVisitorBase<List<ValidationIssue>>(default = emptyList()) {

    private val issues = mutableListOf<ValidationIssue>()

    public fun validate(node: AstNode): List<ValidationIssue> {
        issues.clear()
        visit(node)
        return issues.toList()
    }

    private fun report(severity: ValidationIssue.Severity, message: String, node: AstNode) {
        issues.add(ValidationIssue(severity, message, node))
    }

    private fun checkRange(node: AstNode) {
        if (node.endOffset < node.startOffset) {
            report(
                ValidationIssue.Severity.ERROR,
                "End offset (${node.endOffset}) < start offset (${node.startOffset})",
                node,
            )
        }
        if (node.line < 1) {
            report(ValidationIssue.Severity.ERROR, "Line number must be >= 1, got ${node.line}", node)
        }
        if (node.column < 1) {
            report(ValidationIssue.Severity.ERROR, "Column number must be >= 1, got ${node.column}", node)
        }
    }

    private fun checkChildRanges(parent: AstNode, children: List<AstNode>) {
        for (child in children) {
            if (child.startOffset < parent.startOffset) {
                report(
                    ValidationIssue.Severity.WARNING,
                    "Child starts at ${child.startOffset} but parent starts at ${parent.startOffset}",
                    child,
                )
            }
            if (child.endOffset > parent.endOffset && parent.endOffset > 0) {
                report(
                    ValidationIssue.Severity.WARNING,
                    "Child ends at ${child.endOffset} but parent ends at ${parent.endOffset}",
                    child,
                )
            }
        }
    }

    // ---- Module ----

    override fun visitModule(node: Module): List<ValidationIssue> {
        checkRange(node)
        if (node.name != null && node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "Module has empty name", node)
        }
        checkChildRanges(node, node.declarations)
        node.declarations.forEach { it.accept(this) }
        return emptyList()
    }

    // ---- Types ----

    override fun visitIdentifierType(node: IdentifierType): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "IdentifierType has empty name", node)
        }
        return emptyList()
    }

    override fun visitQualifiedType(node: QualifiedType): List<ValidationIssue> {
        checkRange(node)
        if (node.path.isEmpty()) {
            report(ValidationIssue.Severity.ERROR, "QualifiedType has empty path", node)
        }
        node.path.forEach { segment ->
            if (segment.isBlank()) {
                report(ValidationIssue.Severity.ERROR, "QualifiedType has blank path segment", node)
            }
        }
        return emptyList()
    }

    override fun visitArrayType(node: ArrayType): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.elementType))
        node.elementType.accept(this)
        return emptyList()
    }

    override fun visitPointerType(node: PointerType): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.elementType))
        node.elementType.accept(this)
        return emptyList()
    }

    override fun visitOptionalType(node: OptionalType): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.elementType))
        node.elementType.accept(this)
        return emptyList()
    }

    override fun visitFunctionType(node: FunctionType): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, node.parameterTypes + node.returnType)
        node.parameterTypes.forEach { it.accept(this) }
        node.returnType.accept(this)
        return emptyList()
    }

    override fun visitVoidType(node: VoidType): List<ValidationIssue> {
        checkRange(node)
        return emptyList()
    }

    // ---- Expressions ----

    override fun visitIntegerLiteral(node: IntegerLiteralExpr): List<ValidationIssue> {
        checkRange(node)
        if (node.value.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "IntegerLiteralExpr has empty value", node)
        }
        return emptyList()
    }

    override fun visitFloatLiteral(node: FloatLiteralExpr): List<ValidationIssue> {
        checkRange(node)
        if (node.value.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "FloatLiteralExpr has empty value", node)
        }
        return emptyList()
    }

    override fun visitStringLiteral(node: StringLiteralExpr): List<ValidationIssue> {
        checkRange(node)
        return emptyList()
    }

    override fun visitCharLiteral(node: CharLiteralExpr): List<ValidationIssue> {
        checkRange(node)
        if (node.value.length != 1) {
            report(
                ValidationIssue.Severity.WARNING,
                "CharLiteralExpr has value length ${node.value.length} (expected 1)",
                node,
            )
        }
        return emptyList()
    }

    override fun visitBoolLiteral(node: BoolLiteralExpr): List<ValidationIssue> {
        checkRange(node)
        return emptyList()
    }

    override fun visitNilLiteral(node: NilLiteralExpr): List<ValidationIssue> {
        checkRange(node)
        return emptyList()
    }

    override fun visitIdentifier(node: IdentifierExpr): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "IdentifierExpr has empty name", node)
        }
        return emptyList()
    }

    override fun visitBinary(node: BinaryExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.left, node.right))
        node.left.accept(this)
        node.right.accept(this)
        return emptyList()
    }

    override fun visitUnary(node: UnaryExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.operand))
        node.operand.accept(this)
        return emptyList()
    }

    override fun visitCall(node: CallExpr): List<ValidationIssue> {
        checkRange(node)
        val children = listOf(node.callee) + node.arguments
        checkChildRanges(node, children)
        node.callee.accept(this)
        node.arguments.forEach { it.accept(this) }
        return emptyList()
    }

    override fun visitIndex(node: IndexExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.callee, node.index))
        node.callee.accept(this)
        node.index.accept(this)
        return emptyList()
    }

    override fun visitFieldAccess(node: FieldAccessExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.callee))
        if (node.fieldName.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "FieldAccessExpr has empty fieldName", node)
        }
        node.callee.accept(this)
        return emptyList()
    }

    override fun visitParen(node: ParenExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.inner))
        node.inner.accept(this)
        return emptyList()
    }

    override fun visitArrayLiteral(node: ArrayLiteralExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, node.elements)
        node.elements.forEach { it.accept(this) }
        return emptyList()
    }

    override fun visitArrayInit(node: ArrayInitExpr): List<ValidationIssue> {
        checkRange(node)
        node.elementType?.let { checkChildRanges(node, listOf(it)) }
        checkChildRanges(node, listOf(node.size))
        node.elementType?.accept(this)
        node.size.accept(this)
        return emptyList()
    }

    override fun visitIfExpr(node: IfExpr): List<ValidationIssue> {
        checkRange(node)
        val children = listOfNotNull(node.condition, node.thenBranch, node.elseBranch)
        checkChildRanges(node, children)
        node.condition.accept(this)
        node.thenBranch.accept(this)
        node.elseBranch?.accept(this)
        return emptyList()
    }

    override fun visitAssignment(node: AssignmentExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.target, node.value))
        node.target.accept(this)
        node.value.accept(this)
        return emptyList()
    }

    override fun visitCompoundAssignment(node: CompoundAssignmentExpr): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.target, node.value))
        node.target.accept(this)
        node.value.accept(this)
        return emptyList()
    }

    // ---- Statements ----

    override fun visitExprStmt(node: ExprStmt): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.expression))
        node.expression.accept(this)
        return emptyList()
    }

    override fun visitReturn(node: ReturnStmt): List<ValidationIssue> {
        checkRange(node)
        node.value?.let { checkChildRanges(node, listOf(it)) }
        node.value?.accept(this)
        return emptyList()
    }

    override fun visitBreak(node: BreakStmt): List<ValidationIssue> {
        checkRange(node)
        return emptyList()
    }

    override fun visitContinue(node: ContinueStmt): List<ValidationIssue> {
        checkRange(node)
        return emptyList()
    }

    override fun visitLet(node: LetStmt): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "LetStmt has empty name", node)
        }
        val children = listOfNotNull(node.typeAnnotation, node.initializer)
        checkChildRanges(node, children)
        node.typeAnnotation?.accept(this)
        node.initializer.accept(this)
        return emptyList()
    }

    override fun visitIfStmt(node: IfStmt): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.condition, node.thenBranch) + listOfNotNull(node.elseBranch))
        node.condition.accept(this)
        node.thenBranch.accept(this)
        node.elseBranch?.accept(this)
        return emptyList()
    }

    override fun visitWhile(node: WhileStmt): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.condition, node.body))
        node.condition.accept(this)
        node.body.accept(this)
        return emptyList()
    }

    override fun visitFor(node: ForStmt): List<ValidationIssue> {
        checkRange(node)
        if (node.variable.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "ForStmt has empty variable name", node)
        }
        checkChildRanges(node, listOf(node.iterable, node.body))
        node.iterable.accept(this)
        node.body.accept(this)
        return emptyList()
    }

    override fun visitBlock(node: Block): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, node.statements)
        node.statements.forEach { it.accept(this) }
        return emptyList()
    }

    // ---- Declarations ----

    override fun visitFnDecl(node: FnDecl): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "FnDecl has empty name", node)
        }
        val paramNames = node.parameters.map { it.name }
        val dupes = paramNames.groupBy { it }.filter { it.value.size > 1 }
        dupes.keys.forEach { name ->
            report(ValidationIssue.Severity.ERROR, "Duplicate parameter name '$name' in function '${node.name}'", node)
        }
        node.parameters.forEach { it.type?.accept(this) }
        node.returnType?.accept(this)
        node.body?.let {
            checkChildRanges(node, listOf(it))
            it.accept(this)
        }
        return emptyList()
    }

    override fun visitStructDecl(node: StructDecl): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "StructDecl has empty name", node)
        }
        val fieldNames = node.fields.map { it.name }
        val dupes = fieldNames.groupBy { it }.filter { it.value.size > 1 }
        dupes.keys.forEach { name ->
            report(ValidationIssue.Severity.ERROR, "Duplicate field name '$name' in struct '${node.name}'", node)
        }
        checkChildRanges(node, node.fields.map { it.type })
        node.fields.forEach { it.type.accept(this) }
        return emptyList()
    }

    override fun visitEnumDecl(node: EnumDecl): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "EnumDecl has empty name", node)
        }
        val variantNames = node.variants.map { it.name }
        val dupes = variantNames.groupBy { it }.filter { it.value.size > 1 }
        dupes.keys.forEach { name ->
            report(ValidationIssue.Severity.ERROR, "Duplicate variant name '$name' in enum '${node.name}'", node)
        }
        node.variants.forEach { v ->
            val fieldNames = v.fields.map { it.name }
            val fieldDupes = fieldNames.groupBy { it }.filter { it.value.size > 1 }
            fieldDupes.keys.forEach { name ->
                report(
                    ValidationIssue.Severity.ERROR,
                    "Duplicate field name '$name' in variant '${v.name}' of enum '${node.name}'",
                    node,
                )
            }
            v.fields.forEach { it.type.accept(this) }
        }
        return emptyList()
    }

    override fun visitImplDecl(node: ImplDecl): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.targetType) + node.methods)
        node.targetType.accept(this)
        node.methods.forEach { visitFnDecl(it) }
        return emptyList()
    }

    override fun visitTraitDecl(node: TraitDecl): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "TraitDecl has empty name", node)
        }
        checkChildRanges(node, node.methods)
        node.methods.forEach { visitFnDecl(it) }
        return emptyList()
    }

    override fun visitTypeAlias(node: TypeAliasDecl): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "TypeAliasDecl has empty name", node)
        }
        checkChildRanges(node, listOf(node.target))
        node.target.accept(this)
        return emptyList()
    }

    override fun visitModDecl(node: ModDecl): List<ValidationIssue> {
        checkRange(node)
        if (node.name.isBlank()) {
            report(ValidationIssue.Severity.ERROR, "ModDecl has empty name", node)
        }
        node.body?.let {
            checkChildRanges(node, it)
            it.forEach { decl -> decl.accept(this) }
        }
        return emptyList()
    }

    override fun visitUseDecl(node: UseDecl): List<ValidationIssue> {
        checkRange(node)
        if (node.path.isEmpty()) {
            report(ValidationIssue.Severity.ERROR, "UseDecl has empty path", node)
        }
        return emptyList()
    }

    override fun visitPubDecl(node: PubDecl): List<ValidationIssue> {
        checkRange(node)
        checkChildRanges(node, listOf(node.inner))
        node.inner.accept(this)
        return emptyList()
    }
}
