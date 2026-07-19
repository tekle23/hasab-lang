package hasab.compiler.types

/**
 * Sealed type hierarchy representing all types in HASAB.
 *
 * Types are immutable and interned where possible.
 * Each type carries enough information for downstream phases
 * (HIR generation, bytecode emission).
 */
public sealed interface Type {

    /**
     * Human-readable name of this type.
     */
    public val displayName: String

    /**
     * Whether this type can be assigned to a variable of the given [other] type.
     * Implements structural subtyping for HASAB.
     */
    public fun isAssignableTo(other: Type): Boolean = this == other
}

// ---- Primitive types ----

public data object IntType : Type {
    override val displayName: String get() = "int"
}

public data object FloatType : Type {
    override val displayName: String get() = "float"
}

public data object StringType : Type {
    override val displayName: String get() = "string"
}

public data object BoolType : Type {
    override val displayName: String get() = "bool"
}

public data object CharType : Type {
    override val displayName: String get() = "char"
}

public data object VoidType : Type {
    override val displayName: String get() = "void"
}

// ---- User-defined types ----

/**
 * A struct type with named fields.
 */
public data class StructType(
    val name: String,
    val fields: List<StructTypeField>,
) : Type {
    override val displayName: String get() = name

    public fun fieldByName(fieldName: String): StructTypeField? = fields.find { it.name == fieldName }
}

public data class StructTypeField(
    val name: String,
    val type: Type,
    val isMutable: Boolean,
)

/**
 * An enum type with variants.
 */
public data class EnumType(
    val name: String,
    val variants: List<EnumTypeVariant>,
) : Type {
    override val displayName: String get() = name

    public fun variantByName(name: String): EnumTypeVariant? = variants.find { it.name == name }
}

public data class EnumTypeVariant(
    val name: String,
    val fields: List<Type>,
)

/**
 * A trait type with method signatures.
 */
public data class TraitType(
    val name: String,
    val methods: List<TraitTypeMethod>,
) : Type {
    override val displayName: String get() = name
}

public data class TraitTypeMethod(
    val name: String,
    val parameterTypes: List<Type>,
    val returnType: Type,
)

// ---- Compound types ----

/**
 * Array type: [T]
 */
public data class ArrayType(val elementType: Type) : Type {
    override val displayName: String get() = "[${elementType.displayName}]"

    override fun isAssignableTo(other: Type): Boolean {
        if (other is ArrayType) return elementType.isAssignableTo(other.elementType)
        if (other is OptionalType) return this.isAssignableTo(other.elementType)
        return false
    }
}

/**
 * Pointer type: *T
 */
public data class PointerType(val elementType: Type) : Type {
    override val displayName: String get() = "*${elementType.displayName}"

    override fun isAssignableTo(other: Type): Boolean {
        if (other is PointerType) return elementType.isAssignableTo(other.elementType)
        if (other is OptionalType) return this.isAssignableTo(other.elementType)
        return false
    }
}

/**
 * Optional type: T?
 */
public data class OptionalType(val elementType: Type) : Type {
    override val displayName: String get() = "${elementType.displayName}?"

    override fun isAssignableTo(other: Type): Boolean {
        if (other is OptionalType) return elementType.isAssignableTo(other.elementType)
        return false
    }
}

/**
 * Function type: fn(P1, P2, ...) -> R
 */
public data class FunctionType(
    val parameterTypes: List<Type>,
    val returnType: Type,
) : Type {
    override val displayName: String get() {
        val params = parameterTypes.joinToString(", ") { it.displayName }
        return "fn($params) -> ${returnType.displayName}"
    }
}

/**
 * Lambda type: (P1, P2, ...) -> R
 *
 * Structurally identical to [FunctionType] but represents
 * an anonymous closure rather than a named function.
 * Two lambda types are compatible if their signatures match.
 */
public data class LambdaType(
    val parameterTypes: List<Type>,
    val returnType: Type,
) : Type {
    override val displayName: String get() {
        val params = parameterTypes.joinToString(", ") { it.displayName }
        return "($params) -> ${returnType.displayName}"
    }

    override fun isAssignableTo(other: Type): Boolean {
        if (other is LambdaType) {
            if (parameterTypes.size != other.parameterTypes.size) return false
            return parameterTypes.zip(other.parameterTypes).all { (a, b) -> a.isAssignableTo(b) } &&
                returnType.isAssignableTo(other.returnType)
        }
        if (other is FunctionType) {
            if (parameterTypes.size != other.parameterTypes.size) return false
            return parameterTypes.zip(other.parameterTypes).all { (a, b) -> a.isAssignableTo(b) } &&
                returnType.isAssignableTo(other.returnType)
        }
        return false
    }
}

