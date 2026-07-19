package hasab.compiler.backend.jvm

import hasab.compiler.types.Type
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

public class MethodBuilder(
    internal val methodVisitor: MethodVisitor,
    public val isStatic: Boolean,
    public val methodName: String,
) {

    public fun newLabel(): Label = Label()

    public fun markLabel(label: Label) {
        methodVisitor.visitLabel(label)
    }

    public fun visitLabel(label: Label) {
        methodVisitor.visitLabel(label)
    }

    public fun visitFrame(
        type: Int,
        numLocal: Int,
        local: Array<Any?>,
        numStack: Int,
        stack: Array<Any?>,
    ) {
        methodVisitor.visitFrame(type, numLocal, local, numStack, stack)
    }

    public fun visitInsn(opcode: Int) {
        methodVisitor.visitInsn(opcode)
    }

    public fun visitIntInsn(opcode: Int, operand: Int) {
        methodVisitor.visitIntInsn(opcode, operand)
    }

    public fun visitVarInsn(opcode: Int, `var`: Int) {
        methodVisitor.visitVarInsn(opcode, `var`)
    }

    public fun visitTypeInsn(opcode: Int, type: String) {
        methodVisitor.visitTypeInsn(opcode, type)
    }

    public fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        methodVisitor.visitFieldInsn(opcode, owner, name, descriptor)
    }

    public fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean = false,
    ) {
        methodVisitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    public fun visitInvokeDynamicInsn(
        name: String,
        descriptor: String,
        bootstrapMethodHandle: Handle,
        vararg bootstrapMethodArguments: Any,
    ) {
        methodVisitor.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
    }

    public fun visitJumpInsn(opcode: Int, label: Label) {
        methodVisitor.visitJumpInsn(opcode, label)
    }

    public fun visitLdcInsn(value: Any) {
        methodVisitor.visitLdcInsn(value)
    }

    public fun visitIincInsn(`var`: Int, increment: Int) {
        methodVisitor.visitIincInsn(`var`, increment)
    }

    public fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
        methodVisitor.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    public fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<Label>) {
        methodVisitor.visitLookupSwitchInsn(dflt, keys, labels)
    }

    public fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) {
        methodVisitor.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    public fun visitLineNumber(line: Int, start: Label) {
        methodVisitor.visitLineNumber(line, start)
    }

    public fun visitLocalVariable(
        name: String,
        descriptor: String,
        signature: String?,
        start: Label,
        end: Label,
        index: Int,
    ) {
        methodVisitor.visitLocalVariable(name, descriptor, signature, start, end, index)
    }

    public fun visitMaxs(maxStack: Int, maxLocals: Int) {
        methodVisitor.visitMaxs(maxStack, maxLocals)
    }

    public fun visitEnd() {
        methodVisitor.visitEnd()
    }

    // --- Convenience methods ---

    public fun loadConstant(value: Int) {
        when (value) {
            -1 -> methodVisitor.visitInsn(Opcodes.ICONST_M1)
            in 0..5 -> methodVisitor.visitInsn(Opcodes.ICONST_0 + value)
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> methodVisitor.visitIntInsn(Opcodes.BIPUSH, value)
            in Short.MIN_VALUE..Short.MAX_VALUE -> methodVisitor.visitIntInsn(Opcodes.SIPUSH, value)
            else -> methodVisitor.visitLdcInsn(value)
        }
    }

    public fun loadConstant(value: Float) {
        when (value) {
            0.0f -> methodVisitor.visitInsn(Opcodes.FCONST_0)
            1.0f -> methodVisitor.visitInsn(Opcodes.FCONST_1)
            2.0f -> methodVisitor.visitInsn(Opcodes.FCONST_2)
            else -> methodVisitor.visitLdcInsn(value)
        }
    }

    public fun loadConstant(value: String) {
        methodVisitor.visitLdcInsn(value)
    }

    public fun loadConstant(value: Boolean) {
        methodVisitor.visitInsn(if (value) Opcodes.ICONST_1 else Opcodes.ICONST_0)
    }

    public fun loadConstant(value: Char) {
        methodVisitor.visitIntInsn(Opcodes.BIPUSH, value.code)
    }

    public fun loadLocal(slot: Int, type: Type) {
        methodVisitor.visitVarInsn(JvmTypeMapper.loadOpcode(type), slot)
    }

    public fun storeLocal(slot: Int, type: Type) {
        methodVisitor.visitVarInsn(JvmTypeMapper.storeOpcode(type), slot)
    }

    public fun returnVoid() {
        methodVisitor.visitInsn(Opcodes.RETURN)
    }

    public fun returnValue(type: Type) {
        methodVisitor.visitInsn(JvmTypeMapper.returnOpcode(type))
    }

    public fun ifZerocmp(opcode: Int, label: Label) {
        methodVisitor.visitJumpInsn(opcode, label)
    }

    public fun goto(label: Label) {
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label)
    }

    public fun newArray(elementType: Type) {
        if (JvmTypeMapper.isPrimitive(elementType)) {
            methodVisitor.visitIntInsn(Opcodes.NEWARRAY, JvmTypeMapper.newArrayTypeCode(elementType))
        } else {
            methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, JvmTypeMapper.internalName(elementType))
        }
    }

    public fun arrayLoad(elementType: Type) {
        methodVisitor.visitInsn(JvmTypeMapper.arrayLoadOpcode(elementType))
    }

    public fun arrayStore(elementType: Type) {
        methodVisitor.visitInsn(JvmTypeMapper.arrayStoreOpcode(elementType))
    }
}
