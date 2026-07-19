package hasab.compiler.frontend.ast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AstValidatorTest {

    private val validator = AstValidator()

    // ---- Valid ASTs ----

    @Test
    fun `valid empty module has no issues`() {
        val mod = Module(name = "test", declarations = emptyList(), fileName = "t.hb")
        val issues = validator.validate(mod)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid function declaration has no issues`() {
        val fn = FnDecl(
            name = "main",
            originalName = "main",
            parameters = listOf(FunctionParam("x", IdentifierType("int", "t.hb", 1, 6, 5, 8), false, "t.hb", 1, 6, 5, 8)),
            returnType = IdentifierType("int", "t.hb", 1, 12, 11, 14),
            body = Block(
                statements = listOf(
                    ReturnStmt(IntegerLiteralExpr("42", "t.hb", 1, 11, 10, 12), "t.hb", 1, 5, 4, 13),
                ),
                "t.hb", 1, 1, 0, 15,
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        val issues = validator.validate(fn)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid struct with unique field names has no issues`() {
        val s = StructDecl(
            name = "Point",
            fields = listOf(
                StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 8),
                StructField("y", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 8),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        val issues = validator.validate(s)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid enum with unique variant names has no issues`() {
        val e = EnumDecl(
            name = "Color",
            variants = listOf(
                EnumVariant("Red", emptyList(), "t.hb", 1, 1, 0, 5),
                EnumVariant("Green", emptyList(), "t.hb", 1, 1, 0, 5),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(e)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid binary expression has no issues`() {
        val bin = BinaryExpr(
            IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1), "+",
            IntegerLiteralExpr("2", "t.hb", 1, 5, 4, 5),
            "t.hb", 1, 1, 0, 5,
        )
        val issues = validator.validate(bin)
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `valid complex module has no issues`() {
        val mod = Module(
            name = "test",
            declarations = listOf(
                UseDecl(listOf("std", "io"), false, "t.hb", 1, 1, 0, 15),
                StructDecl("Point", listOf(
                    StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 8),
                ), false, "t.hb", 1, 1, 0, 25),
                EnumDecl("Shape", listOf(
                    EnumVariant("Circle", emptyList(), "t.hb", 1, 1, 0, 5),
                ), false, "t.hb", 1, 1, 0, 40),
                FnDecl("main", "main", emptyList(), VoidType("t.hb", 1, 1, 0, 4),
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
        val issues = validator.validate(mod)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    // ---- Invalid range ----

    @Test
    fun `error when endOffset less than startOffset`() {
        val node = FnDecl(
            name = "f", originalName = "f", parameters = emptyList(), returnType = null, body = null, isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 10, endOffset = 5,
        )
        val issues = validator.validate(node)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("End offset") })
    }

    @Test
    fun `error when line is zero`() {
        val node = IdentifierType("int", "t.hb", 0, 1, 0, 3)
        val issues = validator.validate(node)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("Line number") })
    }

    @Test
    fun `error when column is zero`() {
        val node = IdentifierType("int", "t.hb", 1, 0, 0, 3)
        val issues = validator.validate(node)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("Column number") })
    }

    // ---- Empty names ----

    @Test
    fun `error for empty function name`() {
        val fn = FnDecl(
            name = "", originalName = "", parameters = emptyList(), returnType = null, body = null, isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 5,
        )
        val issues = validator.validate(fn)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty struct name`() {
        val s = StructDecl(
            name = "", fields = emptyList(), isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val issues = validator.validate(s)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty enum name`() {
        val e = EnumDecl(
            name = "", variants = emptyList(), isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val issues = validator.validate(e)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty let name`() {
        val let = LetStmt(
            name = "", typeAnnotation = null, initializer = IntegerLiteralExpr("0", "t.hb", 1, 1, 0, 1),
            isMutable = false, fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val issues = validator.validate(let)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty for variable name`() {
        val forStmt = ForStmt(
            variable = "", iterable = IdentifierExpr("x", "t.hb", 1, 1, 0, 1),
            body = Block(emptyList(), "t.hb", 1, 1, 0, 10),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        val issues = validator.validate(forStmt)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty variable name") })
    }

    @Test
    fun `error for empty identifier name`() {
        val id = IdentifierExpr("", "t.hb", 1, 1, 0, 0)
        val issues = validator.validate(id)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty field access name`() {
        val fa = FieldAccessExpr(
            IdentifierExpr("x", "t.hb", 1, 1, 0, 1), "",
            "t.hb", 1, 1, 0, 3,
        )
        val issues = validator.validate(fa)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty fieldName") })
    }

    @Test
    fun `error for empty integer literal value`() {
        val lit = IntegerLiteralExpr("", "t.hb", 1, 1, 0, 0)
        val issues = validator.validate(lit)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty value") })
    }

    @Test
    fun `error for empty float literal value`() {
        val lit = FloatLiteralExpr("", "t.hb", 1, 1, 0, 0)
        val issues = validator.validate(lit)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty value") })
    }

    @Test
    fun `error for empty use path`() {
        val use = UseDecl(path = emptyList(), isPublic = false, fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10)
        val issues = validator.validate(use)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty path") })
    }

    @Test
    fun `error for empty trait name`() {
        val trait = TraitDecl(
            name = "", methods = emptyList(), isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val issues = validator.validate(trait)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty type alias name`() {
        val alias = TypeAliasDecl(
            name = "", target = IdentifierType("int", "t.hb", 1, 1, 0, 3), isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val issues = validator.validate(alias)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty module name`() {
        val mod = Module(name = "", declarations = emptyList(), fileName = "t.hb")
        val issues = validator.validate(mod)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty name") })
    }

    @Test
    fun `error for empty qualified type path`() {
        val qt = QualifiedType(path = emptyList(), fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 5)
        val issues = validator.validate(qt)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("empty path") })
    }

    @Test
    fun `error for blank qualified type path segment`() {
        val qt = QualifiedType(path = listOf("std", ""), fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10)
        val issues = validator.validate(qt)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.ERROR && it.message.contains("blank path segment") })
    }

    // ---- Duplicate detection ----

    @Test
    fun `error for duplicate parameter names`() {
        val fn = FnDecl(
            name = "f",
            originalName = "f",
            parameters = listOf(
                FunctionParam("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
                FunctionParam("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
            ),
            returnType = null, body = null, isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(fn)
        assertTrue(issues.any { it.message.contains("Duplicate parameter name 'x'") })
    }

    @Test
    fun `error for duplicate struct field names`() {
        val s = StructDecl(
            name = "S",
            fields = listOf(
                StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
                StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(s)
        assertTrue(issues.any { it.message.contains("Duplicate field name 'x'") })
    }

    @Test
    fun `error for duplicate enum variant names`() {
        val e = EnumDecl(
            name = "E",
            variants = listOf(
                EnumVariant("A", emptyList(), "t.hb", 1, 1, 0, 5),
                EnumVariant("A", emptyList(), "t.hb", 1, 1, 0, 5),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(e)
        assertTrue(issues.any { it.message.contains("Duplicate variant name 'A'") })
    }

    @Test
    fun `error for duplicate enum variant field names`() {
        val e = EnumDecl(
            name = "E",
            variants = listOf(
                EnumVariant("V", listOf(
                    StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
                    StructField("x", IdentifierType("int", "t.hb", 1, 1, 0, 3), false, "t.hb", 1, 1, 0, 5),
                ), "t.hb", 1, 1, 0, 10),
            ),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(e)
        assertTrue(issues.any { it.message.contains("Duplicate field name 'x'") })
    }

    // ---- Warnings ----

    @Test
    fun `warning for char literal with wrong length`() {
        val cl = CharLiteralExpr("ab", "t.hb", 1, 1, 0, 4)
        val issues = validator.validate(cl)
        assertTrue(issues.any { it.severity == ValidationIssue.Severity.WARNING && it.message.contains("length 2") })
    }

    @Test
    fun `warning when child starts before parent`() {
        val child = IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1)
        val parent = Block(
            statements = listOf(ExprStmt(child, "t.hb", 1, 1, 0, 1)),
            fileName = "t.hb", line = 1, column = 1, startOffset = 5, endOffset = 20,
        )
        val issues = validator.validate(parent)
        assertTrue(issues.any {
            it.severity == ValidationIssue.Severity.WARNING &&
            it.message.contains("Child starts at")
        })
    }

    // ---- Complex valid ASTs ----

    @Test
    fun `valid if-else statement`() {
        val stmt = IfStmt(
            condition = BinaryExpr(
                IdentifierExpr("x", "t.hb", 1, 4, 3, 4), ">",
                IntegerLiteralExpr("0", "t.hb", 1, 8, 7, 8),
                "t.hb", 1, 4, 3, 8,
            ),
            thenBranch = Block(listOf(
                ReturnStmt(IntegerLiteralExpr("1", "t.hb", 1, 1, 0, 1), "t.hb", 1, 1, 0, 10),
            ), "t.hb", 1, 1, 0, 15),
            elseBranch = Block(listOf(
                ReturnStmt(IntegerLiteralExpr("0", "t.hb", 1, 1, 0, 1), "t.hb", 1, 1, 0, 10),
            ), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(stmt)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid while statement`() {
        val stmt = WhileStmt(
            condition = BoolLiteralExpr(true, "t.hb", 1, 6, 5, 9),
            body = Block(listOf(
                BreakStmt("t.hb", 1, 1, 0, 5),
            ), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(stmt)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid for statement`() {
        val stmt = ForStmt(
            variable = "i",
            iterable = IdentifierExpr("items", "t.hb", 1, 6, 5, 10),
            body = Block(listOf(
                ExprStmt(CallExpr(
                    IdentifierExpr("print", "t.hb", 1, 5, 4, 9),
                    listOf(IdentifierExpr("i", "t.hb", 1, 11, 10, 11)),
                    "t.hb", 1, 5, 4, 12,
                ), "t.hb", 1, 5, 4, 13),
            ), "t.hb", 1, 1, 0, 15),
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(stmt)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid let without type annotation`() {
        val let = LetStmt(
            name = "x",
            typeAnnotation = null,
            initializer = IntegerLiteralExpr("42", "t.hb", 1, 8, 7, 9),
            isMutable = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 10,
        )
        val issues = validator.validate(let)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid type alias`() {
        val alias = TypeAliasDecl(
            name = "MyInt",
            target = IdentifierType("int", "t.hb", 1, 1, 0, 3),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 20,
        )
        val issues = validator.validate(alias)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid use declaration`() {
        val use = UseDecl(
            path = listOf("std", "io"),
            isPublic = false,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 15,
        )
        val issues = validator.validate(use)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid mod declaration with body`() {
        val mod = ModDecl(
            name = "mymod",
            body = listOf(FnDecl("f", "f", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10)),
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        val issues = validator.validate(mod)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid mod declaration without body`() {
        val mod = ModDecl(
            name = "mymod",
            body = null,
            isPublic = true,
            fileName = "t.hb", line = 1, column = 1, startOffset = 0, endOffset = 30,
        )
        val issues = validator.validate(mod)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    @Test
    fun `valid pub declaration`() {
        val fn = FnDecl("main", "main", emptyList(), null, null, false, "t.hb", 1, 1, 0, 10)
        val pub = PubDecl(fn, "t.hb", 1, 1, 0, 15)
        val issues = validator.validate(pub)
        assertTrue(issues.isEmpty(), "Expected no issues, got: $issues")
    }

    // ---- Severity enum test ----

    @Test
    fun `ValidationIssue has correct severity`() {
        val error = ValidationIssue(ValidationIssue.Severity.ERROR, "error", IdentifierType("x", "t.hb", 1, 1, 0, 1))
        val warning = ValidationIssue(ValidationIssue.Severity.WARNING, "warning", IdentifierType("x", "t.hb", 1, 1, 0, 1))
        assertEquals(ValidationIssue.Severity.ERROR, error.severity)
        assertEquals(ValidationIssue.Severity.WARNING, warning.severity)
    }

    @Test
    fun `ValidationIssue data class equality works`() {
        val node = IdentifierType("int", "t.hb", 1, 1, 0, 3)
        val a = ValidationIssue(ValidationIssue.Severity.ERROR, "msg", node)
        val b = ValidationIssue(ValidationIssue.Severity.ERROR, "msg", node)
        assertEquals(a, b)
    }
}
