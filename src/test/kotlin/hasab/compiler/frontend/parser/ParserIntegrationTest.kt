package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ParserIntegrationTest {

    private fun parseModule(code: String): ParseResult {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        return Parser(lexerResult).parse()
    }

    @Test
    fun `empty module`() {
        val r = parseModule("")
        assertFalse(r.hasErrors)
        assertEquals(0, r.module.declarations.size)
    }

    @Test
    fun `single function`() {
        val r = parseModule("fn add(x: int, y: int) -> int { return x + y; }")
        assertFalse(r.hasErrors)
        assertEquals(1, r.module.declarations.size)
        assertIs<FnDecl>(r.module.declarations[0])
    }

    @Test
    fun `multiple functions`() {
        val r = parseModule("""
            fn add(a: int, b: int) -> int { return a + b; }
            fn sub(a: int, b: int) -> int { return a - b; }
        """.trimIndent())
        assertFalse(r.hasErrors)
        assertEquals(2, r.module.declarations.size)
    }

    @Test
    fun `struct and impl`() {
        val r = parseModule("""
            struct Vec { x: int, y: int }
            impl Vec {
                fn new(x: int, y: int) -> Vec { }
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
        assertEquals(2, r.module.declarations.size)
        assertIs<StructDecl>(r.module.declarations[0])
        assertIs<ImplDecl>(r.module.declarations[1])
    }

    @Test
    fun `enum declaration`() {
        val r = parseModule("enum Option { Some(int), None }")
        assertFalse(r.hasErrors)
        assertEquals(1, r.module.declarations.size)
        assertIs<EnumDecl>(r.module.declarations[0])
    }

    @Test
    fun `trait declaration`() {
        val r = parseModule("trait Printable { fn print(self); }")
        assertFalse(r.hasErrors)
        assertIs<TraitDecl>(r.module.declarations[0])
    }

    @Test
    fun `type alias`() {
        val r = parseModule("type Result = Option")
        assertFalse(r.hasErrors)
        assertIs<TypeAliasDecl>(r.module.declarations[0])
    }

    @Test
    fun `use declaration`() {
        val r = parseModule("use std::io")
        assertFalse(r.hasErrors)
        assertIs<UseDecl>(r.module.declarations[0])
    }

    @Test
    fun `mod with body`() {
        val r = parseModule("""
            mod math {
                fn add(a: int, b: int) -> int { return a + b; }
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
        val mod = r.module.declarations[0] as ModDecl
        assertEquals(1, mod.body!!.size)
    }

    @Test
    fun `function with complex body`() {
        val r = parseModule("""
            fn process(x: int) -> int {
                let mut result = x * 2;
                if result > 100 {
                    result = 100;
                }
                return result;
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
        val fn = r.module.declarations[0] as FnDecl
        assertEquals(3, fn.body!!.statements.size)
    }

    @Test
    fun `struct with methods`() {
        val r = parseModule("""
            struct Point { x: float, y: float }
            impl Point {
                fn new(x: float, y: float) -> Point { }
                fn distance(self, other: Point) -> float { }
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
        assertEquals(2, r.module.declarations.size)
        val impl = r.module.declarations[1] as ImplDecl
        assertEquals(2, impl.methods.size)
    }

    @Test
    fun `complex module`() {
        val r = parseModule("""
            use std::io

            struct Config { port: int, host: string }
            trait Serializable { fn serialize(self) -> string; }

            fn main() {
                let config = Config(port = 8080, host = "localhost");
                println(config);
            }
        """.trimIndent())
        assertEquals(4, r.module.declarations.size)
    }

    @Test
    fun `ethiopic source`() {
        val r = parseModule("fn ሰላም() { return 42; }")
        assertFalse(r.hasErrors)
        assertEquals(1, r.module.declarations.size)
    }

    @Test
    fun `source position tracking`() {
        val r = parseModule("fn main() {\n  let x = 42;\n}")
        val fn = r.module.declarations[0] as FnDecl
        assertEquals("main", fn.name)
        assertEquals(1, fn.line)
        assertEquals(1, fn.column)
    }

    @Test
    fun `error recovery - missing semicolon`() {
        val r = parseModule("let x = 42")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `error recovery - invalid function`() {
        val r = parseModule("fn 123badname() { }")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `nested structures`() {
        val r = parseModule("""
            mod app {
                mod handlers {
                    fn handle() { }
                }
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
    }

    @Test
    fun `function with many statements`() {
        val r = parseModule("""
            fn compute() -> int {
                let a = 1;
                let b = 2;
                let c = 3;
                let d = a + b + c;
                return d;
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
    }

    @Test
    fun `chained method calls in expression`() {
        val r = parseModule("""
            fn test() {
                foo.bar().baz(1);
            }
        """.trimIndent())
        assertFalse(r.hasErrors)
    }
}
