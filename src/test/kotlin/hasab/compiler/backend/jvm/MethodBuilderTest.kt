package hasab.compiler.backend.jvm

import hasab.compiler.types.*
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import kotlin.test.Test
import kotlin.test.assertNotNull

class MethodBuilderTest {

    private fun createBuilder(desc: String = "()V"): MethodBuilder {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null)
        val mv = cw.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "test", desc, null, null)
        return MethodBuilder(mv, true, "test")
    }

    @Test
    fun `can create and mark labels`() {
        val mb = createBuilder()
        val label = mb.newLabel()
        mb.markLabel(label)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant int works`() {
        val mb = createBuilder()
        mb.loadConstant(42)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant int zero`() {
        val mb = createBuilder()
        mb.loadConstant(0)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant int five`() {
        val mb = createBuilder()
        mb.loadConstant(5)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant int negative one`() {
        val mb = createBuilder()
        mb.loadConstant(-1)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant int large value`() {
        val mb = createBuilder()
        mb.loadConstant(1000)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant int byte range`() {
        val mb = createBuilder()
        mb.loadConstant(100)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant float works`() {
        val mb = createBuilder()
        mb.loadConstant(3.14f)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant float zero`() {
        val mb = createBuilder()
        mb.loadConstant(0.0f)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant string works`() {
        val mb = createBuilder()
        mb.loadConstant("hello")
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant boolean works`() {
        val mb = createBuilder()
        mb.loadConstant(true)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant boolean false`() {
        val mb = createBuilder()
        mb.loadConstant(false)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `returnVoid works`() {
        val mb = createBuilder()
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `returnValue int works`() {
        val mb = createBuilder("()I")
        mb.loadConstant(42)
        mb.returnValue(IntType)
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `returnValue float works`() {
        val mb = createBuilder("()F")
        mb.loadConstant(3.14f)
        mb.returnValue(FloatType)
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `goto works`() {
        val mb = createBuilder()
        val label = mb.newLabel()
        mb.goto(label)
        mb.markLabel(label)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `can build complete method`() {
        val mb = createBuilder("()I")
        mb.methodVisitor.visitCode()
        mb.loadConstant(42)
        mb.visitInsn(Opcodes.IRETURN)
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `visitVarInsn works`() {
        val mb = createBuilder("()V")
        mb.methodVisitor.visitCode()
        mb.visitVarInsn(Opcodes.ISTORE, 0)
        mb.visitVarInsn(Opcodes.ILOAD, 0)
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `visitTypeInsn works`() {
        val mb = createBuilder("()Ljava/lang/Object;")
        mb.methodVisitor.visitCode()
        mb.visitTypeInsn(Opcodes.NEW, "java/lang/Object")
        mb.visitInsn(Opcodes.DUP)
        mb.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false,
        )
        mb.visitInsn(Opcodes.ARETURN)
        mb.visitMaxs(2, 1)
        mb.visitEnd()
    }

    @Test
    fun `visitFieldInsn works`() {
        val mb = createBuilder("()V")
        mb.methodVisitor.visitCode()
        mb.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `ifZerocmp works`() {
        val mb = createBuilder("()V")
        mb.methodVisitor.visitCode()
        val label = mb.newLabel()
        mb.loadConstant(0)
        mb.ifZerocmp(Opcodes.IFEQ, label)
        mb.loadConstant(1)
        mb.visitInsn(Opcodes.POP)
        mb.markLabel(label)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }

    @Test
    fun `loadConstant char works`() {
        val mb = createBuilder("()V")
        mb.methodVisitor.visitCode()
        mb.loadConstant('A')
        mb.visitInsn(Opcodes.POP)
        mb.returnVoid()
        mb.visitMaxs(1, 1)
        mb.visitEnd()
    }
}
