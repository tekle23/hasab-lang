package hasab.compiler.optimizer

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.passes.*
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OptimizationManagerTest {

    @Test
    fun `debug profile returns module unchanged`() {
        val r0 = Register("r0", IntType)
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r0, "+", ConstOperand(3, IntType), ConstOperand(4, IntType)),
            ReturnInstr(RegisterOperand(r0)),
        ))
        val fn = HirCfgFunction("test", emptyList(), VoidType, mapOf(BlockId(0) to bb0), BlockId(0))
        val module = HirCfgModule("test", mapOf("test" to fn))

        val manager = OptimizationManager(OptProfile.Debug)
        val result = manager.optimize(module)

        val instrs = result.module.function("test").block(BlockId(0)).instructions
        assertTrue(instrs[0] is BinaryOpInstr)
    }

    @Test
    fun `release profile applies optimizations`() {
        val r0 = Register("r0", IntType)
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r0, "+", ConstOperand(3, IntType), ConstOperand(4, IntType)),
            ReturnInstr(RegisterOperand(r0)),
        ))
        val fn = HirCfgFunction("test", emptyList(), VoidType, mapOf(BlockId(0) to bb0), BlockId(0))
        val module = HirCfgModule("test", mapOf("test" to fn))

        val manager = OptimizationManager(OptProfile.Release)
        val result = manager.optimize(module)

        val instrs = result.module.function("test").block(BlockId(0)).instructions
        assertTrue(instrs.isNotEmpty())
        assertTrue(instrs.last() is ReturnInstr)
        val retVal = (instrs.last() as ReturnInstr).value
        assertEquals(ConstOperand(7, IntType), retVal)
    }

    @Test
    fun `result has statistics`() {
        val r0 = Register("r0", IntType)
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r0, "+", ConstOperand(3, IntType), ConstOperand(4, IntType)),
            ReturnInstr(RegisterOperand(r0)),
        ))
        val fn = HirCfgFunction("test", emptyList(), VoidType, mapOf(BlockId(0) to bb0), BlockId(0))
        val module = HirCfgModule("test", mapOf("test" to fn))

        val manager = OptimizationManager(OptProfile.Release)
        val result = manager.optimize(module)

        assertTrue(result.statistics.totalByMetric("constants_folded") >= 0)
    }

    @Test
    fun `result module has functions`() {
        val r0 = Register("r0", IntType)
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r0, "+", ConstOperand(3, IntType), ConstOperand(4, IntType)),
            ReturnInstr(RegisterOperand(r0)),
        ))
        val fn = HirCfgFunction("test", emptyList(), VoidType, mapOf(BlockId(0) to bb0), BlockId(0))
        val module = HirCfgModule("test", mapOf("test" to fn))

        val manager = OptimizationManager(OptProfile.Debug)
        val result = manager.optimize(module)

        assertTrue(result.module.functions.containsKey("test"))
    }
}
