package hasab.compiler.frontend.ast

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AstPrinterTest {

    private val printer = AstPrinter()

    @Test
    fun `print empty module`() {
        val output = printer.print(Module(name = "empty", declarations = emptyList(), fileName = "t.hb"))
        assertContains(output, "Module(name=\"empty\")")
        assertFalse(output.contains("FnDecl"))
    }

    @Test
    fun `print simple function`() {
        val mod = Module(
            name = "test",
            declarations = listOf(
                FnDecl("main", "main", emptyList(), VoidType("t.hb", 1, 1, 0, 4), Block(
                    listOf(
                        ExprStmt(CallExpr(
                            callee = IdentifierExpr("println", "t.hb", 1, 5, 4, 11),
                            arguments = listOf(IntegerLiteralExpr("42", "t.hb", 1, 13, 12, 14)),
                            fileName = "t.hb", line = 1, column = 5, startOffset = 4, endOffset = 15,
                        ), "t.hb", 1, 5, 4, 16),
                    ),
                    "t.hb", 1, 3, 2, 20,
                ), true, "t.hb", 1, 1, 0, 20),
            ),
            fileName = "t.hb",
        )
        val output = printer.print(mod)
        assertContains(output, "Module(name=\"test\")")
        assertContains(output, "FnDecl(name=\"main\", public=true)")
        assertContains(output, "Block")
        assertContains(output, "CallExpr")
        assertContains(output, "IdentifierExpr(name=\"println\")")
        assertContains(output, "IntegerLiteralExpr(value=\"42\")")
        assertContains(output, "VoidType")
    }

    @Test
    fun `print struct declaration`() {
        val s = StructDecl(
            name = "Point",
            fields = listOf(
                StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 8),
                StructField("y", IdentifierType("int", "t.hb", 1, 1, 0, 3), true, "t.hb", 1, 1, 0, 8),
            ),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        val output = printer.print(s)
        assertContains(output, "StructDecl(name=\"Point\", public=true)")
        assertContains(output, "Field(name=\"x\", mutable=false)")
        assertContains(output, "Field(name=\"y\", mutable=true)")
    }

    @Test
    fun `print enum declaration`() {
        val e = EnumDecl(
            name = "Color",
            variants = listOf(
                EnumVariant("Red", emptyList(), "t.hb", 1, 1, 0, 5),
                EnumVariant("Green", listOf(
                    StructField("shade", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 8),
                ), "t.hb", 1, 1, 0, 15),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val output = printer.print(e)
        assertContains(output, "EnumDecl(name=\"Color\", public=false)")
        assertContains(output, "Variant(name=\"Red\")")
        assertContains(output, "Variant(name=\"Green\")")
        assertContains(output, "Field(name=\"shade\")")
    }

    @Test
    fun `print binary expression`() {
        val expr = BinaryExpr(
            left = IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1),
            operator = "+",
            right = IntegerLiteralExpr("2", "t.hb", 1, 5, 4, 5),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 5,
        )
        val output = printer.print(expr)
        assertContains(output, "BinaryExpr(operator=\"+\")")
        assertContains(output, "IntegerLiteralExpr(value=\"1\")")
        assertContains(output, "IntegerLiteralExpr(value=\"2\")")
    }

    @Test
    fun `print unary expression`() {
        val expr = UnaryExpr("-", IntegerLiteralExpr("5", "t.hb", 1, 2, 1, 2), "t.hb", 1, 1, 0, 2)
        val output = printer.print(expr)
        assertContains(output, "UnaryExpr(operator=\"-\")")
        assertContains(output, "IntegerLiteralExpr(value=\"5\")")
    }

    @Test
    fun `print call expression`() {
        val call = CallExpr(
            callee = IdentifierExpr("add", "t.hb", 1, 1, 0, 3),
            arguments = listOf(
                IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5),
                IntegerLiteralExpr("2", "t.hb", 1, 7, 6, 7),
            ),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 8,
        )
        val output = printer.print(call)
        assertContains(output, "CallExpr(args=2)")
        assertContains(output, "IdentifierExpr(name=\"add\")")
    }

    @Test
    fun `print field access`() {
        val fa = FieldAccessExpr(
            callee = IdentifierExpr("p", "t.hb", 1, 1, 0, 1),
            fieldName = "x",
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 3,
        )
        val output = printer.print(fa)
        assertContains(output, "FieldAccessExpr(fieldName=\"x\")")
    }

    @Test
    fun `print index expression`() {
        val idx = IndexExpr(
            callee = IdentifierExpr("arr", "t.hb", 1, 1, 0, 3),
            index = IntegerLiteralExpr("0", "t.hb", 1, 5, 4, 5),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 6,
        )
        val output = printer.print(idx)
        assertContains(output, "IndexExpr")
    }

    @Test
    fun `print let statement`() {
        val let = LetStmt(
            name = "x",
            typeAnnotation = IdentifierType("int", "t.hb", 1, 6, 5, 8),
            initializer = IntegerLiteralExpr("0", "t.hb", 1, 12, 11, 12),
            isMutable = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 13,
        )
        val output = printer.print(let)
        assertContains(output, "LetStmt(name=\"x\", mutable=true)")
        assertContains(output, "typeAnnotation")
        assertContains(output, "initializer")
    }

    @Test
    fun `print return statement with value`() {
        val ret = ReturnStmt(
            value = IntegerLiteralExpr("42", "t.hb", 1, 8, 7, 9),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val output = printer.print(ret)
        assertContains(output, "ReturnStmt")
        assertContains(output, "IntegerLiteralExpr(value=\"42\")")
    }

    @Test
    fun `print return statement without value`() {
        val ret = ReturnStmt(value = null, fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 7)
        val output = printer.print(ret)
        assertContains(output, "ReturnStmt")
    }

    @Test
    fun `print if statement`() {
        val ifStmt = IfStmt(
            condition = BoolLiteralExpr(true, "t.hb", 1, 4, 3, 7),
            thenBranch = Block(listOf(
                ExprStmt(IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1), "t.hb", 1, 1, 0, 2),
            ), "t.hb", 1, 1, 0, 10),
            elseBranch = Block(listOf(
                ExprStmt(IntegerLiteralExpr("2", "t.hb", 1, 1, 0, 1), "t.hb", 1, 1, 0, 2),
            ), "t.hb", 1, 1, 0, 10),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val output = printer.print(ifStmt)
        assertContains(output, "IfStmt")
        assertContains(output, "condition")
        assertContains(output, "then")
        assertContains(output, "else")
    }

    @Test
    fun `print while statement`() {
        val whileStmt = WhileStmt(
            condition = BoolLiteralExpr(true, "t.hb", 1, 6, 5, 9),
            body = Block(emptyList(), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val output = printer.print(whileStmt)
        assertContains(output, "WhileStmt")
        assertContains(output, "condition")
        assertContains(output, "body")
    }

    @Test
    fun `print for statement`() {
        val forStmt = ForStmt(
            variable = "i",
            iterable = IdentifierExpr("arr", "t.hb", 1, 6, 5, 8),
            body = Block(emptyList(), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val output = printer.print(forStmt)
        assertContains(output, "ForStmt(variable=\"i\")")
        assertContains(output, "iterable")
    }

    @Test
    fun `print impl declaration`() {
        val impl = ImplDecl(
            targetType = IdentifierType("Point", "t.hb", 1, 6, 5, 10),
            methods = listOf(
                FnDecl("distance", "distance", emptyList(), IdentifierType("float", "t.hb", 1, 1, 0, 5), null, false, "t.hb", 1, 1, 0, 20),
            ),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 40,
        )
        val output = printer.print(impl)
        assertContains(output, "ImplDecl")
        assertContains(output, "targetType")
        assertContains(output, "FnDecl(name=\"distance\", public=false)")
    }

    @Test
    fun `print trait declaration`() {
        val trait = TraitDecl(
            name = "Comparable",
            methods = listOf(
                FnDecl("compare", "compare", emptyList(), IdentifierType("int", "t.hb", 1, 1, 0, 3), null, false, "t.hb", 1, 1, 0, 20),
            ),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 40,
        )
        val output = printer.print(trait)
        assertContains(output, "TraitDecl(name=\"Comparable\", public=true)")
    }

    @Test
    fun `print type alias declaration`() {
        val alias = TypeAliasDecl(
            name = "MyInt",
            target = IdentifierType("int", "t.hb", 1, 1, 0, 3),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val output = printer.print(alias)
        assertContains(output, "TypeAliasDecl(name=\"MyInt\", public=false)")
    }

    @Test
    fun `print use declaration`() {
        val use = UseDecl(
            path = listOf("std", "io"),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        val output = printer.print(use)
        assertContains(output, "UseDecl(path=std::io, public=false)")
    }

    @Test
    fun `print pub declaration`() {
        val fn = FnDecl("main", "main", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10)
        val pub = PubDecl(fn, "t.hb", 1, 1, 0, 15)
        val output = printer.print(pub)
        assertContains(output, "PubDecl")
        assertContains(output, "FnDecl(name=\"main\", public=false)")
    }

    @Test
    fun `print mod declaration`() {
        val mod = ModDecl(
            name = "mymod",
            body = listOf(
                FnDecl("f", "f", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10),
            ),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        val output = printer.print(mod)
        assertContains(output, "ModDecl(name=\"mymod\", public=true)")
    }

    @Test
    fun `print type nodes`() {
        val at = ArrayType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        assertContains(printer.print(at), "ArrayType")

        val pt = PointerType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        assertContains(printer.print(pt), "PointerType")

        val ot = OptionalType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        assertContains(printer.print(ot), "OptionalType")

        val ft = FunctionType(
            listOf(IdentifierType("int", "t.hb", 1, 1, 0, 3)),
            VoidType("t.hb", 1, 1, 0, 4),
            "t.hb", 1, 1, 0, 10,
        )
        val ftOutput = printer.print(ft)
        assertContains(ftOutput, "FunctionType(params=1)")
        assertContains(ftOutput, "-> returnType")

        val qt = QualifiedType(listOf("std", "io"), "t.hb", 1, 1, 0, 10)
        assertContains(printer.print(qt), "QualifiedType(path=std::io)")
    }

    @Test
    fun `print break and continue statements`() {
        val brk = BreakStmt("t.hb", 1, 1, 0, 5)
        assertContains(printer.print(brk), "BreakStmt")

        val cont = ContinueStmt("t.hb", 1, 1, 0, 8)
        assertContains(printer.print(cont), "ContinueStmt")
    }

    @Test
    fun `print expression types`() {
        val fl = FloatLiteralExpr("3.14", "t.hb", 1, 1, 0, 4)
        assertContains(printer.print(fl), "FloatLiteralExpr(value=\"3.14\")")

        val sl = StringLiteralExpr("hello", "t.hb", 1, 1, 0, 7)
        assertContains(printer.print(sl), "StringLiteralExpr(value=\"hello\")")

        val cl = CharLiteralExpr("a", "t.hb", 1, 1, 0, 3)
        assertContains(printer.print(cl), "CharLiteralExpr(value=\"a\")")

        val bl = BoolLiteralExpr(true, "t.hb", 1, 1, 0, 4)
        assertContains(printer.print(bl), "BoolLiteralExpr(value=true)")

        val nl = NilLiteralExpr("t.hb", 1, 1, 0, 3)
        assertContains(printer.print(nl), "NilLiteralExpr")

        val id = IdentifierExpr("x", "t.hb", 1, 1, 0, 1)
        assertContains(printer.print(id), "IdentifierExpr(name=\"x\")")
    }

    @Test
    fun `print assignment expressions`() {
        val assign = AssignmentExpr(
            IdentifierExpr("x", "t.hb", 1, 1, 0, 1),
            IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5),
            "t.hb", 1, 1, 0, 6,
        )
        val output = printer.print(assign)
        assertContains(output, "AssignmentExpr")

        val comp = CompoundAssignmentExpr(
            IdentifierExpr("x", "t.hb", 1, 1, 0, 1),
            "+=",
            IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5),
            "t.hb", 1, 1, 0, 6,
        )
        val compOutput = printer.print(comp)
        assertContains(compOutput, "CompoundAssignmentExpr(operator=\"+=\")")
    }

    @Test
    fun `print array literal and init expressions`() {
        val arrLit = ArrayLiteralExpr(
            listOf(IntegerLiteralExpr("1", "t.hb", 1, 2, 1, 2), IntegerLiteralExpr("2", "t.hb", 1, 5, 4, 5)),
            "t.hb", 1, 1, 0, 7,
        )
        val arrOutput = printer.print(arrLit)
        assertContains(arrOutput, "ArrayLiteralExpr(elements=2)")

        val arrInit = ArrayInitExpr(
            IdentifierType("int", "t.hb", 1, 2, 1, 4),
            IntegerLiteralExpr("10", "t.hb", 1, 6, 5, 7),
            "t.hb", 1, 1, 0, 8,
        )
        val initOutput = printer.print(arrInit)
        assertContains(initOutput, "ArrayInitExpr")
        assertContains(initOutput, "size")
    }

    @Test
    fun `print if expression`() {
        val ifExpr = IfExpr(
            BoolLiteralExpr(true, "t.hb", 1, 3, 2, 6),
            IntegerLiteralExpr("1", "t.hb", 1, 8, 7, 8),
            IntegerLiteralExpr("2", "t.hb", 1, 14, 13, 14),
            "t.hb", 1, 1, 0, 18,
        )
        val output = printer.print(ifExpr)
        assertContains(output, "IfExpr")
        assertContains(output, "condition")
        assertContains(output, "then")
        assertContains(output, "else")
    }

    @Test
    fun `print paren expression`() {
        val paren = ParenExpr(
            IntegerLiteralExpr("1", "t.hb", 1, 2, 1, 2),
            "t.hb", 1, 1, 0, 3,
        )
        assertContains(printer.print(paren), "ParenExpr")
    }

    @Test
    fun `print extern function (null body)`() {
        val fn = FnDecl(
            name = "extern_fn",
            originalName = "extern_fn",
            parameters = listOf(FunctionParam("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5)),
            returnType = VoidType("t.hb", 1, 1, 0, 4),
            body = null,
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 25,
        )
        val output = printer.print(fn)
        assertContains(output, "(extern)")
        assertContains(output, "Param(name=\"x\", mutable=false)")
    }
}
