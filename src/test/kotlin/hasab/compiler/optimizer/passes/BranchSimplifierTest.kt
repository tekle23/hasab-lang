package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.OptStatistics
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class BranchSimplifierTest {

    private val pass = BranchSimplifier()
    private val context = PassContext(OptStatistics())

    @Test
    fun `simplify true branch to jump`() {
        val bb0 = HirBasicBlock(BlockId(0), listOf(BranchInstr(ConstOperand(true, BoolType), BlockId(1), BlockId(2))))
        val bb1 = HirBasicBlock(BlockId(1), listOf(JumpInstr(BlockId(3))))
        val bb2 = HirBasicBlock(BlockId(2), listOf(JumpInstr(BlockId(3))))
        val bb3 = HirBasicBlock(BlockId(3), listOf())
        val module = HirCfgModule("test", mapOf("test" to HirCfgFunction("test", emptyList(), VoidType, mapOf(
            BlockId(0) to bb0, BlockId(1) to bb1, BlockId(2) to bb2, BlockId(3) to bb3
        ), BlockId(0))))
        val result = pass.run(module, context)
        assertTrue(result.changed)
        val instrs0 = result.module.function("test").block(BlockId(0)).instructions
        val term = instrs0[0]
        assertTrue(term is JumpInstr)
        assertEquals(BlockId(1), (term as JumpInstr).target)
    }

    @Test
    fun `simplify false branch to jump`() {
        val bb0 = HirBasicBlock(BlockId(0), listOf(BranchInstr(ConstOperand(false, BoolType), BlockId(1), BlockId(2))))
        val bb1 = HirBasicBlock(BlockId(1), listOf(JumpInstr(BlockId(3))))
        val bb2 = HirBasicBlock(BlockId(2), listOf(JumpInstr(BlockId(3))))
        val bb3 = HirBasicBlock(BlockId(3), listOf())
        val module = HirCfgModule("test", mapOf("test" to HirCfgFunction("test", emptyList(), VoidType, mapOf(
            BlockId(0) to bb0, BlockId(1) to bb1, BlockId(2) to bb2, BlockId(3) to bb3
        ), BlockId(0))))
        val result = pass.run(module, context)
        assertTrue(result.changed)
        val instrs0 = result.module.function("test").block(BlockId(0)).instructions
        val term = instrs0[0]
        assertTrue(term is JumpInstr)
        assertEquals(BlockId(2), (term as JumpInstr).target)
    }

    @Test
    fun `non-constant condition not simplified`() {
        val r = Register("r", BoolType)
        val bb0 = HirBasicBlock(BlockId(0), listOf(BranchInstr(RegisterOperand(r), BlockId(1), BlockId(2))))
        val bb1 = HirBasicBlock(BlockId(1), listOf(JumpInstr(BlockId(0))))
        val bb2 = HirBasicBlock(BlockId(2), listOf())
        val module = HirCfgModule("test", mapOf("test" to HirCfgFunction("test", emptyList(), VoidType, mapOf(
            BlockId(0) to bb0, BlockId(1) to bb1, BlockId(2) to bb2
        ), BlockId(0))))
        val result = pass.run(module, context)
        assertFalse(result.changed)
    }

    @Test
    fun `simplify dead branch then fold constant`() {
        val bb0 = HirBasicBlock(BlockId(0), listOf(BranchInstr(ConstOperand(true, BoolType), BlockId(1), BlockId(2))))
        val bb1 = HirBasicBlock(BlockId(1), listOf(JumpInstr(BlockId(3))))
        val bb2 = HirBasicBlock(BlockId(2), listOf(BranchInstr(ConstOperand(false, BoolType), BlockId(0), BlockId(3))))
        val bb3 = HirBasicBlock(BlockId(3), listOf())
        val module = HirCfgModule("test", mapOf("test" to HirCfgFunction("test", emptyList(), VoidType, mapOf(
            BlockId(0) to bb0, BlockId(1) to bb1, BlockId(2) to bb2, BlockId(3) to bb3
        ), BlockId(0))))
        val result = pass.run(module, context)
        assertTrue(result.changed)
    }
}
