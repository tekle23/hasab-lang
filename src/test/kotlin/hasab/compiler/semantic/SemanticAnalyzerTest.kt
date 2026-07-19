package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SemanticAnalyzerTest {

    private fun analyze(source: String): SemanticModel {
        val sourceFile = SourceFile("test.hasab", source)
        val lexerResult = Lexer(sourceFile).tokenize()
        val parseResult = Parser(lexerResult).parse()
        return SemanticAnalyzer().analyze(parseResult.module)
    }

    // ---- Empty / simple programs ----

    @Test
    fun `empty module produces no errors`() {
        val model = analyze("")
        assertFalse(model.hasErrors)
    }

    @Test
    fun `simple function declaration is collected`() {
        val model = analyze("fn main() {}")
        assertFalse(model.hasErrors)
        val fn = model.lookupSymbol("main")
        assertNotNull(fn)
        assertTrue(fn is FunctionSymbol)
    }

    @Test
    fun `extern function declaration is collected`() {
        val model = analyze("fn main();")
        assertFalse(model.hasErrors)
        val fn = model.lookupSymbol("main")
        assertNotNull(fn)
        assertTrue(fn is FunctionSymbol && fn.isExtern)
    }

    @Test
    fun `struct declaration is collected`() {
        val model = analyze("struct Point { x: int, y: int }")
        assertFalse(model.hasErrors)
        val s = model.lookupSymbol("Point")
        assertNotNull(s)
        assertTrue(s is StructSymbol && s.childSymbols == listOf("x", "y"))
    }

    @Test
    fun `enum declaration is collected`() {
        val model = analyze("enum Color { Red(int), Green(int) }")
        assertFalse(model.hasErrors)
        val e = model.lookupSymbol("Color")
        assertNotNull(e)
        assertTrue(e is EnumSymbol && e.childSymbols == listOf("Red", "Green"))
    }

    @Test
    fun `trait declaration is collected`() {
        val model = analyze("trait Drawable { fn draw(self); }")
        assertFalse(model.hasErrors)
        val t = model.lookupSymbol("Drawable")
        assertNotNull(t)
        assertTrue(t is TraitSymbol && t.childSymbols == listOf("draw"))
    }

    @Test
    fun `type alias is collected`() {
        val model = analyze("type MyInt = int")
        assertFalse(model.hasErrors)
        val ta = model.lookupSymbol("MyInt")
        assertNotNull(ta)
        assertTrue(ta is TypeAliasSymbol)
    }

    @Test
    fun `mod declaration is collected`() {
        val model = analyze("mod mymod { fn helper() {} }")
        assertFalse(model.hasErrors)
        val mod = model.lookupSymbol("mymod")
        assertNotNull(mod)
        assertTrue(mod is ModuleSymbol)
    }

    // ---- Duplicate detection ----

    @Test
    fun `duplicate function declaration reports error`() {
        val model = analyze("fn main() {} fn main() {}")
        assertTrue(model.hasErrors)
        assertTrue(model.errors.any { it.code == DiagnosticCode.DUPLICATE_DECLARATION })
    }

    @Test
    fun `duplicate struct declaration reports error`() {
        val model = analyze("struct S {} struct S {}")
        assertTrue(model.hasErrors)
        assertTrue(model.errors.any { it.code == DiagnosticCode.DUPLICATE_DECLARATION })
    }

    @Test
    fun `duplicate parameter reports error`() {
        val model = analyze("fn f(x: int, x: float) {}")
        assertTrue(model.hasErrors)
        assertTrue(model.errors.any { it.code == DiagnosticCode.DUPLICATE_PARAMETER })
    }

    // ---- Symbol resolution ----

    @Test
    fun `undefined variable reports error`() {
        val model = analyze("fn main() { println(x); }")
        assertTrue(model.hasErrors)
        assertTrue(model.errors.any { it.code == DiagnosticCode.UNDEFINED_VARIABLE })
    }

    @Test
    fun `defined variable resolves successfully`() {
        val model = analyze("fn main() { let x: int = 42; }")
        assertFalse(model.hasErrors)
    }

    @Test
    fun `undefined type reports error`() {
        val model = analyze("fn main() { let x: UnknownType = 42; }")
        assertTrue(model.hasErrors)
        assertTrue(model.errors.any { it.code == DiagnosticCode.UNDEFINED_TYPE })
    }

    @Test
    fun `builtin types do not report errors`() {
        val model = analyze("fn main() { let x: int = 42; let y: float = 3.14; let s: string = \"hi\"; let b: bool = true; let c: char = 'a'; }")
        assertFalse(model.hasErrors)
    }

    // ---- Structural validation ----

    @Test
    fun `break outside loop reports error`() {
        val model = analyze("fn main() { break; }")
        assertTrue(model.hasErrors)
        assertTrue(model.errors.any { it.code == DiagnosticCode.BREAK_OUTSIDE_LOOP })
    }

    @Test
    fun `continue outside loop reports error`() {
        val model = analyze("fn main() { continue; }")
        assertTrue(model.hasErrors)
        assertTrue(model.errors.any { it.code == DiagnosticCode.CONTINUE_OUTSIDE_LOOP })
    }

    @Test
    fun `break inside while is valid`() {
        val model = analyze("fn main() { while (true) { break; } }")
        assertFalse(model.hasErrors)
    }

    @Test
    fun `continue inside for is valid`() {
        val model = analyze("fn main() { while (true) { continue; } }")
        assertFalse(model.hasErrors)
    }

    // ---- pub declarations ----

    @Test
    fun `pub function is public`() {
        val model = analyze("pub fn main() {}")
        assertFalse(model.hasErrors)
        val fn = model.lookupSymbol("main")
        assertNotNull(fn)
        assertEquals(Visibility.PUBLIC, fn.visibility)
    }

    @Test
    fun `pub struct is public`() {
        val model = analyze("pub struct Point { x: int }")
        assertFalse(model.hasErrors)
        val s = model.lookupSymbol("Point")
        assertNotNull(s)
        assertEquals(Visibility.PUBLIC, s.visibility)
    }

    // ---- Module graph ----

    @Test
    fun `mod declaration creates module graph entry`() {
        val model = analyze("mod mymod { fn helper() {} }")
        assertFalse(model.hasErrors)
        assertTrue(model.moduleGraph.values.any { it.name.endsWith("::mymod") })
    }

    // ---- Symbol count queries ----

    @Test
    fun `functions returns all function symbols`() {
        val model = analyze("fn a() {} fn b() {} fn c() {}")
        assertEquals(3, model.functions().size)
    }

    @Test
    fun `structs returns all struct symbols`() {
        val model = analyze("struct A {} struct B {}")
        assertEquals(2, model.structs().size)
    }

    @Test
    fun `enums returns all enum symbols`() {
        val model = analyze("enum E1 { V1(int) } enum E2 { V2(int) }")
        assertEquals(2, model.enums().size)
    }

    // ---- Complex programs ----

    @Test
    fun `valid complex program produces no errors`() {
        val source = """
            struct Point {
                x: int,
                y: int,
            }
            enum Shape {
                Circle(float),
                Rect(float, float),
            }
            trait Drawable {
                fn draw(self);
            }
            fn distance(a: Point, b: Point) -> float {
                return 0.0;
            }
            fn main() {
                let p: Point = Point(1, 2);
            }
        """.trimIndent()
        val model = analyze(source)
        assertFalse(model.hasErrors, "Expected no errors, got: ${model.errors.map { it.format() }}")
    }

    @Test
    fun `impl block collects methods`() {
        val model = analyze("struct Point { x: int } impl Point { fn new() {} fn distance(self) {} }")
        assertFalse(model.hasErrors)
    }

    // ---- SemanticModel convenience queries ----

    @Test
    fun `symbolsOfKind returns correct subset`() {
        val model = analyze("fn a() {} fn b() {} struct S {}")
        assertEquals(2, model.symbolsOfKind(SymbolKind.FUNCTION).size)
        assertEquals(1, model.symbolsOfKind(SymbolKind.STRUCT).size)
        assertEquals(0, model.symbolsOfKind(SymbolKind.ENUM).size)
    }

    @Test
    fun `toResult produces correct snapshot`() {
        val model = analyze("fn main() {}")
        val result = model.toResult()
        assertFalse(result.hasErrors)
        assertNotNull(result.symbolTable.lookup("main"))
    }

    // ---- Diagnostics formatting ----

    @Test
    fun `diagnostic has correct format`() {
        val diag = SemanticDiagnostic(
            code = DiagnosticCode.UNDEFINED_VARIABLE,
            severity = DiagnosticSeverity.ERROR,
            message = "Undefined variable 'x'",
            range = hasab.compiler.frontend.lexer.SourceRange(
                hasab.compiler.frontend.lexer.SourcePosition(1, 5, 4),
                hasab.compiler.frontend.lexer.SourcePosition(1, 6, 5),
            ),
            fileName = "test.hb",
            hint = "Declare 'x'",
        )
        val formatted = diag.format()
        assertTrue(formatted.contains("test.hb:1:5"))
        assertTrue(formatted.contains("error[HSB2001]"))
        assertTrue(formatted.contains("Undefined variable 'x'"))
        assertTrue(formatted.contains("Declare 'x'"))
    }

    @Test
    fun `DiagnosticFix data class works`() {
        val fix = DiagnosticFix(
            description = "Add 'pub'",
            startOffset = 0,
            endOffset = 0,
            replacement = "pub ",
        )
        assertEquals("Add 'pub'", fix.description)
        assertEquals("pub ", fix.replacement)
    }

    @Test
    fun `didYouMean returns closest match`() {
        val candidates = listOf("x", "y", "arr", "items", "name")
        assertEquals("x", didYouMean("X", candidates))
        assertEquals("arr", didYouMean("ar", candidates))
        assertEquals("items", didYouMean("itms", candidates))
    }

    @Test
    fun `didYouMean returns null for no close match`() {
        val candidates = listOf("abc", "def")
        assertNull(didYouMean("xyz123", candidates))
    }

    @Test
    fun `didYouMean returns null for empty candidates`() {
        assertNull(didYouMean("x", emptyList()))
    }

    @Test
    fun `diagnostic includes didYouMean`() {
        val diag = SemanticDiagnostic(
            code = DiagnosticCode.UNDEFINED_VARIABLE,
            severity = DiagnosticSeverity.ERROR,
            message = "Undefined variable 'pritn'",
            range = hasab.compiler.frontend.lexer.SourceRange(
                hasab.compiler.frontend.lexer.SourcePosition(1, 5, 4),
                hasab.compiler.frontend.lexer.SourcePosition(1, 10, 9),
            ),
            fileName = "test.hb",
            didYouMean = "println",
        )
        val formatted = diag.format()
        assertTrue(formatted.contains("did you mean: 'println'?"))
    }

    // ---- Local variable tracking ----

    @Test
    fun `local let variable is tracked`() {
        val model = analyze("fn main() { let x: int = 42; }")
        assertFalse(model.hasErrors)
        val x = model.lookupSymbol("x")
        assertNotNull(x)
        assertTrue(x is VariableSymbol)
    }

    @Test
    fun `local variable used in same scope resolves`() {
        val model = analyze("fn main() { let x: int = 42; let y = x; }")
        assertFalse(model.hasErrors)
    }

    // ---- Node bindings ----

    @Test
    fun `node bindings are populated for resolved identifiers`() {
        val model = analyze("fn main() { let x: int = 42; let y = x; }")
        assertFalse(model.hasErrors)
        assertTrue(model.nodeBindings.isNotEmpty())
    }

    @Test
    fun `node bindings map to correct symbols`() {
        val model = analyze("fn main() { let x: int = 42; let y = x; }")
        assertFalse(model.hasErrors)
        val xSym = model.lookupSymbol("x")
        assertNotNull(xSym)
        assertTrue(model.nodeBindings.values.any { it === xSym })
    }

    // ---- PACKAGE and CLASS scope kinds ----

    @Test
    fun `scope kinds include PACKAGE and CLASS`() {
        assertTrue(ScopeKind.values().contains(ScopeKind.PACKAGE))
        assertTrue(ScopeKind.values().contains(ScopeKind.CLASS))
    }
}
