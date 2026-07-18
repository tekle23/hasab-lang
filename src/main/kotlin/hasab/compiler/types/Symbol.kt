package hasab.compiler.types

public sealed interface Symbol {
    public val name: String
    public val type: ResolvedType
}

public data class VariableSymbol(
    override val name: String,
    override val type: ResolvedType,
    val isMutable: Boolean,
) : Symbol

public data class FunctionSymbol(
    override val name: String,
    val parameterTypes: List<ResolvedType>,
    override val type: ResolvedType,
) : Symbol {
    val returnType: ResolvedType get() = type
}

public data class StructSymbol(
    override val name: String,
    val fields: LinkedHashMap<String, ResolvedType>,
) : Symbol {
    override val type: ResolvedType get() = ResolvedType.StructType(name, fields)
}

public data class EnumSymbol(
    override val name: String,
    val variants: LinkedHashMap<String, List<ResolvedType>>,
) : Symbol {
    override val type: ResolvedType get() = ResolvedType.EnumType(name, variants)
}

public data class TypeAliasSymbol(
    override val name: String,
    override val type: ResolvedType,
) : Symbol

public data class TraitSymbol(
    override val name: String,
    val methods: LinkedHashMap<String, ResolvedType>,
) : Symbol {
    override val type: ResolvedType get() = ResolvedType.TraitType(name, methods)
}
