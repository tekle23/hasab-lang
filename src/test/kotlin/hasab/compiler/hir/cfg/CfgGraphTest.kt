package hasab.compiler.hir.cfg

import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CfgGraphTest {

    private fun buildTestCfg(): HirCfgFunction {
        // bb0: Branch -> bb1, bb2
        // bb1: Jump -> bb3
        // bb2: Jump -> bb3
        // bb3: Return
        val bb0 = HirBasicBlock(BlockId(0), listOf(
            BranchInstr(ConstOperand(true, BoolType), BlockId(1), BlockId(2)),
        ))
        val bb1 = HirBasicBlock(BlockId(1), listOf(
            JumpInstr(BlockId(3)),
        ))
        val bb2 = HirBasicBlock(BlockId(2), listOf(
            JumpInstr(BlockId(3)),
        ))
        val bb3 = HirBasicBlock(BlockId(3), listOf(
            ReturnInstr(null),
        ))

        return HirCfgFunction(
            name = "test",
            parameters = emptyList(),
            returnType = VoidType,
            blocks = mapOf(
                BlockId(0) to bb0,
                BlockId(1) to bb1,
                BlockId(2) to bb2,
                BlockId(3) to bb3,
            ),
            entryBlockId = BlockId(0),
        )
    }

    @Test
    fun `successors of branch block`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        val succs = graph.successors(BlockId(0))
        assertEquals(2, succs.size)
        assertTrue(BlockId(1) in succs)
        assertTrue(BlockId(2) in succs)
    }

    @Test
    fun `successors of jump block`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        val succs = graph.successors(BlockId(1))
        assertEquals(1, succs.size)
        assertEquals(BlockId(3), succs[0])
    }

    @Test
    fun `successors of return block is empty`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        val succs = graph.successors(BlockId(3))
        assertTrue(succs.isEmpty())
    }

    @Test
    fun `predecessors of merge block`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        val preds = graph.predecessors(BlockId(3))
        assertEquals(2, preds.size)
        assertTrue(BlockId(1) in preds)
        assertTrue(BlockId(2) in preds)
    }

    @Test
    fun `entry block dominates all`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        assertTrue(graph.dominates(BlockId(0), BlockId(1)))
        assertTrue(graph.dominates(BlockId(0), BlockId(2)))
        assertTrue(graph.dominates(BlockId(0), BlockId(3)))
    }

    @Test
    fun `bb1 does not dominate bb2`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        assertFalse(graph.dominates(BlockId(1), BlockId(2)))
    }

    @Test
    fun `block dominates itself`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        assertTrue(graph.dominates(BlockId(1), BlockId(1)))
    }

    @Test
    fun `reachability check`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        assertTrue(graph.reachable(BlockId(0), BlockId(3)))
        assertTrue(graph.reachable(BlockId(0), BlockId(1)))
        assertFalse(graph.reachable(BlockId(1), BlockId(2)))
    }

    @Test
    fun `reverse post order starts with entry`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        val rpo = graph.reversePostOrder
        assertEquals(BlockId(0), rpo.first())
    }

    @Test
    fun `reverse post order includes all blocks`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        val rpo = graph.reversePostOrder
        assertEquals(4, rpo.size)
    }

    @Test
    fun `dominator set of entry is just itself`() {
        val graph = CfgGraph.fromFunction(buildTestCfg())
        val domSet = graph.dominatorSet(BlockId(0))
        assertEquals(setOf(BlockId(0)), domSet)
    }
}
