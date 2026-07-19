package hasab.compiler.hir.cfg

/**
 * A module in CFG form: a collection of CFG functions.
 */
public data class HirCfgModule(
    public val name: String?,
    public val functions: Map<String, HirCfgFunction>,
) {
    /** Total basic block count across all functions. */
    public val blockCount: Int
        get() = functions.values.sumOf { it.blocks.size }

    /** Total instruction count across all functions. */
    public val instructionCount: Int
        get() = functions.values.sumOf { it.instructionCount }

    /** Get a function by name, throwing if not found. */
    public fun function(name: String): HirCfgFunction = functions[name]
        ?: throw IllegalArgumentException("Function '$name' not found in module")
}
