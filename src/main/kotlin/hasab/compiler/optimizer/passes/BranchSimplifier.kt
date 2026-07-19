package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.OptimizationPass
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.PassResult

/**
 * Simplifies branches whose condition is a compile-time constant:
 * - `branch(true,  t, f)` → `jump t`
 * - `branch(false, t, f)` → `jump f`
 */
public class BranchSimplifier : OptimizationPass {
    override val name: String = "BranchSimplifier"
    override val description: String = "Simplifies branches with constant conditions"

    override fun run(module: HirCfgModule, context: PassContext): PassResult {
        var totalSimplified = 0
        val newFunctions = mutableMapOf<String, HirCfgFunction>()

        for ((fnName, fn) in module.functions) {
            val newBlocks = mutableMapOf<BlockId, HirBasicBlock>()
            var fnChanged = false

            for ((blockId, block) in fn.blocks) {
                val newInstructions = mutableListOf<HirInstruction>()
                var blockChanged = false

                for (instr in block.instructions) {
                    if (instr is BranchInstr && instr.condition is ConstOperand) {
                        val condValue = (instr.condition as ConstOperand).value
                        val replacement = when (condValue) {
                            true -> JumpInstr(target = instr.trueBlock, comment = "simplified branch")
                            false -> JumpInstr(target = instr.falseBlock, comment = "simplified branch")
                            else -> null
                        }
                        if (replacement != null) {
                            newInstructions.add(replacement)
                            blockChanged = true
                            totalSimplified++
                            continue
                        }
                    }
                    newInstructions.add(instr)
                }

                if (blockChanged) {
                    newBlocks[blockId] = block.copy(instructions = newInstructions)
                    fnChanged = true
                } else {
                    newBlocks[blockId] = block
                }
            }

            if (fnChanged) {
                newFunctions[fnName] = fn.copy(blocks = newBlocks)
            } else {
                newFunctions[fnName] = fn
            }
        }

        val changed = newFunctions != module.functions
        return PassResult(
            module = module.copy(functions = newFunctions),
            changed = changed,
            stats = mapOf("branches_simplified" to totalSimplified),
        )
    }
}
