package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.OptStatistics
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ConstantPropagationTest {

    private val pass = ConstantPropagation()
    private val context = PassContext(OptStatistics())

    private fun makeModule(blocks: Map<BlockId, HirBasicBlock>): HirCfgModule {
        val fn = HirCfgFunction("test", emptyList(), VoidType, blocks, BlockId(0))
        return HirCfgModule("test", mapOf("test" to fn))
    }

    @Test
    fun `propagate constant assignment`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(42, IntType)),
            AssignInstr(r1, RegisterOperand(r0)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val instr1 = instrs[1] as AssignInstr
        assertEquals(ConstOperand(42, IntType), instr1.value)
    }

    @Test
    fun `propagate through binary op`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val r2 = Register("r2", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(5, IntType)),
            AssignInstr(r1, ConstOperand(10, IntType)),
            BinaryOpInstr(r2, "+", RegisterOperand(r0), RegisterOperand(r1)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val bin = instrs[2] as BinaryOpInstr
        assertEquals(ConstOperand(5, IntType), bin.left)
        assertEquals(ConstOperand(10, IntType), bin.right)
    }

    @Test
    fun `propagate through function call`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(7, IntType)),
            CallInstr(r1, "foo", VoidType, listOf(RegisterOperand(r0))),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val call = instrs[1] as CallInstr
        assertEquals(ConstOperand(7, IntType), call.arguments[0])
    }

    @Test
    fun `non-constant value not propagated`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val param = Register("param", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, RegisterOperand(param)),
            AssignInstr(r1, RegisterOperand(r0)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val instr1 = instrs[1] as AssignInstr
        assertEquals(RegisterOperand(r0), instr1.value)
    }

    @Test
    fun `propagate with two blocks`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(99, IntType)),
            AssignInstr(r1, RegisterOperand(r0)),
            JumpInstr(BlockId(1)),
        ))
        val bb1 = HirBasicBlock(BlockId(1), listOf(
            ReturnInstr(null),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block, BlockId(1) to bb1)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val instr = instrs[1] as AssignInstr
        assertEquals(ConstOperand(99, IntType), instr.value)
    }

    @Test
    fun `no instructions left unchanged`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(3, IntType)),
            AssignInstr(r1, ConstOperand(4, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        val instr1 = instrs[1] as AssignInstr
        assertEquals(ConstOperand(4, IntType), instr1.value)
    }
}
