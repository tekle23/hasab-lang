package hasab.compiler.frontend.ast

import kotlin.test.Test
import kotlin.test.assertEquals

class AstVisitorTest {

    // ---- Node counter visitor ----

    private class NodeCounter : AstVisitorBase<Int>(default = 0) {
        var modules = 0; private set
        var fnDecls = 0; private set
        var structDecls = 0; private set
        var enumDecls = 0; private set
        var intLiterals = 0; private set
        var binaryExprs = 0; private set
        var identifierExprs = 0; private set
        var blocks = 0; private set
        var letStmts = 0; private set
        var returnStmts = 0; private set
        var callExprs = 0; private set
        var stringLiterals = 0; private set
        var typeIdentifiers = 0; private set
        var voidTypes = 0; private set
        var ifStmts = 0; private set
        var whileStmts = 0; private set
        var forStmts = 0; private set
        var exprStmts = 0; private set
        var assignments = 0; private set
        var compoundAssignments = 0; private set
        var ifExprs = 0; private set
        var arrayLiterals = 0; private set
        var arrayInits = 0; private set
        var fieldAccesses = 0; private set
        var indexExprs = 0; private set
        var unaryExprs = 0; private set
        var parenExprs = 0; private set
        var implDecls = 0; private set
        var traitDecls = 0; private set
        var typeAliasDecls = 0; private set
        var modDecls = 0; private set
        var useDecls = 0; private set
        var pubDecls = 0; private set
        var breakStmts = 0; private set
        var continueStmts = 0; private set
        var qualifiedTypes = 0; private set
        var arrayTypes = 0; private set
        var pointerTypes = 0; private set
        var optionalTypes = 0; private set
        var functionTypes = 0; private set
        var floatLiterals = 0; private set
        var charLiterals = 0; private set
        var boolLiterals = 0; private set
        var nilLiterals = 0; private set

        override fun visitModule(node: Module): Int {
            modules++
            return super.visitModule(node)
        }
        override fun visitFnDecl(node: FnDecl): Int { fnDecls++; return super.visitFnDecl(node) }
        override fun visitStructDecl(node: StructDecl): Int { structDecls++; return super.visitStructDecl(node) }
        override fun visitEnumDecl(node: EnumDecl): Int { enumDecls++; return super.visitEnumDecl(node) }
        override fun visitIntegerLiteral(node: IntegerLiteralExpr): Int { intLiterals++; return super.visitIntegerLiteral(node) }
        override fun visitBinary(node: BinaryExpr): Int { binaryExprs++; return super.visitBinary(node) }
        override fun visitIdentifier(node: IdentifierExpr): Int { identifierExprs++; return super.visitIdentifier(node) }
        override fun visitBlock(node: Block): Int { blocks++; return super.visitBlock(node) }
        override fun visitLet(node: LetStmt): Int { letStmts++; return super.visitLet(node) }
        override fun visitReturn(node: ReturnStmt): Int { returnStmts++; return super.visitReturn(node) }
        override fun visitCall(node: CallExpr): Int { callExprs++; return super.visitCall(node) }
        override fun visitStringLiteral(node: StringLiteralExpr): Int { stringLiterals++; return super.visitStringLiteral(node) }
        override fun visitIdentifierType(node: IdentifierType): Int { typeIdentifiers++; return super.visitIdentifierType(node) }
        override fun visitVoidType(node: VoidType): Int { voidTypes++; return super.visitVoidType(node) }
        override fun visitIfStmt(node: IfStmt): Int { ifStmts++; return super.visitIfStmt(node) }
        override fun visitWhile(node: WhileStmt): Int { whileStmts++; return super.visitWhile(node) }
        override fun visitFor(node: ForStmt): Int { forStmts++; return super.visitFor(node) }
        override fun visitExprStmt(node: ExprStmt): Int { exprStmts++; return super.visitExprStmt(node) }
        override fun visitAssignment(node: AssignmentExpr): Int { assignments++; return super.visitAssignment(node) }
        override fun visitCompoundAssignment(node: CompoundAssignmentExpr): Int { compoundAssignments++; return super.visitCompoundAssignment(node) }
        override fun visitIfExpr(node: IfExpr): Int { ifExprs++; return super.visitIfExpr(node) }
        override fun visitArrayLiteral(node: ArrayLiteralExpr): Int { arrayLiterals++; return super.visitArrayLiteral(node) }
        override fun visitArrayInit(node: ArrayInitExpr): Int { arrayInits++; return super.visitArrayInit(node) }
        override fun visitFieldAccess(node: FieldAccessExpr): Int { fieldAccesses++; return super.visitFieldAccess(node) }
        override fun visitIndex(node: IndexExpr): Int { indexExprs++; return super.visitIndex(node) }
        override fun visitUnary(node: UnaryExpr): Int { unaryExprs++; return super.visitUnary(node) }
        override fun visitParen(node: ParenExpr): Int { parenExprs++; return super.visitParen(node) }
        override fun visitImplDecl(node: ImplDecl): Int { implDecls++; return super.visitImplDecl(node) }
        override fun visitTraitDecl(node: TraitDecl): Int { traitDecls++; return super.visitTraitDecl(node) }
        override fun visitTypeAlias(node: TypeAliasDecl): Int { typeAliasDecls++; return super.visitTypeAlias(node) }
        override fun visitModDecl(node: ModDecl): Int { modDecls++; return super.visitModDecl(node) }
        override fun visitUseDecl(node: UseDecl): Int { useDecls++; return super.visitUseDecl(node) }
        override fun visitPubDecl(node: PubDecl): Int { pubDecls++; return super.visitPubDecl(node) }
        override fun visitBreak(node: BreakStmt): Int { breakStmts++; return super.visitBreak(node) }
        override fun visitContinue(node: ContinueStmt): Int { continueStmts++; return super.visitContinue(node) }
        override fun visitQualifiedType(node: QualifiedType): Int { qualifiedTypes++; return super.visitQualifiedType(node) }
        override fun visitArrayType(node: ArrayType): Int { arrayTypes++; return super.visitArrayType(node) }
        override fun visitPointerType(node: PointerType): Int { pointerTypes++; return super.visitPointerType(node) }
        override fun visitOptionalType(node: OptionalType): Int { optionalTypes++; return super.visitOptionalType(node) }
        override fun visitFunctionType(node: FunctionType): Int { functionTypes++; return super.visitFunctionType(node) }
        override fun visitFloatLiteral(node: FloatLiteralExpr): Int { floatLiterals++; return super.visitFloatLiteral(node) }
        override fun visitCharLiteral(node: CharLiteralExpr): Int { charLiterals++; return super.visitCharLiteral(node) }
        override fun visitBoolLiteral(node: BoolLiteralExpr): Int { boolLiterals++; return super.visitBoolLiteral(node) }
        override fun visitNilLiteral(node: NilLiteralExpr): Int { nilLiterals++; return super.visitNilLiteral(node) }
    }

