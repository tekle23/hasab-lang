package hasab.compiler.hir.cfg

/**
 * Control flow graph with predecessor/successor tracking and dominator analysis.
 *
 * Built from a [HirCfgFunction] and provides analysis queries used by
 * optimization passes (dead code elimination, loop detection, etc.).
 */
public class CfgGraph private constructor(
    public val blocks: Map<BlockId, HirBasicBlock>,
    public val entryBlockId: BlockId,
    private val _successors: Map<BlockId, List<BlockId>>,
    private val _predecessors: Map<BlockId, List<BlockId>>,
) {
    /** Blocks in reverse post-order (used for iterative dataflow). */
    public val reversePostOrder: List<BlockId> by lazy { computeReversePostOrder() }

    /** Direct successors of a block. */
    public fun successors(blockId: BlockId): List<BlockId> = _successors[blockId] ?: emptyList()

    /** Direct predecessors of a block. */
    public fun predecessors(blockId: BlockId): List<BlockId> = _predecessors[blockId] ?: emptyList()

    /** Does block [a] dominate block [b]? */
    public fun dominates(a: BlockId, b: BlockId): Boolean {
        if (a == b) return true
        return dominatorSet(b)?.contains(a) ?: false
    }

    /** Is block [to] reachable from block [from]? */
    public fun reachable(from: BlockId, to: BlockId): Boolean {
        val visited = mutableSetOf<BlockId>()
        val queue = ArrayDeque<BlockId>()
        queue.add(from)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == to) return true
            if (!visited.add(current)) continue
            for (succ in successors(current)) queue.add(succ)
        }
        return false
    }

    /** Blocks that must be exited before [blockId] can be reached. */
    public fun dominatorSet(blockId: BlockId): Set<BlockId>? = dominators[blockId]

    /** Blocks in dominance tree order (parents before children). */
    public fun dominatorTree(): Map<BlockId, List<BlockId>> {
        val tree = mutableMapOf<BlockId, MutableList<BlockId>>()
        for (id in blocks.keys) tree[id] = mutableListOf()
        for ((block, domSet) in dominators) {
            for (dom in domSet) {
                if (dom != block) {
                    tree[dom]?.add(block)
                    break
                }
            }
        }
        return tree
    }

    /** Dominator map: each block -> set of blocks that dominate it. */
    public val dominators: Map<BlockId, Set<BlockId>> by lazy { computeDominators() }

    private fun computeDominators(): Map<BlockId, Set<BlockId>> {
        val allBlocks = blocks.keys.toMutableSet()
        val result = mutableMapOf<BlockId, MutableSet<BlockId>>()

        for (id in allBlocks) {
            result[id] = if (id == entryBlockId) mutableSetOf(id) else allBlocks.toMutableSet()
        }

        var changed = true
        while (changed) {
            changed = false
            for (block in allBlocks) {
                if (block == entryBlockId) continue
                val preds = predecessors(block)
                if (preds.isEmpty()) continue
                var newSet = result[preds[0]]!!.toMutableSet()
                for (i in 1 until preds.size) {
                    newSet = newSet.intersect(result[preds[i]]!!).toMutableSet()
                }
                newSet.add(block)
                if (newSet != result[block]) {
                    result[block] = newSet
                    changed = true
                }
            }
        }

        return result
    }

    private fun computeReversePostOrder(): List<BlockId> {
        val visited = mutableSetOf<BlockId>()
        val postOrder = mutableListOf<BlockId>()

        fun dfs(blockId: BlockId) {
            if (!visited.add(blockId)) return
            for (succ in successors(blockId)) dfs(succ)
            postOrder.add(blockId)
        }

        dfs(entryBlockId)

        for (id in blocks.keys) {
            if (id !in visited) postOrder.add(id)
        }

        return postOrder.reversed()
    }

    public companion object {
        /**
         * Build a [CfgGraph] from a [HirCfgFunction].
         */
        public fun fromFunction(fn: HirCfgFunction): CfgGraph {
            val succs = mutableMapOf<BlockId, MutableList<BlockId>>()
            val preds = mutableMapOf<BlockId, MutableList<BlockId>>()

            for ((id, _) in fn.blocks) {
                succs[id] = mutableListOf()
                preds[id] = preds.getOrPut(id) { mutableListOf() }
            }

            for ((id, block) in fn.blocks) {
                when (val term = block.terminator) {
                    is BranchInstr -> {
                        succs[id]!!.add(term.trueBlock)
                        succs[id]!!.add(term.falseBlock)
                        preds.getOrPut(term.trueBlock) { mutableListOf() }.add(id)
                        preds.getOrPut(term.falseBlock) { mutableListOf() }.add(id)
                    }
                    is JumpInstr -> {
                        succs[id]!!.add(term.target)
                        preds.getOrPut(term.target) { mutableListOf() }.add(id)
                    }
                    is SwitchInstr -> {
                        for ((_, target) in term.cases) {
                            succs[id]!!.add(target)
                            preds.getOrPut(target) { mutableListOf() }.add(id)
                        }
                        succs[id]!!.add(term.defaultBlock)
                        preds.getOrPut(term.defaultBlock) { mutableListOf() }.add(id)
                    }
                    is ReturnInstr -> { /* no successors */ }
                    else -> { /* non-terminator, should not happen */ }
                }
            }

            return CfgGraph(fn.blocks, fn.entryBlockId, succs, preds)
        }
    }
}
