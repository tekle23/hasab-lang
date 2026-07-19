package hasab.compiler.types

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ---- TypedSemanticModel Tests ----

class TypedSemanticModelTest {

    @Test
    fun `empty model has no diagnostics`() {
        val model = TypedSemanticModel.empty()
        assertFalse(model.hasErrors)
        assertEquals(0, model.diagnostics.size)
    }

    @Test
    fun `typeOf returns null for unknown node`() {
        val model = TypedSemanticModel.empty()
        val node = IntegerLiteralExpr("42", "test.hb", 1, 1, 0, 2)
        assertNull(model.typeOf(node))
    }

    @Test
    fun `typeOrDefault returns UnknownType for unknown node`() {
        val model = TypedSemanticModel.empty()
        val node = IntegerLiteralExpr("42", "test.hb", 1, 1, 0, 2)
        assertEquals(UnknownType, model.typeOrDefault(node))
    }

    @Test
    fun `isMutableVariable tracks mutable vars`() {
        val model = TypedSemanticModel.empty()
        model.addMutableVar("x")
        assertTrue(model.isMutableVariable("x"))
        assertFalse(model.isMutableVariable("y"))
    }

    @Test
    fun `functionOverloads tracks overloads`() {
        val model = TypedSemanticModel.empty()
        model.addFunctionOverload("add", FunctionType(listOf(IntType, IntType), IntType))
        model.addFunctionOverload("add", FunctionType(listOf(FloatType, FloatType), FloatType))
        assertEquals(2, model.functionOverloads("add").size)
        assertEquals(0, model.functionOverloads("other").size)
    }

    @Test
    fun `snapshot produces independent copy`() {
        val model = TypedSemanticModel.empty()
        model.addMutableVar("x")
        val snapshot = model.snapshot()
        model.addMutableVar("y")
        assertTrue(model.isMutableVariable("y"))
        assertFalse(snapshot.isMutableVariable("y"))
    }
}

// ---- TypeResolver Tests ----

class TypeResolverTest {

    private val env = TypeEnvironment.root().define("Point", StructType("Point", listOf(
        StructTypeField("x", IntType, false),
        StructTypeField("y", IntType, false),
    )))

    @Test
    fun `resolve identifier type`() {
        val node = IdentifierType("int", "test.hb", 1, 1, 0, 3)
        assertEquals(IntType, TypeResolver.resolve(node, env))
    }

    @Test
    fun `resolve user-defined type`() {
        val node = IdentifierType("Point", "test.hb", 1, 1, 0, 5)
        val result = TypeResolver.resolve(node, env)
        assertIs<StructType>(result)
        assertEquals("Point", (result as StructType).name)
    }

    @Test
    fun `resolve unknown type returns UnknownType`() {
        val node = IdentifierType("Foo", "test.hb", 1, 1, 0, 3)
        assertEquals(UnknownType, TypeResolver.resolve(node, env))
    }

    @Test
    fun `resolve unknown type calls onUndefined`() {
        val node = IdentifierType("Foo", "test.hb", 1, 1, 0, 3)
        var reported: String? = null
        TypeResolver.resolve(node, env) { reported = it }
        assertEquals("Foo", reported)
    }

    @Test
    fun `resolve array type`() {
        val node = ArrayType(IdentifierType("int", "test.hb", 1, 1, 0, 3), "test.hb", 1, 1, 0, 7)
        val result = TypeResolver.resolve(node, env)
        assertIs<ArrayType>(result)
        assertEquals(IntType, (result as ArrayType).elementType)
    }

    @Test
    fun `resolve optional type`() {
        val node = OptionalType(IdentifierType("int", "test.hb", 1, 1, 0, 3), "test.hb", 1, 1, 0, 4)
        val result = TypeResolver.resolve(node, env)
        assertIs<OptionalType>(result)
        assertEquals(IntType, (result as OptionalType).elementType)
    }

    @Test
    fun `resolve pointer type`() {
        val node = PointerType(IdentifierType("int", "test.hb", 1, 1, 0, 3), "test.hb", 1, 1, 0, 4)
        val result = TypeResolver.resolve(node, env)
        assertIs<PointerType>(result)
    }

