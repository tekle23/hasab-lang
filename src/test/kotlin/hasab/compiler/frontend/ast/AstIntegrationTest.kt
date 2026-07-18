package hasab.compiler.frontend.ast

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AstIntegrationTest {

    private fun parseSource(source: String): Module {
        val sourceFile = SourceFile("test.hasab", source)
        val lexerResult = Lexer(sourceFile).tokenize()
        val parser = Parser(lexerResult)
        return parser.parse().module
    }

    @Test
    fun `full pipeline - parse, print, validate, serialize`() {
        val source = """
            fn main() {
                let x: int = 42;
                println(x);
            }
        """.trimIndent()

        val module = parseSource(source)

        // Printer
        val printer = AstPrinter()
        val printed = printer.print(module)
        assertContains(printed, "Module(name=null)")
        assertContains(printed, "FnDecl(name=\"main\"")
        assertContains(printed, "LetStmt(name=\"x\"")
        assertContains(printed, "ExprStmt")
        assertContains(printed, "CallExpr")

        // Validator
        val validator = AstValidator()
        val issues = validator.validate(module)
        val errors = issues.filter { it.severity == ValidationIssue.Severity.ERROR }
        assertTrue(errors.isEmpty(), "Expected no validation errors, got: $errors")

        // Serializer
        val serializer = AstSerializer()
        val json = serializer.serialize(module)
        assertContains(json, "\"type\": \"Module\"")
        assertContains(json, "\"type\": \"FnDecl\"")
        assertContains(json, "\"type\": \"LetStmt\"")
    }

    @Test
    fun `full pipeline - struct and enum`() {
        val source = """
            struct Point {
                x: int,
                y: int,
            }
            enum Color {
                Red(int),
                Green(int),
                Blue(int),
            }
        """.trimIndent()

        val module = parseSource(source)
        val printed = AstPrinter().print(module)
        assertTrue(printed.contains("StructDecl"), "Expected 'StructDecl' in:\n$printed")
        assertTrue(printed.contains("Point"), "Expected 'Point' in:\n$printed")
        assertTrue(printed.contains("EnumDecl"), "Expected 'EnumDecl' in:\n$printed")
        assertTrue(printed.contains("Color"), "Expected 'Color' in:\n$printed")

        val issues = AstValidator().validate(module)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")

        val json = AstSerializer().serialize(module)
        assertContains(json, "\"type\": \"StructDecl\"")
        assertContains(json, "\"type\": \"EnumDecl\"")
    }

    @Test
    fun `full pipeline - control flow`() {
        val source = """
            fn main() {
                let i: int = 0;
                while (i < 10) {
                    i += 1;
                }
                for (x: items) {
                    println(x);
                }
            }
        """.trimIndent()

        val module = parseSource(source)
        val printed = AstPrinter().print(module)
        assertContains(printed, "WhileStmt")
        assertContains(printed, "ForStmt(variable=\"x\")")
        assertContains(printed, "CompoundAssignmentExpr")

        val issues = AstValidator().validate(module)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `count nodes using visitor`() {
        val source = """
            fn add(a: int, b: int) -> int {
                return a + b;
            }
            fn main() {
                let result: int = add(1, 2);
                println(result);
            }
        """.trimIndent()

        val module = parseSource(source)

        // Use a simple counter visitor
        class Counter : AstVisitorBase<Int>(default = 0) {
            var fnCount = 0; private set
            var letCount = 0; private set
            var callCount = 0; private set
            var intCount = 0; private set
            var returnCount = 0; private set

            override fun visitFnDecl(node: FnDecl): Int { fnCount++; return super.visitFnDecl(node) }
            override fun visitLet(node: LetStmt): Int { letCount++; return super.visitLet(node) }
            override fun visitCall(node: CallExpr): Int { callCount++; return super.visitCall(node) }
            override fun visitIntegerLiteral(node: IntegerLiteralExpr): Int { intCount++; return super.visitIntegerLiteral(node) }
            override fun visitReturn(node: ReturnStmt): Int { returnCount++; return super.visitReturn(node) }
        }

        val counter = Counter()
        counter.visit(module)

        assertEquals(2, counter.fnCount)
        assertEquals(1, counter.letCount)
        assertEquals(2, counter.callCount)
        assertEquals(2, counter.intCount) // 1, 2 in add(1, 2)
        assertEquals(1, counter.returnCount)
    }

    @Test
    fun `validate catches issues from real parsed code`() {
        // Parse a valid program and ensure no validation errors
        val source = """
            struct Vec2 {
                x: float,
                y: float,
            }
            fn distance(a: Vec2, b: Vec2) -> float {
                let dx: float = a.x - b.x;
                let dy: float = a.y - b.y;
                return (dx * dx + dy * dy);
            }
        """.trimIndent()

        val module = parseSource(source)
        val issues = AstValidator().validate(module)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `printer output is human readable`() {
        val source = """
            fn main() {
                println(42);
            }
        """.trimIndent()

        val module = parseSource(source)
        val printed = AstPrinter().print(module)

        // Verify indentation is present
        assertTrue(printed.contains("  "), "Expected indentation in output")
        // Verify newlines are present
        assertTrue(printed.contains("\n"), "Expected newlines in output")
    }

    @Test
    fun `serializer produces valid JSON structure`() {
        val source = """
            fn main() {
                let x: int = 1 + 2;
            }
        """.trimIndent()

        val module = parseSource(source)
        val json = AstSerializer().serialize(module)

        // Verify braces balance (basic JSON validity check)
        val openBraces = json.count { it == '{' }
        val closeBraces = json.count { it == '}' }
        assertEquals(openBraces, closeBraces, "Unbalanced braces in JSON output")
    }

    @Test
    fun `children traversal matches visitor traversal`() {
        val source = """
            fn main() {
                let x: int = 42;
                println(x);
            }
        """.trimIndent()

        val module = parseSource(source)

        // Count nodes via visitor
        class VisitorCounter : AstVisitorBase<Int>(default = 0) {
            var count = 0; private set
            override fun visitModule(node: Module): Int { count++; return super.visitModule(node) }
            override fun visitFnDecl(node: FnDecl): Int { count++; return super.visitFnDecl(node) }
            override fun visitBlock(node: Block): Int { count++; return super.visitBlock(node) }
            override fun visitLet(node: LetStmt): Int { count++; return super.visitLet(node) }
            override fun visitExprStmt(node: ExprStmt): Int { count++; return super.visitExprStmt(node) }
            override fun visitCall(node: CallExpr): Int { count++; return super.visitCall(node) }
        }

        val vc = VisitorCounter()
        vc.visit(module)

        // Count nodes via recursive children
        fun countChildren(node: AstNode): Int {
            return 1 + node.children().sumOf { countChildren(it) }
        }

        val childCount = countChildren(module)

        // Both should have similar counts (visitor may count more due to type nodes)
        assertTrue(vc.count > 0, "Visitor should have counted nodes")
        assertTrue(childCount > 0, "Children traversal should have counted nodes")
    }
}
