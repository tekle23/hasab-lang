package hasab.compiler.types

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TypeCheckerTest {

    private fun check(code: String): TypeCheckResult {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        return TypeChecker(parseResult.module).check()
    }

    // ── Function declarations ──────────────────────────────────────

    @Test
    fun `valid function declaration`() {
        val r = check("fn add(x: int, y: int) -> int { return x + y; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `function with no return type`() {
        val r = check("fn noop() { }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `function returning wrong type`() {
        val r = check("fn getNum() -> int { return \"hello\"; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("Return type mismatch") })
    }

    @Test
    fun `function with missing return has no type error`() {
        val r = check("fn getNum() -> int { }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `function returning void with expression`() {
        val r = check("fn noop() { return 42; }")
        assertTrue(r.hasErrors)
    }

    // ── Variable declarations ──────────────────────────────────────

    @Test
    fun `let declaration with matching type`() {
        val r = check("fn main() { let x: int = 42; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `let declaration with type mismatch`() {
        val r = check("fn main() { let x: int = \"hello\"; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("Type mismatch") })
    }

    @Test
    fun `let declaration with inference`() {
        val r = check("fn main() { let x = 42; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `mut variable can be reassigned`() {
        val r = check("fn main() { mut x = 1; x = 2; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `immutable variable cannot be reassigned`() {
        val r = check("fn main() { let x = 1; x = 2; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("non-assignable") })
    }

    @Test
    fun `duplicate variable in same scope`() {
        val r = check("fn main() { let x = 1; let x = 2; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("already defined") })
    }

    @Test
    fun `nil init requires type annotation`() {
        val r = check("fn main() { let x = nil; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("Cannot infer type") })
    }

    // ── Binary expressions ─────────────────────────────────────────

    @Test
    fun `int arithmetic`() {
        val r = check("fn main() { let x = 1 + 2; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `int comparison`() {
        val r = check("fn main() { let x = 1 < 2; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `bool logical and`() {
        val r = check("fn main() { let x = true && false; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `int and string cannot compare`() {
        val r = check("fn main() { let x = 1 < \"hello\"; }")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `int and bool cannot add`() {
        val r = check("fn main() { let x = 1 + true; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("cannot be applied") })
    }

    @Test
    fun `string concatenation`() {
        val r = check("fn main() { let x = \"hello\" + \" world\"; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `string and int cannot add`() {
        val r = check("fn main() { let x = \"hello\" + 1; }")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `int bitwise operators`() {
        val r = check("fn main() { let x = 1 & 2; let y = 1 | 2; let z = 1 ^ 2; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `int shift operators`() {
        val r = check("fn main() { let x = 1 << 2; let y = 8 >> 1; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `range operator`() {
        val r = check("fn main() { let x = 0..10; }")
        assertFalse(r.hasErrors)
    }

    // ── Unary expressions ──────────────────────────────────────────

    @Test
    fun `unary minus on int`() {
        val r = check("fn main() { let x = -42; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `unary not on bool`() {
        val r = check("fn main() { let x = !true; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `unary not on int is error`() {
        val r = check("fn main() { let x = !42; }")
        assertTrue(r.hasErrors)
    }

    // ── If statements ──────────────────────────────────────────────

    @Test
    fun `if with bool condition`() {
        val r = check("fn main() { if true { let x = 1; } }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `if with non-bool condition`() {
        val r = check("fn main() { if 42 { let x = 1; } }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("Condition must be 'bool'") })
    }

    @Test
    fun `if-else with different branches`() {
        val r = check("fn main() { if true { let x = 1; } else { let x = 2; } }")
        assertFalse(r.hasErrors)
    }

    // ── While/for loops ────────────────────────────────────────────

    @Test
    fun `while with bool condition`() {
        val r = check("fn main() { while true { } }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `while with non-bool condition`() {
        val r = check("fn main() { while 42 { } }")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `for loop over array`() {
        val r = check("fn main() { let arr = [1, 2, 3]; for i in arr { } }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `break outside loop is error`() {
        val r = check("fn main() { break; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("'break' outside of loop") })
    }

    @Test
    fun `continue outside loop is error`() {
        val r = check("fn main() { continue; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("'continue' outside of loop") })
    }

    @Test
    fun `break inside loop is ok`() {
        val r = check("fn main() { while true { break; } }")
        assertFalse(r.hasErrors)
    }

    // ── Struct declarations ────────────────────────────────────────

    @Test
    fun `struct declaration`() {
        val r = check("struct Point { x: int, y: int }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `field access on struct`() {
        val r = check("""
            struct Point { x: int, y: int }
            fn getX(p: Point) -> int { return p.x; }
        """.trimIndent())
        assertFalse(r.hasErrors)
    }

    @Test
    fun `unknown field access`() {
        val r = check("""
            struct Point { x: int, y: int }
            fn getX(p: Point) -> int { return p.z; }
        """.trimIndent())
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("no field 'z'") })
    }

    // ── Enum declarations ──────────────────────────────────────────

    @Test
    fun `enum declaration`() {
        val r = check("enum Color { Red, Green, Blue }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `enum with fields`() {
        val r = check("enum Result { Ok(int), Err(string) }")
        assertFalse(r.hasErrors)
    }

    // ── Function calls ─────────────────────────────────────────────

    @Test
    fun `function call with correct args`() {
        val r = check("fn add(x: int, y: int) -> int { return x + y; } fn main() { let r = add(1, 2); }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `function call with wrong arg count`() {
        val r = check("fn add(x: int, y: int) -> int { return x + y; } fn main() { let r = add(1); }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("Expected 2 arguments, got 1") })
    }

    @Test
    fun `function call with wrong arg type`() {
        val r = check("fn add(x: int, y: int) -> int { return x + y; } fn main() { let r = add(1, \"hi\"); }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("expected 'int'") })
    }

    @Test
    fun `calling non-function is error`() {
        val r = check("fn main() { let x = 42; let r = x(1); }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("not a function") })
    }

    // ── Array operations ───────────────────────────────────────────

    @Test
    fun `array index on int array`() {
        val r = check("fn main() { let arr = [1, 2, 3]; let v = arr[0]; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `array index with non-int`() {
        val r = check("fn main() { let arr = [1, 2, 3]; let v = arr[true]; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("index must be 'int'") })
    }

    @Test
    fun `indexing non-array`() {
        val r = check("fn main() { let x = 42; let v = x[0]; }")
        assertTrue(r.hasErrors)
    }

    // ── Assignment ─────────────────────────────────────────────────

    @Test
    fun `assignment to mutable variable`() {
        val r = check("fn main() { mut x = 1; x = 2; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `assignment type mismatch`() {
        val r = check("fn main() { mut x: int = 1; x = \"hi\"; }")
        assertTrue(r.hasErrors)
    }

    // ── Self parameter ─────────────────────────────────────────────

    @Test
    fun `function with self parameter`() {
        val r = check("struct Foo { val: int } impl Foo { fn getVal(self) -> int { return self.val; } }")
        assertFalse(r.hasErrors)
    }

    // ── Error tolerance ────────────────────────────────────────────

    @Test
    fun `undefined variable reports error`() {
        val r = check("fn main() { let x = y; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("Undefined variable 'y'") })
    }

    @Test
    fun `undefined type reports error`() {
        val r = check("fn main() { let x: UnknownType = 1; }")
        assertTrue(r.hasErrors)
        assertTrue(r.errors.any { it.message.contains("Unknown type") })
    }

    @Test
    fun `multiple errors collected`() {
        val r = check("fn main() { let x = y; let x = z; }")
        assertTrue(r.errors.size >= 2)
    }

    // ── Type compatibility ─────────────────────────────────────────

    @Test
    fun `int to float is compatible`() {
        val r = check("fn main() { let x: float = 42; }")
        assertFalse(r.hasErrors)
    }

    // ── Complex programs ───────────────────────────────────────────

    @Test
    fun `struct with methods type checks`() {
        val r = check("""
            struct Counter { value: int }
            impl Counter {
                fn increment(self) { self.value = self.value + 1; }
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
    }

    @Test
    fun `nested function calls`() {
        val r = check("""
            fn double(x: int) -> int { return x * 2; }
            fn main() { let r = double(double(5)); }
        """.trimIndent())
        assertFalse(r.hasErrors)
    }

    @Test
    fun `if expression`() {
        val r = check("fn main() { let x = if true { 1 } else { 2 }; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `if expression without else is optional`() {
        val r = check("fn main() { let x = if true { 1 }; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `if expression branches incompatible`() {
        val r = check("fn main() { let x = if true { 1 } else { \"hi\" }; }")
        assertTrue(r.hasErrors)
    }
}