    @Test
    fun `resolve function type`() {
        val node = FunctionType(
            listOf(IdentifierType("int", "test.hb", 1, 1, 0, 3)),
            IdentifierType("int", "test.hb", 1, 1, 4, 7),
            "test.hb", 1, 1, 0, 7,
        )
        val result = TypeResolver.resolve(node, env)
        assertIs<hasab.compiler.types.FunctionType>(result)
        assertEquals(1, (result as hasab.compiler.types.FunctionType).parameterTypes.size)
    }

    @Test
    fun `resolve void type`() {
        val node = VoidType("test.hb", 1, 1, 0, 4)
        assertEquals(VoidType, TypeResolver.resolve(node, env))
    }
}

// ---- NumericPromotionRules Tests ----

class NumericPromotionRulesTest {

    @Test
    fun `same types are always compatible`() {
        assertTrue(NumericPromotionRules.canPromote(IntType, IntType))
        assertTrue(NumericPromotionRules.canPromote(FloatType, FloatType))
        assertTrue(NumericPromotionRules.canPromote(StringType, StringType))
    }

    @Test
    fun `int promotes to float`() {
        assertTrue(NumericPromotionRules.canPromote(IntType, FloatType))
        assertEquals(FloatType, NumericPromotionRules.promote(IntType, FloatType))
    }

    @Test
    fun `float does not promote to int`() {
        assertFalse(NumericPromotionRules.canPromote(FloatType, IntType))
    }

    @Test
    fun `int and string are incompatible for arithmetic`() {
        assertNull(NumericPromotionRules.binaryResultType("+", IntType, StringType))
    }

    @Test
    fun `string concatenation`() {
        assertEquals(StringType, NumericPromotionRules.binaryResultType("+", StringType, StringType))
    }

    @Test
    fun `comparison operators`() {
        assertEquals(BoolType, NumericPromotionRules.binaryResultType("<", IntType, IntType))
        assertEquals(BoolType, NumericPromotionRules.binaryResultType("==", IntType, IntType))
        assertNull(NumericPromotionRules.binaryResultType("<", IntType, StringType))
    }

    @Test
    fun `logical operators require bool`() {
        assertEquals(BoolType, NumericPromotionRules.binaryResultType("&&", BoolType, BoolType))
        assertNull(NumericPromotionRules.binaryResultType("&&", IntType, BoolType))
    }

    @Test
    fun `bitwise operators require int`() {
        assertEquals(IntType, NumericPromotionRules.binaryResultType("&", IntType, IntType))
        assertNull(NumericPromotionRules.binaryResultType("&", IntType, StringType))
    }

    @Test
    fun `unary minus on int`() {
        assertEquals(IntType, NumericPromotionRules.unaryResultType("-", IntType))
    }

    @Test
    fun `unary not on bool`() {
        assertEquals(BoolType, NumericPromotionRules.unaryResultType("!", BoolType))
    }

    @Test
    fun `unary not on int is invalid`() {
        assertNull(NumericPromotionRules.unaryResultType("!", IntType))
    }

    @Test
    fun `dereference pointer`() {
        val ptrType = PointerType(IntType)
        assertEquals(IntType, NumericPromotionRules.unaryResultType("*", ptrType))
    }

    @Test
    fun `address-of`() {
        val result = NumericPromotionRules.unaryResultType("&", IntType)
        assertIs<PointerType>(result)
    }

    @Test
    fun `common branch type with compatible types`() {
        assertEquals(IntType, NumericPromotionRules.commonBranchType(IntType, IntType))
        assertEquals(FloatType, NumericPromotionRules.commonBranchType(IntType, FloatType))
        assertEquals(FloatType, NumericPromotionRules.commonBranchType(FloatType, IntType))
    }

    @Test
    fun `are comparable`() {
        assertTrue(NumericPromotionRules.areComparable(IntType, IntType))
        assertTrue(NumericPromotionRules.areComparable(IntType, FloatType))
        assertFalse(NumericPromotionRules.areComparable(IntType, StringType))
    }
}

// ---- FunctionCallChecker Tests ----

class FunctionCallCheckerTest {

