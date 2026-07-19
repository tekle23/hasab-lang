package hasab.compiler.hir

import hasab.compiler.frontend.lexer.SourceRange

/**
 * Bidirectional mapping between AST source positions and HIR nodes.
 *
 * Each HIR node is identified by its [Int] identity (System.identityHashCode).
 * The map records which source range produced each HIR node, enabling:
 * - Debugging: map optimized HIR back to source lines
 * - Error reporting: reference original source for generated code
 * - Profiling: map execution counters back to source
 */
public class HirSourceMap {

    private val hirToAst = mutableMapOf<Int, SourceRange>()
    private val astToHir = mutableMapOf<Int, MutableList<Int>>()

    /**
     * Record that a HIR node came from the given AST source range.
     */
    public fun record(hirNodeId: Int, astRange: SourceRange) {
        hirToAst[hirNodeId] = astRange
        val astKey = astRange.start.offset
        astToHir.getOrPut(astKey) { mutableListOf() }.add(hirNodeId)
    }

    /**
     * Get the AST source range for a HIR node.
     */
    public fun astRangeOf(hirNodeId: Int): SourceRange? = hirToAst[hirNodeId]

    /**
     * Get all HIR node IDs that map to the given AST offset.
     */
    public fun hirNodesAt(astOffset: Int): List<Int> = astToHir[astOffset] ?: emptyList()

    /**
     * Get all HIR node IDs that map to any offset within [range].
     */
    public fun hirNodesInRange(range: SourceRange): List<Int> {
        return hirToAst.entries
            .filter { (_, astRange) ->
                astRange.start.offset >= range.start.offset &&
                    astRange.end.offset <= range.end.offset
            }
            .map { it.key }
    }

    /**
     * Total number of recorded mappings.
     */
    public val size: Int get() = hirToAst.size
}
