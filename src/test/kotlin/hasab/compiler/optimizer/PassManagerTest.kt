package hasab.compiler.optimizer

import hasab.compiler.hir.cfg.*
import hasab.compiler.optimizer.passes.ConstantFolder
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PassManagerTest {

    private fun makeModuleWithConstantFold(): HirCfgFunction {
        val r0 = Register("r0", IntType)
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            BinaryOpInstr(r0, "+", ConstOperand(3, IntType), ConstOperand(4, IntType)),
            ReturnInstr(RegisterOperand(r0)),
        ))
        return HirCfgFunction("test", emptyList(), VoidType, mapOf(BlockId(0) to bb0), BlockId(0))
    }

    @Test
    fun `single pass run applies fold`() {
        val fn = makeModuleWithConstantFold()
        val module = HirCfgModule("test", mapOf("test" to fn))
        val stats = OptStatistics()
        val context = PassContext(stats)
        val manager = PassManager(listOf(ConstantFolder()))
        val result = manager.run(module, context, maxIterations = 1)
        val resultFn = result.function("test")
        val instrs = resultFn.block(BlockId(0)).instructions
        assertTrue(instrs[0] is AssignInstr)
    }

    @Test
    fun `pass manager runs multiple iterations`() {
        val fn = makeModuleWithConstantFold()
        val module = HirCfgModule("test", mapOf("test" to fn))
        val stats = OptStatistics()
        val context = PassContext(stats)
        val manager = PassManager(listOf(ConstantFolder()))
        val result = manager.run(module, context, maxIterations = 5)
        assertTrue(result.functions.isNotEmpty())
    }

    @Test
    fun `empty pass list returns unchanged module`() {
        val fn = makeModuleWithConstantFold()
        val module = HirCfgModule("test", mapOf("test" to fn))
        val stats = OptStatistics()
        val context = PassContext(stats)
        val manager = PassManager(emptyList())
        val result = manager.run(module, context)
        assertEquals(module, result)
    }
}
