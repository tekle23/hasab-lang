package hasab.compiler.hir

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.types.TypeChecker
import hasab.compiler.types.IntType
import hasab.compiler.types.VoidType
import hasab.compiler.types.FunctionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HirVisitorBaseTest {

    private class NodeCounter : HirVisitorBase<Unit>(Unit) {
        var fnCount = 0
        var structCount = 0
        var intLiteralCount = 0
        var binaryCount = 0
        var letCount = 0
        var returnCount = 0

        override fun visitFnDecl(node: HirFnDecl) {
            fnCount++
            super.visitFnDecl(node)
        }

        override fun visitStructDecl(node: HirStructDecl) {
            structCount++
            super.visitStructDecl(node)
        }

        override fun visitIntLiteral(node: HirIntLiteral) {
            intLiteralCount++
            super.visitIntLiteral(node)
        }

        override fun visitBinary(node: HirBinary) {
            binaryCount++
            super.visitBinary(node)
        }

        override fun visitLetStmt(node: HirLetStmt) {
            letCount++
            super.visitLetStmt(node)
        }

        override fun visitReturnStmt(node: HirReturnStmt) {
            returnCount++
            super.visitReturnStmt(node)
        }
    }

    private fun lower(code: String): HirModule {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val typeChecker = TypeChecker()
        val typeCheckResult = typeChecker.check(parseResult.module)
        val lowering = AstToHirLowering(typeCheckResult.environment)
        return lowering.lower(parseResult.module)
    }

    @Test
    fun `visitor counts function declarations`() {
        val module = lower("fn add(x: int, y: int) -> int { return x + y; }")
        val counter = NodeCounter()
        counter.visitModule(module)
        assertEquals(1, counter.fnCount)
    }

    @Test
    fun `visitor counts multiple functions`() {
        val module = lower("""
            fn add(x: int, y: int) -> int { return x + y; }
            fn sub(x: int, y: int) -> int { return x - y; }
        """.trimIndent())
        val counter = NodeCounter()
        counter.visitModule(module)
        assertEquals(2, counter.fnCount)
    }

    @Test
    fun `visitor counts struct declarations`() {
        val module = lower("struct Point { x: int, y: int }")
        val counter = NodeCounter()
        counter.visitModule(module)
        assertEquals(1, counter.structCount)
    }

    @Test
    fun `visitor counts expressions in binary ops`() {
        val module = lower("fn main() { let z = 1 + 2; }")
        val counter = NodeCounter()
        counter.visitModule(module)
        assertEquals(1, counter.binaryCount)
        assertTrue(counter.intLiteralCount >= 2)
    }

    @Test
    fun `visitor counts let statements`() {
        val module = lower("fn main() { let x = 1; let y = 2; }")
        val counter = NodeCounter()
        counter.visitModule(module)
        assertEquals(2, counter.letCount)
    }

    @Test
    fun `visitor counts return statements`() {
        val module = lower("fn main() { return; } fn getValue() -> int { return 42; }")
        val counter = NodeCounter()
        counter.visitModule(module)
        assertEquals(2, counter.returnCount)
    }

    @Test
    fun `visitor walks nested blocks via if`() {
        val module = lower("fn main() { if true { let x = 1; } }")
        val counter = NodeCounter()
        counter.visitModule(module)
        assertEquals(1, counter.letCount)
        assertEquals(1, counter.fnCount)
    }
}