    // ---- Name collector visitor ----

    private class NameCollector : AstVisitorBase<String>(default = "") {
        val names = mutableListOf<String>()

        override fun visitFnDecl(node: FnDecl): String {
            names.add("fn:${node.name}")
            return super.visitFnDecl(node)
        }

        override fun visitStructDecl(node: StructDecl): String {
            names.add("struct:${node.name}")
            return super.visitStructDecl(node)
        }

        override fun visitIdentifier(node: IdentifierExpr): String {
            names.add("id:${node.name}")
            return super.visitIdentifier(node)
        }
    }

    // ---- Tests: accept dispatch ----

    @Test
    fun `accept dispatches to visitModule`() {
        val counter = NodeCounter()
        val mod = Module(
            name = "test",
            declarations = listOf(
                FnDecl("main", emptyList(), null, Block(emptyList(), "t.hb", 1, 1, 0, 10), false, "t.hb", 1, 1, 0, 15),
            ),
            fileName = "t.hb",
        )
        mod.accept(counter)
        assertEquals(1, counter.modules)
        assertEquals(1, counter.fnDecls)
    }

    @Test
    fun `accept dispatches to visitBinary`() {
        val counter = NodeCounter()
        val expr = BinaryExpr(
            left = IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1),
            operator = "+",
            right = IntegerLiteralExpr("2", "t.hb", 1, 3, 2, 3),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 3,
        )
        expr.accept(counter)
        assertEquals(1, counter.binaryExprs)
        assertEquals(2, counter.intLiterals)
    }

    @Test
    fun `accept dispatches to visitCall`() {
        val counter = NodeCounter()
        val call = CallExpr(
            callee = IdentifierExpr("println", "t.hb", 1, 1, 0, 7),
            arguments = listOf(IntegerLiteralExpr("42", "t.hb", 1, 9, 8, 10)),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 11,
        )
        call.accept(counter)
        assertEquals(1, counter.callExprs)
        assertEquals(1, counter.identifierExprs)
        assertEquals(1, counter.intLiterals)
    }

    @Test
    fun `accept dispatches to visitLet with type annotation`() {
        val counter = NodeCounter()
        val let = LetStmt(
            name = "x",
            typeAnnotation = IdentifierType("int", "t.hb", 1, 6, 5, 8),
            initializer = IntegerLiteralExpr("0", "t.hb", 1, 11, 10, 11),
            isMutable = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 12,
        )
        let.accept(counter)
        assertEquals(1, counter.letStmts)
        assertEquals(1, counter.typeIdentifiers)
        assertEquals(1, counter.intLiterals)
    }

    @Test
    fun `accept dispatches to visitReturn with value`() {
        val counter = NodeCounter()
        val ret = ReturnStmt(
            value = IntegerLiteralExpr("42", "t.hb", 1, 8, 7, 9),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        ret.accept(counter)
        assertEquals(1, counter.returnStmts)
        assertEquals(1, counter.intLiterals)
    }

    @Test
    fun `accept dispatches to visitReturn without value`() {
        val counter = NodeCounter()
        val ret = ReturnStmt(
            value = null,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 7,
        )
        ret.accept(counter)
        assertEquals(1, counter.returnStmts)
        assertEquals(0, counter.intLiterals)
    }

    @Test
    fun `accept dispatches to visitBlock recursively`() {
        val counter = NodeCounter()
        val blk = Block(
            statements = listOf(
                BreakStmt("t.hb", 1, 1, 0, 5),
                ContinueStmt("t.hb", 1, 1, 6, 14),
            ),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        blk.accept(counter)
        assertEquals(1, counter.blocks)
        assertEquals(1, counter.breakStmts)
        assertEquals(1, counter.continueStmts)
    }

    @Test
    fun `accept dispatches to visitIfStmt`() {
        val counter = NodeCounter()
        val stmt = IfStmt(
            condition = BoolLiteralExpr(true, "t.hb", 1, 4, 3, 7),
            thenBranch = Block(emptyList(), "t.hb", 1, 1, 0, 10),
            elseBranch = Block(emptyList(), "t.hb", 1, 1, 0, 10),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        stmt.accept(counter)
        assertEquals(1, counter.ifStmts)
        assertEquals(2, counter.blocks)
        assertEquals(1, counter.boolLiterals)
    }

    @Test
    fun `accept dispatches to visitWhile`() {
        val counter = NodeCounter()
        val stmt = WhileStmt(
            condition = BoolLiteralExpr(true, "t.hb", 1, 6, 5, 9),
            body = Block(emptyList(), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        stmt.accept(counter)
        assertEquals(1, counter.whileStmts)
    }

    @Test
    fun `accept dispatches to visitFor`() {
        val counter = NodeCounter()
        val stmt = ForStmt(
            variable = "i",
            iterable = IdentifierExpr("arr", "t.hb", 1, 6, 5, 8),
            body = Block(emptyList(), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        stmt.accept(counter)
        assertEquals(1, counter.forStmts)
        assertEquals(1, counter.identifierExprs)
    }

    @Test
    fun `accept dispatches to visitStructDecl`() {
        val counter = NodeCounter()
        val s = StructDecl(
            name = "Point",
            fields = listOf(
                StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
                StructField("y", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
            ),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        s.accept(counter)
        assertEquals(1, counter.structDecls)
        assertEquals(2, counter.typeIdentifiers)
    }

    @Test
    fun `accept dispatches to visitEnumDecl`() {
        val counter = NodeCounter()
        val e = EnumDecl(
            name = "Color",
            variants = listOf(
                EnumVariant("Red", emptyList(), "t.hb", 1, 1, 0, 5),
                EnumVariant("Green", emptyList(), "t.hb", 1, 1, 0, 5),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        e.accept(counter)
        assertEquals(1, counter.enumDecls)
    }

    @Test
    fun `accept dispatches to visitImplDecl`() {
        val counter = NodeCounter()
        val impl = ImplDecl(
            targetType = IdentifierType("int", "t.hb", 1, 6, 5, 8),
            methods = listOf(
                FnDecl("add", emptyList(), null, null, false, "t.hb", 1, 1, 0, 15),
            ),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        impl.accept(counter)
        assertEquals(1, counter.implDecls)
        assertEquals(1, counter.fnDecls)
        assertEquals(1, counter.typeIdentifiers)
    }

    @Test
    fun `accept dispatches to visitTraitDecl`() {
        val counter = NodeCounter()
        val trait = TraitDecl(
            name = "Comparable",
            methods = listOf(
                FnDecl("compare", emptyList(), null, null, false, "t.hb", 1, 1, 0, 20),
            ),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 40,
        )
        trait.accept(counter)
        assertEquals(1, counter.traitDecls)
        assertEquals(1, counter.fnDecls)
    }

    @Test
    fun `accept dispatches to visitTypeAlias`() {
        val counter = NodeCounter()
        val alias = TypeAliasDecl(
            name = "MyInt",
            target = IdentifierType("int", "t.hb", 1, 1, 0, 3),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        alias.accept(counter)
        assertEquals(1, counter.typeAliasDecls)
        assertEquals(1, counter.typeIdentifiers)
    }

    @Test
    fun `accept dispatches to visitUseDecl`() {
        val counter = NodeCounter()
        val use = UseDecl(
            path = listOf("std", "io"),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        use.accept(counter)
        assertEquals(1, counter.useDecls)
    }

    @Test
    fun `accept dispatches to visitPubDecl`() {
        val counter = NodeCounter()
        val fn = FnDecl("main", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10)
        val pub = PubDecl(fn, "t.hb", 1, 1, 0, 15)
        pub.accept(counter)
        assertEquals(1, counter.pubDecls)
        assertEquals(1, counter.fnDecls)
    }

    @Test
    fun `accept dispatches to visitModDecl`() {
        val counter = NodeCounter()
        val mod = ModDecl(
            name = "mymod",
            body = listOf(FnDecl("f", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10)),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        mod.accept(counter)
        assertEquals(1, counter.modDecls)
        assertEquals(1, counter.fnDecls)
    }

    @Test
    fun `accept dispatches complex expression types`() {
        val counter = NodeCounter()
        // UnaryExpr
        val unary = UnaryExpr("-", IntegerLiteralExpr("5", "t.hb", 1, 2, 1, 2), "t.hb", 1, 1, 0, 2)
        unary.accept(counter)
        assertEquals(1, counter.unaryExprs)

        // ParenExpr
        val paren = ParenExpr(IntegerLiteralExpr("1", "t.hb", 1, 2, 1, 2), "t.hb", 1, 1, 0, 3)
        paren.accept(counter)
        assertEquals(1, counter.parenExprs)

        // FieldAccessExpr
        val fa = FieldAccessExpr(IdentifierExpr("x", "t.hb", 1, 1, 0, 1), "y", "t.hb", 1, 1, 0, 3)
        fa.accept(counter)
        assertEquals(1, counter.fieldAccesses)

        // IndexExpr
        val idx = IndexExpr(IdentifierExpr("a", "t.hb", 1, 1, 0, 1), IntegerLiteralExpr("0", "t.hb", 1, 3, 2, 3), "t.hb", 1, 1, 0, 4)
        idx.accept(counter)
        assertEquals(1, counter.indexExprs)

        // ArrayLiteralExpr
        val arr = ArrayLiteralExpr(listOf(IntegerLiteralExpr("1", "t.hb", 1, 2, 1, 2)), "t.hb", 1, 1, 0, 4)
        arr.accept(counter)
        assertEquals(1, counter.arrayLiterals)

        // ArrayInitExpr
        val arrInit = ArrayInitExpr(IdentifierType("int", "t.hb", 1, 2, 1, 4), IntegerLiteralExpr("10", "t.hb", 1, 6, 5, 7), "t.hb", 1, 1, 0, 8)
        arrInit.accept(counter)
        assertEquals(1, counter.arrayInits)

        // IfExpr
        val ifExpr = IfExpr(BoolLiteralExpr(true, "t.hb", 1, 3, 2, 6), IntegerLiteralExpr("1", "t.hb", 1, 8, 7, 8), null, "t.hb", 1, 1, 0, 12)
        ifExpr.accept(counter)
        assertEquals(1, counter.ifExprs)

        // AssignmentExpr
        val assign = AssignmentExpr(IdentifierExpr("x", "t.hb", 1, 1, 0, 1), IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5), "t.hb", 1, 1, 0, 6)
        assign.accept(counter)
        assertEquals(1, counter.assignments)

        // CompoundAssignmentExpr
        val comp = CompoundAssignmentExpr(IdentifierExpr("x", "t.hb", 1, 1, 0, 1), "+=", IntegerLiteralExpr("1", "t.hb", 1, 5, 4, 5), "t.hb", 1, 1, 0, 6)
        comp.accept(counter)
        assertEquals(1, counter.compoundAssignments)

        // ExprStmt
        val exprStmt = ExprStmt(IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1), "t.hb", 1, 1, 0, 2)
        exprStmt.accept(counter)
        assertEquals(1, counter.exprStmts)
    }

    // ---- Tests: Name collector ----

    @Test
    fun `name collector collects function names from module`() {
        val collector = NameCollector()
        val mod = Module(
            name = "test",
            declarations = listOf(
                FnDecl("main", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10),
                StructDecl("Point", emptyList(), false, "t.hb", 1, 1, 0, 20),
            ),
            fileName = "t.hb",
        )
        mod.accept(collector)
        assertEquals(listOf("fn:main", "struct:Point"), collector.names)
    }

    @Test
    fun `name collector collects identifiers from expressions`() {
        val collector = NameCollector()
        val expr = BinaryExpr(
            left = IdentifierExpr("x", "t.hb", 1, 1, 0, 1),
            operator = "+",
            right = IdentifierExpr("y", "t.hb", 1, 3, 2, 3),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 3,
        )
        expr.accept(collector)
        assertEquals(listOf("id:x", "id:y"), collector.names)
    }

    // ---- Tests: Type visitor dispatch ----

    @Test
    fun `accept dispatches all type node variants`() {
        val counter = NodeCounter()
        val ft = FunctionType(
            parameterTypes = listOf(IdentifierType("int", "t.hb", 1, 1, 0, 3)),
            returnType = VoidType("t.hb", 1, 1, 0, 4),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        ft.accept(counter)
        assertEquals(1, counter.functionTypes)
        assertEquals(1, counter.typeIdentifiers)
        assertEquals(1, counter.voidTypes)

        val qt = QualifiedType(listOf("std", "io"), "t.hb", 1, 1, 0, 10)
        qt.accept(counter)
        assertEquals(1, counter.qualifiedTypes)

        val at = ArrayType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        at.accept(counter)
        assertEquals(1, counter.arrayTypes)

        val pt = PointerType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        pt.accept(counter)
        assertEquals(1, counter.pointerTypes)

        val ot = OptionalType(IdentifierType("int", "t.hb", 1, 1, 0, 3), "t.hb", 1, 1, 0, 8)
        ot.accept(counter)
        assertEquals(1, counter.optionalTypes)
    }

    // ---- Tests: visit() via AstVisitorBase ----

    @Test
    fun `visit entry point dispatches correctly`() {
        val counter = NodeCounter()
        val mod = Module(
            name = "test",
            declarations = listOf(
                FnDecl("main", emptyList(), null, Block(
                    statements = listOf(
                        ExprStmt(CallExpr(
                            callee = IdentifierExpr("println", "t.hb", 1, 5, 4, 11),
                            arguments = listOf(IntegerLiteralExpr("42", "t.hb", 1, 13, 12, 14)),
                            fileName = "t.hb", line = 1, column = 5, startOffset = 4, endOffset = 15,
                        ), "t.hb", 1, 5, 4, 16),
                    ),
                    fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
                ), false, "t.hb", 1, 1, 0, 20),
            ),
            fileName = "t.hb",
        )
        counter.visit(mod)
        assertEquals(1, counter.modules)
        assertEquals(1, counter.fnDecls)
        assertEquals(1, counter.blocks)
        assertEquals(1, counter.exprStmts)
        assertEquals(1, counter.callExprs)
        assertEquals(1, counter.identifierExprs)
        assertEquals(1, counter.intLiterals)
    }

    // ---- Tests: Bool/Float/Char/Nil literals ----

    @Test
    fun `accept dispatches literal types`() {
        val counter = NodeCounter()
        BoolLiteralExpr(true, "t.hb", 1, 1, 0, 4).accept(counter)
        assertEquals(1, counter.boolLiterals)

        FloatLiteralExpr("3.14", "t.hb", 1, 1, 0, 4).accept(counter)
        assertEquals(1, counter.floatLiterals)

        CharLiteralExpr("a", "t.hb", 1, 1, 0, 3).accept(counter)
        assertEquals(1, counter.charLiterals)

        NilLiteralExpr("t.hb", 1, 1, 0, 3).accept(counter)
        assertEquals(1, counter.nilLiterals)

        StringLiteralExpr("hello", "t.hb", 1, 1, 0, 7).accept(counter)
        assertEquals(1, counter.stringLiterals)
    }
}
