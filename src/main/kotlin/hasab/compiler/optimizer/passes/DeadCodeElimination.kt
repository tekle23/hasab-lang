package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.OptimizationPass
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.PassResult

/**
 * Dead code elimination with two sub-passes:
 * 1. Remove unreachable blocks (not in reverse post order except entry).
 * 2. Remove instructions whose target register is never used afterwards.
 */
public class DeadCodeElimination : OptimizationPass {
    override val name: String = "DeadCodeElimination"
    override val description: String = "Removes unreachable blocks and dead registers"

    override fun run(module: HirCfgModule, context: PassContext): PassResult {
        var totalBlocksRemoved = 0
        var totalRegistersRemoved = 0
        val newFunctions = mutableMapOf<String, HirCfgFunction>()

        for ((fnName, fn) in module.functions) {
            val cfg = CfgGraph.fromFunction(fn)
            val reachableBlockIds = cfg.reversePostOrder.toSet()

            val blocksRemoved = fn.blocks.keys.count { it != fn.entryBlockId && it !in reachableBlockIds }

            val liveBlocks = fn.blocks.filter { (id, _) -> id == fn.entryBlockId || id in reachableBlockIds }

            val registersUsed = mutableSetOf<Register>()
            for ((_, block) in liveBlocks) {
                for (instr in block.instructions) {
                    collectOperandRegisters(instr, registersUsed)
                }
            }

            val registersDefined = mutableMapOf<Register, HirInstruction>()
            for ((_, block) in liveBlocks) {
                for (instr in block.instructions) {
                    val target = instr.targetRegister()
                    if (target != null) {
                        registersDefined[target] = instr
                    }
                }
            }

            val deadRegisters = mutableSetOf<Register>()
            for ((reg, definingInstr) in registersDefined) {
                if (reg !in registersUsed) {
                    if (definingInstr is CallInstr) continue
                    deadRegisters.add(reg)
                }
            }

            val newBlocks = mutableMapOf<BlockId, HirBasicBlock>()
            for ((blockId, block) in liveBlocks) {
                val newInstructions = block.instructions.filter { instr ->
                    val target = instr.targetRegister()
                    target == null || target !in deadRegisters
                }
                newBlocks[blockId] = block.copy(instructions = newInstructions)
            }

            newFunctions[fnName] = fn.copy(blocks = newBlocks)
            totalBlocksRemoved += blocksRemoved
            totalRegistersRemoved += deadRegisters.size
        }

        val changed = newFunctions != module.functions
        return PassResult(
            module = module.copy(functions = newFunctions),
            changed = changed,
            stats = mapOf(
                "blocks_removed" to totalBlocksRemoved,
                "registers_removed" to totalRegistersRemoved,
            ),
        )
    }

    private fun collectOperandRegisters(instr: HirInstruction, out: MutableSet<Register>) {
        fun collect(op: Operand) {
            if (op is RegisterOperand) out.add(op.register)
        }

        when (instr) {
            is AssignInstr -> collect(instr.value)
            is BinaryOpInstr -> { collect(instr.left); collect(instr.right) }
            is UnaryOpInstr -> collect(instr.operand)
            is CallInstr -> instr.arguments.forEach { collect(it) }
            is LoadFieldInstr -> collect(instr.base)
            is LoadIndexInstr -> { collect(instr.base); collect(instr.index) }
            is ArrayLiteralInstr -> instr.elements.forEach { collect(it) }
            is ArrayInitInstr -> collect(instr.size)
            is PhiInstr -> instr.sources.forEach { collect(it.second) }
            is CastInstr -> collect(instr.source)
            is NullCheckInstr -> collect(instr.source)
            is NullAssertInstr -> collect(instr.source)
            is ReturnInstr -> instr.value?.let { collect(it) }
            is BranchInstr -> collect(instr.condition)
            is JumpInstr -> {}
            is SwitchInstr -> collect(instr.subject)
            is StoreFieldInstr -> { collect(instr.base); collect(instr.value) }
            is StoreIndexInstr -> { collect(instr.base); collect(instr.index); collect(instr.value) }
        }
    }
}
