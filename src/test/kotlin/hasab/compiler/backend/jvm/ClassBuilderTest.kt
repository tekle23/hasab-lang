package hasab.compiler.backend.jvm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClassBuilderTest {

    @Test
    fun `creates valid class bytes`() {
        val builder = ClassBuilder("Test")
        builder.begin()
        builder.addDefaultConstructor()
        val bytes = builder.build()
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
        assertEquals(0xCA.toByte(), bytes[0])
    }

    @Test
    fun `class has correct name`() {
        val builder = ClassBuilder("MyClass")
        builder.begin()
        builder.addDefaultConstructor()
        val bytes = builder.build()
        val cr = ClassReader(bytes)
        assertEquals("MyClass", cr.className)
    }

    @Test
    fun `class extends correct super`() {
        val builder = ClassBuilder("Test", superName = "java/lang/Object")
        builder.begin()
        builder.addDefaultConstructor()
        val bytes = builder.build()
        val cr = ClassReader(bytes)
        assertEquals("java/lang/Object", cr.superName)
    }

    @Test
    fun `can add fields`() {
        val builder = ClassBuilder("Test")
        builder.begin()
        builder.addField("x", "I", Opcodes.ACC_PUBLIC)
        builder.addField("name", "Ljava/lang/String;", Opcodes.ACC_PUBLIC)
        builder.addDefaultConstructor()
        val bytes = builder.build()
        val cr = ClassReader(bytes)
        val classNode = org.objectweb.asm.tree.ClassNode()
        cr.accept(classNode, ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES)
        assertEquals(2, classNode.fields.size)
        assertEquals("x", classNode.fields[0].name)
        assertEquals("I", classNode.fields[0].desc)
        assertEquals("name", classNode.fields[1].name)
        assertEquals("Ljava/lang/String;", classNode.fields[1].desc)
    }

    @Test
    fun `can add static methods`() {
        val builder = ClassBuilder("Test")
        builder.begin()
        val mb = builder.addMethod(
            "main",
            "([Ljava/lang/String;)V",
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
        )
        mb.methodVisitor.visitCode()
        mb.visitInsn(Opcodes.RETURN)
        mb.visitMaxs(1, 1)
        mb.visitEnd()
        val bytes = builder.build()
        val cr = ClassReader(bytes)
        assertEquals("Test", cr.className)
    }

    @Test
    fun `default constructor generates valid bytecode`() {
        val builder = ClassBuilder("Test")
        builder.begin()
        builder.addDefaultConstructor()
        val bytes = builder.build()
        BytecodeValidator().validateOrThrow(bytes)
    }

    @Test
    fun `multiple methods can be added`() {
        val builder = ClassBuilder("Test")
        builder.begin()

        val mb1 = builder.addMethod("foo", "()V", Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC)
        mb1.methodVisitor.visitCode()
        mb1.visitInsn(Opcodes.RETURN)
        mb1.visitMaxs(1, 1)
        mb1.visitEnd()

        val mb2 = builder.addMethod("bar", "()V", Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC)
        mb2.methodVisitor.visitCode()
        mb2.visitInsn(Opcodes.RETURN)
        mb2.visitMaxs(1, 1)
        mb2.visitEnd()

        val bytes = builder.build()
        val cr = ClassReader(bytes)
        assertEquals("Test", cr.className)
    }

    @Test
    fun `class with custom super name`() {
        val builder = ClassBuilder("Test", superName = "java/lang/Enum")
        builder.begin()
        builder.addDefaultConstructor()
        val bytes = builder.build()
        val cr = ClassReader(bytes)
        assertEquals("java/lang/Enum", cr.superName)
    }

    @Test
    fun `class with field and method validates`() {
        val builder = ClassBuilder("Test")
        builder.begin()
        builder.addField("x", "I", Opcodes.ACC_PUBLIC)
        val mb = builder.addMethod("getX", "()I", Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC)
        mb.methodVisitor.visitCode()
        mb.visitInsn(Opcodes.ICONST_0)
        mb.visitInsn(Opcodes.IRETURN)
        mb.visitMaxs(1, 1)
        mb.visitEnd()
        builder.addDefaultConstructor()
        val bytes = builder.build()
        BytecodeValidator().validateOrThrow(bytes)
    }

    @Test
    fun `getClassWriter returns class writer`() {
        val builder = ClassBuilder("Test")
        assertNotNull(builder.getClassWriter())
    }

    @Test
    fun `class with string method validates`() {
        val builder = ClassBuilder("Test")
        builder.begin()
        val mb = builder.addMethod("hello", "()Ljava/lang/String;", Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC)
        mb.methodVisitor.visitCode()
        mb.visitLdcInsn("hello")
        mb.visitInsn(Opcodes.ARETURN)
        mb.visitMaxs(1, 1)
        mb.visitEnd()
        builder.addDefaultConstructor()
        val bytes = builder.build()
        BytecodeValidator().validateOrThrow(bytes)
    }
}
