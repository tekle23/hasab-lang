package hasab.compiler.backend.jvm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BytecodeValidatorTest {

    private fun createValidClassBytes(): ByteArray {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null)
        cw.visitField(Opcodes.ACC_PUBLIC, "x", "I", null, null)
        val mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "main",
            "([Ljava/lang/String;)V",
            null,
            null,
        )
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()

        val init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        init.visitCode()
        init.visitVarInsn(Opcodes.ALOAD, 0)
        init.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false,
        )
        init.visitInsn(Opcodes.RETURN)
        init.visitMaxs(1, 1)
        init.visitEnd()

        cw.visitEnd()
        return cw.toByteArray()
    }

    @Test
    fun `valid class passes validation`() {
        val validator = BytecodeValidator()
        val bytes = createValidClassBytes()
        val result = validator.validate(bytes)
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validateOrThrow does not throw for valid`() {
        val validator = BytecodeValidator()
        val bytes = createValidClassBytes()
        validator.validateOrThrow(bytes)
    }

    @Test
    fun `invalid bytes fail validation`() {
        val validator = BytecodeValidator()
        val invalidBytes = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val result = validator.validate(invalidBytes)
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    fun `validateOrThrow throws for invalid`() {
        val validator = BytecodeValidator()
        val invalidBytes = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        assertFailsWith<BytecodeValidationException> {
            validator.validateOrThrow(invalidBytes)
        }
    }

    @Test
    fun `empty bytes fail validation`() {
        val validator = BytecodeValidator()
        val result = validator.validate(ByteArray(0))
        assertFalse(result.isValid)
    }

    @Test
    fun `validation error contains message`() {
        val validator = BytecodeValidator()
        val invalidBytes = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        val result = validator.validate(invalidBytes)
        assertFalse(result.isValid)
        assertTrue(result.errors[0].message.isNotEmpty())
    }

    @Test
    fun `class with method but no constructor validates`() {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null)
        val mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "main",
            "([Ljava/lang/String;)V",
            null,
            null,
        )
        mv.visitCode()
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
        cw.visitEnd()
        val bytes = cw.toByteArray()
        val validator = BytecodeValidator()
        val result = validator.validate(bytes)
        assertTrue(result.isValid)
    }

    @Test
    fun `class with multiple fields validates`() {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null)
        cw.visitField(Opcodes.ACC_PUBLIC, "a", "I", null, null)
        cw.visitField(Opcodes.ACC_PUBLIC, "b", "Ljava/lang/String;", null, null)
        cw.visitField(Opcodes.ACC_PUBLIC, "c", "Z", null, null)
        val init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        init.visitCode()
        init.visitVarInsn(Opcodes.ALOAD, 0)
        init.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false,
        )
        init.visitInsn(Opcodes.RETURN)
        init.visitMaxs(1, 1)
        init.visitEnd()
        cw.visitEnd()
        val bytes = cw.toByteArray()
        BytecodeValidator().validateOrThrow(bytes)
    }

    @Test
    fun `class with static initializer validates`() {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null)
        val clinit = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
        clinit.visitCode()
        clinit.visitInsn(Opcodes.RETURN)
        clinit.visitMaxs(1, 1)
        clinit.visitEnd()
        cw.visitEnd()
        val bytes = cw.toByteArray()
        BytecodeValidator().validateOrThrow(bytes)
    }
}
