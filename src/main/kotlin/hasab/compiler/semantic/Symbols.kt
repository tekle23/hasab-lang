package hasab.compiler.semantic

import hasab.compiler.frontend.lexer.SourceRange

/**
 * Base interface for all semantic symbols.
 * Every declaration in HASAB becomes a Symbol.
 */
public sealed interface Symbol {
    public val name: String
    public val kind: SymbolKind
    public val visibility: Visibility
    public val range: SourceRange
    public val fileName: String
    public val docComment: String?
    public val parentModule: String?
}

/**
 * A variable declaration.
 */
public data class VariableSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
    val isMutable: Boolean = false,
    val typeAnnotation: String? = null,
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.VARIABLE
}

/**
 * A function declaration.
 */
public data class FunctionSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
    val parameterCount: Int = 0,
    val isExtern: Boolean = false,
    val childSymbols: List<String> = emptyList(),
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.FUNCTION
}

/**
 * A struct declaration.
 */
public data class StructSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
    val childSymbols: List<String> = emptyList(),
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.STRUCT
}

/**
 * An enum declaration.
 */
public data class EnumSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
    val childSymbols: List<String> = emptyList(),
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.ENUM
}

/**
 * A trait declaration.
 */
public data class TraitSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
    val childSymbols: List<String> = emptyList(),
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.TRAIT
}

/**
 * A type alias declaration.
 */
public data class TypeAliasSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.TYPE_ALIAS
}

/**
 * A module declaration.
 */
public data class ModuleSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.MODULE
}

/**
 * A function parameter.
 */
public data class ParameterSymbol(
    override val name: String,
    override val visibility: Visibility,
    override val range: SourceRange,
    override val fileName: String,
    override val docComment: String? = null,
    override val parentModule: String? = null,
    val isMutable: Boolean = false,
    val typeAnnotation: String? = null,
) : Symbol {
    override val kind: SymbolKind get() = SymbolKind.PARAMETER
}
