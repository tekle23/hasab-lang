package hasab.compiler.backend

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.types.TypeChecker
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JavaSourceGeneratorTest {

    private fun generate(code: String): String {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val tc = TypeChecker(parseResult.module)
        val result = tc.check()
        return JavaSourceGenerator(result.diagnostics).generate(parseResult.module)
    }

    private fun compileAndGenerate(code: String): CompilationResult {
        return HasabToJavaCompiler.compile(code)
    }

    // ── Function generation ────────────────────────────────────────

    @Test
    fun `generate simple function`() {
        val java = generate("fn add(x: int, y: int) -> int { return x + y; }")
        assertContains(java, "public static int add(int x, int y)")
        assertContains(java, "return x + y;")
    }

    @Test
    fun `generate void function`() {
        val java = generate("fn noop() { }")
        assertContains(java, "public static void noop()")
    }

    @Test
    fun `generate function with string param`() {
        val java = generate("fn greet(name: string) { }")
        assertContains(java, "public static void greet(String name)")
    }

    @Test
    fun `generate function with bool param`() {
        val java = generate("fn check(flag: bool) -> bool { return flag; }")
        assertContains(java, "public static boolean check(boolean flag)")
        assertContains(java, "return flag;")
    }

    // ── Struct generation ──────────────────────────────────────────

    @Test
    fun `generate struct`() {
        val java = generate("struct Point { x: int, y: int }")
        assertContains(java, "public class Point {")
        assertContains(java, "public int x;")
        assertContains(java, "public int y;")
        assertContains(java, "public Point() {}")
        assertContains(java, "this.x = x;")
        assertContains(java, "this.y = y;")
    }

    @Test
    fun `generate struct with float field`() {
        val java = generate("struct Vec2 { x: float, y: float }")
        assertContains(java, "public double x;")
        assertContains(java, "public double y;")
    }

    // ── Enum generation ────────────────────────────────────────────

    @Test
    fun `generate simple enum`() {
        val java = generate("enum Color { Red, Green, Blue }")
        assertContains(java, "public enum Color {")
        assertContains(java, "Red,")
        assertContains(java, "Green,")
        assertContains(java, "Blue;")
    }

    @Test
    fun `generate enum with fields`() {
        val java = generate("enum Result { Ok(int), Err(string) }")
        assertContains(java, "public enum Result {")
        assertContains(java, "Ok(int")
        assertContains(java, "public static class Ok_Fields")
    }

    // ── Control flow generation ────────────────────────────────────

    @Test
    fun `generate if statement`() {
        val java = generate("fn main() { if true { let x = 1; } }")
        assertContains(java, "if (true)")
        assertContains(java, "int x = 1;")
    }

    @Test
    fun `generate if-else statement`() {
        val java = generate("fn main() { if true { let x = 1; } else { let y = 2; } }")
        assertContains(java, "} else {")
    }

    @Test
    fun `generate while loop`() {
        val java = generate("fn main() { while true { } }")
        assertContains(java, "while (true)")
    }

    @Test
    fun `generate for loop`() {
        val java = generate("fn main() { let arr = [1, 2]; for (i: arr) { } }")
        assertContains(java, "for (var i : ")
    }

    @Test
    fun `generate break and continue`() {
        val java = generate("fn main() { while true { break; continue; } }")
        assertContains(java, "break;")
        assertContains(java, "continue;")
    }

    // ── Expression generation ──────────────────────────────────────

    @Test
    fun `generate binary expression`() {
        val java = generate("fn main() { let x = 1 + 2; }")
        assertContains(java, "1 + 2")
    }

    @Test
    fun `generate comparison expression`() {
        val java = generate("fn main() { let x = 1 < 2; }")
        assertContains(java, "1 < 2")
    }

    @Test
    fun `generate string literal`() {
        val java = generate("fn main() { let x = \"hello\"; }")
        assertContains(java, "\"hello\"")
    }

    @Test
    fun `generate bool literal`() {
        val java = generate("fn main() { let x = true; }")
        assertContains(java, "true")
    }

    @Test
    fun `generate nil literal`() {
        val java = generate("fn main() { let x = nil; }")
        assertContains(java, "null")
    }

    @Test
    fun `generate println as System out`() {
        val java = generate("fn main() { println(\"hello\"); }")
        assertContains(java, "System.out.println(\"hello\")")
    }

    @Test
    fun `generate println with expression`() {
        val java = generate("fn main() { println(1 + 2); }")
        assertContains(java, "System.out.println(1 + 2)")
    }

    // ── Field access ───────────────────────────────────────────────

    @Test
    fun `generate field access`() {
        val java = generate("""
            struct Point { x: int, y: int }
            fn getX(p: Point) -> int { return p.x; }
        """.trimIndent())
        assertContains(java, "p.x")
    }

    // ── Array generation ───────────────────────────────────────────

    @Test
    fun `generate array literal`() {
        val java = generate("fn main() { let arr = [1, 2, 3]; }")
        assertContains(java, "new Object[]{")
    }

    @Test
    fun `generate array init`() {
        val java = generate("fn main() { let arr = .[10]; }")
        assertContains(java, "new Object[10]")
    }

    @Test
    fun `generate array index`() {
        val java = generate("fn main() { let arr = [1, 2]; let x = arr[0]; }")
        assertContains(java, "arr[0]")
    }

    // ── Assignment ─────────────────────────────────────────────────

    @Test
    fun `generate assignment`() {
        val java = generate("fn main() { mut x = 1; x = 2; }")
        assertContains(java, "x = 2")
    }

    @Test
    fun `generate compound assignment`() {
        val java = generate("fn main() { mut x: int = 1; x += 2; }")
        println("COMPOUND OUTPUT:\n$java")
        assertTrue(java.contains("x += 2"), "Expected 'x += 2' in output")
    }

    // ── Type alias ─────────────────────────────────────────────────

    @Test
    fun `generate type alias`() {
        val java = generate("type IntList = [int]")
        assertContains(java, "public static class IntList extends int[]")
    }

    // ── Full pipeline ──────────────────────────────────────────────

    @Test
    fun `full pipeline compile`() {
        val result = compileAndGenerate("""
            struct Point { x: int, y: int }
            fn distance(p: Point) -> int { return p.x + p.y; }
        """.trimIndent())
        assertFalse(result.hasErrors)
        assertContains(result.javaSource, "public class Point")
        assertContains(result.javaSource, "public static int distance")
    }

    @Test
    fun `type errors produce comments`() {
        val result = compileAndGenerate("fn main() { let x: float = \"hello\"; }")
        assertTrue(result.hasErrors)
        assertContains(result.javaSource, "// TYPE ERROR:")
    }
}
