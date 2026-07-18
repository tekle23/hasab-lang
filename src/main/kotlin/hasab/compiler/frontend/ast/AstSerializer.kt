package hasab.compiler.frontend.ast

/**
 * Serializes an AST to a JSON string representation.
 *
 * Each node is represented as a JSON object with:
 * - `"type"`: the simple class name of the node
 * - `"sourceLocation"`: `{fileName, line, column, startOffset, endOffset}`
 * - `"docComment"`: the documentation comment if present
 * - Node-specific fields (name, value, operator, etc.)
 * - `"children"`: a list of serialized child nodes (when applicable)
 *
 * Usage:
 * ```
 * val serializer = AstSerializer()
 * val json = serializer.serialize(module)
 * println(json)
 * ```
 */
public class AstSerializer : AstVisitorBase<String>(default = "{}") {

    private val sb = StringBuilder()
    private var indent = 0

    public fun serialize(node: AstNode): String {
        sb.clear()
        indent = 0
        node.accept(this)
        return sb.toString()
    }

    // ---- Helpers ----

    private fun line(text: String) {
        sb.append("  ".repeat(indent))
        sb.appendLine(text)
    }

    private inline fun withIndent(crossinline block: () -> Unit) {
        indent++
        block()
        indent--
    }

    private fun emitObject(type: String, node: AstNode, extraFields: String = "", children: List<AstNode> = emptyList()) {
        line("{")
        withIndent {
            line("\"type\": \"$type\",")
            emitSourceLocation(node)
            node.docComment?.let { line("\"docComment\": ${it.toJsonString()},") }
            if (extraFields.isNotEmpty()) {
                line(extraFields)
            }
            if (children.isNotEmpty()) {
                line("\"children\": [")
                withIndent {
                    children.forEachIndexed { idx, child ->
                        child.accept(this)
                        if (idx < children.size - 1) sb.append(",")
                        sb.appendLine()
                    }
                }
                line("]")
            }
        }
        line("}")
    }

    private fun emitSourceLocation(node: AstNode) {
        line("\"fileName\": ${node.fileName.toJsonString()},")
        line("\"line\": ${node.line},")
        line("\"column\": ${node.column},")
        line("\"startOffset\": ${node.startOffset},")
        line("\"endOffset\": ${node.endOffset},")
    }

