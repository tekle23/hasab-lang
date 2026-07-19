package hasab.compiler.types

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Immutable, thread-safe model holding all type-checking results.
 *
 * Produced by [TypeCheckerEngine] and consumed by downstream phases
 * (HIR lowering, code generation, IDE tooling).
 *
 * All read access is thread-safe via [AtomicReference] and [ConcurrentHashMap].
 * No mutation methods are exposed publicly.
 */
public class TypedSemanticModel private constructor(
    private val _typeBindings: AtomicReference<Map<Int, Type>>,
    private val _environment: AtomicReference<TypeEnvironment>,
    private val _diagnostics: AtomicReference<List<TypeDiagnostic>>,
    private val _mutableVars: AtomicReference<Set<String>>,
    private val _functionOverloads: AtomicReference<Map<String, List<FunctionType>>>,
) {

    public companion object {
        /**
         * Create an empty semantic model.
         */
        public fun empty(): TypedSemanticModel = TypedSemanticModel(
            _typeBindings = AtomicReference(emptyMap()),
            _environment = AtomicReference(TypeEnvironment.root()),
            _diagnostics = AtomicReference(emptyList()),
            _mutableVars = AtomicReference(emptySet()),
            _functionOverloads = AtomicReference(emptyMap()),
        )
    }

    // ---- Read access (thread-safe) ----

    /**
     * Get the resolved [Type] for an AST node, or null if unknown.
     *
     * Uses the node's identity hash code as the key for O(1) lookup.
     */
    public fun typeOf(node: AstNode): Type? = _typeBindings.get()[System.identityHashCode(node)]

    /**
     * Get the resolved [Type] for an AST node, returning [UnknownType] if unknown.
     */
    public fun typeOrDefault(node: AstNode): Type = typeOf(node) ?: UnknownType

    /**
     * Get all type bindings (node identity hash → type).
     */
    public val typeBindings: Map<Int, Type> get() = _typeBindings.get()

    /**
     * The final type environment with all bindings.
     */
    public val environment: TypeEnvironment get() = _environment.get()

    /**
     * All diagnostics produced during type checking.
     */
    public val diagnostics: List<TypeDiagnostic> get() = _diagnostics.get()

    /**
     * All errors (subset of diagnostics with ERROR severity).
     */
    public val errors: List<TypeDiagnostic>
        get() = diagnostics.filter { it.severity == DiagnosticSeverity.ERROR }

    /**
     * All warnings (subset of diagnostics with WARNING severity).
     */
    public val warnings: List<TypeDiagnostic>
        get() = diagnostics.filter { it.severity != DiagnosticSeverity.ERROR }

    /**
     * Whether the type checking produced any errors.
     */
    public val hasErrors: Boolean get() = errors.isNotEmpty()

    /**
     * Check if a variable name is mutable (declared with `mut`).
     */
    public fun isMutableVariable(name: String): Boolean = name in _mutableVars.get()

    /**
     * Get all function overloads for a given function name.
     */
    public fun functionOverloads(name: String): List<FunctionType> =
        _functionOverloads.get()[name] ?: emptyList()

    /**
     * Get all mutable variable names.
     */
    public val mutableVariables: Set<String> get() = _mutableVars.get()

    // ---- Mutable builder (used internally during engine execution) ----

    internal fun recordType(node: AstNode, type: Type) {
        _typeBindings.updateAndGet { existing ->
            existing + (System.identityHashCode(node) to type)
        }
    }

    internal fun recordTypes(pairs: List<Pair<AstNode, Type>>) {
        _typeBindings.updateAndGet { existing ->
            existing + pairs.associate { (node, type) -> System.identityHashCode(node) to type }
        }
    }

    internal fun updateEnvironment(env: TypeEnvironment) {
        _environment.set(env)
    }

    internal fun addDiagnostics(diags: List<TypeDiagnostic>) {
        _diagnostics.updateAndGet { existing -> existing + diags }
    }

    internal fun addMutableVar(name: String) {
        _mutableVars.updateAndGet { existing -> existing + name }
    }

    internal fun addFunctionOverload(name: String, type: FunctionType) {
        _functionOverloads.updateAndGet { existing ->
            val current = existing[name] ?: emptyList()
            existing + (name to current + type)
        }
    }

    internal fun snapshot(): TypedSemanticModel = TypedSemanticModel(
        _typeBindings = AtomicReference(_typeBindings.get()),
        _environment = AtomicReference(_environment.get()),
        _diagnostics = AtomicReference(_diagnostics.get()),
        _mutableVars = AtomicReference(_mutableVars.get()),
        _functionOverloads = AtomicReference(_functionOverloads.get()),
    )
}
