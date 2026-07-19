package hasab.compiler.backend.jvm

import hasab.compiler.types.*
import org.objectweb.asm.Opcodes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JvmTypeMapperTest {

    @Test
    fun `int maps to I descriptor`() {
        assertEquals("I", JvmTypeMapper.descriptor(IntType))
    }

    @Test
    fun `float maps to F descriptor`() {
        assertEquals("F", JvmTypeMapper.descriptor(FloatType))
    }

    @Test
    fun `bool maps to Z descriptor`() {
        assertEquals("Z", JvmTypeMapper.descriptor(BoolType))
    }

    @Test
    fun `char maps to C descriptor`() {
        assertEquals("C", JvmTypeMapper.descriptor(CharType))
    }

    @Test
    fun `string maps to L descriptor`() {
        assertEquals("Ljava/lang/String;", JvmTypeMapper.descriptor(StringType))
    }

    @Test
    fun `void maps to V descriptor`() {
        assertEquals("V", JvmTypeMapper.descriptor(VoidType))
    }

    @Test
    fun `array type maps correctly`() {
        assertEquals("[I", JvmTypeMapper.descriptor(ArrayType(IntType)))
    }

    @Test
    fun `struct type maps correctly`() {
        val struct = StructType("Point", listOf(
            StructTypeField("x", IntType, false),
            StructTypeField("y", IntType, false),
        ))
        assertEquals("LPoint;", JvmTypeMapper.descriptor(struct))
    }

    @Test
    fun `optional type maps to Object`() {
        assertEquals("Ljava/lang/Object;", JvmTypeMapper.descriptor(OptionalType(IntType)))
    }

    @Test
    fun `pointer type maps to Object`() {
        assertEquals("Ljava/lang/Object;", JvmTypeMapper.descriptor(PointerType(IntType)))
    }

    @Test
    fun `int is primitive`() {
        assertTrue(JvmTypeMapper.isPrimitive(IntType))
    }

    @Test
    fun `float is primitive`() {
        assertTrue(JvmTypeMapper.isPrimitive(FloatType))
    }

    @Test
    fun `bool is primitive`() {
        assertTrue(JvmTypeMapper.isPrimitive(BoolType))
    }

    @Test
    fun `string is not primitive`() {
        assertEquals(false, JvmTypeMapper.isPrimitive(StringType))
    }

    @Test
    fun `int load opcode is ILOAD`() {
        assertEquals(Opcodes.ILOAD, JvmTypeMapper.loadOpcode(IntType))
    }

    @Test
    fun `float load opcode is FLOAD`() {
        assertEquals(Opcodes.FLOAD, JvmTypeMapper.loadOpcode(FloatType))
    }

    @Test
    fun `string load opcode is ALOAD`() {
        assertEquals(Opcodes.ALOAD, JvmTypeMapper.loadOpcode(StringType))
    }

    @Test
    fun `bool load opcode is ILOAD`() {
        assertEquals(Opcodes.ILOAD, JvmTypeMapper.loadOpcode(BoolType))
    }

    @Test
    fun `char load opcode is ILOAD`() {
        assertEquals(Opcodes.ILOAD, JvmTypeMapper.loadOpcode(CharType))
    }

    @Test
    fun `int return opcode is IRETURN`() {
        assertEquals(Opcodes.IRETURN, JvmTypeMapper.returnOpcode(IntType))
    }

    @Test
    fun `float return opcode is FRETURN`() {
        assertEquals(Opcodes.FRETURN, JvmTypeMapper.returnOpcode(FloatType))
    }

    @Test
    fun `void return opcode is RETURN`() {
        assertEquals(Opcodes.RETURN, JvmTypeMapper.returnOpcode(VoidType))
    }

    @Test
    fun `string return opcode is ARETURN`() {
        assertEquals(Opcodes.ARETURN, JvmTypeMapper.returnOpcode(StringType))
    }

    @Test
    fun `int store opcode is ISTORE`() {
        assertEquals(Opcodes.ISTORE, JvmTypeMapper.storeOpcode(IntType))
    }

    @Test
    fun `float store opcode is FSTORE`() {
        assertEquals(Opcodes.FSTORE, JvmTypeMapper.storeOpcode(FloatType))
    }

    @Test
    fun `string store opcode is ASTORE`() {
        assertEquals(Opcodes.ASTORE, JvmTypeMapper.storeOpcode(StringType))
    }

    @Test
    fun `nested array type maps correctly`() {
        assertEquals("[[I", JvmTypeMapper.descriptor(ArrayType(ArrayType(IntType))))
    }

    @Test
    fun `float is floating point`() {
        assertEquals(true, JvmTypeMapper.isFloatingPoint(FloatType))
    }

    @Test
    fun `int is not floating point`() {
        assertEquals(false, JvmTypeMapper.isFloatingPoint(IntType))
    }

    @Test
    fun `enum type maps to L descriptor`() {
        val enum = EnumType("Color", listOf(
            EnumTypeVariant("Red", emptyList()),
            EnumTypeVariant("Green", emptyList()),
        ))
        assertEquals("LColor;", JvmTypeMapper.descriptor(enum))
    }

    @Test
    fun `function type maps to Object`() {
        val fnType = FunctionType(listOf(IntType), StringType)
        assertEquals("Ljava/lang/Object;", JvmTypeMapper.descriptor(fnType))
    }

    @Test
    fun `lambda type maps to Object`() {
        val lambdaType = LambdaType(listOf(IntType, FloatType), BoolType)
        assertEquals("Ljava/lang/Object;", JvmTypeMapper.descriptor(lambdaType))
    }

    @Test
    fun `internal name for struct`() {
        val struct = StructType("Point", listOf(
            StructTypeField("x", IntType, false),
        ))
        assertEquals("Point", JvmTypeMapper.internalName(struct))
    }

    @Test
    fun `internal name for enum`() {
        val enum = EnumType("Color", listOf(
            EnumTypeVariant("Red", emptyList()),
        ))
        assertEquals("Color", JvmTypeMapper.internalName(enum))
    }
}
