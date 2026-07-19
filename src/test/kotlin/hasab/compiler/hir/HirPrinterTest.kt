package hasab.compiler.hir

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.types.TypeChecker
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class HirPrinterTest {

    private fun lower(code: String): HirModule {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val typeChecker = TypeChecker()
        val typeCheckResult = typeChecker.check(parseResult.module)
        val lowering = AstToHirLowering(typeCheckResult.environment)
        return lowering.lower(parseResult.module)
    }

    private fun print(code: String): String {
        return HirPrinter().print(lower(code))
    }

    @Test
    fun `print module header`() {
        val output = print("fn main() { }")
        assertTrue(output.contains("module"))
    }

    @Test
    fun `print function declaration`() {
        val output = print("fn add(x: int, y: int) -> int { return x + y; }")
        assertTrue(output.contains("fn add"))
        assertTrue(output.contains("int"))
    }

    @Test
    fun `print function with return type`() {
        val output = print("fn getValue() -> int { return 42; }")
        assertTrue(output.contains("-> int"))
    }

    @Test
    fun `print struct declaration`() {
        val output = print("struct Point { x: int, y: int }")
        assertTrue(output.contains("struct Point"))
        assertTrue(output.contains("x: int"))
        assertTrue(output.contains("y: int"))
    }

    @Test
    fun `print enum declaration`() {
        val output = print("enum Color { Red, Green, Blue }")
        assertTrue(output.contains("enum Color"))
        assertTrue(output.contains("Red"))
    }

    @Test
    fun `print let statement`() {
        val output = print("fn main() { let x = 42; }")
        assertTrue(output.contains("let x ="))
    }

    @Test
    fun `print return statement`() {
        val output = print("fn main() { return; }")
        assertTrue(output.contains("return"))
    }

    @Test
    fun `print while loop`() {
        val output = print("fn main() { while true { } }")
        assertTrue(output.contains("while"))
    }

    @Test
    fun `print pub visibility`() {
        val output = print("pub fn main() { }")
        assertTrue(output.contains("pub fn"))
    }

    @Test
    fun `print function body block`() {
        val output = print("fn main() { let x = 1; let y = 2; }")
        assertTrue(output.contains("{"))
        assertTrue(output.contains("}"))
    }
}
