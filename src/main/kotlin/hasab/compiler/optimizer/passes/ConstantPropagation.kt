package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.OptimizationPass
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.PassResult

/**
 * Forward constant propagation: when a register is assigned a constant,
 * replace subsequent uses of that register within the same block.
 */
public class ConstantPropagation : OptimizationPass {
    override val name: String = "ConstantPropagation"
    override val description: String = "Propagates constants to use sites"

    override fun run(module: HirCfgModule, context: PassContext): PassResult {
        var totalPropagations = 0
        val newFunctions = mutableMapOf<String, HirCfgFunction>()

        for ((fnName, fn) in module.functions) {
            val newBlocks = mutableMapOf<BlockId, HirBasicBlock>()
            var fnChanged = false

            for ((blockId, block) in fn.blocks) {
                val replacements = mutableMapOf<Register, ConstOperand>()
                val newInstructions = mutableListOf<HirInstruction>()
                var blockChanged = false

                for (instr in block.instructions) {
                    val substituted = substituteOperands(instr, replacements)
                    newInstructions.add(substituted)

                    val target = instr.targetRegister()
                    if (target != null && instr is AssignInstr && instr.value is ConstOperand) {
                        replacements[target] = instr.value as ConstOperand
                    }

                    if (substituted !== instr) {
                        blockChanged = true
                        totalPropagations++
                    }
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
            stats = mapOf("propagations" to totalPropagations),
        )
    }

    private fun substituteOperands(
        instr: HirInstruction,
        replacements: Map<Register, ConstOperand>,
    ): HirInstruction {
        fun replace(op: Operand): Operand {
            if (op is RegisterOperand) {
                return replacements[op.register] ?: op
            }
            return op
        }

        return when (instr) {
            is AssignInstr -> instr.copy(value = replace(instr.value))
            is BinaryOpInstr -> instr.copy(left = replace(instr.left), right = replace(instr.right))
            is UnaryOpInstr -> instr.copy(operand = replace(instr.operand))
            is CallInstr -> instr.copy(arguments = instr.arguments.map { replace(it) })
            is LoadFieldInstr -> instr.copy(base = replace(instr.base))
            is LoadIndexInstr -> instr.copy(base = replace(instr.base), index = replace(instr.index))
            is ArrayLiteralInstr -> instr.copy(elements = instr.elements.map { replace(it) })
            is ArrayInitInstr -> instr.copy(size = replace(instr.size))
            is PhiInstr -> instr.copy(sources = instr.sources.map { (bid, op) -> bid to replace(op) })
            is CastInstr -> instr.copy(source = replace(instr.source))
            is NullCheckInstr -> instr.copy(source = replace(instr.source))
            is NullAssertInstr -> instr.copy(source = replace(instr.source))
            is ReturnInstr -> instr.copy(value = instr.value?.let { replace(it) })
            is BranchInstr -> instr.copy(condition = replace(instr.condition))
            is JumpInstr -> instr
            is SwitchInstr -> instr.copy(subject = replace(instr.subject))
            is StoreFieldInstr -> instr.copy(base = replace(instr.base), value = replace(instr.value))
            is StoreIndexInstr -> instr.copy(base = replace(instr.base), index = replace(instr.index), value = replace(instr.value))
        }
    }
}
