package hasab.compiler.hir

import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class HirValidatorTest {

    private val validator = HirValidator()

    private fun validAddModule(): HirModule {
        return HirModule("test", listOf(
            HirFnDecl(
                name = "add",
                parameters = listOf(HirParam("x", IntType, false), HirParam("y", IntType, false)),
                type = FunctionType(listOf(IntType, IntType), IntType),
                returnType = IntType,
                body = HirBlock(listOf(
                    HirReturnStmt(
                        HirBinary(HirIdentifier("x", IntType), "+", HirIdentifier("y", IntType), IntType),
                        IntType,
                    ),
                )),
                isPublic = false,
            ),
        ))
    }

    @Test
    fun `valid function has no errors`() {
        val errors = validator.validate(validAddModule())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `function with unknown return type reports error`() {
        val module = HirModule("test", listOf(
            HirFnDecl(
                name = "bad",
                parameters = emptyList(),
                type = FunctionType(emptyList(), UnknownType),
                returnType = UnknownType,
                body = null,
                isPublic = false,
            ),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.any { it.code == "HIR003" })
    }

    @Test
    fun `function with void parameter reports error`() {
        val module = HirModule("test", listOf(
            HirFnDecl(
                name = "bad",
                parameters = listOf(HirParam("x", VoidType, false)),
                type = FunctionType(listOf(VoidType), VoidType),
                returnType = VoidType,
                body = null,
                isPublic = false,
            ),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.any { it.code == "HIR002" })
    }

    @Test
    fun `struct with unknown field type reports error`() {
        val module = HirModule("test", listOf(
            HirStructDecl("Bad", listOf(HirField("x", UnknownType, false)), false),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.any { it.code == "HIR010" })
    }

    @Test
    fun `valid struct has no errors`() {
        val module = HirModule("test", listOf(
            HirStructDecl("Point", listOf(
                HirField("x", IntType, false),
                HirField("y", IntType, false),
            ), false),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `impl with valid methods has no errors`() {
        val module = HirModule("test", listOf(
            HirStructDecl("Counter", listOf(HirField("value", IntType, true)), false),
            HirImplDecl(
                targetType = StructType("Counter", listOf(StructTypeField("value", IntType, true))),
                methods = listOf(
                    HirFnDecl(
                        name = "increment",
                        parameters = listOf(HirParam("self", StructType("Counter", listOf(StructTypeField("value", IntType, true))), false)),
                        type = FunctionType(listOf(StructType("Counter", listOf(StructTypeField("value", IntType, true)))), VoidType),
                        returnType = VoidType,
                        body = HirBlock(emptyList()),
                        isPublic = false,
                    ),
                ),
            ),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `enum with unknown variant field reports error`() {
        val module = HirModule("test", listOf(
            HirEnumDecl("Bad", listOf(
                HirEnumVariant("Ok", listOf(UnknownType)),
            ), false),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.any { it.code == "HIR011" })
    }

    @Test
    fun `type alias with unknown target reports error`() {
        val module = HirModule("test", listOf(
            HirTypeAliasDecl("MyType", UnknownType, false),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.any { it.code == "HIR012" })
    }

    @Test
    fun `multiple errors collected`() {
        val module = HirModule("test", listOf(
            HirFnDecl("a", listOf(HirParam("x", VoidType, false)), FunctionType(listOf(VoidType), UnknownType), UnknownType, null, false),
            HirStructDecl("B", listOf(HirField("f", UnknownType, false)), false),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.size >= 3)
    }

    @Test
    fun `binary with unknown type reports error`() {
        val module = HirModule("test", listOf(
            HirFnDecl("fn", emptyList(), FunctionType(emptyList(), VoidType), VoidType,
                HirBlock(listOf(
                    HirExprStmt(HirBinary(HirIdentifier("x", UnknownType), "+", HirIdentifier("y", UnknownType), UnknownType)),
                )),
                false,
            ),
        ))
        val errors = validator.validate(module)
        assertTrue(errors.any { it.code == "HIR006" })
    }
}
