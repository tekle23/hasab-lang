package hasab.compiler.hir.cfg

import hasab.compiler.types.Type
import hasab.compiler.types.FunctionType

/**
 * A function in CFG form: a collection of basic blocks with an entry point.
 */
public data class HirCfgFunction(
    public val name: String,
    public val parameters: List<ParamOperand>,
    public val returnType: Type,
    public val blocks: Map<BlockId, HirBasicBlock>,
    public val entryBlockId: BlockId,
) {
    /** Function type derived from parameters and return type. */
    public val functionType: FunctionType
        get() = FunctionType(parameters.map { it.type }, returnType)

    /** Total instruction count across all blocks. */
    public val instructionCount: Int
        get() = blocks.values.sumOf { it.instructions.size }

    /** Get a block by ID, throwing if not found. */
    public fun block(id: BlockId): HirBasicBlock = blocks[id]
        ?: throw IllegalArgumentException("Block $id not found in function $name")
}
