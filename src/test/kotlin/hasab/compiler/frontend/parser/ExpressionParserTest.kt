package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.*
import hasab.compiler.frontend.ast.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ExpressionParserTest {

    private fun parseExpr(code: String): Expr {
        val source = SourceFile("test.hasab", code)
        val result = Lexer(source).tokenize()
        val stream = TokenStream(result.tokens)
        val diagnostics = mutableListOf<ParserDiagnostic>()
        return ExpressionParser(stream, diagnostics).parseExpression()
    }

    private fun parseExprWithDiag(code: String): Pair<Expr, List<ParserDiagnostic>> {
        val source = SourceFile("test.hasab", code)
        val result = Lexer(source).tokenize()
        val stream = TokenStream(result.tokens)
        val diagnostics = mutableListOf<ParserDiagnostic>()
        val expr = ExpressionParser(stream, diagnostics).parseExpression()
        return expr to diagnostics
    }

    @Test
    fun `integer literal`() {
        val e = parseExpr("42")
        assertIs<IntegerLiteralExpr>(e)
        assertEquals("42", e.value)
    }

    @Test
    fun `float literal`() {
        val e = parseExpr("3.14")
        assertIs<FloatLiteralExpr>(e)
        assertEquals("3.14", e.value)
    }

    @Test
    fun `string literal`() {
        val e = parseExpr("\"hello\"")
        assertIs<StringLiteralExpr>(e)
        assertEquals("hello", e.value)
    }

    @Test
    fun `char literal`() {
        val e = parseExpr("'a'")
        assertIs<CharLiteralExpr>(e)
    }

    @Test
    fun `bool true literal`() {
        val e = parseExpr("true")
        assertIs<BoolLiteralExpr>(e)
        assertEquals(true, e.value)
    }

    @Test
    fun `bool false literal`() {
        val e = parseExpr("false")
        assertIs<BoolLiteralExpr>(e)
        assertEquals(false, e.value)
    }

    @Test
    fun `nil literal`() {
        val e = parseExpr("nil")
        assertIs<NilLiteralExpr>(e)
    }

    @Test
    fun `identifier`() {
        val e = parseExpr("foo")
        assertIs<IdentifierExpr>(e)
        assertEquals("foo", e.name)
    }

    @Test
    fun `ethiopic identifier`() {
        val e = parseExpr("ሰላም")
        assertIs<IdentifierExpr>(e)
        assertEquals("ሰላም", e.name)
    }

    @Test
    fun `parenthesized expression`() {
        val e = parseExpr("(42)")
        assertIs<ParenExpr>(e)
        assertIs<IntegerLiteralExpr>(e.inner)
    }

    @Test
    fun `binary addition`() {
        val e = parseExpr("1 + 2")
        assertIs<BinaryExpr>(e)
        assertEquals("+", e.operator)
        assertIs<IntegerLiteralExpr>(e.left)
        assertIs<IntegerLiteralExpr>(e.right)
    }

    @Test
    fun `binary subtraction`() {
        val e = parseExpr("10 - 3")
        assertIs<BinaryExpr>(e)
        assertEquals("-", e.operator)
    }

    @Test
    fun `binary multiplication`() {
        val e = parseExpr("a * b")
        assertIs<BinaryExpr>(e)
        assertEquals("*", e.operator)
    }

    @Test
    fun `binary division`() {
        val e = parseExpr("x / y")
        assertIs<BinaryExpr>(e)
        assertEquals("/", e.operator)
    }

    @Test
    fun `binary modulo`() {
        val e = parseExpr("a % b")
        assertIs<BinaryExpr>(e)
        assertEquals("%", e.operator)
    }

    @Test
    fun `operator precedence multiplication before addition`() {
        val e = parseExpr("1 + 2 * 3")
        assertIs<BinaryExpr>(e)
        assertEquals("+", e.operator)
        assertIs<IntegerLiteralExpr>(e.left)
        val right = e.right
        assertIs<BinaryExpr>(right)
        assertEquals("*", right.operator)
    }

    @Test
    fun `operator precedence paren overrides`() {
        val e = parseExpr("(1 + 2) * 3")
        assertIs<BinaryExpr>(e)
        assertEquals("*", e.operator)
        assertIs<ParenExpr>(e.left)
    }

    @Test
    fun `chained multiplication`() {
        val e = parseExpr("a * b * c")
        assertIs<BinaryExpr>(e)
        assertEquals("*", e.operator)
        assertIs<BinaryExpr>(e.left)
    }

    @Test
    fun `comparison operators`() {
        val e = parseExpr("a < b")
        assertIs<BinaryExpr>(e)
        assertEquals("<", e.operator)
    }

    @Test
    fun `less equal operator`() {
        val e = parseExpr("a <= b")
        assertIs<BinaryExpr>(e)
        assertEquals("<=", e.operator)
    }

    @Test
    fun `greater equal operator`() {
        val e = parseExpr("a >= b")
        assertIs<BinaryExpr>(e)
        assertEquals(">=", e.operator)
    }

    @Test
    fun `equality operator`() {
        val e = parseExpr("a == b")
        assertIs<BinaryExpr>(e)
        assertEquals("==", e.operator)
    }

    @Test
    fun `not equal operator`() {
        val e = parseExpr("a != b")
        assertIs<BinaryExpr>(e)
        assertEquals("!=", e.operator)
    }

    @Test
    fun `logical and`() {
        val e = parseExpr("a && b")
        assertIs<BinaryExpr>(e)
        assertEquals("&&", e.operator)
    }

    @Test
    fun `logical or`() {
        val e = parseExpr("a || b")
        assertIs<BinaryExpr>(e)
        assertEquals("||", e.operator)
    }

    @Test
    fun `bitwise and`() {
        val e = parseExpr("a & b")
        assertIs<BinaryExpr>(e)
        assertEquals("&", e.operator)
    }

    @Test
    fun `bitwise or`() {
        val e = parseExpr("a | b")
        assertIs<BinaryExpr>(e)
        assertEquals("|", e.operator)
    }

    @Test
    fun `bitwise xor`() {
        val e = parseExpr("a ^ b")
        assertIs<BinaryExpr>(e)
        assertEquals("^", e.operator)
    }

    @Test
    fun `shift left`() {
        val e = parseExpr("a << 2")
        assertIs<BinaryExpr>(e)
        assertEquals("<<", e.operator)
    }

    @Test
    fun `shift right`() {
        val e = parseExpr("a >> 2")
        assertIs<BinaryExpr>(e)
        assertEquals(">>", e.operator)
    }

    @Test
    fun `unary minus`() {
        val e = parseExpr("-5")
        assertIs<UnaryExpr>(e)
        assertEquals("-", e.operator)
        assertIs<IntegerLiteralExpr>(e.operand)
    }

    @Test
    fun `unary not`() {
        val e = parseExpr("!flag")
        assertIs<UnaryExpr>(e)
        assertEquals("!", e.operator)
    }

    @Test
    fun `unary bitwise not`() {
        val e = parseExpr("~x")
        assertIs<UnaryExpr>(e)
        assertEquals("~", e.operator)
    }

    @Test
    fun `double unary minus`() {
        val e = parseExpr("--5")
        assertIs<UnaryExpr>(e)
        val inner = e.operand
        assertIs<UnaryExpr>(inner)
    }

    @Test
    fun `function call no args`() {
        val e = parseExpr("foo()")
        assertIs<CallExpr>(e)
        assertEquals(0, e.arguments.size)
        assertIs<IdentifierExpr>(e.callee)
    }

    @Test
    fun `function call with args`() {
        val e = parseExpr("add(1, 2)")
        assertIs<CallExpr>(e)
        assertEquals(2, e.arguments.size)
    }

    @Test
    fun `method call via dot`() {
        val e = parseExpr("obj.method()")
        assertIs<CallExpr>(e)
        assertIs<FieldAccessExpr>(e.callee)
    }

    @Test
    fun `field access`() {
        val e = parseExpr("obj.field")
        assertIs<FieldAccessExpr>(e)
        assertEquals("field", e.fieldName)
    }

    @Test
    fun `chained field access`() {
        val e = parseExpr("a.b.c")
        assertIs<FieldAccessExpr>(e)
        assertEquals("c", e.fieldName)
        assertIs<FieldAccessExpr>(e.callee)
    }

    @Test
    fun `index expression`() {
        val e = parseExpr("arr[0]")
        assertIs<IndexExpr>(e)
        assertIs<IntegerLiteralExpr>(e.index)
    }

    @Test
    fun `nested index`() {
        val e = parseExpr("arr[i + 1]")
        assertIs<IndexExpr>(e)
        assertIs<BinaryExpr>(e.index)
    }

    @Test
    fun `call with field access chain`() {
        val e = parseExpr("obj.method(arg)")
        assertIs<CallExpr>(e)
        assertEquals(1, e.arguments.size)
        assertIs<FieldAccessExpr>(e.callee)
    }

    @Test
    fun `assignment expression`() {
        val e = parseExpr("x = 10")
        assertIs<AssignmentExpr>(e)
        assertEquals("x", (e.target as IdentifierExpr).name)
        assertIs<IntegerLiteralExpr>(e.value)
    }

    @Test
    fun `complex precedence`() {
        val e = parseExpr("1 + 2 * 3 - 4 / 5")
        assertIs<BinaryExpr>(e)
        assertEquals("-", e.operator)
    }

    @Test
    fun `chained calls`() {
        val e = parseExpr("a()(b)")
        assertIs<CallExpr>(e)
        assertIs<CallExpr>(e.callee)
    }

    @Test
    fun `ternary-like if expression`() {
        val e = parseExpr("if true { 1 } else { 0 }")
        assertIs<IfExpr>(e)
        assertIs<BoolLiteralExpr>(e.condition)
        assertNotNull(e.elseBranch)
    }

    @Test
    fun `if expression without else`() {
        val e = parseExpr("if x { 1 }")
        assertIs<IfExpr>(e)
        assertEquals(null, e.elseBranch)
    }

    // ── Safe navigation (?.) and null assertion (!!) ──────────────

    @Test
    fun `safe field access`() {
        val e = parseExpr("obj?.field")
        assertIs<SafeFieldAccessExpr>(e)
        assertEquals("field", e.fieldName)
        assertIs<IdentifierExpr>(e.callee)
    }

    @Test
    fun `null assert postfix`() {
        val e = parseExpr("x!!")
        assertIs<NullAssertExpr>(e)
        assertIs<IdentifierExpr>(e.operand)
    }

    @Test
    fun `safe field access chained`() {
        val e = parseExpr("a?.b?.c")
        assertIs<SafeFieldAccessExpr>(e)
        assertEquals("c", e.fieldName)
        assertIs<SafeFieldAccessExpr>(e.callee)
    }

    @Test
    fun `null assert on field access`() {
        val e = parseExpr("obj.field!!")
        assertIs<NullAssertExpr>(e)
        assertIs<FieldAccessExpr>(e.operand)
    }

    @Test
    fun `safe field access then null assert`() {
        val e = parseExpr("obj?.field!!")
        assertIs<NullAssertExpr>(e)
        assertIs<SafeFieldAccessExpr>(e.operand)
    }

    @Test
    fun `safe call via dot question`() {
        val e = parseExpr("obj?.method()")
        assertIs<CallExpr>(e)
        assertIs<SafeFieldAccessExpr>(e.callee)
    }

    @Test
    fun `null assert on index`() {
        val e = parseExpr("arr[0]!!")
        assertIs<NullAssertExpr>(e)
        assertIs<IndexExpr>(e.operand)
    }
}