    private fun String.toJsonString(): String = "\"${this.replace("\\", "\\\\").replace("\"", "\\\"")}\""

    private fun List<String>.toJsonStringArray(): String =
        "[${joinToString(", ") { it.toJsonString() }}]"

    private fun List<AstNode>.serializeList() {
        forEachIndexed { idx, child ->
            child.accept(this@AstSerializer)
            if (idx < size - 1) sb.append(",")
            sb.appendLine()
        }
    }

    // ---- Module ----

    override fun visitModule(node: Module): String {
        emitObject("Module", node,
            extraFields = "\"name\": ${node.name?.toJsonString()},",
            children = node.declarations,
        )
        return ""
    }

    // ---- Types ----

    override fun visitIdentifierType(node: IdentifierType): String {
        emitObject("IdentifierType", node, extraFields = "\"name\": ${node.name.toJsonString()}")
        return ""
    }

    override fun visitQualifiedType(node: QualifiedType): String {
        emitObject("QualifiedType", node, extraFields = "\"path\": ${node.path.toJsonStringArray()}")
        return ""
    }

    override fun visitArrayType(node: ArrayType): String {
        emitObject("ArrayType", node, children = listOf(node.elementType))
        return ""
    }

    override fun visitPointerType(node: PointerType): String {
        emitObject("PointerType", node, children = listOf(node.elementType))
        return ""
    }

    override fun visitOptionalType(node: OptionalType): String {
        emitObject("OptionalType", node, children = listOf(node.elementType))
        return ""
    }

    override fun visitFunctionType(node: FunctionType): String {
        emitObject("FunctionType", node, children = node.parameterTypes + node.returnType)
        return ""
    }

    override fun visitVoidType(node: VoidType): String {
        emitObject("VoidType", node)
        return ""
    }

    // ---- Expressions ----

    override fun visitIntegerLiteral(node: IntegerLiteralExpr): String {
        emitObject("IntegerLiteralExpr", node, extraFields = "\"value\": ${node.value.toJsonString()}")
        return ""
    }

    override fun visitFloatLiteral(node: FloatLiteralExpr): String {
        emitObject("FloatLiteralExpr", node, extraFields = "\"value\": ${node.value.toJsonString()}")
        return ""
    }

    override fun visitStringLiteral(node: StringLiteralExpr): String {
        emitObject("StringLiteralExpr", node, extraFields = "\"value\": ${node.value.toJsonString()}")
        return ""
    }

    override fun visitCharLiteral(node: CharLiteralExpr): String {
        emitObject("CharLiteralExpr", node, extraFields = "\"value\": ${node.value.toJsonString()}")
        return ""
    }

    override fun visitBoolLiteral(node: BoolLiteralExpr): String {
        emitObject("BoolLiteralExpr", node, extraFields = "\"value\": ${node.value}")
        return ""
    }

    override fun visitNilLiteral(node: NilLiteralExpr): String {
        emitObject("NilLiteralExpr", node)
        return ""
    }

    override fun visitIdentifier(node: IdentifierExpr): String {
        emitObject("IdentifierExpr", node, extraFields = "\"name\": ${node.name.toJsonString()}")
        return ""
    }

    override fun visitBinary(node: BinaryExpr): String {
        emitObject("BinaryExpr", node,
            extraFields = "\"operator\": ${node.operator.toJsonString()},",
            children = listOf(node.left, node.right),
        )
        return ""
    }

    override fun visitUnary(node: UnaryExpr): String {
        emitObject("UnaryExpr", node,
            extraFields = "\"operator\": ${node.operator.toJsonString()},",
            children = listOf(node.operand),
        )
        return ""
    }

    override fun visitCall(node: CallExpr): String {
        emitObject("CallExpr", node,
            extraFields = "\"argumentCount\": ${node.arguments.size},",
            children = listOf(node.callee) + node.arguments,
        )
        return ""
    }

    override fun visitIndex(node: IndexExpr): String {
        emitObject("IndexExpr", node, children = listOf(node.callee, node.index))
        return ""
    }

    override fun visitFieldAccess(node: FieldAccessExpr): String {
        emitObject("FieldAccessExpr", node,
            extraFields = "\"fieldName\": ${node.fieldName.toJsonString()},",
            children = listOf(node.callee),
        )
        return ""
    }

    override fun visitParen(node: ParenExpr): String {
        emitObject("ParenExpr", node, children = listOf(node.inner))
        return ""
    }

    override fun visitArrayLiteral(node: ArrayLiteralExpr): String {
        emitObject("ArrayLiteralExpr", node, children = node.elements)
        return ""
    }

    override fun visitArrayInit(node: ArrayInitExpr): String {
        emitObject("ArrayInitExpr", node, children = listOfNotNull(node.elementType, node.size))
        return ""
    }

    override fun visitIfExpr(node: IfExpr): String {
        emitObject("IfExpr", node, children = listOfNotNull(node.condition, node.thenBranch, node.elseBranch))
        return ""
    }

    override fun visitAssignment(node: AssignmentExpr): String {
        emitObject("AssignmentExpr", node, children = listOf(node.target, node.value))
        return ""
    }

    override fun visitCompoundAssignment(node: CompoundAssignmentExpr): String {
        emitObject("CompoundAssignmentExpr", node,
            extraFields = "\"operator\": ${node.operator.toJsonString()},",
            children = listOf(node.target, node.value),
        )
        return ""
    }

    // ---- Statements ----

    override fun visitExprStmt(node: ExprStmt): String {
        emitObject("ExprStmt", node, children = listOf(node.expression))
        return ""
    }

    override fun visitReturn(node: ReturnStmt): String {
        emitObject("ReturnStmt", node, children = listOfNotNull(node.value))
        return ""
    }

    override fun visitBreak(node: BreakStmt): String {
        emitObject("BreakStmt", node)
        return ""
    }

    override fun visitContinue(node: ContinueStmt): String {
        emitObject("ContinueStmt", node)
        return ""
    }

    override fun visitLet(node: LetStmt): String {
        emitObject("LetStmt", node,
            extraFields = "\"name\": ${node.name.toJsonString()},\"isMutable\": ${node.isMutable},",
            children = listOfNotNull(node.typeAnnotation, node.initializer),
        )
        return ""
    }

    override fun visitIfStmt(node: IfStmt): String {
        emitObject("IfStmt", node, children = listOf(node.condition, node.thenBranch) + listOfNotNull(node.elseBranch))
        return ""
    }

    override fun visitWhile(node: WhileStmt): String {
        emitObject("WhileStmt", node, children = listOf(node.condition, node.body))
        return ""
    }

    override fun visitFor(node: ForStmt): String {
        emitObject("ForStmt", node,
            extraFields = "\"variable\": ${node.variable.toJsonString()},",
            children = listOf(node.iterable, node.body),
        )
        return ""
    }

    override fun visitBlock(node: Block): String {
        emitObject("Block", node, children = node.statements)
        return ""
    }

    // ---- Declarations ----

    override fun visitFnDecl(node: FnDecl): String {
        emitObject("FnDecl", node,
            extraFields = "\"name\": ${node.name.toJsonString()},\"isPublic\": ${node.isPublic},",
            children = node.body?.let { listOf(it) } ?: emptyList(),
        )
        return ""
    }

    override fun visitStructDecl(node: StructDecl): String {
        emitObject("StructDecl", node,
            extraFields = "\"name\": ${node.name.toJsonString()},\"isPublic\": ${node.isPublic},",
        )
        return ""
    }

    override fun visitEnumDecl(node: EnumDecl): String {
        emitObject("EnumDecl", node,
            extraFields = "\"name\": ${node.name.toJsonString()},\"isPublic\": ${node.isPublic},",
        )
        return ""
    }

    override fun visitImplDecl(node: ImplDecl): String {
        emitObject("ImplDecl", node, children = listOf(node.targetType) + node.methods)
        return ""
    }

    override fun visitTraitDecl(node: TraitDecl): String {
        emitObject("TraitDecl", node,
            extraFields = "\"name\": ${node.name.toJsonString()},\"isPublic\": ${node.isPublic},",
            children = node.methods,
        )
        return ""
    }

    override fun visitTypeAlias(node: TypeAliasDecl): String {
        emitObject("TypeAliasDecl", node,
            extraFields = "\"name\": ${node.name.toJsonString()},\"isPublic\": ${node.isPublic},",
            children = listOf(node.target),
        )
        return ""
    }

    override fun visitModDecl(node: ModDecl): String {
        emitObject("ModDecl", node,
            extraFields = "\"name\": ${node.name.toJsonString()},\"isPublic\": ${node.isPublic},",
            children = node.body ?: emptyList(),
        )
        return ""
    }

    override fun visitUseDecl(node: UseDecl): String {
        emitObject("UseDecl", node,
            extraFields = "\"path\": ${node.path.toJsonStringArray()},\"isPublic\": ${node.isPublic},",
        )
        return ""
    }

    override fun visitPubDecl(node: PubDecl): String {
        emitObject("PubDecl", node, children = listOf(node.inner))
        return ""
    }
}