    private fun makeCall(calleeName: String, argCount: Int): CallExpr {
        val args = (0 until argCount).map { i ->
            IntegerLiteralExpr("$i", "test.hb", 1, 10 + i * 4, 10 + i * 4, 11 + i * 4)
        }
        return CallExpr(
            callee = IdentifierExpr(calleeName, "test.hb", 1, 1, 0, calleeName.length),
            arguments = args,
            fileName = "test.hb", line = 1, column = 1,
            startOffset = 0, endOffset = calleeName.length + 2 + argCount * 4,
        )
    }

    @Test
    fun `callable function returns its return type`() {
        val fnType = FunctionType(listOf(IntType, IntType), IntType)
        val diagnostics = DiagnosticCollector()
        val call = makeCall("add", 2)
        val result = FunctionCallChecker.checkCall(call, fnType, diagnostics)
        assertEquals(IntType, result)
        assertFalse(diagnostics.hasErrors())
    }

    @Test
    fun `wrong argument count reports error`() {
        val fnType = FunctionType(listOf(IntType, IntType), IntType)
        val diagnostics = DiagnosticCollector()
        val call = makeCall("add", 1)
        FunctionCallChecker.checkCall(call, fnType, diagnostics)
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `non-callable type reports error`() {
        val diagnostics = DiagnosticCollector()
        val call = makeCall("x", 0)
        val result = FunctionCallChecker.checkCall(call, IntType, diagnostics)
        assertEquals(UnknownType, result)
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `unknown callee type returns unknown`() {
        val diagnostics = DiagnosticCollector()
        val call = makeCall("x", 0)
        val result = FunctionCallChecker.checkCall(call, UnknownType, diagnostics)
        assertEquals(UnknownType, result)
        assertFalse(diagnostics.hasErrors())
    }
}

// ---- FieldAccessChecker Tests ----

class FieldAccessCheckerTest {

    private fun makeFieldAccess(fieldName: String): FieldAccessExpr {
        return FieldAccessExpr(
            callee = IdentifierExpr("p", "test.hb", 1, 1, 0, 1),
            fieldName = fieldName,
            fileName = "test.hb", line = 1, column = 3,
            startOffset = 0, endOffset = 3 + fieldName.length,
        )
    }

    private val pointType = StructType("Point", listOf(
        StructTypeField("x", IntType, false),
        StructTypeField("y", IntType, false),
    ))

    @Test
    fun `access valid field`() {
        val diagnostics = DiagnosticCollector()
        val expr = makeFieldAccess("x")
        val result = FieldAccessChecker.checkFieldAccess(expr, pointType, diagnostics)
        assertEquals(IntType, result)
        assertFalse(diagnostics.hasErrors())
    }

    @Test
    fun `access invalid field reports error`() {
        val diagnostics = DiagnosticCollector()
        val expr = makeFieldAccess("z")
        val result = FieldAccessChecker.checkFieldAccess(expr, pointType, diagnostics)
        assertEquals(UnknownType, result)
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `access on non-struct type reports error`() {
        val diagnostics = DiagnosticCollector()
        val expr = makeFieldAccess("x")
        val result = FieldAccessChecker.checkFieldAccess(expr, IntType, diagnostics)
        assertEquals(UnknownType, result)
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `access on pointer to struct`() {
        val diagnostics = DiagnosticCollector()
        val expr = makeFieldAccess("x")
        val result = FieldAccessChecker.checkFieldAccess(expr, PointerType(pointType), diagnostics)
        assertEquals(IntType, result)
        assertFalse(diagnostics.hasErrors())
    }

    @Test
    fun `access enum variant`() {
        val enumType = EnumType("Color", listOf(
            EnumTypeVariant("Red", emptyList()),
        ))
        val diagnostics = DiagnosticCollector()
        val expr = makeFieldAccess("Red")
        val result = FieldAccessChecker.checkFieldAccess(expr, enumType, diagnostics)
        assertIs<FunctionType>(result)
        assertFalse(diagnostics.hasErrors())
    }
}

// ---- OverloadResolver Tests ----

class OverloadResolverTest {

    @Test
    fun `single exact match`() {
        val overloads = listOf(
            FunctionType(listOf(IntType, IntType), IntType),
        )
        val result = OverloadResolver.resolve(overloads, listOf(IntType, IntType))
        assertIs<OverloadResolver.ResolutionResult.SingleMatch>(result)
    }

    @Test
    fun `no match when arg count differs`() {
        val overloads = listOf(
            FunctionType(listOf(IntType, IntType), IntType),
        )
        val result = OverloadResolver.resolve(overloads, listOf(IntType))
        assertIs<OverloadResolver.ResolutionResult.NoMatch>(result)
    }

    @Test
    fun `promotion match selects float overload`() {
        val overloads = listOf(
            FunctionType(listOf(FloatType, FloatType), FloatType),
        )
        val result = OverloadResolver.resolve(overloads, listOf(IntType, IntType))
        assertIs<OverloadResolver.ResolutionResult.SingleMatch>(result)
    }

    @Test
    fun `empty overloads is no match`() {
        val result = OverloadResolver.resolve(emptyList(), listOf(IntType))
        assertIs<OverloadResolver.ResolutionResult.NoMatch>(result)
    }

    @Test
    fun `exact match preferred over promotion match`() {
        val overloads = listOf(
            FunctionType(listOf(IntType, IntType), IntType),
            FunctionType(listOf(FloatType, FloatType), FloatType),
        )
        val result = OverloadResolver.resolve(overloads, listOf(IntType, IntType))
        assertIs<OverloadResolver.ResolutionResult.SingleMatch>(result)
        assertEquals(IntType, (result as OverloadResolver.ResolutionResult.SingleMatch).function.returnType)
    }

    @Test
    fun `ambiguous when only promotion matches`() {
        val overloads = listOf(
            FunctionType(listOf(FloatType, FloatType), FloatType),
            FunctionType(listOf(FloatType, FloatType), FloatType),
        )
        val result = OverloadResolver.resolve(overloads, listOf(IntType, IntType))
        assertIs<OverloadResolver.ResolutionResult.Ambiguous>(result)
    }
}

// ---- GenericTypeChecker Tests ----

class GenericTypeCheckerTest {

    @Test
    fun `fresh type variable has unique id`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val v1 = checker.freshTypeVariable()
        val v2 = checker.freshTypeVariable()
        assertTrue(v1.id != v2.id)
    }

    @Test
    fun `substitute function type with concrete types`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val tVar = TypeVariable(0, "T")
        val fnType = FunctionType(listOf(tVar), tVar)
        val result = checker.substituteFunctionType(fnType, listOf(IntType))
        assertIs<GenericTypeChecker.SubstitutionResult.Success>(result)
        val resolved = (result as GenericTypeChecker.SubstitutionResult.Success).resolvedType
        assertIs<FunctionType>(resolved)
        assertEquals(IntType, resolved.parameterTypes[0])
        assertEquals(IntType, resolved.returnType)
    }

    @Test
    fun `substitute fails on type mismatch`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val fnType = FunctionType(listOf(TypeVariable(0, "T"), TypeVariable(0, "T")), TypeVariable(0, "T"))
        val result = checker.substituteFunctionType(fnType, listOf(IntType, FloatType))
        assertIs<GenericTypeChecker.SubstitutionResult.Failure>(result)
    }

    @Test
    fun `substitute handles array types`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val fnType = FunctionType(listOf(ArrayType(TypeVariable(0, "T"))), TypeVariable(0, "T"))
        val result = checker.substituteFunctionType(fnType, listOf(ArrayType(IntType)))
        assertIs<GenericTypeChecker.SubstitutionResult.Success>(result)
    }

