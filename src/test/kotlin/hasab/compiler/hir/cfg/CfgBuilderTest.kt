package hasab.compiler.hir.cfg

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.hir.AstToHirLowering
import hasab.compiler.hir.HirFnDecl
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class CfgBuilderTest {

    private fun buildCfg(code: String, index: Int = 0): HirCfgFunction {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val typeChecker = TypeChecker()
        val typeCheckResult = typeChecker.check(parseResult.module)
        val lowering = AstToHirLowering(typeCheckResult.environment)
        val hirModule = lowering.lower(parseResult.module)
        val fns = hirModule.declarations.filterIsInstance<HirFnDecl>()
        return CfgBuilder().build(fns[index])
    }

    @Test
    fun `simple function has blocks with return`() {
        val cfg = buildCfg("fn main() { }")
        assertTrue(cfg.blocks.isNotEmpty())
        val entry = cfg.block(cfg.entryBlockId)
        assertTrue(entry.terminator is ReturnInstr)
    }

    @Test
    fun `function with let has assignment instruction`() {
        val cfg = buildCfg("fn main() { let x = 42; }")
        val entry = cfg.block(cfg.entryBlockId)
        val hasAssign = entry.instructions.any { it is AssignInstr }
        assertTrue(hasAssign)
    }

    @Test
    fun `if statement creates multiple blocks`() {
        val cfg = buildCfg("fn main() { if true { let x = 1; } }")
        assertTrue(cfg.blocks.size >= 3)
    }

    @Test
    fun `while loop creates header and body blocks`() {
        val cfg = buildCfg("fn main() { while true { } }")
        assertTrue(cfg.blocks.size >= 3)
    }

    @Test
    fun `return creates return terminator`() {
        val cfg = buildCfg("fn main() { return; }")
        val entry = cfg.block(cfg.entryBlockId)
        assertTrue(entry.terminator is ReturnInstr)
    }

    @Test
    fun `function parameters are recorded`() {
        val cfg = buildCfg("fn add(x: int, y: int) -> int { return x + y; }")
        assertEquals(2, cfg.parameters.size)
        assertEquals("x", cfg.parameters[0].name)
        assertEquals("y", cfg.parameters[1].name)
        assertEquals(IntType, cfg.parameters[0].type)
    }

    @Test
    fun `binary expression produces binary op instruction`() {
        val cfg = buildCfg("fn main() { let z = 1 + 2; }")
        val allInstrs = cfg.blocks.values.flatMap { it.instructions }
        val hasBinary = allInstrs.any { it is BinaryOpInstr }
        assertTrue(hasBinary)
    }

    @Test
    fun `function call produces call instruction`() {
        val cfg = buildCfg("""
            fn add(x: int, y: int) -> int { return x + y; }
            fn main() { let r = add(1, 2); }
        """.trimIndent(), index = 1)
        val allInstrs = cfg.blocks.values.flatMap { it.instructions }
        val hasCall = allInstrs.any { it is CallInstr }
        assertTrue(hasCall)
    }

    @Test
    fun `entry block id is correct`() {
        val cfg = buildCfg("fn main() { }")
        assertEquals(BlockId(0), cfg.entryBlockId)
    }

    @Test
    fun `void function gets implicit return`() {
        val cfg = buildCfg("fn main() { let x = 1; }")
        val lastBlock = cfg.blocks.values.last()
        assertTrue(lastBlock.terminator is ReturnInstr)
    }

    @Test
    fun `multi-block function has correct block count`() {
        val cfg = buildCfg("fn main() { if true { let x = 1; } else { let y = 2; } }")
        assertTrue(cfg.blocks.size >= 4)
    }
}
