package hasab.compiler.frontend.ast

// ── Type AST Nodes ─────────────────────────────────────────────

public sealed interface TypeNode : AstNode

public data class IdentifierType(
    val name: String,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : TypeNode

public data class QualifiedType(
    val path: List<String>,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : TypeNode

public data class ArrayType(
    val elementType: TypeNode,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : TypeNode

public data class PointerType(
    val elementType: TypeNode,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : TypeNode

public data class OptionalType(
    val elementType: TypeNode,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : TypeNode

public data class FunctionType(
    val parameterTypes: List<TypeNode>,
    val returnType: TypeNode,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : TypeNode

public data class VoidType(
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : TypeNode
