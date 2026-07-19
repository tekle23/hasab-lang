package hasab.compiler

import hasab.compiler.backend.HasabToJavaCompiler
import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.semantic.SemanticAnalyzer
import hasab.compiler.types.TypeChecker
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureVerificationTest {

    private fun verify(name: String, code: String) {
        val source = SourceFile("$name.has", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val semanticResult = SemanticAnalyzer().analyze(parseResult.module)
        val typeResult = TypeChecker().check(parseResult.module)
        val errors = mutableListOf<String>()
        if (parseResult.hasErrors) errors.add("parse")
        if (semanticResult.hasErrors) errors.add("semantic: ${semanticResult.errors.joinToString("; ") { it.message }}")
        if (typeResult.hasErrors) errors.add("type: ${typeResult.diagnostics.joinToString("; ") { it.message }}")
        assertTrue(errors.isEmpty(), "$name failed: ${errors.joinToString("; ")}")
    }

    private fun verifyFull(name: String, code: String) {
        val result = HasabToJavaCompiler.compile(code, "$name.has")
        assertFalse(result.hasErrors, "$name has errors: ${result.typeDiagnostics.joinToString("; ") { it.message }}")
        assertTrue(result.javaSource.isNotBlank(), "$name produced empty Java")
    }

    @Test fun `println string literal`() = verify("t1", "fn main() { println(\"hello\"); }")
    @Test fun `println int literal`() = verify("t2", "fn main() { println(42); }")
    @Test fun `println variable`() = verify("t3", "fn main() { let x = 10; println(x); }")
    @Test fun `print no newline`() = verify("t4", "fn main() { print(\"hi\"); print(\" there\"); }")
    @Test fun `len array`() = verify("t5", "fn main() { let a = [1,2,3]; println(len(a)); }")
    @Test fun `len string`() = verify("t6", "fn main() { println(len(\"hello\")); }")
    @Test fun `abs`() = verify("t7", "fn main() { println(abs(-42)); }")
    @Test fun `min max`() = verify("t8", "fn main() { println(min(3,7)); println(max(3,7)); }")
    @Test fun `sqrt pow`() = verify("t9", "fn main() { let s = 4.0; println(sqrt(s)); println(pow(2.0, 10.0)); }")
    @Test fun `str`() = verify("t10", "fn main() { println(str(42)); }")
    @Test fun `now`() = verify("t11", "fn main() { println(now()); }")
    @Test fun `struct basic`() = verify("t12", "struct P { x: int, y: int } fn main() { let p = P(1,2); println(p.x); }")
    @Test fun `struct field access`() = verify("t13", "struct P { x: int, y: int } fn main() { let p = P(1,2); println(p.y); }")
    @Test fun `if else`() = verify("t14", "fn main() { let x = 5; if x > 3 { println(1); } else { println(0); } }")
    @Test fun `while loop`() = verify("t15", "fn main() { let mut i = 0; while i < 5 { println(i); i += 1; } }")
    @Test fun `for array`() = verify("t16", "fn main() { let a = [1,2,3]; for (x: a) { println(x); } }")
    @Test fun `break continue`() = verify("t17", "fn main() { let mut i = 0; while true { if i == 3 { break; } i += 1; } }")
    @Test fun `enum simple`() = verify("t18", "enum Dir { N, S, E, W } fn main() { let d = Dir.N; println(d); }")
    @Test fun `enum with data`() = verify("t19", "enum R { Ok(int), Err(string) } fn main() { let r = R.Ok(42); println(r); }")
    @Test fun `impl method`() = verify("t20", "struct V { x: float, y: float } impl V { fn length(self) -> float { return sqrt(self.x * self.x + self.y * self.y); } } fn main() { let v = V(3.0, 4.0); println(v.length()); }")
    @Test fun `fn with return`() = verify("t21", "fn add(a: int, b: int) -> int { return a + b; } fn main() { println(add(3, 4)); }")
    @Test fun `fn recursive`() = verify("t22", "fn fib(n: int) -> int { if n <= 1 { return n; } return fib(n - 1) + fib(n - 2); } fn main() { println(fib(10)); }")
    @Test fun `mutation`() = verify("t23", "fn main() { let mut x = 0; x += 1; x *= 3; println(x); }")
    @Test fun `comparisons`() = verify("t24", "fn main() { println(1 < 2); println(1 == 1); println(1 != 2); }")
    @Test fun `logical ops`() = verify("t25", "fn main() { println(true && false); println(true || false); println(!true); }")
    @Test fun `nested struct`() = verify("t26", "struct A { v: int } struct B { a: A } fn main() { let b = B(A(42)); println(b.a.v); }")
    @Test fun `array init`() = verify("t27", "fn main() { let a = .[10]; println(len(a)); }")
    @Test fun `array index`() = verify("t28", "fn main() { let a = [10,20,30]; println(a[1]); }")
    @Test fun `array modify`() = verify("t29", "fn main() { let mut a = [1,2,3]; a[0] = 99; println(a[0]); }")
    @Test fun `full pipeline struct`() = verifyFull("fp1", "struct P { x: int, y: int } fn main() { let p = P(1,2); println(p.x); }")
    @Test fun `full pipeline fn`() = verifyFull("fp2", "fn add(a: int, b: int) -> int { return a + b; } fn main() { println(add(3, 4)); }")
    @Test fun `full pipeline while`() = verifyFull("fp3", "fn main() { let mut i = 0; while i < 5 { println(i); i += 1; } }")
}