    @Test
    fun `argument count mismatch fails`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val fnType = FunctionType(listOf(TypeVariable(0, "T")), TypeVariable(0, "T"))
        val result = checker.substituteFunctionType(fnType, listOf(IntType, FloatType))
        assertIs<GenericTypeChecker.SubstitutionResult.Failure>(result)
    }

    @Test
    fun `validate bounds passes when no bounds`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val tv = TypeVariable(0, "T", bounds = emptyList())
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 10, 10))
        assertTrue(checker.validateBounds(tv, IntType, range, "test.hb"))
    }

    @Test
    fun `validate bounds passes when struct satisfies trait bound`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val trait = TraitType("Printable", listOf(
            TraitTypeMethod("print", listOf(StringType), VoidType),
        ))
        val tv = TypeVariable(0, "T", bounds = listOf(trait))
        val struct = StructType("MyStruct", listOf(
            StructTypeField("print", FunctionType(listOf(StringType), VoidType), false),
        ))
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 10, 10))
        assertTrue(checker.validateBounds(tv, struct, range, "test.hb"))
    }

    @Test
    fun `validate bounds fails when struct missing required method`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val trait = TraitType("Printable", listOf(
            TraitTypeMethod("print", listOf(StringType), VoidType),
            TraitTypeMethod("format", listOf(), StringType),
        ))
        val tv = TypeVariable(0, "T", bounds = listOf(trait))
        val struct = StructType("MyStruct", listOf(
            StructTypeField("print", FunctionType(listOf(StringType), VoidType), false),
        ))
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 10, 10))
        assertFalse(checker.validateBounds(tv, struct, range, "test.hb"))
    }

    @Test
    fun `validate bounds passes for UnknownType`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val trait = TraitType("Printable", listOf(
            TraitTypeMethod("print", listOf(StringType), VoidType),
        ))
        val tv = TypeVariable(0, "T", bounds = listOf(trait))
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 10, 10))
        assertTrue(checker.validateBounds(tv, UnknownType, range, "test.hb"))
    }

    @Test
    fun `fresh type variable has correct bounds`() {
        val checker = GenericTypeChecker(DiagnosticCollector())
        val trait = TraitType("Comparable", listOf(
            TraitTypeMethod("compare", listOf(), IntType),
        ))
        val tv = checker.freshTypeVariable("T", listOf(trait))
        assertEquals(1, tv.bounds.size)
        assertIs<TraitType>(tv.bounds[0])
    }
}

