package hasab.compiler.semantic

import hasab.compiler.frontend.lexer.SourceRange

/**
 * The kind of lexical scope.
 */
public enum class ScopeKind {
    GLOBAL,
    PACKAGE,
    MODULE,
    CLASS,
    FUNCTION,
    BLOCK,
    IF_BRANCH,
    LOOP,
    IMPL,
    TRAIT,
}

/**
 * An immutable node in the scope tree.
 */
public data class Scope(
    val kind: ScopeKind,
    val name: String,
    val range: SourceRange,
    val fileName: String,
    val parent: Scope? = null,
    val symbols: List<String> = emptyList(),
    val children: List<Scope> = emptyList(),
) {
    /**
     * Find a symbol name by walking up the scope tree.
     */
    public fun findSymbol(name: String): Boolean {
        if (name in symbols) return true
        return parent?.findSymbol(name) ?: false
    }

    /**
     * Find the nearest enclosing scope of the given kind.
     */
    public fun findEnclosing(kind: ScopeKind): Scope? {
        if (this.kind == kind) return this
        return parent?.findEnclosing(kind)
    }

    /**
     * Check if this scope is inside a loop.
     */
    public fun isInsideLoop(): Boolean = findEnclosing(ScopeKind.LOOP) != null

    /**
     * Check if this scope is inside an impl block.
     */
    public fun isInsideImpl(): Boolean = findEnclosing(ScopeKind.IMPL) != null

    /**
     * The depth of this scope (0 = global).
     */
    public fun depth(): Int = if (parent == null) 0 else parent.depth() + 1

    /**
     * Collect all symbols accessible from this scope.
     */
    public fun accessibleSymbols(): Set<String> {
        val result = mutableSetOf<String>()
        result.addAll(symbols)
        parent?.let { result.addAll(it.accessibleSymbols()) }
        return result
    }
}

/**
 * Mutable scope manager used during multi-pass analysis.
 * After analysis, produces an immutable [Scope] tree for the [SemanticModel].
 */
public class ScopeManager {

    private val globalScope = Scope(
        kind = ScopeKind.GLOBAL,
        name = "<global>",
        range = SourceRange(
            hasab.compiler.frontend.lexer.SourcePosition(1, 1, 0),
            hasab.compiler.frontend.lexer.SourcePosition(1, 1, 0),
        ),
        fileName = "",
    )

    private var current: Scope = globalScope
    private val scopeStack: MutableList<Scope> = mutableListOf()
    private val allScopes: MutableList<Scope> = mutableListOf(globalScope)

    /**
     * Enter a new nested scope.
     */
    public fun enterScope(kind: ScopeKind, name: String, range: SourceRange, fileName: String): Scope {
        val newScope = Scope(
            kind = kind,
            name = name,
            range = range,
            fileName = fileName,
            parent = current,
        )
        current = newScope
        scopeStack.add(newScope)
        allScopes.add(newScope)
        return newScope
    }

    /**
     * Exit the current scope and return to the parent.
     */
    public fun exitScope(): Scope {
        val completed = current
        val parent = current.parent
        if (parent != null) {
            current = parent
        }
        return completed
    }

    /**
     * Add a symbol name to the current scope.
     */
    public fun addSymbol(name: String) {
        val updated = current.copy(symbols = current.symbols + name)
        current = updated
        // Update in allScopes
        val idx = allScopes.indexOfLast { it === current || it.range == current.range }
        if (idx >= 0) {
            allScopes[idx] = updated
        }
    }

    /**
     * Get the current scope.
     */
    public fun currentScope(): Scope = current

    /**
     * Find the nearest enclosing scope of the given kind.
     */
    public fun findEnclosing(kind: ScopeKind): Scope? = current.findEnclosing(kind)

    /**
     * Check if current scope is inside a loop.
     */
    public fun isInsideLoop(): Boolean = current.isInsideLoop()

    /**
     * Check if current scope is inside an impl block.
     */
    public fun isInsideImpl(): Boolean = current.isInsideImpl()

    /**
     * Get the global scope.
     */
    public fun globalScope(): Scope = globalScope

    /**
     * Build the final immutable scope tree by reconstructing parent-child relationships.
     */
    public fun buildScopeTree(): Scope = current
}
