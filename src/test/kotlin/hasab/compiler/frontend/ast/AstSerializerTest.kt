package hasab.compiler.frontend.ast

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AstSerializerTest {

    private val serializer = AstSerializer()

    @Test
    fun `serialize empty module`() {
        val mod = Module(name = "test", declarations = emptyList(), fileName = "t.hb")
        val json = serializer.serialize(mod)
        assertContains(json, "\"type\": \"Module\"")
        assertContains(json, "\"name\": \"test\"")
        assertContains(json, "\"fileName\": \"t.hb\"")
        assertContains(json, "\"line\": 1")
        assertContains(json, "\"column\": 1")
        assertContains(json, "\"startOffset\": 0")
        assertContains(json, "\"endOffset\": 0")
    }

    @Test
    fun `serialize simple function`() {
        val fn = FnDecl(
            name = "main",
            parameters = emptyList(),
            returnType = VoidType("t.hb", 1, 1, 0, 4),
            body = Block(listOf(
                ExprStmt(CallExpr(
                    callee = IdentifierExpr("println", "t.hb", 1, 5, 4, 11),
                    arguments = listOf(IntegerLiteralExpr("42", "t.hb", 1, 13, 12, 14)),
                    fileName = "t.hb", line = 1, column = 5, startOffset = 4, endOffset = 15,
                ), "t.hb", 1, 5, 4, 16),
            ), "t.hb", 1, 3, 2, 20),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val json = serializer.serialize(fn)
        assertContains(json, "\"type\": \"FnDecl\"")
        assertContains(json, "\"name\": \"main\"")
        assertContains(json, "\"isPublic\": false")
    }

    @Test
    fun `serialize struct declaration`() {
        val s = StructDecl(
            name = "Point",
            fields = listOf(
                StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 8),
            ),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        val json = serializer.serialize(s)
        assertContains(json, "\"type\": \"StructDecl\"")
        assertContains(json, "\"name\": \"Point\"")
        assertContains(json, "\"isPublic\": true")
    }

    @Test
    fun `serialize enum declaration`() {
        val e = EnumDecl(
            name = "Color",
            variants = listOf(
                EnumVariant("Red", emptyList(), "t.hb", 1, 1, 0, 5),
                EnumVariant("Blue", listOf(
                    StructField("shade", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 8),
                ), "t.hb", 1, 1, 0, 15),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val json = serializer.serialize(e)
        assertContains(json, "\"type\": \"EnumDecl\"")
        assertContains(json, "\"name\": \"Color\"")
    }

    @Test
    fun `serialize binary expression`() {
        val bin = BinaryExpr(
            IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1), "+",
            IntegerLiteralExpr("2", "t.hb", 1, 5, 4, 5),
            "t.hb", 1, 1, 0, 5,
        )
        val json = serializer.serialize(bin)
        assertContains(json, "\"type\": \"BinaryExpr\"")
        assertContains(json, "\"operator\": \"+\"")
        assertContains(json, "\"children\"")
    }

    @Test
    fun `serialize unary expression`() {
        val unary = UnaryExpr("-", IntegerLiteralExpr("5", "t.hb", 1, 2, 1, 2), "t.hb", 1, 1, 0, 2)
        val json = serializer.serialize(unary)
        assertContains(json, "\"type\": \"UnaryExpr\"")
        assertContains(json, "\"operator\": \"-\"")
    }

    @Test
    fun `serialize call expression`() {
        val call = CallExpr(
            callee = IdentifierExpr("add", "t.hb", 1, 1, 0, 3),
            arguments = listOf(
                IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5),
                IntegerLiteralExpr("2", "t.hb", 1, 7, 6, 7),
            ),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 8,
        )
        val json = serializer.serialize(call)
        assertContains(json, "\"type\": \"CallExpr\"")
        assertContains(json, "\"argumentCount\": 2")
    }

    @Test
    fun `serialize field access expression`() {
        val fa = FieldAccessExpr(
            IdentifierExpr("p", "t.hb", 1, 1, 0, 1), "x",
            "t.hb", 1, 1, 0, 3,
        )
        val json = serializer.serialize(fa)
        assertContains(json, "\"type\": \"FieldAccessExpr\"")
        assertContains(json, "\"fieldName\": \"x\"")
    }

    @Test
    fun `serialize index expression`() {
        val idx = IndexExpr(
            IdentifierExpr("arr", "t.hb", 1, 1, 0, 3),
            IntegerLiteralExpr("0", "t.hb", 1, 5, 4, 5),
            "t.hb", 1, 1, 0, 6,
        )
        val json = serializer.serialize(idx)
        assertContains(json, "\"type\": \"IndexExpr\"")
    }

    @Test
    fun `serialize let statement`() {
        val let = LetStmt(
            name = "x",
            typeAnnotation = IdentifierType("int", "t.hb", 1, 6, 5, 8),
            initializer = IntegerLiteralExpr("0", "t.hb", 1, 12, 11, 12),
            isMutable = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 13,
        )
        val json = serializer.serialize(let)
        assertContains(json, "\"type\": \"LetStmt\"")
        assertContains(json, "\"name\": \"x\"")
        assertContains(json, "\"isMutable\": true")
    }

    @Test
    fun `serialize return statement with value`() {
        val ret = ReturnStmt(
            value = IntegerLiteralExpr("42", "t.hb", 1, 8, 7, 9),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val json = serializer.serialize(ret)
        assertContains(json, "\"type\": \"ReturnStmt\"")
        assertContains(json, "\"children\"")
    }

    @Test
    fun `serialize return statement without value`() {
        val ret = ReturnStmt(value = null, fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 7)
        val json = serializer.serialize(ret)
        assertContains(json, "\"type\": \"ReturnStmt\"")
        assertFalse(json.contains("\"children\""))
    }

    @Test
    fun `serialize if statement`() {
        val stmt = IfStmt(
            condition = BoolLiteralExpr(true, "t.hb", 1, 4, 3, 7),
            thenBranch = Block(listOf(BreakStmt("t.hb", 1, 1, 0, 5)), "t.hb", 1, 1, 0, 10),
            elseBranch = Block(listOf(ContinueStmt("t.hb", 1, 1, 0, 8)), "t.hb", 1, 1, 0, 10),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val json = serializer.serialize(stmt)
        assertContains(json, "\"type\": \"IfStmt\"")
        assertContains(json, "\"children\"")
    }

    @Test
    fun `serialize while statement`() {
        val stmt = WhileStmt(
            condition = BoolLiteralExpr(true, "t.hb", 1, 6, 5, 9),
            body = Block(emptyList(), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val json = serializer.serialize(stmt)
        assertContains(json, "\"type\": \"WhileStmt\"")
    }

    @Test
    fun `serialize for statement`() {
        val stmt = ForStmt(
            variable = "i",
            iterable = IdentifierExpr("arr", "t.hb", 1, 6, 5, 8),
            body = Block(emptyList(), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val json = serializer.serialize(stmt)
        assertContains(json, "\"type\": \"ForStmt\"")
        assertContains(json, "\"variable\": \"i\"")
    }

    @Test
    fun `serialize block`() {
        val blk = Block(
            statements = listOf(BreakStmt("t.hb", 1, 1, 0, 5), ContinueStmt("t.hb", 1, 1, 0, 8)),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val json = serializer.serialize(blk)
        assertContains(json, "\"type\": \"Block\"")
        assertContains(json, "\"children\"")
    }

    @Test
    fun `serialize impl declaration`() {
        val impl = ImplDecl(
            targetType = IdentifierType("Point", "t.hb", 1, 6, 5, 10),
            methods = listOf(FnDecl("distance", emptyList(), null, null, false, "t.hb", 1, 1, 0, 20)),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 40,
        )
        val json = serializer.serialize(impl)
        assertContains(json, "\"type\": \"ImplDecl\"")
    }

    @Test
    fun `serialize trait declaration`() {
        val trait = TraitDecl(
            name = "Comparable",
            methods = emptyList(),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 40,
        )
        val json = serializer.serialize(trait)
        assertContains(json, "\"type\": \"TraitDecl\"")
        assertContains(json, "\"name\": \"Comparable\"")
        assertContains(json, "\"isPublic\": true")
    }

    @Test
    fun `serialize type alias`() {
        val alias = TypeAliasDecl(
            name = "MyInt",
            target = IdentifierType("int", "t.hb", 1, 1, 0, 3),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val json = serializer.serialize(alias)
        assertContains(json, "\"type\": \"TypeAliasDecl\"")
        assertContains(json, "\"name\": \"MyInt\"")
    }

    @Test
    fun `serialize use declaration`() {
        val use = UseDecl(
            path = listOf("std", "io"),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        val json = serializer.serialize(use)
        assertContains(json, "\"type\": \"UseDecl\"")
        assertContains(json, "\"path\": [\"std\", \"io\"]")
    }

    @Test
    fun `serialize mod declaration`() {
        val mod = ModDecl(
            name = "mymod",
            body = listOf(FnDecl("f", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10)),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        val json = serializer.serialize(mod)
        assertContains(json, "\"type\": \"ModDecl\"")
        assertContains(json, "\"name\": \"mymod\"")
    }

    @Test
    fun `serialize pub declaration`() {
        val fn = FnDecl("main", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10)
        val pub = PubDecl(fn, "t.hb", 1, 1, 0, 15)
        val json = serializer.serialize(pub)
        assertContains(json, "\"type\": \"PubDecl\"")
    }

    @Test
    fun `serialize break and continue`() {
        val brk = BreakStmt("t.hb", 1, 1, 0, 5)
        assertContains(serializer.serialize(brk), "\"type\": \"BreakStmt\"")

        val cont = ContinueStmt("t.hb", 1, 1, 0, 8)
        assertContains(serializer.serialize(cont), "\"type\": \"ContinueStmt\"")
    }

    // ---- Type nodes ----

    @Test
    fun `serialize identifier type`() {
        val t = IdentifierType("int", "t.hb", 1, 1, 0, 3)
        val json = serializer.serialize(t)
        assertContains(json, "\"type\": \"IdentifierType\"")
        assertContains(json, "\"name\": \"int\"")
    }

    @Test
    fun `serialize qualified type`() {
        val t = QualifiedType(listOf("std", "io"), "t.hb", 1, 1, 0, 10)
        val json = serializer.serialize(t)
        assertContains(json, "\"type\": \"QualifiedType\"")
        assertContains(json, "\"path\": [\"std\", \"io\"]")
    }

    @Test
    fun `serialize array type`() {
        val t = ArrayType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        val json = serializer.serialize(t)
        assertContains(json, "\"type\": \"ArrayType\"")
    }

    @Test
    fun `serialize pointer type`() {
        val t = PointerType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        val json = serializer.serialize(t)
        assertContains(json, "\"type\": \"PointerType\"")
    }

    @Test
    fun `serialize optional type`() {
        val t = OptionalType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        val json = serializer.serialize(t)
        assertContains(json, "\"type\": \"OptionalType\"")
    }

    @Test
    fun `serialize function type`() {
        val t = FunctionType(
            listOf(IdentifierType("int", "t.hb", 1, 1, 0, 3)),
            VoidType("t.hb", 1, 1, 0, 4),
            "t.hb", 1, 1, 0, 10,
        )
        val json = serializer.serialize(t)
        assertContains(json, "\"type\": \"FunctionType\"")
    }

    @Test
    fun `serialize void type`() {
        val t = VoidType("t.hb", 1, 1, 0, 4)
        val json = serializer.serialize(t)
        assertContains(json, "\"type\": \"VoidType\"")
    }

    // ---- Expression types ----

    @Test
    fun `serialize all literal types`() {
        assertContains(serializer.serialize(IntegerLiteralExpr("42", "t.hb", 1, 1, 0, 2)), "\"type\": \"IntegerLiteralExpr\"")
        assertContains(serializer.serialize(FloatLiteralExpr("3.14", "t.hb", 1, 1, 0, 4)), "\"type\": \"FloatLiteralExpr\"")
        assertContains(serializer.serialize(StringLiteralExpr("hi", "t.hb", 1, 1, 0, 4)), "\"type\": \"StringLiteralExpr\"")
        assertContains(serializer.serialize(CharLiteralExpr("a", "t.hb", 1, 1, 0, 3)), "\"type\": \"CharLiteralExpr\"")
        assertContains(serializer.serialize(BoolLiteralExpr(true, "t.hb", 1, 1, 0, 4)), "\"type\": \"BoolLiteralExpr\"")
        assertContains(serializer.serialize(NilLiteralExpr("t.hb", 1, 1, 0, 3)), "\"type\": \"NilLiteralExpr\"")
        assertContains(serializer.serialize(IdentifierExpr("x", "t.hb", 1, 1, 0, 1)), "\"type\": \"IdentifierExpr\"")
    }

    @Test
    fun `serialize array literal`() {
        val arr = ArrayLiteralExpr(
            listOf(IntegerLiteralExpr("1", "t.hb", 1, 2, 1, 2)),
            "t.hb", 1, 1, 0, 4,
        )
        val json = serializer.serialize(arr)
        assertContains(json, "\"type\": \"ArrayLiteralExpr\"")
        assertContains(json, "\"children\"")
    }

    @Test
    fun `serialize array init`() {
        val arr = ArrayInitExpr(
            IdentifierType("int", "t.hb", 1, 2, 1, 4),
            IntegerLiteralExpr("10", "t.hb", 1, 6, 5, 7),
            "t.hb", 1, 1, 0, 8,
        )
        val json = serializer.serialize(arr)
        assertContains(json, "\"type\": \"ArrayInitExpr\"")
    }

    @Test
    fun `serialize if expression`() {
        val ifExpr = IfExpr(
            BoolLiteralExpr(true, "t.hb", 1, 3, 2, 6),
            IntegerLiteralExpr("1", "t.hb", 1, 8, 7, 8),
            null,
            "t.hb", 1, 1, 0, 12,
        )
        val json = serializer.serialize(ifExpr)
        assertContains(json, "\"type\": \"IfExpr\"")
    }

    @Test
    fun `serialize assignment expression`() {
        val assign = AssignmentExpr(
            IdentifierExpr("x", "t.hb", 1, 1, 0, 1),
            IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5),
            "t.hb", 1, 1, 0, 6,
        )
        val json = serializer.serialize(assign)
        assertContains(json, "\"type\": \"AssignmentExpr\"")
    }

    @Test
    fun `serialize compound assignment expression`() {
        val comp = CompoundAssignmentExpr(
            IdentifierExpr("x", "t.hb", 1, 1, 0, 1), "+=",
            IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5),
            "t.hb", 1, 1, 0, 6,
        )
        val json = serializer.serialize(comp)
        assertContains(json, "\"type\": \"CompoundAssignmentExpr\"")
        assertContains(json, "\"operator\": \"+=\"")
    }

    @Test
    fun `serialize paren expression`() {
        val paren = ParenExpr(IntegerLiteralExpr("1", "t.hb", 1, 2, 1, 2), "t.hb", 1, 1, 0, 3)
        val json = serializer.serialize(paren)
        assertContains(json, "\"type\": \"ParenExpr\"")
    }

    // ---- Doc comment serialization ----

    @Test
    fun `serialize node with doc comment`() {
        val fn = FnDecl(
            name = "main", parameters = emptyList(), returnType = null, body = null, isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
            docComment = "Entry point",
        )
        val json = serializer.serialize(fn)
        assertContains(json, "\"docComment\": \"Entry point\"")
    }

    @Test
    fun `serialize node without doc comment omits field`() {
        val fn = FnDecl(
            name = "main", parameters = emptyList(), returnType = null, body = null, isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val json = serializer.serialize(fn)
        assertFalse(json.contains("docComment"))
    }

    // ---- Module serialization ----

    @Test
    fun `serialize full module`() {
        val mod = Module(
            name = "test",
            declarations = listOf(
                UseDecl(listOf("std", "io"), false, "t.hb", 1, 1, 0, 15),
                FnDecl("main", emptyList(), VoidType("t.hb", 1, 1, 0, 4),
                    Block(listOf(
                        ExprStmt(CallExpr(
                            IdentifierExpr("println", "t.hb", 1, 5, 4, 11),
                            listOf(IntegerLiteralExpr("42", "t.hb", 1, 13, 12, 14)),
                            "t.hb", 1, 5, 4, 15,
                        ), "t.hb", 1, 5, 4, 16),
                    ), "t.hb", 1, 1, 0, 20),
                    false, "t.hb", 1, 1, 0, 20),
            ),
            fileName = "t.hb",
        )
        val json = serializer.serialize(mod)
        assertContains(json, "\"type\": \"Module\"")
        assertContains(json, "\"name\": \"test\"")
        assertContains(json, "\"type\": \"UseDecl\"")
        assertContains(json, "\"type\": \"FnDecl\"")
        assertContains(json, "\"type\": \"Block\"")
        assertContains(json, "\"type\": \"ExprStmt\"")
        assertContains(json, "\"type\": \"CallExpr\"")
        assertContains(json, "\"type\": \"IdentifierExpr\"")
        assertContains(json, "\"type\": \"IntegerLiteralExpr\"")
    }

    // ---- Source location ----

    @Test
    fun `serialize includes source location for all node types`() {
        val node = IntegerLiteralExpr("42", "t.hb", 5, 10, 100, 102)
        val json = serializer.serialize(node)
        assertContains(json, "\"fileName\": \"t.hb\"")
        assertContains(json, "\"line\": 5")
        assertContains(json, "\"column\": 10")
        assertContains(json, "\"startOffset\": 100")
        assertContains(json, "\"endOffset\": 102")
    }

    // ---- Special character escaping ----

    @Test
    fun `serialize escapes quotes in string values`() {
        val sl = StringLiteralExpr("say \"hello\"", "t.hb", 1, 1, 0, 13)
        val json = serializer.serialize(sl)
        assertContains(json, "\"value\": \"say \\\"hello\\\"\"")
    }

    @Test
    fun `serialize escapes backslashes in filenames`() {
        val id = IdentifierType("int", "path\\to\\file.hb", 1, 1, 0, 3)
        val json = serializer.serialize(id)
        assertContains(json, "\"fileName\": \"path\\\\to\\\\file.hb\"")
    }
}
