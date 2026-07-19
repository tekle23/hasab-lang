package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.OptimizationPass
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.PassResult

/**
 * Copy propagation: replaces uses of a target register with the source
 * register when the target is assigned from the source via a plain copy
 * (`assign target, source`).
 */
public class CopyPropagation : OptimizationPass {
    override val name: String = "CopyPropagation"
    override val description: String = "Eliminates redundant register-to-register copies"

    override fun run(module: HirCfgModule, context: PassContext): PassResult {
        var totalCopiesEliminated = 0
        val newFunctions = mutableMapOf<String, HirCfgFunction>()

        for ((fnName, fn) in module.functions) {
            val newBlocks = mutableMapOf<BlockId, HirBasicBlock>()
            var fnChanged = false

            for ((blockId, block) in fn.blocks) {
                val copies = mutableMapOf<Register, Register>()
                val newInstructions = mutableListOf<HirInstruction>()
                var blockChanged = false

                for (instr in block.instructions) {
                    val substituted = substituteCopies(instr, copies)
                    newInstructions.add(substituted)

                    if (instr is AssignInstr && instr.value is RegisterOperand) {
                        val source = (instr.value as RegisterOperand).register
                        val resolved = copies[source] ?: source
                        copies[instr.target] = resolved
                    }

                    if (substituted !== instr) {
                        blockChanged = true
                        totalCopiesEliminated++
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
            stats = mapOf("copies_eliminated" to totalCopiesEliminated),
        )
    }

    private fun resolve(reg: Register, copies: Map<Register, Register>): Register {
        var current = reg
        val seen = mutableSetOf<Register>()
        while (current in copies && seen.add(current)) {
            current = copies[current]!!
        }
        return current
    }

    private fun substituteCopies(instr: HirInstruction, copies: Map<Register, Register>): HirInstruction {
        fun replaceReg(reg: Register): Register = resolve(reg, copies)

        fun replace(op: Operand): Operand = when (op) {
            is RegisterOperand -> RegisterOperand(replaceReg(op.register))
            else -> op
        }

        return when (instr) {
            is AssignInstr -> when (instr.value) {
                is RegisterOperand -> instr.copy(value = replace(instr.value))
                else -> instr
            }
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
