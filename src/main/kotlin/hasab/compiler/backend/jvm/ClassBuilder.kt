package hasab.compiler.backend.jvm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

public class ClassBuilder(
    public val className: String,
    public val access: Int = Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER,
    public val superName: String = "java/lang/Object",
) {
    internal val classWriter: ClassWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    public fun begin() {
        classWriter.visit(Opcodes.V21, access, className, null, superName, null)
    }

    public fun addField(
        name: String,
        descriptor: String,
        access: Int = Opcodes.ACC_PUBLIC,
        signature: String? = null,
        value: Any? = null,
    ) {
        classWriter.visitField(access, name, descriptor, signature, value)
    }

    public fun addMethod(
        name: String,
        descriptor: String,
        access: Int = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
        signature: String? = null,
        exceptions: Array<String>? = null,
    ): MethodBuilder {
        val mv = classWriter.visitMethod(access, name, descriptor, signature, exceptions)
        val isStatic = (access and Opcodes.ACC_STATIC) != 0
        return MethodBuilder(mv, isStatic, name)
    }

    public fun addDefaultConstructor() {
        val mv = classWriter.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            "()V",
            null,
            null,
        )
        mv.visitCode()
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            superName,
            "<init>",
            "()V",
            false,
        )
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    public fun build(): ByteArray {
        classWriter.visitEnd()
        return classWriter.toByteArray()
    }

    public fun getClassWriter(): ClassWriter = classWriter
}
