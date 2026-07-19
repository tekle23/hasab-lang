package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.OptStatistics
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CFGOptimizerTest {

    private val pass = CFGOptimizer()
    private val context = PassContext(OptStatistics())

    private fun makeModule(blocks: Map<BlockId, HirBasicBlock>): HirCfgModule {
        val fn = HirCfgFunction("test", emptyList(), VoidType, blocks, BlockId(0))
        return HirCfgModule("test", mapOf("test" to fn))
    }

    @Test
    fun `merge linear blocks`() {
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(Register("a", IntType), ConstOperand(1, IntType)),
            JumpInstr(BlockId(1)),
        ))
        val bb1 = HirBasicBlock(BlockId(1), listOf(
            AssignInstr(Register("b", IntType), ConstOperand(2, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to bb0, BlockId(1) to bb1)), context)
        assertTrue(result.changed)
        val fn = result.module.function("test")
        assertEquals(1, fn.blocks.size)
        assertEquals(2, fn.block(BlockId(0)).instructions.size)
    }

    @Test
    fun `do not merge blocks with multiple predecessors`() {
        val r = Register("r", BoolType)
        val bb0 = HirBasicBlock(BlockId(0), listOf(BranchInstr(RegisterOperand(r), BlockId(1), BlockId(2))))
        val bb1 = HirBasicBlock(BlockId(1), listOf(
            AssignInstr(Register("a", IntType), ConstOperand(1, IntType)),
            JumpInstr(BlockId(3)),
        ))
        val bb2 = HirBasicBlock(BlockId(2), listOf(
            AssignInstr(Register("b", IntType), ConstOperand(2, IntType)),
            JumpInstr(BlockId(3)),
        ))
        val bb3 = HirBasicBlock(BlockId(3), listOf(
            ReturnInstr(null),
        ))
        val module = HirCfgModule("test", mapOf("test" to HirCfgFunction("test", emptyList(), VoidType, mapOf(
            BlockId(0) to bb0, BlockId(1) to bb1, BlockId(2) to bb2, BlockId(3) to bb3
        ), BlockId(0))))
        val result = pass.run(module, context)
        assertFalse(result.changed)
    }

    @Test
    fun `no blocks returns unchanged`() {
        val fn = HirCfgFunction("test", emptyList(), VoidType, emptyMap(), BlockId(0))
        val module = HirCfgModule("test", mapOf("test" to fn))
        val result = pass.run(module, context)
        assertFalse(result.changed)
    }

    @Test
    fun `single block unchanged`() {
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(Register("x", IntType), ConstOperand(1, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to bb0)), context)
        assertFalse(result.changed)
    }
}
