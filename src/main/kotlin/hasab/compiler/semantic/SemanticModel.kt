package hasab.compiler.semantic

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import java.util.concurrent.atomic.AtomicReference

/**
 * Immutable semantic model holding all analysis results.
 * Thread-safe: uses copy-on-write semantics via AtomicReference.
 *
 * Created by [SemanticAnalyzer] and consumed by downstream phases
 * (type checking, code generation, IDE tooling).
 */
public class SemanticModel private constructor(
    private val _symbolTable: AtomicReference<SymbolTable>,
    private val _scopeTree: AtomicReference<Scope?>,
    private val _diagnostics: AtomicReference<List<SemanticDiagnostic>>,
    private val _imports: AtomicReference<Map<String, ResolvedImport>>,
    private val _moduleGraph: AtomicReference<Map<String, ModuleInfo>>,
    private val _nodeBindings: AtomicReference<Map<AstNode, SemanticSymbol>>,
) {
    public companion object {
        /**
         * Create an empty semantic model.
         */
        public fun empty(): SemanticModel = SemanticModel(
            _symbolTable = AtomicReference(SymbolTable.EMPTY),
            _scopeTree = AtomicReference(null),
            _diagnostics = AtomicReference(emptyList()),
            _imports = AtomicReference(emptyMap()),
            _moduleGraph = AtomicReference(emptyMap()),
            _nodeBindings = AtomicReference(emptyMap()),
        )
    }

    // ---- Read access (thread-safe) ----

    /**
     * The global symbol table with all collected declarations.
     */
    public val symbolTable: SymbolTable get() = _symbolTable.get()

    /**
     * The scope tree produced by scope analysis.
     */
    public val scopeTree: Scope? get() = _scopeTree.get()

    /**
     * All diagnostics produced during analysis.
     */
    public val diagnostics: List<SemanticDiagnostic> get() = _diagnostics.get()

    /**
     * All errors (subset of diagnostics with ERROR severity).
     */
    public val errors: List<SemanticDiagnostic>
        get() = _diagnostics.get().filter {
            it.severity == hasab.compiler.frontend.lexer.DiagnosticSeverity.ERROR
        }

    /**
     * All warnings (subset of diagnostics with WARNING severity).
     */
    public val warnings: List<SemanticDiagnostic>
        get() = _diagnostics.get().filter {
            it.severity == hasab.compiler.frontend.lexer.DiagnosticSeverity.WARNING
        }

    /**
     * Whether the analysis produced any errors.
     */
    public val hasErrors: Boolean get() = errors.isNotEmpty()

    /**
     * Resolved import map: path-string -> resolved import.
     */
    public val imports: Map<String, ResolvedImport> get() = _imports.get()

    /**
     * Module graph: module-name -> module info.
     */
    public val moduleGraph: Map<String, ModuleInfo> get() = _moduleGraph.get()

    /**
     * Binding map: AST node -> the symbol it refers to.
     * Useful for IDE tooling (go-to-definition, find-references).
     */
    public val nodeBindings: Map<AstNode, SemanticSymbol> get() = _nodeBindings.get()

    // ---- Mutable builder (used internally during analysis) ----

    internal fun updateSymbolTable(table: SymbolTable) {
        _symbolTable.set(table)
    }

    internal fun updateScopeTree(tree: Scope) {
        _scopeTree.set(tree)
    }

    internal fun updateDiagnostics(diags: List<SemanticDiagnostic>) {
        _diagnostics.set(diags)
    }

    internal fun addDiagnostics(newDiags: List<SemanticDiagnostic>) {
        _diagnostics.updateAndGet { existing -> existing + newDiags }
    }

    internal fun updateImports(imports: Map<String, ResolvedImport>) {
        _imports.set(imports)
    }

    internal fun updateModuleGraph(graph: Map<String, ModuleInfo>) {
        _moduleGraph.set(graph)
    }

    internal fun updateNodeBindings(bindings: Map<AstNode, SemanticSymbol>) {
        _nodeBindings.set(bindings)
    }

    // ---- Convenience queries ----

    /**
     * Look up a symbol by name from the global symbol table.
     */
    public fun lookupSymbol(name: String): SemanticSymbol? = symbolTable.lookup(name)

    /**
     * Get the symbol that a given AST node refers to.
     */
    public fun bindingFor(node: AstNode): SemanticSymbol? = nodeBindings[node]

    /**
     * Get all symbols of a given kind.
     */
    public fun symbolsOfKind(kind: SymbolKind): List<SemanticSymbol> {
        return symbolTable.allVisibleSymbols().values.filter { it.kind == kind }
    }

    /**
     * Get all function symbols.
     */
    public fun functions(): List<SemanticSymbol> = symbolsOfKind(SymbolKind.FUNCTION)

    /**
     * Get all struct symbols.
     */
    public fun structs(): List<SemanticSymbol> = symbolsOfKind(SymbolKind.STRUCT)

    /**
     * Get all enum symbols.
     */
    public fun enums(): List<SemanticSymbol> = symbolsOfKind(SymbolKind.ENUM)

    /**
     * Build a SemanticResult snapshot.
     */
    public fun toResult(): SemanticResult = SemanticResult(
        diagnostics = diagnostics,
        symbolTable = symbolTable,
        scopeTree = scopeTree,
        imports = imports,
        moduleGraph = moduleGraph,
    )
}