// ---- Special types ----

/**
 * A type alias — transparent wrapper that resolves to [target].
 */
public data class TypeAliasType(
    val name: String,
    val target: Type,
) : Type {
    override val displayName: String get() = name

    override fun isAssignableTo(other: Type): Boolean = target.isAssignableTo(other)
}

/**
 * A type variable (for inference).
 * Represents an unknown type that will be resolved during type checking.
 */
public data class TypeVariable(
    val id: Int,
    val name: String = "?$id",
    val bounds: List<Type> = emptyList(),
) : Type {
    override val displayName: String get() = name

    override fun isAssignableTo(other: Type): Boolean = true
}

/**
 * The error/unknown type — produced when type inference fails.
 * Propagates silently to avoid cascading errors.
 */
public data object UnknownType : Type {
    override val displayName: String get() = "<unknown>"

    override fun isAssignableTo(other: Type): Boolean = true
}

/**
 * The bottom type — represents unreachable code.
 */
public data object BottomType : Type {
    override val displayName: String get() = "<never>"

    override fun isAssignableTo(other: Type): Boolean = true
}

// ---- Numeric promotion rules ----

/**
 * Check if [source] can be implicitly promoted to [target] for numeric types.
 * Rules: int → float (widening), int → string (stringification).
 */
public fun isNumericPromotion(source: Type, target: Type): Boolean {
    if (source == target) return true
    if (source is IntType && target is FloatType) return true
    return false
}

/**
 * Check if two types are compatible (assignable or promotable).
 */
public fun areTypesCompatible(source: Type, target: Type): Boolean {
    if (source.isAssignableTo(target)) return true
    if (isNumericPromotion(source, target)) return true
    if (target is OptionalType) return source is NilLiteralType || source.isAssignableTo(target.elementType)
    if (source is OptionalType) return source.elementType.isAssignableTo(target)
    return false
}

/**
 * Sentinel for nil literal type — checked against OptionalType.
 */
public data object NilLiteralType : Type {
    override val displayName: String get() = "nil"

    override fun isAssignableTo(other: Type): Boolean {
        return other is OptionalType
    }
}

// ---- Type compatibility matrix ----

/**
 * Explicit type compatibility matrix for HASAB.
 *
 * Defines which implicit conversions are allowed between primitive types.
 * This matrix is the single source of truth for type compatibility rules.
 *
 * | Source  | → Int | → Float | → String | → Bool | → Char |
 * |---------|-------|---------|----------|--------|--------|
 * | Int     |   ✓   |    ✓    |    ✗     |   ✗    |   ✗    |
 * | Float   |   ✗   |    ✓    |    ✗     |   ✗    |   ✗    |
 * | String  |   ✗   |    ✗    |    ✓     |   ✗    |   ✗    |
 * | Bool    |   ✗   |    ✗    |    ✗     |   ✓    |   ✗    |
 * | Char    |   ✗   |    ✗    |    ✗     |   ✗    |   ✓    |
 */
public object TypeCompatibilityMatrix {

    private data class TypePair(val source: Type, val target: Type)

    private val compatible: Set<TypePair> = setOf(
        // Identity (each type is compatible with itself — handled by equality check)
        // Widening promotions
        TypePair(IntType, FloatType),
    )

    /**
     * Check if [source] can be implicitly converted to [target].
     */
    public fun isImplicitlyConvertible(source: Type, target: Type): Boolean {
        if (source == target) return true
        return TypePair(source, target) in compatible
    }

    /**
     * Get the common type for two types (for binary operations, if-expressions).
     * Returns null if no common type exists.
     */
    public fun commonType(a: Type, b: Type): Type? {
        if (a == b) return a
        if (isImplicitlyConvertible(a, b)) return b
        if (isImplicitlyConvertible(b, a)) return a
        return null
    }

    /**
     * Check if two types are comparable (==, !=, <, >, etc.).
     */
    public fun areComparable(a: Type, b: Type): Boolean {
        if (a == b) return true
        return isImplicitlyConvertible(a, b) || isImplicitlyConvertible(b, a)
    }

    /**
     * Get a human-readable description of allowed conversions from a source type.
     */
    public fun allowedConversions(source: Type): List<Type> = when (source) {
        is IntType -> listOf(FloatType)
        is FloatType -> listOf(FloatType)
        is StringType -> listOf(StringType)
        is BoolType -> listOf(BoolType)
        is CharType -> listOf(CharType)
        else -> emptyList()
    }
}
