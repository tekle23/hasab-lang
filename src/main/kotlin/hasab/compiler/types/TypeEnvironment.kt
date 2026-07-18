package hasab.compiler.types

public class TypeEnvironment(private val parent: TypeEnvironment? = null) {

    private val symbols: MutableMap<String, Symbol> = mutableMapOf()

    public fun define(symbol: Symbol) {
        if (symbols.containsKey(symbol.name)) {
            throw IllegalArgumentException("Symbol '${symbol.name}' already defined in this scope")
        }
        symbols[symbol.name] = symbol
    }

    public fun defineOrOverride(symbol: Symbol) {
        symbols[symbol.name] = symbol
    }

    public fun lookup(name: String): Symbol? {
        return symbols[name] ?: parent?.lookup(name)
    }

    public fun lookupCurrent(name: String): Symbol? = symbols[name]

    public fun hasCurrent(name: String): Boolean = symbols.containsKey(name)

    public fun enterScope(): TypeEnvironment = TypeEnvironment(this)

    public fun parent(): TypeEnvironment? = parent
}
