package hasab.compiler.frontend.ast

/**
 * Pretty-prints an AST as a readable indented tree.
 *
 * Usage:
 * ```
 * val printer = AstPrinter()
 * val output = printer.print(module)
 * println(output)
 * ```
 *
 * Output format:
 * ```
 * Module(name="test")
 *   FnDecl(name="main", public=false)
 *     Block
 *       ExprStmt
 *         CallExpr
 *           IdentifierExpr(name="println")
 *           IntegerLiteralExpr(value="42")
 * ```
 */
public class AstPrinter : AstVisitorBase<String>(default = "") {

    private val sb = StringBuilder()
    private var indent = 0

    public fun print(node: AstNode): String {
        sb.clear()
        indent = 0
        visit(node)
        return sb.toString()
    }

    override fun visit(node: AstNode): String {
        node.children().forEach { child ->
            // We don't call visit(child) here — each visitXxx handles its own output
        }
        return super.visit(node)
    }

    private fun line(text: String) {
        sb.append("  ".repeat(indent))
        sb.appendLine(text)
    }

    private inline fun withIndent(crossinline block: () -> Unit) {
        indent++
        block()
        indent--
    }

    // ---- Module ----

    override fun visitModule(node: Module): String {
        line("Module(name=${node.name?.quote()})")
        withIndent {
            node.declarations.forEach { it.accept(this) }
        }
        return ""
    }

    // ---- Types ----

    override fun visitIdentifierType(node: IdentifierType): String {
        line("IdentifierType(name=${node.name.quote()})")
        return ""
    }

    override fun visitQualifiedType(node: QualifiedType): String {
        line("QualifiedType(path=${node.path.joinToString("::")})")
        return ""
    }

    override fun visitArrayType(node: ArrayType): String {
        line("ArrayType")
        withIndent { node.elementType.accept(this) }
        return ""
    }

    override fun visitPointerType(node: PointerType): String {
        line("PointerType")
        withIndent { node.elementType.accept(this) }
        return ""
    }

    override fun visitOptionalType(node: OptionalType): String {
        line("OptionalType")
        withIndent { node.elementType.accept(this) }
        return ""
    }

    override fun visitFunctionType(node: FunctionType): String {
        line("FunctionType(params=${node.parameterTypes.size})")
        withIndent {
            node.parameterTypes.forEach { it.accept(this) }
            line("-> returnType")
            withIndent { node.returnType.accept(this) }
        }
        return ""
    }

    override fun visitVoidType(node: VoidType): String {
        line("VoidType")
        return ""
    }

    // ---- Expressions ----

    override fun visitIntegerLiteral(node: IntegerLiteralExpr): String {
        line("IntegerLiteralExpr(value=${node.value.quote()})")
        return ""
    }

    override fun visitFloatLiteral(node: FloatLiteralExpr): String {
        line("FloatLiteralExpr(value=${node.value.quote()})")
        return ""
    }

    override fun visitStringLiteral(node: StringLiteralExpr): String {
        line("StringLiteralExpr(value=${node.value.quote()})")
        return ""
    }

    override fun visitCharLiteral(node: CharLiteralExpr): String {
        line("CharLiteralExpr(value=${node.value.quote()})")
        return ""
    }

    override fun visitBoolLiteral(node: BoolLiteralExpr): String {
        line("BoolLiteralExpr(value=${node.value})")
        return ""
    }

    override fun visitNilLiteral(node: NilLiteralExpr): String {
        line("NilLiteralExpr")
        return ""
    }

    override fun visitIdentifier(node: IdentifierExpr): String {
        line("IdentifierExpr(name=${node.name.quote()})")
        return ""
    }

    override fun visitBinary(node: BinaryExpr): String {
        line("BinaryExpr(operator=${node.operator.quote()})")
        withIndent {
            node.left.accept(this)
            node.right.accept(this)
        }
        return ""
    }

    override fun visitUnary(node: UnaryExpr): String {
        line("UnaryExpr(operator=${node.operator.quote()})")
        withIndent { node.operand.accept(this) }
        return ""
    }

    override fun visitCall(node: CallExpr): String {
        line("CallExpr(args=${node.arguments.size})")
        withIndent {
            node.callee.accept(this)
            node.arguments.forEach { it.accept(this) }
        }
        return ""
    }

    override fun visitIndex(node: IndexExpr): String {
        line("IndexExpr")
        withIndent {
            node.callee.accept(this)
            node.index.accept(this)
        }
        return ""
    }

    override fun visitFieldAccess(node: FieldAccessExpr): String {
        line("FieldAccessExpr(fieldName=${node.fieldName.quote()})")
        withIndent { node.callee.accept(this) }
        return ""
    }

    override fun visitParen(node: ParenExpr): String {
        line("ParenExpr")
        withIndent { node.inner.accept(this) }
        return ""
    }

    override fun visitArrayLiteral(node: ArrayLiteralExpr): String {
        line("ArrayLiteralExpr(elements=${node.elements.size})")
        withIndent { node.elements.forEach { it.accept(this) } }
        return ""
    }

    override fun visitArrayInit(node: ArrayInitExpr): String {
        line("ArrayInitExpr")
        withIndent {
            node.elementType?.accept(this)
            line("size")
            withIndent { node.size.accept(this) }
        }
        return ""
    }

    override fun visitIfExpr(node: IfExpr): String {
        line("IfExpr")
        withIndent {
            line("condition")
            withIndent { node.condition.accept(this) }
            line("then")
            withIndent { node.thenBranch.accept(this) }
            if (node.elseBranch != null) {
                line("else")
                withIndent { node.elseBranch!!.accept(this) }
            }
        }
        return ""
    }

