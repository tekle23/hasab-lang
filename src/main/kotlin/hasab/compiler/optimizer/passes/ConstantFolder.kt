package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.OptimizationPass
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.PassResult
import hasab.compiler.types.BoolType
import hasab.compiler.types.IntType
import hasab.compiler.types.FloatType

/**
 * Folds binary and unary operations on constant operands into
 * a single constant assignment.
 */
public class ConstantFolder : OptimizationPass {
    override val name: String = "ConstantFolder"
    override val description: String = "Folds constant expressions"

    override fun run(module: HirCfgModule, context: PassContext): PassResult {
        var totalFolded = 0
        val newFunctions = mutableMapOf<String, hasab.compiler.hir.cfg.HirCfgFunction>()

        for ((fnName, fn) in module.functions) {
            val newBlocks = mutableMapOf<BlockId, HirBasicBlock>()
            var changed = false

            for ((blockId, block) in fn.blocks) {
                val newInstructions = mutableListOf<HirInstruction>()
                for (instr in block.instructions) {
                    val folded = tryFold(instr)
                    if (folded != null) {
                        newInstructions.add(folded)
                        changed = true
                        totalFolded++
                    } else {
                        newInstructions.add(instr)
                    }
                }
                if (changed) {
                    newBlocks[blockId] = block.copy(instructions = newInstructions)
                } else {
                    newBlocks[blockId] = block
                }
            }

            if (changed) {
                newFunctions[fnName] = fn.copy(blocks = newBlocks)
            } else {
                newFunctions[fnName] = fn
            }
        }

        val changed = newFunctions != module.functions
        return PassResult(
            module = module.copy(functions = newFunctions),
            changed = changed,
            stats = mapOf("constants_folded" to totalFolded),
        )
    }

    private fun tryFold(instr: HirInstruction): HirInstruction? = when (instr) {
        is BinaryOpInstr -> tryFoldBinary(instr)
        is UnaryOpInstr -> tryFoldUnary(instr)
        else -> null
    }

    private fun tryFoldBinary(instr: BinaryOpInstr): HirInstruction? {
        val left = instr.left
        val right = instr.right
        if (left !is ConstOperand || right !is ConstOperand) return null

        val result = when (instr.operator) {
            "+" -> foldNumeric(left, right, { a, b -> a + b }, { a, b -> a + b })
            "-" -> foldNumeric(left, right, { a, b -> a - b }, { a, b -> a - b })
            "*" -> foldNumeric(left, right, { a, b -> a * b }, { a, b -> a * b })
            "/" -> foldNumericNonZero(left, right) { a, b -> a / b }
            "%" -> foldNumericNonZero(left, right) { a, b -> a % b }
            "==" -> foldComparison(left, right) { a, b -> a == b }
            "!=" -> foldComparison(left, right) { a, b -> a != b }
            "<" -> foldOrderedComparison(left, right) { a, b -> a < b }
            ">" -> foldOrderedComparison(left, right) { a, b -> a > b }
            "<=" -> foldOrderedComparison(left, right) { a, b -> a <= b }
            ">=" -> foldOrderedComparison(left, right) { a, b -> a >= b }
            "&&" -> foldBooleanLogic(left, right) { a, b -> a && b }
            "||" -> foldBooleanLogic(left, right) { a, b -> a || b }
            else -> return null
        } ?: return null

        return AssignInstr(target = instr.target, value = result, comment = "folded: ${instr.operator}")
    }

    private fun tryFoldUnary(instr: UnaryOpInstr): HirInstruction? {
        val operand = instr.operand
        if (operand !is ConstOperand) return null

        val result = when (instr.operator) {
            "-" -> when (operand.value) {
                is Int -> ConstOperand(-operand.value as Int, operand.type)
                is Double -> ConstOperand(-(operand.value as Double), operand.type)
                is Float -> ConstOperand(-(operand.value as Float), operand.type)
                else -> null
            }
            "!" -> when (operand.value) {
                is Boolean -> ConstOperand(!(operand.value as Boolean), BoolType)
                else -> null
            }
            else -> null
        } ?: return null

        return AssignInstr(target = instr.target, value = result, comment = "folded: ${instr.operator}")
    }

    private fun foldNumeric(
        left: ConstOperand,
        right: ConstOperand,
        intOp: (Int, Int) -> Int,
        doubleOp: (Double, Double) -> Double,
    ): ConstOperand? {
        val l = left.value
        val r = right.value
        return when {
            l is Int && r is Int -> ConstOperand(intOp(l, r), left.type)
            l is Double && r is Double -> ConstOperand(doubleOp(l, r), left.type)
            l is Number && r is Number -> {
                val ld = l.toDouble()
                val rd = r.toDouble()
                ConstOperand(doubleOp(ld, rd), left.type)
            }
            else -> null
        }
    }

    private fun foldNumericNonZero(
        left: ConstOperand,
        right: ConstOperand,
        op: (Int, Int) -> Int,
    ): ConstOperand? {
        val l = left.value as? Int ?: return null
        val r = right.value as? Int ?: return null
        if (r == 0) return null
        return ConstOperand(op(l, r), left.type)
    }

    @Suppress("UNCHECKED_CAST")
    private fun foldComparison(
        left: ConstOperand,
        right: ConstOperand,
        eq: (Any?, Any?) -> Boolean,
    ): ConstOperand {
        val result = eq(left.value, right.value)
        return ConstOperand(result, BoolType)
    }

    @Suppress("UNCHECKED_CAST")
    private fun foldOrderedComparison(
        left: ConstOperand,
        right: ConstOperand,
        cmp: (Comparable<Any>, Comparable<Any>) -> Boolean,
    ): ConstOperand? {
        val l = left.value as? Comparable<*> ?: return null
        val r = right.value as? Comparable<*> ?: return null
        @Suppress("UNCHECKED_CAST")
        val result = cmp(l as Comparable<Any>, r as Comparable<Any>)
        return ConstOperand(result, BoolType)
    }

    @Suppress("UNCHECKED_CAST")
    private fun foldBooleanLogic(
        left: ConstOperand,
        right: ConstOperand,
        op: (Boolean, Boolean) -> Boolean,
    ): ConstOperand? {
        val l = left.value as? Boolean ?: return null
        val r = right.value as? Boolean ?: return null
        return ConstOperand(op(l, r), BoolType)
    }
}
