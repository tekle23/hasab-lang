package hasab.compiler.backend.jvm

import hasab.compiler.hir.cfg.Register
import hasab.compiler.types.*
import org.objectweb.asm.Opcodes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StackFrameAnalyzerTest {

    private val mapper = JvmTypeMapper
    private val analyzer = StackFrameAnalyzer(mapper)

    @Test
    fun `int frame type is INTEGER`() {
        assertEquals(Opcodes.INTEGER, analyzer.frameType(IntType))
    }

    @Test
    fun `float frame type is FLOAT`() {
        assertEquals(Opcodes.FLOAT, analyzer.frameType(FloatType))
    }

    @Test
    fun `bool frame type is INTEGER`() {
        assertEquals(Opcodes.INTEGER, analyzer.frameType(BoolType))
    }

    @Test
    fun `char frame type is INTEGER`() {
        assertEquals(Opcodes.INTEGER, analyzer.frameType(CharType))
    }

    @Test
    fun `string frame type is class name`() {
        assertEquals("java/lang/String", analyzer.frameType(StringType))
    }

    @Test
    fun `void frame type throws`() {
        assertFailsWith<IllegalArgumentException> {
            analyzer.frameType(VoidType)
        }
    }

    @Test
    fun `entry frame for static method with no params`() {
        val frame = analyzer.entryFrame(emptyList(), isStatic = true)
        assertEquals(0, frame.size)
    }

    @Test
    fun `entry frame for instance method with no params`() {
        val frame = analyzer.entryFrame(emptyList(), isStatic = false)
        assertEquals(1, frame.size)
        assertEquals("this", frame[0])
    }

    @Test
    fun `entry frame for instance method with params`() {
        val params = listOf("x" to IntType, "y" to FloatType)
        val frame = analyzer.entryFrame(params, isStatic = false)
        assertEquals(3, frame.size)
        assertEquals("this", frame[0])
        assertEquals(Opcodes.INTEGER, frame[1])
        assertEquals(Opcodes.FLOAT, frame[2])
    }

    @Test
    fun `entry frame for static method with params`() {
        val params = listOf("x" to IntType, "s" to StringType)
        val frame = analyzer.entryFrame(params, isStatic = true)
        assertEquals(2, frame.size)
        assertEquals(Opcodes.INTEGER, frame[0])
        assertEquals("java/lang/String", frame[1])
    }

    @Test
    fun `register frame type delegates`() {
        val reg = Register("r", IntType)
        assertEquals(Opcodes.INTEGER, analyzer.registerFrameType(reg))
    }

    @Test
    fun `register frame type for float`() {
        val reg = Register("r", FloatType)
        assertEquals(Opcodes.FLOAT, analyzer.registerFrameType(reg))
    }

    @Test
    fun `register frame type for string`() {
        val reg = Register("r", StringType)
        assertEquals("java/lang/String", analyzer.registerFrameType(reg))
    }

    @Test
    fun `struct frame type is internal name`() {
        val struct = StructType("Point", listOf(
            StructTypeField("x", IntType, false),
        ))
        assertEquals("Point", analyzer.frameType(struct))
    }

    @Test
    fun `optional frame type is java lang object`() {
        assertEquals("java/lang/Object", analyzer.frameType(OptionalType(IntType)))
    }

    @Test
    fun `pointer frame type is java lang object`() {
        assertEquals("java/lang/Object", analyzer.frameType(PointerType(IntType)))
    }

    @Test
    fun `array frame type is descriptor`() {
        val result = analyzer.frameType(ArrayType(IntType))
        assertEquals("[I", result)
    }

    @Test
    fun `nil literal frame type is NULL`() {
        assertEquals(Opcodes.NULL, analyzer.frameType(NilLiteralType))
    }

    @Test
    fun `localsArraySize for static with params`() {
        val params = listOf("x" to IntType, "y" to FloatType)
        assertEquals(2, analyzer.localsArraySize(params, isStatic = true))
    }

    @Test
    fun `localsArraySize for instance with params`() {
        val params = listOf("x" to IntType)
        assertEquals(2, analyzer.localsArraySize(params, isStatic = false))
    }

    @Test
    fun `localsArraySize for static no params`() {
        assertEquals(0, analyzer.localsArraySize(emptyList(), isStatic = true))
    }

    @Test
    fun `localsArraySize for instance no params`() {
        assertEquals(1, analyzer.localsArraySize(emptyList(), isStatic = false))
    }

    @Test
    fun `registerSlotSize is always 1`() {
        assertEquals(1, analyzer.registerSlotSize(IntType))
        assertEquals(1, analyzer.registerSlotSize(StringType))
        assertEquals(1, analyzer.registerSlotSize(FloatType))
    }
}
