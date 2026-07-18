package hasab.compiler.types

public sealed interface ResolvedType {

    public fun displayName(): String

    public object IntType : ResolvedType {
        override fun displayName(): String = "int"
    }

    public object FloatType : ResolvedType {
        override fun displayName(): String = "float"
    }

    public object StringType : ResolvedType {
        override fun displayName(): String = "string"
    }

    public object BoolType : ResolvedType {
        override fun displayName(): String = "bool"
    }

    public object CharType : ResolvedType {
        override fun displayName(): String = "char"
    }

    public object VoidType : ResolvedType {
        override fun displayName(): String = "void"
    }

    public object NilType : ResolvedType {
        override fun displayName(): String = "nil"
    }

    public data class ArrayType(val elementType: ResolvedType) : ResolvedType {
        override fun displayName(): String = "[${elementType.displayName()}]"
    }

    public data class PointerType(val elementType: ResolvedType) : ResolvedType {
        override fun displayName(): String = "*${elementType.displayName()}"
    }

    public data class OptionalType(val elementType: ResolvedType) : ResolvedType {
        override fun displayName(): String = "${elementType.displayName()}?"
    }

    public data class FunctionType(
        val parameterTypes: List<ResolvedType>,
        val returnType: ResolvedType,
    ) : ResolvedType {
        override fun displayName(): String {
            val params = parameterTypes.joinToString(", ") { it.displayName() }
            return "fn($params) -> ${returnType.displayName()}"
        }
    }

    public data class StructType(
        val name: String,
        val fields: LinkedHashMap<String, ResolvedType>,
    ) : ResolvedType {
        override fun displayName(): String = name
    }

    public data class EnumType(
        val name: String,
        val variants: LinkedHashMap<String, List<ResolvedType>>,
    ) : ResolvedType {
        override fun displayName(): String = name
    }

    public data class TypeAlias(
        val name: String,
        val underlying: ResolvedType,
    ) : ResolvedType {
        override fun displayName(): String = name
    }

    public data class TraitType(
        val name: String,
        val methods: LinkedHashMap<String, ResolvedType>,
    ) : ResolvedType {
        override fun displayName(): String = name
    }

    public object ErrorType : ResolvedType {
        override fun displayName(): String = "<error>"
    }
}