    override fun visitAssignment(node: AssignmentExpr): String {
        line("AssignmentExpr")
        withIndent {
            line("target")
            withIndent { node.target.accept(this) }
            line("value")
            withIndent { node.value.accept(this) }
        }
        return ""
    }

    override fun visitCompoundAssignment(node: CompoundAssignmentExpr): String {
        line("CompoundAssignmentExpr(operator=${node.operator.quote()})")
        withIndent {
            line("target")
            withIndent { node.target.accept(this) }
            line("value")
            withIndent { node.value.accept(this) }
        }
        return ""
    }

    // ---- Statements ----

    override fun visitExprStmt(node: ExprStmt): String {
        line("ExprStmt")
        withIndent { node.expression.accept(this) }
        return ""
    }

    override fun visitReturn(node: ReturnStmt): String {
        line("ReturnStmt")
        withIndent { node.value?.accept(this) }
        return ""
    }

    override fun visitBreak(node: BreakStmt): String {
        line("BreakStmt")
        return ""
    }

    override fun visitContinue(node: ContinueStmt): String {
        line("ContinueStmt")
        return ""
    }

    override fun visitLet(node: LetStmt): String {
        line("LetStmt(name=${node.name.quote()}, mutable=${node.isMutable})")
        withIndent {
            node.typeAnnotation?.let {
                line("typeAnnotation")
                withIndent { it.accept(this) }
            }
            line("initializer")
            withIndent { node.initializer.accept(this) }
        }
        return ""
    }

    override fun visitIfStmt(node: IfStmt): String {
        line("IfStmt")
        withIndent {
            line("condition")
            withIndent { node.condition.accept(this) }
            line("then")
            withIndent { node.thenBranch.accept(this) }
            if (node.elseBranch != null) {
                line("else")
                withIndent { (node.elseBranch as Stmt).accept(this) }
            }
        }
        return ""
    }

    override fun visitWhile(node: WhileStmt): String {
        line("WhileStmt")
        withIndent {
            line("condition")
            withIndent { node.condition.accept(this) }
            line("body")
            withIndent { node.body.accept(this) }
        }
        return ""
    }

    override fun visitFor(node: ForStmt): String {
        line("ForStmt(variable=${node.variable.quote()})")
        withIndent {
            line("iterable")
            withIndent { node.iterable.accept(this) }
            line("body")
            withIndent { node.body.accept(this) }
        }
        return ""
    }

    override fun visitBlock(node: Block): String {
        line("Block(stmts=${node.statements.size})")
        withIndent { node.statements.forEach { it.accept(this) } }
        return ""
    }

    // ---- Declarations ----

    override fun visitFnDecl(node: FnDecl): String {
        line("FnDecl(name=${node.name.quote()}, public=${node.isPublic})")
        withIndent {
            if (node.parameters.isNotEmpty()) {
                line("params")
                withIndent {
                    node.parameters.forEach { p ->
                        line("Param(name=${p.name.quote()}, mutable=${p.isMutable})")
                        p.type?.let { withIndent { it.accept(this) } }
                    }
                }
            }
            if (node.returnType != null) {
                line("returnType")
                withIndent { node.returnType!!.accept(this) }
            }
            if (node.body != null) {
                node.body!!.accept(this)
            } else {
                line("(extern)")
            }
        }
        return ""
    }

    override fun visitStructDecl(node: StructDecl): String {
        line("StructDecl(name=${node.name.quote()}, public=${node.isPublic})")
        withIndent {
            node.fields.forEach { f ->
                line("Field(name=${f.name.quote()}, mutable=${f.isMutable})")
                withIndent { f.type.accept(this) }
            }
        }
        return ""
    }

    override fun visitEnumDecl(node: EnumDecl): String {
        line("EnumDecl(name=${node.name.quote()}, public=${node.isPublic})")
        withIndent {
            node.variants.forEach { v ->
                line("Variant(name=${v.name.quote()})")
                withIndent {
                    v.fields.forEach { f ->
                        line("Field(name=${f.name.quote()})")
                        withIndent { f.type.accept(this) }
                    }
                }
            }
        }
        return ""
    }

    override fun visitImplDecl(node: ImplDecl): String {
        line("ImplDecl")
        withIndent {
            line("targetType")
            withIndent { node.targetType.accept(this) }
            node.methods.forEach { visitFnDecl(it) }
        }
        return ""
    }

    override fun visitTraitDecl(node: TraitDecl): String {
        line("TraitDecl(name=${node.name.quote()}, public=${node.isPublic})")
        withIndent { node.methods.forEach { visitFnDecl(it) } }
        return ""
    }

    override fun visitTypeAlias(node: TypeAliasDecl): String {
        line("TypeAliasDecl(name=${node.name.quote()}, public=${node.isPublic})")
        withIndent { node.target.accept(this) }
        return ""
    }

    override fun visitModDecl(node: ModDecl): String {
        line("ModDecl(name=${node.name.quote()}, public=${node.isPublic})")
        withIndent { node.body?.forEach { it.accept(this) } }
        return ""
    }

    override fun visitUseDecl(node: UseDecl): String {
        line("UseDecl(path=${node.path.joinToString("::")}, public=${node.isPublic})")
        return ""
    }

    override fun visitPubDecl(node: PubDecl): String {
        line("PubDecl")
        withIndent { node.inner.accept(this) }
        return ""
    }

    private fun String.quote(): String = "\"${this.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}