// ---- NullSafetyChecker Tests ----

class NullSafetyCheckerTest {

    @Test
    fun `nil assigned to non-optional type reports error`() {
        val diagnostics = DiagnosticCollector()
        val nilExpr = NilLiteralExpr("test.hb", 1, 1, 0, 3)
        NullSafetyChecker.checkNilAssignment(nilExpr, IntType, diagnostics)
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `nil assigned to optional type is ok`() {
        val diagnostics = DiagnosticCollector()
        val nilExpr = NilLiteralExpr("test.hb", 1, 1, 0, 3)
        NullSafetyChecker.checkNilAssignment(nilExpr, OptionalType(IntType), diagnostics)
        assertFalse(diagnostics.hasErrors())
    }

    @Test
    fun `bool condition is ok`() {
        val diagnostics = DiagnosticCollector()
        NullSafetyChecker.checkCondition(
            BoolLiteralExpr(true, "test.hb", 1, 1, 0, 4),
            BoolType, diagnostics
        )
        assertFalse(diagnostics.hasErrors())
    }

    @Test
    fun `int condition reports error`() {
        val diagnostics = DiagnosticCollector()
        NullSafetyChecker.checkCondition(
            IntegerLiteralExpr("42", "test.hb", 1, 1, 0, 2),
            IntType, diagnostics
        )
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `nil condition reports error`() {
        val diagnostics = DiagnosticCollector()
        NullSafetyChecker.checkCondition(
            NilLiteralExpr("test.hb", 1, 1, 0, 3),
            NilLiteralType, diagnostics
        )
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `nil without type annotation reports error`() {
        val diagnostics = DiagnosticCollector()
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 1, 10))
        NullSafetyChecker.checkNilWithoutAnnotation(NilLiteralType, false, range, "test.hb", diagnostics)
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `nil with type annotation is ok`() {
        val diagnostics = DiagnosticCollector()
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 1, 10))
        NullSafetyChecker.checkNilWithoutAnnotation(NilLiteralType, true, range, "test.hb", diagnostics)
        assertFalse(diagnostics.hasErrors())
    }

    @Test
    fun `return type mismatch reports error`() {
        val diagnostics = DiagnosticCollector()
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 1, 10))
        NullSafetyChecker.checkReturnType(StringType, IntType, range, "test.hb", diagnostics)
        assertTrue(diagnostics.hasErrors())
    }

    @Test
    fun `nil return for optional type is ok`() {
        val diagnostics = DiagnosticCollector()
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 1, 10))
        NullSafetyChecker.checkReturnType(NilLiteralType, OptionalType(IntType), range, "test.hb", diagnostics)
        assertFalse(diagnostics.hasErrors())
    }
}

