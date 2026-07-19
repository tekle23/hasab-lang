package hasab.compiler.hir

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HirLoweringTest {

    private fun lower(code: String): HirModule {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val typeChecker = TypeChecker()
        val typeCheckResult = typeChecker.check(parseResult.module)
        val lowering = AstToHirLowering(typeCheckResult.environment)
        return lowering.lower(parseResult.module)
    }

    // ---- Function declarations ----

    @Test
    fun `lower simple function`() {
        val m = lower("fn add(x: int, y: int) -> int { return x + y; }")
        assertEquals(1, m.declarations.size)
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        assertEquals("add", fn.name)
        assertEquals(2, fn.parameters.size)
        assertEquals(IntType, fn.parameters[0].type)
        assertEquals(IntType, fn.parameters[1].type)
        assertEquals(IntType, fn.returnType)
        assertFalse(fn.isPublic)
        assertNotNull(fn.body)
    }

    @Test
    fun `lower void function`() {
        val m = lower("fn noop() { }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        assertEquals(VoidType, fn.returnType)
        assertEquals(0, fn.parameters.size)
    }

    @Test
    fun `lower function with multiple params`() {
        val m = lower("fn foo(a: int, b: float, c: string) -> bool { return true; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        assertEquals(3, fn.parameters.size)
        assertEquals(IntType, fn.parameters[0].type)
        assertEquals(FloatType, fn.parameters[1].type)
        assertEquals(StringType, fn.parameters[2].type)
        assertEquals(BoolType, fn.returnType)
    }

    // ---- Struct declarations ----

    @Test
    fun `lower struct declaration`() {
        val m = lower("struct Point { x: int, y: int }")
        assertEquals(1, m.declarations.size)
        val s = assertIs<HirStructDecl>(m.declarations[0])
        assertEquals("Point", s.name)
        assertEquals(2, s.fields.size)
        assertEquals("x", s.fields[0].name)
        assertEquals(IntType, s.fields[0].type)
        assertEquals("y", s.fields[1].name)
        assertEquals(IntType, s.fields[1].type)
    }

    @Test
    fun `lower struct with mutable field`() {
        val m = lower("struct Foo { mut val: int }")
        val s = assertIs<HirStructDecl>(m.declarations[0])
        assertTrue(s.fields[0].isMutable)
    }

    // ---- Enum declarations ----

    @Test
    fun `lower enum declaration`() {
        val m = lower("enum Color { Red, Green, Blue }")
        val e = assertIs<HirEnumDecl>(m.declarations[0])
        assertEquals("Color", e.name)
        assertEquals(3, e.variants.size)
        assertEquals("Red", e.variants[0].name)
        assertTrue(e.variants[0].fieldTypes.isEmpty())
    }

    @Test
    fun `lower enum with fields`() {
        val m = lower("enum Result { Ok(int), Err(string) }")
        val e = assertIs<HirEnumDecl>(m.declarations[0])
        assertEquals(2, e.variants.size)
        assertEquals(listOf(IntType), e.variants[0].fieldTypes)
        assertEquals(listOf(StringType), e.variants[1].fieldTypes)
    }

    // ---- Type alias ----

    @Test
    fun `lower type alias`() {
        val m = lower("type IntOrString = int | string")
        val ta = assertIs<HirTypeAliasDecl>(m.declarations[0])
        assertEquals("IntOrString", ta.name)
    }

    // ---- Impl declarations ----

    @Test
    fun `lower impl declaration`() {
        val m = lower("""
            struct Counter { value: int }
            impl Counter {
                fn increment(self) { self.value = self.value + 1; }
            }
        """.trimIndent())
        assertEquals(2, m.declarations.size)
        val impl = assertIs<HirImplDecl>(m.declarations[1])
        assertIs<StructType>(impl.targetType)
        assertEquals("Counter", (impl.targetType as StructType).name)
        assertEquals(1, impl.methods.size)
        assertEquals("increment", impl.methods[0].name)
    }

    // ---- Expression type annotations ----

    @Test
    fun `int literal has int type`() {
        val m = lower("fn main() { let x = 42; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        assertIs<HirIntLiteral>(letStmt.initializer)
        assertEquals(IntType, letStmt.initializer.type)
        assertEquals(IntType, letStmt.type)
    }

    @Test
    fun `float literal has float type`() {
        val m = lower("fn main() { let x = 3.14; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        assertEquals(FloatType, letStmt.initializer.type)
    }

    @Test
    fun `string literal has string type`() {
        val m = lower("""fn main() { let x = "hello"; }""")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        assertEquals(StringType, letStmt.initializer.type)
    }

    @Test
    fun `bool literal has bool type`() {
        val m = lower("fn main() { let x = true; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        assertEquals(BoolType, letStmt.initializer.type)
    }

    // ---- Binary expressions ----

    @Test
    fun `int arithmetic has int type`() {
        val m = lower("fn main() { let x = 1 + 2; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        val bin = assertIs<HirBinary>(letStmt.initializer)
        assertEquals(IntType, bin.type)
        assertEquals("+", bin.operator)
    }

    @Test
    fun `comparison has bool type`() {
        val m = lower("fn main() { let x = 1 < 2; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        val bin = assertIs<HirBinary>(letStmt.initializer)
        assertEquals(BoolType, bin.type)
    }

    @Test
    fun `float plus int has float type`() {
        val m = lower("fn main() { let x = 1.0 + 2; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        val bin = assertIs<HirBinary>(letStmt.initializer)
        assertEquals(FloatType, bin.type)
    }

    // ---- Identifier ----

    @Test
    fun `identifier resolves to declared type`() {
        val m = lower("fn main() { let x: int = 1; let y = x; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[1])
        val ident = assertIs<HirIdentifier>(letStmt.initializer)
        assertEquals("x", ident.name)
        assertEquals(IntType, ident.type)
    }

    // ---- Field access ----

    @Test
    fun `field access on struct`() {
        val m = lower("""
            struct Point { x: int, y: int }
            fn getX(p: Point) -> int { return p.x; }
        """.trimIndent())
        val fn = assertIs<HirFnDecl>(m.declarations[1])
        val ret = assertIs<HirReturnStmt>(fn.body!!.statements[0])
        val fieldAccess = assertIs<HirFieldAccess>(ret.value!!)
        assertEquals("x", fieldAccess.fieldName)
        assertEquals(IntType, fieldAccess.type)
    }

    // ---- Array operations ----

    @Test
    fun `array literal has array type`() {
        val m = lower("fn main() { let arr = [1, 2, 3]; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[0])
        val arrLit = assertIs<HirArrayLiteral>(letStmt.initializer)
        assertIs<ArrayType>(arrLit.type)
        assertEquals(IntType, (arrLit.type as ArrayType).elementType)
    }

    @Test
    fun `array index has element type`() {
        val m = lower("fn main() { let arr = [1, 2, 3]; let v = arr[0]; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val letStmt = assertIs<HirLetStmt>(fn.body!!.statements[1])
        val idx = assertIs<HirIndex>(letStmt.initializer)
        assertIs<OptionalType>(idx.type)
        assertEquals(IntType, (idx.type as OptionalType).elementType)
    }

    // ---- Control flow ----

    @Test
    fun `if statement`() {
        val m = lower("fn main() { if true { let x = 1; } }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val ifStmt = assertIs<HirIfStmt>(fn.body!!.statements[0])
        assertEquals(BoolType, ifStmt.condition.type)
        assertEquals(1, ifStmt.thenBranch.statements.size)
    }

    @Test
    fun `while statement`() {
        val m = lower("fn main() { while true { break; } }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val whileStmt = assertIs<HirWhileStmt>(fn.body!!.statements[0])
        assertEquals(BoolType, whileStmt.condition.type)
        assertIs<HirBreakStmt>(whileStmt.body.statements[0])
    }

    @Test
    fun `for statement`() {
        val m = lower("fn main() { let arr = [1, 2, 3]; for (i: arr) { } }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val forStmt = assertIs<HirForStmt>(fn.body!!.statements[1])
        assertEquals("i", forStmt.variable)
    }

    @Test
    fun `break and continue`() {
        val m = lower("fn main() { while true { break; continue; } }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val whileStmt = assertIs<HirWhileStmt>(fn.body!!.statements[0])
        assertEquals(2, whileStmt.body.statements.size)
        assertIs<HirBreakStmt>(whileStmt.body.statements[0])
        assertIs<HirContinueStmt>(whileStmt.body.statements[1])
    }

    // ---- Return statement ----

    @Test
    fun `return with value`() {
        val m = lower("fn getNum() -> int { return 42; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val ret = assertIs<HirReturnStmt>(fn.body!!.statements[0])
        assertNotNull(ret.value)
        assertEquals(IntType, ret.value!!.type)
    }

    @Test
    fun `return without value`() {
        val m = lower("fn noop() { return; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val ret = assertIs<HirReturnStmt>(fn.body!!.statements[0])
        assertEquals(null, ret.value)
    }

    // ---- Function calls ----

    @Test
    fun `function call has return type`() {
        val m = lower("""
            fn add(x: int, y: int) -> int { return x + y; }
            fn main() { let r = add(1, 2); }
        """.trimIndent())
        val mainFn = assertIs<HirFnDecl>(m.declarations[1])
        val letStmt = assertIs<HirLetStmt>(mainFn.body!!.statements[0])
        val call = assertIs<HirCall>(letStmt.initializer)
        assertEquals(IntType, call.type)
        assertEquals(2, call.arguments.size)
    }

    // ---- Assignment ----

    @Test
    fun `assignment expression`() {
        val m = lower("fn main() { mut x = 1; x = 2; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val exprStmt = assertIs<HirExprStmt>(fn.body!!.statements[1])
        val assign = assertIs<HirAssignment>(exprStmt.expression)
        assertEquals(IntType, assign.type)
    }

    @Test
    fun `compound assignment`() {
        val m = lower("fn main() { mut x = 1; x += 2; }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        val exprStmt = assertIs<HirExprStmt>(fn.body!!.statements[1])
        val compound = assertIs<HirCompoundAssignment>(exprStmt.expression)
        assertEquals("+=", compound.operator)
        assertEquals(IntType, compound.type)
    }

    // ---- Complex programs ----

    @Test
    fun `multi-declaration program`() {
        val m = lower("""
            struct Point { x: int, y: int }
            enum Color { Red, Green, Blue }
            fn distance(p1: Point, p2: Point) -> float { return 0.0; }
        """.trimIndent())
        assertEquals(3, m.declarations.size)
        assertIs<HirStructDecl>(m.declarations[0])
        assertIs<HirEnumDecl>(m.declarations[1])
        assertIs<HirFnDecl>(m.declarations[2])
    }

    @Test
    fun `nested blocks preserve scope`() {
        val m = lower("fn main() { let x = 1; { let y = 2; } }")
        val fn = assertIs<HirFnDecl>(m.declarations[0])
        assertEquals(2, fn.body!!.statements.size)
        assertIs<HirLetStmt>(fn.body!!.statements[0])
        assertIs<HirBlock>(fn.body!!.statements[1])
    }

    // ---- Use declaration ----

    @Test
    fun `lower use declaration`() {
        val m = lower("use std::io;")
        val use = assertIs<HirUseDecl>(m.declarations[0])
        assertEquals(listOf("std", "io"), use.path)
    }
}
