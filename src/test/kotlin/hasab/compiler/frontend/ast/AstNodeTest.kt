package hasab.compiler.frontend.ast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AstNodeTest {

    // ---- docComment defaults ----

    @Test
    fun `docComment defaults to null on all node types`() {
        val fn = fnDecl("main")
        assertNull(fn.docComment)

        val intType = intType()
        assertNull(intType.docComment)

        val lit = intLiteral("42")
        assertNull(lit.docComment)

        val block = block()
        assertNull(block.docComment)

        val module = module()
        assertNull(module.docComment)
    }

    @Test
    fun `docComment can be set on nodes`() {
        val fn = fnDecl("main", docComment = "Entry point")
        assertEquals("Entry point", fn.docComment)
    }

    @Test
    fun `docComment defaults to null on TypeAliasDecl`() {
        val alias = TypeAliasDecl(
            name = "MyInt",
            target = intType(),
            isPublic = false,
            fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        assertNull(alias.docComment)
    }

    // ---- children() ----

    @Test
    fun `children of leaf nodes are empty`() {
        assertEquals(emptyList(), intLiteral("42").children())
        assertEquals(emptyList(), floatLiteral("3.14").children())
        assertEquals(emptyList(), stringLiteral("hello").children())
        assertEquals(emptyList(), charLiteral("c").children())
        assertEquals(emptyList(), boolLiteral(true).children())
        assertEquals(emptyList(), nilLiteral().children())
        assertEquals(emptyList(), identifier("x").children())
        assertEquals(emptyList(), intType().children())
        assertEquals(emptyList(), voidType().children())
        assertEquals(emptyList(), breakStmt().children())
        assertEquals(emptyList(), continueStmt().children())
    }

    @Test
    fun `children of BinaryExpr are left and right`() {
        val bin = binaryExpr(intLiteral("1"), "+", intLiteral("2"))
        assertEquals(2, bin.children().size)
        assertEquals(bin.left, bin.children()[0])
        assertEquals(bin.right, bin.children()[1])
    }

    @Test
    fun `children of CallExpr are callee and arguments`() {
        val call = callExpr("f", intLiteral("1"), intLiteral("2"))
        assertEquals(3, call.children().size)
    }

    @Test
    fun `children of FnDecl include param types, return type, body`() {
        val paramType = intType()
        val retType = voidType()
        val body = block()
        val fn = FnDecl(
            name = "f",
            parameters = listOf(FunctionParam("x", paramType, false, "f.hb", 1, 1, 0, 5)),
            returnType = retType,
            body = body,
            isPublic = false,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val children = fn.children()
        assertEquals(3, children.size) // paramType + returnType + body
    }

    @Test
    fun `children of FnDecl with null body and no return type`() {
        val fn = FnDecl(
            name = "f",
            parameters = emptyList(),
            returnType = null,
            body = null,
            isPublic = false,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        assertEquals(emptyList(), fn.children())
    }

    @Test
    fun `children of IfStmt are condition thenBranch elseBranch`() {
        val stmt = IfStmt(
            condition = boolLiteral(true),
            thenBranch = block(),
            elseBranch = block(),
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        assertEquals(3, stmt.children().size)
    }

    @Test
    fun `children of LetStmt with type annotation`() {
        val stmt = LetStmt(
            name = "x",
            typeAnnotation = intType(),
            initializer = intLiteral("0"),
            isMutable = true,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        assertEquals(2, stmt.children().size) // typeAnnotation + initializer
    }

    @Test
    fun `children of LetStmt without type annotation`() {
        val stmt = LetStmt(
            name = "x",
            typeAnnotation = null,
            initializer = intLiteral("0"),
            isMutable = true,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        assertEquals(1, stmt.children().size) // just initializer
    }

    @Test
    fun `children of Block are its statements`() {
        val b = Block(
            statements = listOf(breakStmt(), continueStmt()),
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        assertEquals(2, b.children().size)
    }

    @Test
    fun `children of StructDecl are field types`() {
        val field = StructField("x", intType(), false, "f.hb", 1, 1, 0, 5)
        val struct = StructDecl(
            name = "S",
            fields = listOf(field),
            isPublic = false,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        assertEquals(1, struct.children().size)
    }

    @Test
    fun `children of Module are declarations`() {
        val m = Module(
            name = "test",
            declarations = listOf(fnDecl("a"), fnDecl("b")),
            fileName = "test.hb",
        )
        assertEquals(2, m.children().size)
    }

    @Test
    fun `children of ArrayLiteralExpr are elements`() {
        val arr = ArrayLiteralExpr(
            elements = listOf(intLiteral("1"), intLiteral("2"), intLiteral("3")),
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        assertEquals(3, arr.children().size)
    }

    @Test
    fun `children of ArrayInitExpr are element type and size`() {
        val arr = ArrayInitExpr(
            elementType = intType(),
            size = intLiteral("10"),
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        assertEquals(2, arr.children().size)
    }

    @Test
    fun `children of ImplDecl are targetType and methods`() {
        val impl = ImplDecl(
            targetType = intType(),
            methods = listOf(fnDecl("a"), fnDecl("b")),
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        assertEquals(3, impl.children().size)
    }

    @Test
    fun `children of UseDecl are empty`() {
        val use = UseDecl(
            path = listOf("std", "io"),
            isPublic = false,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        assertEquals(emptyList(), use.children())
    }

    @Test
    fun `children of PubDecl wraps inner`() {
        val inner = fnDecl("f")
        val pub = PubDecl(
            inner = inner,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        assertEquals(1, pub.children().size)
        assertEquals(inner, pub.children()[0])
    }

    @Test
    fun `children of ModDecl with null body are empty`() {
        val mod = ModDecl(
            name = "m",
            body = null,
            isPublic = false,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        assertEquals(emptyList(), mod.children())
    }

    @Test
    fun `children of ForStmt are iterable and body`() {
        val forStmt = ForStmt(
            variable = "i",
            iterable = identifier("arr"),
            body = block(),
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        assertEquals(2, forStmt.children().size)
    }

    @Test
    fun `children of ReturnStmt with value`() {
        val ret = ReturnStmt(
            value = intLiteral("42"),
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        assertEquals(1, ret.children().size)
    }

    @Test
    fun `children of ReturnStmt without value`() {
        val ret = ReturnStmt(
            value = null,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 7,
        )
        assertEquals(emptyList(), ret.children())
    }

    @Test
    fun `children of TypeAliasDecl are its target type`() {
        val alias = TypeAliasDecl(
            name = "MyInt",
            target = intType(),
            isPublic = false,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        assertEquals(1, alias.children().size)
    }

    @Test
    fun `children of TraitDecl are its methods`() {
        val trait = TraitDecl(
            name = "Comparable",
            methods = listOf(fnDecl("compare")),
            isPublic = false,
            fileName = "f.hb", line = 1, column = 1, startOffset = 0, endOffset = 40,
        )
        assertEquals(1, trait.children().size)
    }

    // ---- range() ----

    @Test
    fun `range returns correct SourceRange`() {
        val fn = fnDecl("main")
        val range = fn.range()
        assertEquals(fn.line, range.start.line)
        assertEquals(fn.column, range.start.column)
        assertEquals(fn.startOffset, range.start.offset)
    }

    // ---- Helper factories ----

    private fun fnDecl(
        name: String,
        docComment: String? = null,
    ) = FnDecl(
        name = name,
        parameters = emptyList(),
        returnType = null,
        body = null,
        isPublic = false,
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        docComment = docComment,
    )

    private fun intType() = IdentifierType(
        name = "int",
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 3,
    )

    private fun voidType() = VoidType(
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 4,
    )

    private fun intLiteral(value: String) = IntegerLiteralExpr(
        value = value,
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = value.length,
    )

    private fun floatLiteral(value: String) = FloatLiteralExpr(
        value = value,
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = value.length,
    )

    private fun stringLiteral(value: String) = StringLiteralExpr(
        value = value,
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = value.length + 2,
    )

    private fun charLiteral(value: String) = CharLiteralExpr(
        value = value,
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = value.length + 2,
    )

    private fun boolLiteral(value: Boolean) = BoolLiteralExpr(
        value = value,
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = if (value) 4 else 5,
    )

    private fun nilLiteral() = NilLiteralExpr(
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 3,
    )

    private fun identifier(name: String) = IdentifierExpr(
        name = name,
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = name.length,
    )

    private fun binaryExpr(left: Expr, op: String, right: Expr) = BinaryExpr(
        left = left, operator = op, right = right,
        fileName = "test.hb", line = 1, column = 1, startOffset = left.startOffset, endOffset = right.endOffset,
    )

    private fun callExpr(callee: String, vararg args: Expr) = CallExpr(
        callee = identifier(callee),
        arguments = args.toList(),
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
    )

    private fun block(vararg stmts: Stmt) = Block(
        statements = stmts.toList(),
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
    )

    private fun breakStmt() = BreakStmt(
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 5,
    )

    private fun continueStmt() = ContinueStmt(
        fileName = "test.hb", line = 1, column = 1, startOffset = 0, endOffset = 8,
    )

    private fun module(vararg decls: Decl) = Module(
        name = "test",
        declarations = decls.toList(),
        fileName = "test.hb",
    )
}
