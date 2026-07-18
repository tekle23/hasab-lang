package hasab.compiler.frontend.ast

// ── Declaration AST Nodes ──────────────────────────────────────

public data class FunctionParam(
    val name: String,
    val type: TypeNode?,
    val isMutable: Boolean,
    val fileName: String,
    val line: Int,
    val column: Int,
    val startOffset: Int,
    val endOffset: Int,
)

public data class StructField(
    val name: String,
    val type: TypeNode,
    val isMutable: Boolean,
    val fileName: String,
    val line: Int,
    val column: Int,
    val startOffset: Int,
    val endOffset: Int,
)

public data class EnumVariant(
    val name: String,
    val fields: List<StructField>,
    val fileName: String,
    val line: Int,
    val column: Int,
    val startOffset: Int,
    val endOffset: Int,
)

public sealed interface Decl : AstNode

public data class FnDecl(
    val name: String,
    val parameters: List<FunctionParam>,
    val returnType: TypeNode?,
    val body: Block?,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class StructDecl(
    val name: String,
    val fields: List<StructField>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class EnumDecl(
    val name: String,
    val variants: List<EnumVariant>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class ImplDecl(
    val targetType: TypeNode,
    val methods: List<FnDecl>,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class TraitDecl(
    val name: String,
    val methods: List<FnDecl>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class TypeAliasDecl(
    val name: String,
    val target: TypeNode,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class ModDecl(
    val name: String,
    val body: List<Decl>?,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class UseDecl(
    val path: List<String>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl

public data class PubDecl(
    val inner: Decl,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
) : Decl
