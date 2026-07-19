package hasab.compiler.hir

import hasab.compiler.types.Type

// ---- HIR top-level ----

public data class HirModule(
    val name: String?,
    val declarations: List<HirDecl>,
) {
    val type: Type get() = hasab.compiler.types.VoidType
}

public sealed interface HirDecl : HirNode

// ---- Declarations ----

public data class HirParam(
    val name: String,
    val type: Type,
    val isMutable: Boolean,
)

public data class HirField(
    val name: String,
    val type: Type,
    val isMutable: Boolean,
)

public data class HirEnumVariant(
    val name: String,
    val fieldTypes: List<Type>,
)

public data class HirFnDecl(
    val name: String,
    val parameters: List<HirParam>,
    override val type: Type,
    val returnType: Type,
    val body: HirBlock?,
    val isPublic: Boolean,
) : HirDecl {
    override val hirChildren: List<HirNode> get() = listOfNotNull(body)
}

public data class HirStructDecl(
    val name: String,
    val fields: List<HirField>,
    val isPublic: Boolean,
) : HirDecl {
    override val type: Type get() = hasab.compiler.types.VoidType
}

public data class HirEnumDecl(
    val name: String,
    val variants: List<HirEnumVariant>,
    val isPublic: Boolean,
) : HirDecl {
    override val type: Type get() = hasab.compiler.types.VoidType
}

public data class HirTraitDecl(
    val name: String,
    val methods: List<HirFnDecl>,
    val isPublic: Boolean,
) : HirDecl {
    override val type: Type get() = hasab.compiler.types.VoidType
}

public data class HirTypeAliasDecl(
    val name: String,
    val targetType: Type,
    val isPublic: Boolean,
) : HirDecl {
    override val type: Type get() = hasab.compiler.types.VoidType
}

public data class HirImplDecl(
    val targetType: Type,
    val methods: List<HirFnDecl>,
) : HirDecl {
    override val type: Type get() = hasab.compiler.types.VoidType
}

public data class HirUseDecl(
    val path: List<String>,
    val isPublic: Boolean,
) : HirDecl {
    override val type: Type get() = hasab.compiler.types.VoidType
}
