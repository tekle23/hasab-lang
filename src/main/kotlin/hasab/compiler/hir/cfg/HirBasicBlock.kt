package hasab.compiler.hir.cfg

/**
 * A basic block in the control flow graph.
 *
 * Contains a sequence of non-terminating instructions followed by
 * exactly one terminating instruction. The terminator determines
 * the successors of this block.
 */
public data class HirBasicBlock(
    public val id: BlockId,
    public val instructions: List<HirInstruction>,
) {
    /** The terminator instruction (last instruction in the block). */
    public val terminator: HirInstruction
        get() = instructions.last()

    /** Whether this block ends with a terminator that prevents fall-through. */
    public val hasUnreachableTermination: Boolean
        get() = terminator is ReturnInstr || terminator is JumpInstr || terminator is SwitchInstr

    /** Block label for display. */
    public val label: String get() = "bb${id.value}"
}
