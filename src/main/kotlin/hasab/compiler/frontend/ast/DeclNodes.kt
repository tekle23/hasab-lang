package hasab.compiler.frontend.ast

// -- Declaration AST Nodes --

public data class FunctionParam(
    val name: String,
    val type: TypeNode?,
    val isMutable: Boolean,
    val fileName: String,
    val line: Int,
    val column: Int,
    val startOffset: Int,
    val endOffset: Int,
    val docComment: String? = null,
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
    val docComment: String? = null,
)

public data class EnumVariant(
    val name: String,
    val fields: List<StructField>,
    val fileName: String,
    val line: Int,
    val column: Int,
    val startOffset: Int,
    val endOffset: Int,
    val docComment: String? = null,
)

public sealed interface Decl : AstNode

public data class FnDecl(
    val name: String,
    val originalName: String = name,
    val parameters: List<FunctionParam>,
    val returnType: TypeNode?,
    val body: Block?,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> =
        parameters.mapNotNull { it.type } + listOfNotNull(returnType, body)
}

public data class StructDecl(
    val name: String,
    val fields: List<StructField>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = fields.map { it.type }
}

public data class EnumDecl(
    val name: String,
    val variants: List<EnumVariant>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = variants.flatMap { v -> v.fields.map { it.type } }
}

public data class ImplDecl(
    val targetType: TypeNode,
    val methods: List<FnDecl>,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = listOf(targetType) + methods
}

public data class TraitDecl(
    val name: String,
    val methods: List<FnDecl>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = methods
}

public data class TypeAliasDecl(
    val name: String,
    val target: TypeNode,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = listOf(target)
}

public data class ModDecl(
    val name: String,
    val body: List<Decl>?,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = body ?: emptyList()
}

public data class UseDecl(
    val path: List<String>,
    val isPublic: Boolean,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = emptyList()
}

public data class PubDecl(
    val inner: Decl,
    override val fileName: String,
    override val line: Int,
    override val column: Int,
    override val startOffset: Int,
    override val endOffset: Int,
    override val docComment: String? = null,
) : Decl {
    override fun children(): List<AstNode> = listOf(inner)
}
