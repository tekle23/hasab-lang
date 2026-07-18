package hasab.compiler.types

public object BuiltinTypes {

    public val INT: ResolvedType = ResolvedType.IntType
    public val FLOAT: ResolvedType = ResolvedType.FloatType
    public val STRING: ResolvedType = ResolvedType.StringType
    public val BOOL: ResolvedType = ResolvedType.BoolType
    public val CHAR: ResolvedType = ResolvedType.CharType
    public val VOID: ResolvedType = ResolvedType.VoidType
    public val NIL: ResolvedType = ResolvedType.NilType

    private val registry: Map<String, ResolvedType> = mapOf(
        "int" to INT,
        "float" to FLOAT,
        "string" to STRING,
        "bool" to BOOL,
        "char" to CHAR,
        "void" to VOID,
    )

    public fun lookup(name: String): ResolvedType? = registry[name]

    public fun isBuiltin(name: String): Boolean = registry.containsKey(name)

    public fun registerBuiltins(env: TypeEnvironment) {
        for ((name, type) in registry) {
            env.define(TypeAliasSymbol(name, type))
        }
    }
}
