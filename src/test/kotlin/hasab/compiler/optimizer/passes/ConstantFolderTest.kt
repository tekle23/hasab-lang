package hasab.compiler.optimizer.passes

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.PassContext
import hasab.compiler.optimizer.OptStatistics
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ConstantFolderTest {

    private val pass = ConstantFolder()
    private val context = PassContext(OptStatistics())

    private fun makeModule(blocks: Map<BlockId, HirBasicBlock>): HirCfgModule {
        val fn = HirCfgFunction("test", emptyList(), VoidType, blocks, BlockId(0))
        return HirCfgModule("test", mapOf("test" to fn))
    }

    @Test
    fun `fold int addition`() {
        val r = Register("r", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "+", ConstOperand(3, IntType), ConstOperand(4, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertTrue(result.changed)
        assertEquals(1, result.stats["constants_folded"])
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertTrue(instr is AssignInstr)
        assertEquals(ConstOperand(7, IntType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold int subtraction`() {
        val r = Register("r", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "-", ConstOperand(10, IntType), ConstOperand(3, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(7, IntType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold int multiplication`() {
        val r = Register("r", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "*", ConstOperand(6, IntType), ConstOperand(7, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(42, IntType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold int division`() {
        val r = Register("r", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "/", ConstOperand(20, IntType), ConstOperand(4, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(5, IntType), (instr as AssignInstr).value)
    }

    @Test
    fun `division by zero not folded`() {
        val r = Register("r", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "/", ConstOperand(10, IntType), ConstOperand(0, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertFalse(result.changed)
    }

    @Test
    fun `fold equality comparison`() {
        val r = Register("r", BoolType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "==", ConstOperand(5, IntType), ConstOperand(5, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(true, BoolType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold less-than comparison`() {
        val r = Register("r", BoolType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "<", ConstOperand(3, IntType), ConstOperand(5, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(true, BoolType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold boolean and`() {
        val r = Register("r", BoolType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "&&", ConstOperand(true, BoolType), ConstOperand(false, BoolType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(false, BoolType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold boolean or`() {
        val r = Register("r", BoolType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "||", ConstOperand(true, BoolType), ConstOperand(false, BoolType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(true, BoolType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold unary minus`() {
        val r = Register("r", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            UnaryOpInstr(r, "-", ConstOperand(42, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(-42, IntType), (instr as AssignInstr).value)
    }

    @Test
    fun `fold unary not`() {
        val r = Register("r", BoolType)
        val block = HirBasicBlock(BlockId(0), listOf(
            UnaryOpInstr(r, "!", ConstOperand(true, BoolType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(false, BoolType), (instr as AssignInstr).value)
    }

    @Test
    fun `non-constant operands not folded`() {
        val r = Register("r", IntType)
        val x = Register("x", IntType)
        val y = Register("y", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "+", RegisterOperand(x), RegisterOperand(y)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        assertFalse(result.changed)
    }

    @Test
    fun `modular fold`() {
        val r = Register("r", IntType)
        val block = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r, "%", ConstOperand(17, IntType), ConstOperand(5, IntType)),
        ))
        val result = pass.run(makeModule(mapOf(BlockId(0) to block)), context)
        val instr = result.module.function("test").block(BlockId(0)).instructions[0]
        assertEquals(ConstOperand(2, IntType), (instr as AssignInstr).value)
    }
}
