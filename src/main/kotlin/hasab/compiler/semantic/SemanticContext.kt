package hasab.compiler.semantic

import hasab.compiler.frontend.ast.AstNode
import java.util.concurrent.atomic.AtomicReference

/**
 * Shared mutable state container passed between semantic analysis passes.
 *
 * Replaces the scattered state that was previously held by individual pass classes.
 * Each pass reads from and writes to this context, enabling clean pass composition.
 *
 * Lifecycle:
 * 1. Created before analysis begins
 * 2. Populated by each pass in order
 * 3. Consumed after analysis completes to build the [SemanticModel]
 */
public class SemanticContext {

    // ---- Symbol Table ----

    private val _symbolTable: AtomicReference<SymbolTable> = AtomicReference(SymbolTable.EMPTY)

    public var symbolTable: SymbolTable
        get() = _symbolTable.get()
        set(value) { _symbolTable.set(value) }

    // ---- Scope Tree ----

    private val _scopeTree: AtomicReference<Scope?> = AtomicReference(null)

    public var scopeTree: Scope?
        get() = _scopeTree.get()
        set(value) { _scopeTree.set(value) }

    // ---- Diagnostics ----

    private val _diagnostics: MutableList<SemanticDiagnostic> = mutableListOf()

    public val diagnostics: List<SemanticDiagnostic> get() = _diagnostics.toList()

    public fun addDiagnostic(diagnostic: SemanticDiagnostic) {
        _diagnostics.add(diagnostic)
    }

    public fun addDiagnostics(newDiags: List<SemanticDiagnostic>) {
        _diagnostics.addAll(newDiags)
    }

    public fun clearDiagnostics() {
        _diagnostics.clear()
    }

    // ---- Scope Manager ----

    public val scopeManager: ScopeManager = ScopeManager()

    // ---- Current Module ----

    public var currentModule: String? = null

    // ---- Node Bindings (AST → Symbol) ----

    private val _nodeBindings: MutableMap<AstNode, Symbol> = mutableMapOf()

    public val nodeBindings: Map<AstNode, Symbol> get() = _nodeBindings.toMap()

    public fun bindNode(node: AstNode, symbol: Symbol) {
        _nodeBindings[node] = symbol
    }

    public fun bindNodes(bindings: Map<AstNode, Symbol>) {
        _nodeBindings.putAll(bindings)
    }

    // ---- Imports ----

    private val _imports: MutableMap<String, ResolvedImport> = mutableMapOf()

    public val imports: Map<String, ResolvedImport> get() = _imports.toMap()

    public fun addImport(key: String, import: ResolvedImport) {
        _imports[key] = import
    }

    public fun addImports(newImports: Map<String, ResolvedImport>) {
        _imports.putAll(newImports)
    }

    // ---- Module Graph ----

    private val _moduleGraph: MutableMap<String, ModuleInfo> = mutableMapOf()

    public val moduleGraph: Map<String, ModuleInfo> get() = _moduleGraph.toMap()

    public fun setModuleGraph(graph: Map<String, ModuleInfo>) {
        _moduleGraph.clear()
        _moduleGraph.putAll(graph)
    }

    // ---- Convenience Queries ----

    /**
     * Look up a symbol by name.
     */
    public fun lookupSymbol(name: String): Symbol? = symbolTable.lookup(name)

    /**
     * Get all symbols of a given kind.
     */
    public fun symbolsOfKind(kind: SymbolKind): List<Symbol> {
        return symbolTable.allVisibleSymbols().values.filter { it.kind == kind }
    }

    /**
     * Whether any errors have been recorded.
     */
    public val hasErrors: Boolean
        get() = _diagnostics.any { it.severity == hasab.compiler.frontend.lexer.DiagnosticSeverity.ERROR }

    /**
     * Build a snapshot of all analysis results.
     */
    public fun toResult(): SemanticResult = SemanticResult(
        diagnostics = diagnostics,
        symbolTable = symbolTable,
        scopeTree = scopeTree,
        imports = imports,
        moduleGraph = moduleGraph,
    )
}
