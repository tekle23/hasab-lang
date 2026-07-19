package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.OptStatistics
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DeadCodeEliminationTest {

    private val pass = DeadCodeElimination()
    private val context = PassContext(OptStatistics())

    private fun makeModule(blocks: Map<BlockId, HirBasicBlock>, params: List<ParamOperand> = emptyList()): HirCfgModule {
        val fn = HirCfgFunction("test", params, VoidType, blocks, BlockId(0))
        return HirCfgModule("test", mapOf("test" to fn))
    }

    @Test
    fun `remove unused assignment`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(42, IntType)),
            AssignInstr(r1, ConstOperand(10, IntType)),
            ReturnInstr(RegisterOperand(r1)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        assertEquals(2, instrs.size)
        assertEquals("r1", (instrs[0] as AssignInstr).target.name)
        assertTrue(instrs[1] is ReturnInstr)
    }

    @Test
    fun `keep used assignment`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(5, IntType)),
            AssignInstr(r1, RegisterOperand(r0)),
            ReturnInstr(RegisterOperand(r1)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertFalse(result.changed)
        assertEquals(3, result.module.function("test").block(BlockId(0)).instructions.size)
    }

    @Test
    fun `keep side-effecting instructions`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            CallInstr(r0, "sideEffect", VoidType, emptyList()),
            AssignInstr(r1, ConstOperand(1, IntType)),
            ReturnInstr(null),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        assertEquals(2, instrs.size)
        assertTrue(instrs[0] is CallInstr)
        assertTrue(instrs[1] is ReturnInstr)
    }

    @Test
    fun `keep used in binary op`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val r2 = Register("r2", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(3, IntType)),
            AssignInstr(r1, ConstOperand(4, IntType)),
            BinaryOpInstr(r2, "+", RegisterOperand(r0), RegisterOperand(r1)),
            ReturnInstr(RegisterOperand(r2)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertFalse(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        assertEquals(4, instrs.size)
    }

    @Test
    fun `remove chained dead code`() {
        val r0 = Register("r0", IntType)
        val r1 = Register("r1", IntType)
        val r2 = Register("r2", IntType)
        val r3 = Register("r3", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            AssignInstr(r0, ConstOperand(1, IntType)),
            AssignInstr(r1, RegisterOperand(r0)),
            AssignInstr(r2, RegisterOperand(r1)),
            AssignInstr(r3, ConstOperand(99, IntType)),
            ReturnInstr(RegisterOperand(r3)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        assertEquals(4, instrs.size)
        assertEquals("r3", (instrs[3] as ReturnInstr).value.let { (it as RegisterOperand).register.name })
    }

    @Test
    fun `remove unused unary op`() {
        val r0 = Register("r0", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            UnaryOpInstr(r0, "-", ConstOperand(5, IntType)),
            ReturnInstr(null),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        val instrs = result.module.function("test").block(BlockId(0)).instructions
        assertEquals(1, instrs.size)
        assertTrue(instrs[0] is ReturnInstr)
    }
}
