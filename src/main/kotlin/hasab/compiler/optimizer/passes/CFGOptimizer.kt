package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.OptimizationPass
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.PassResult

/**
 * Structural CFG optimization with two sub-passes:
 * 1. **Block merging** — if block A ends with `jump B` and B has only
 *    one predecessor (A), merge B's body into A.
 * 2. **Empty block removal** — if a block has no instructions beyond
 *    the terminator and that terminator is `jump T`, redirect all
 *    incoming edges to T.
 */
public class CFGOptimizer : OptimizationPass {
    override val name: String = "CFGOptimizer"
    override val description: String = "Merges trivial blocks and removes empty forwarding blocks"

    override fun run(module: HirCfgModule, context: PassContext): PassResult {
        var totalMerged = 0
        var totalRemoved = 0
        val newFunctions = mutableMapOf<String, HirCfgFunction>()

        for ((fnName, fn) in module.functions) {
            var blocks = fn.blocks.toMutableMap()
            val cfg = CfgGraph.fromFunction(fn)

            // Phase 1: block merging — iterates until stable
            var mergeChanged = true
            while (mergeChanged) {
                mergeChanged = false
                val currentCfg = CfgGraph.fromFunction(fn.copy(blocks = blocks))
                val currentBlocks = blocks.toMutableMap()
                val toMerge = mutableSetOf<BlockId>()

                for ((blockId, block) in currentBlocks.toList()) {
                    val terminator = block.terminator
                    if (terminator !is JumpInstr) continue
                    val targetId = terminator.target
                    val targetBlock = currentBlocks[targetId] ?: continue

                    if (blockId == targetId) continue
                    val preds = currentCfg.predecessors(targetId)
                    if (preds.size != 1) continue

                    if (blockId in toMerge || targetId in toMerge) continue

                    val mergedInstructions = block.instructions.dropLast(1) + targetBlock.instructions
                    currentBlocks[blockId] = block.copy(instructions = mergedInstructions)
                    currentBlocks.remove(targetId)
                    toMerge.add(targetId)
                    mergeChanged = true
                    totalMerged++
                }

                if (toMerge.isNotEmpty()) {
                    blocks = currentBlocks
                }
            }

            // Phase 2: empty block removal (forwarding jumps)
            val forwardJumps = mutableMapOf<BlockId, BlockId>()
            for ((blockId, block) in blocks) {
                if (block.instructions.size == 1 && block.terminator is JumpInstr) {
                    forwardJumps[blockId] = (block.terminator as JumpInstr).target
                }
            }

            if (forwardJumps.isNotEmpty()) {
                fun resolveTarget(id: BlockId): BlockId {
                    var current = id
                    val visited = mutableSetOf<BlockId>()
                    while (current in forwardJumps && visited.add(current)) {
                        current = forwardJumps[current]!!
                    }
                    return current
                }

                val newBlocks = mutableMapOf<BlockId, HirBasicBlock>()
                for ((blockId, block) in blocks) {
                    if (blockId in forwardJumps) {
                        totalRemoved++
                        continue
                    }
                    val newInstructions = block.instructions.map { instr ->
                        redirectTerminator(instr, forwardJumps, ::resolveTarget)
                    }
                    newBlocks[blockId] = block.copy(instructions = newInstructions)
                }

                val newEntryId = if (fn.entryBlockId in forwardJumps) resolveTarget(fn.entryBlockId) else fn.entryBlockId
                blocks = newBlocks
                if (newEntryId != fn.entryBlockId) {
                    // Entry block itself was a forwarding jump; rebuild.
                    val resolved = resolveTarget(fn.entryBlockId)
                    val entryBlock = blocks[resolved] ?: continue
                    val mergedInstructions = mutableListOf<HirInstruction>()
                    // Find the original entry block content.
                    val originalEntry = fn.blocks[fn.entryBlockId]
                    if (originalEntry != null) {
                        mergedInstructions.addAll(originalEntry.instructions.dropLast(1))
                    }
                    mergedInstructions.addAll(entryBlock.instructions)
                    blocks[resolved] = entryBlock.copy(instructions = mergedInstructions)
                }
            }

            newFunctions[fnName] = fn.copy(blocks = blocks)
        }

        val changed = newFunctions != module.functions
        return PassResult(
            module = module.copy(functions = newFunctions),
            changed = changed,
            stats = mapOf(
                "blocks_merged" to totalMerged,
                "blocks_removed" to totalRemoved,
            ),
        )
    }

    private fun redirectTerminator(
        instr: HirInstruction,
        forwardJumps: Map<BlockId, BlockId>,
        resolve: (BlockId) -> BlockId,
    ): HirInstruction = when (instr) {
        is JumpInstr -> {
            val resolved = resolve(instr.target)
            if (resolved != instr.target) instr.copy(target = resolved) else instr
        }
        is BranchInstr -> {
            val t = resolve(instr.trueBlock)
            val f = resolve(instr.falseBlock)
            if (t != instr.trueBlock || f != instr.falseBlock) {
                instr.copy(trueBlock = t, falseBlock = f)
            } else {
                instr
            }
        }
        is SwitchInstr -> {
            var changed = false
            val newCases = instr.cases.map { (value, target) ->
                val resolved = resolve(target)
                if (resolved != target) {
                    changed = true
                    value to resolved
                } else {
                    value to target
                }
            }
            val newDefault = resolve(instr.defaultBlock)
            if (changed || newDefault != instr.defaultBlock) {
                instr.copy(cases = newCases, defaultBlock = newDefault)
            } else {
                instr
            }
        }
        else -> instr
    }
}