// ---- TypeCheckerEngine Integration Tests ----

class TypeCheckerEngineTest {

    private fun check(code: String): TypeCheckResult {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        return TypeChecker().check(parseResult.module)
    }

    @Test
    fun `typed model has type bindings for expressions`() {
        val r = check("fn main() { let x = 42; }")
        assertFalse(r.hasErrors)
        val fn = r.typedModel.environment.lookup("main")
        assertNotNull(fn)
    }

    @Test
    fun `typed model tracks mutability`() {
        val r = check("fn main() { mut x = 1; x = 2; }")
        assertFalse(r.hasErrors)
        assertTrue(r.typedModel.isMutableVariable("x"))
    }

    @Test
    fun `immutable variable reassignment is error`() {
        val r = check("fn main() { let x = 1; x = 2; }")
        assertTrue(r.hasErrors)
    }

    @Test
    fun `struct type resolves correctly`() {
        val r = check("struct Point { x: int, y: int }")
        val pointType = r.typedModel.environment.lookup("Point")
        assertIs<StructType>(pointType)
        assertEquals("Point", (pointType as StructType).name)
        assertEquals(2, pointType.fields.size)
    }

    @Test
    fun `function return type is correct`() {
        val r = check("fn add(x: int, y: int) -> int { return x + y; }")
        assertFalse(r.hasErrors)
        val fnType = r.typedModel.environment.lookup("add")
        assertIs<FunctionType>(fnType)
        assertEquals(IntType, (fnType as FunctionType).returnType)
    }

    @Test
    fun `expression types recorded in model`() {
        val r = check("fn main() { let x = 42; }")
        assertFalse(r.hasErrors)
    }

    @Test
    fun `complex program type checks`() {
        val r = check("""
            struct Point { x: int, y: int }
            fn distance(p: Point) -> int { return p.x + p.y; }
            fn main() { let d = distance; }
        """.trimIndent())
        assertFalse(r.hasErrors)
    }

    @Test
    fun `multiple errors collected`() {
        val r = check("fn main() { let x = y; let z = w; }")
        assertTrue(r.errors.size >= 2)
    }
}

// ---- TypeDiagnostic Bilingual Output Tests ----

class TypeDiagnosticBilingualTest {

