package hasab.compiler.types

/**
 * Scope-aware type environment that maps names to their types.
 *
 * Supports nested scopes via parent chaining. Each scope level
 * can shadow outer bindings. Built-in types and functions are
 * always accessible.
 */
public class TypeEnvironment private constructor(
    private val bindings: Map<String, Type>,
    private val parent: TypeEnvironment?,
) {
    public companion object {
        /**
         * Create a root environment with built-in types and functions pre-loaded.
         */
        public fun root(): TypeEnvironment {
            var env = TypeEnvironment(emptyMap(), null)
            // Built-in types
            env = env.define("int", IntType)
            env = env.define("float", FloatType)
            env = env.define("string", StringType)
            env = env.define("bool", BoolType)
            env = env.define("char", CharType)
            env = env.define("void", VoidType)
            // Built-in I/O functions
            val anyParam = TypeVariable(0)
            env = env.define("println", FunctionType(listOf(anyParam), VoidType))
            env = env.define("print", FunctionType(listOf(anyParam), VoidType))
            // Built-in math/conversion functions
            env = env.define("len", FunctionType(listOf(TypeVariable(1)), IntType))
            env = env.define("abs", FunctionType(listOf(IntType), IntType))
            env = env.define("sqrt", FunctionType(listOf(FloatType), FloatType))
            env = env.define("pow", FunctionType(listOf(FloatType, FloatType), FloatType))
            env = env.define("min", FunctionType(listOf(IntType, IntType), IntType))
            env = env.define("max", FunctionType(listOf(IntType, IntType), IntType))
            env = env.define("str", FunctionType(listOf(TypeVariable(2)), StringType))
            env = env.define("now", FunctionType(listOf(), IntType))
            return env
        }
    }

    /**
     * Enter a new nested scope.
     */
    public fun enterScope(): TypeEnvironment = TypeEnvironment(emptyMap(), this)

    /**
     * Define a new name-to-type binding in the current scope.
     */
    public fun define(name: String, type: Type): TypeEnvironment {
        val newBindings = bindings + (name to type)
        return TypeEnvironment(newBindings, parent)
    }

    /**
     * Look up a type by name, walking up through parent scopes.
     */
    public fun lookup(name: String): Type? {
        return bindings[name] ?: parent?.lookup(name)
    }

    /**
     * Check if a name is defined in the current scope (no parent lookup).
     */
    public fun hasCurrent(name: String): Boolean = bindings.containsKey(name)

    /**
     * Check if a name is defined anywhere.
     */
    public fun has(name: String): Boolean = lookup(name) != null

    /**
     * Update an existing binding in the current scope (for type inference refinement).
     */
    public fun refine(name: String, type: Type): TypeEnvironment {
        if (bindings.containsKey(name)) {
            return TypeEnvironment(bindings + (name to type), parent)
        }
        return parent?.let { TypeEnvironment(bindings, it.refine(name, type)) } ?: this
    }
}
