package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.OptStatistics
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CopyPropagationTest {

    private val pass = CopyPropagation()
    private val context = PassContext(OptStatistics())

    private fun makeModule(blocks: Map<BlockId, HirBasicBlock>): HirCfgModule {
        val fn = HirCfgFunction("test", emptyList(), VoidType, blocks, BlockId(0))
        return HirCfgModule("test", mapOf("test" to fn))
    }

    @Test
    fun `propagate simple copy`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val r2 = Register("r2", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, RegisterOperand(r1)),
            BinaryOpInstr(r2, "+", RegisterOperand(r0), ConstOperand(1, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val bin = instrs[1] as BinaryOpInstr
        assertEquals(RegisterOperand(r1), bin.left)
    }

    @Test
    fun `propagate through chain`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val r2 = Register("r2", IntType)
        val r3 = Register("r3", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, RegisterOperand(r1)),
            AssignInstr(r2, RegisterOperand(r0)),
            AssignInstr(r3, RegisterOperand(r2)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val last = instrs[2] as AssignInstr
        assertEquals(RegisterOperand(r1), last.value)
    }

    @Test
    fun `propagate call result`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val r2 = Register("r2", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            CallInstr(r0, "getVal", IntType, emptyList()),
            AssignInstr(r1, RegisterOperand(r0)),
            BinaryOpInstr(r2, "+", RegisterOperand(r1), ConstOperand(1, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val bin = instrs[2] as BinaryOpInstr
        assertEquals(RegisterOperand(r0), bin.left)
    }

    @Test
    fun `no copy not propagated`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(42, IntType)),
            AssignInstr(r1, RegisterOperand(r0)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertFalse(result.changed)
    }

    @Test
    fun `propagate in function arguments`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, RegisterOperand(r1)),
            CallInstr(r0, "use", VoidType, listOf(RegisterOperand(r0))),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val call = instrs[1] as CallInstr
        assertEquals(RegisterOperand(r1), call.arguments[0])
    }
}