    private fun makeDiagnostic(
        code: TypeDiagnosticCode,
        message: String,
        expectedType: Type? = null,
        foundType: Type? = null,
        suggestion: String? = null,
        hint: String? = null,
        didYouMean: String? = null,
    ): TypeDiagnostic {
        val range = SourceRange(SourcePosition(3, 5, 10), SourcePosition(3, 15, 20))
        return TypeDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = range,
            fileName = "test.hasab",
            expectedType = expectedType,
            foundType = foundType,
            suggestion = suggestion,
            hint = hint,
            didYouMean = didYouMean,
        )
    }

    @Test
    fun `format English includes code and message`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.TYPE_MISMATCH,
            "Cannot assign 'string' to 'int'",
            expectedType = IntType,
            foundType = StringType,
        )
        val output = d.format()
        assertTrue(output.contains("error[HSB3001]"))
        assertTrue(output.contains("Cannot assign 'string' to 'int'"))
        assertTrue(output.contains("expected: int"))
        assertTrue(output.contains("found:    string"))
    }

    @Test
    fun `format English includes suggestion from code default`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.TYPE_MISMATCH,
            "Type mismatch",
        )
        val output = d.format()
        assertTrue(output.contains("suggestion: Change the variable type or convert the value to match"))
    }

    @Test
    fun `format English includes hint`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.MUTABILITY_VIOLATION,
            "Cannot modify immutable variable",
            hint = "Declare with 'mut' to allow reassignment",
        )
        val output = d.format()
        assertTrue(output.contains("hint: Declare with 'mut' to allow reassignment"))
    }

    @Test
    fun `format English includes did you mean`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.NO_SUCH_FIELD,
            "No such field on type",
            didYouMean = "name",
        )
        val output = d.format()
        assertTrue(output.contains("did you mean: 'name'?"))
    }

    @Test
    fun `format English includes source location`() {
        val d = makeDiagnostic(TypeDiagnosticCode.TYPE_MISMATCH, "Type mismatch")
        val output = d.format()
        assertTrue(output.contains("test.hasab:3:5"))
    }

    @Test
    fun `format Amharic includes amharic code prefix`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.CANNOT_ASSIGN_TYPE,
            "Cannot assign value of this type",
            expectedType = IntType,
            foundType = StringType,
        )
        val output = d.formatAmharic()
        assertTrue(output.contains("ስህተት[HSB3012]"))
        assertTrue(output.contains("በዚህ ዓይነት ዋጋ መመደብ አይቻልም"))
        assertTrue(output.contains("ይፈልጋል: int"))
        assertTrue(output.contains("ተገኝቷል: string"))
    }

    @Test
    fun `format Amharic includes suggestion in amharic`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.UNDEFINED_VARIABLE,
            "Undefined variable",
        )
        val output = d.formatAmharic()
        assertTrue(output.contains("ምክር:"))
        assertTrue(output.contains("ተ弃ኑን ከመጠቀም በፊት ይግለጹ"))
    }

    @Test
    fun `format Bilingual includes both languages`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.TYPE_MISMATCH,
            "Type mismatch",
            expectedType = IntType,
            foundType = StringType,
        )
        val output = d.formatBilingual()
        assertTrue(output.contains("en: Type mismatch"))
        assertTrue(output.contains("አማ: የዓይነት ልዩነት"))
        assertTrue(output.contains("expected: int"))
        assertTrue(output.contains("found:    string"))
    }

    @Test
    fun `format Bilingual includes did you mean`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.NO_SUCH_FIELD,
            "No such field",
            didYouMean = "x",
        )
        val output = d.formatBilingual()
        assertTrue(output.contains("did you mean: 'x'?"))
    }

    @Test
    fun `custom suggestion overrides default`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.ARGUMENT_TYPE_MISMATCH,
            "Argument type mismatch",
            expectedType = IntType,
            foundType = StringType,
            suggestion = "Pass an int value instead",
        )
        val output = d.format()
        assertTrue(output.contains("suggestion: Pass an int value instead"))
    }

    @Test
    fun `null foundType omits found line`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.NOT_CALLABLE,
            "Expression is not callable",
        )
        val output = d.format()
        assertFalse(output.contains("found:"))
    }

    @Test
    fun `warning severity uses warning prefix`() {
        val range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 5, 5))
        val d = TypeDiagnostic(
            code = TypeDiagnosticCode.SAFE_NAV_ON_NON_NULL,
            severity = DiagnosticSeverity.WARNING,
            message = "Safe navigation on non-nullable type",
            range = range,
            fileName = "test.hasab",
        )
        val enOutput = d.format()
        assertTrue(enOutput.contains("warning[HSB3023]"))
        val amOutput = d.formatAmharic()
        assertTrue(amOutput.contains("ማስጠንቀቂያ[HSB3023]"))
    }

    @Test
    fun `hint translation for mut`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.MUTABILITY_VIOLATION,
            "Cannot modify immutable variable",
            hint = "Declare with 'mut' to allow reassignment",
        )
        val output = d.formatAmharic()
        assertTrue(output.contains("'mut' በማድረግ ይቀይሩ"))
    }

    @Test
    fun `hint translation for type annotation`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.CANNOT_INFER,
            "Cannot infer type",
            hint = "Add a type annotation: 'let x: Type = nil'",
        )
        val output = d.formatAmharic()
        assertTrue(output.contains("ዓይነት ማብራሪያ ያክሉ"))
    }

    @Test
    fun `amharic suggestion for declare before use`() {
        val d = makeDiagnostic(
            TypeDiagnosticCode.UNDEFINED_VARIABLE,
            "Undefined variable",
            suggestion = "Declare the variable before use, or check for typos",
        )
        val output = d.formatAmharic()
        assertTrue(output.contains("ተ弃ኑን ከመጠቀም በፊት ይግለጹ"))
    }
}
