package hasab.compiler.semantic

import hasab.compiler.frontend.lexer.SourceRange

/**
 * The kind of semantic symbol being declared.
 */
public enum class SymbolKind {
    VARIABLE,
    FUNCTION,
    STRUCT,
    ENUM,
    TRAIT,
    TYPE_ALIAS,
    MODULE,
    PARAMETER,
    FIELD,
    VARIANT,
}

/**
 * Visibility level for a symbol.
 */
public enum class Visibility {
    PUBLIC,
    MODULE_LOCAL,
}

/**
 * Backward-compatible alias for the old flat symbol type.
 * New code should use the typed subtypes of [Symbol].
 */
public typealias SemanticSymbol = Symbol

/**
 * Immutable, thread-safe symbol table backed by a persistent map.
 *
 * Lookup walks up through parent scopes automatically.
 */
public class SymbolTable private constructor(
    private val symbols: Map<String, Symbol>,
    private val parent: SymbolTable?,
) {
    public companion object {
        public val EMPTY: SymbolTable = SymbolTable(emptyMap(), null)
    }

    /**
     * Create a child scope that inherits from this table.
     */
    public fun enterScope(): SymbolTable = SymbolTable(emptyMap(), this)

    /**
     * Define a new symbol in the current scope.
     * Returns a new SymbolTable with the symbol added.
     */
    public fun define(symbol: Symbol): SymbolTable {
        val newSymbols = symbols + (symbol.name to symbol)
        return SymbolTable(newSymbols, parent)
    }

    /**
     * Define multiple symbols in the current scope.
     */
    public fun defineAll(vararg newSymbols: Symbol): SymbolTable {
        var table = this
        for (sym in newSymbols) {
            table = table.define(sym)
        }
        return table
    }

    /**
     * Look up a symbol in the current scope or any parent scope.
     */
    public fun lookup(name: String): Symbol? {
        return symbols[name] ?: parent?.lookup(name)
    }

    /**
     * Look up a symbol only in the current scope (no parent lookup).
     */
    public fun lookupCurrent(name: String): Symbol? = symbols[name]

    /**
     * Check if a name is defined in the current scope.
     */
    public fun hasCurrent(name: String): Boolean = symbols.containsKey(name)

    /**
     * Check if a name is defined in any scope.
     */
    public fun has(name: String): Boolean = lookup(name) != null

    /**
     * Get all symbols defined in the current scope only.
     */
    public fun currentSymbols(): Map<String, Symbol> = symbols.toMap()

    /**
     * Get all symbols accessible from this scope (including parents).
     */
    public fun allVisibleSymbols(): Map<String, Symbol> {
        val result = mutableMapOf<String, Symbol>()
        parent?.let { result.putAll(it.allVisibleSymbols()) }
        result.putAll(symbols)
        return result
    }

    /**
     * Get the parent scope.
     */
    public fun parentScope(): SymbolTable? = parent

    /**
     * The depth of this scope (0 = global).
     */
    public fun depth(): Int = if (parent == null) 0 else parent.depth() + 1

    /**
     * Collect all visible symbols as a list for fuzzy matching.
     */
    public fun allSymbolNames(): List<String> = allVisibleSymbols().keys.toList()
}
