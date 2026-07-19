package hasab.compiler.backend.jvm

import hasab.compiler.hir.cfg.Register
import hasab.compiler.types.*
import org.objectweb.asm.Opcodes

public class StackFrameAnalyzer(private val typeMapper: JvmTypeMapper) {

    public fun frameType(type: Type): Any = when (type) {
        is IntType -> Opcodes.INTEGER
        is BoolType -> Opcodes.INTEGER
        is CharType -> Opcodes.INTEGER
        is FloatType -> Opcodes.FLOAT
        is StringType -> "java/lang/String"
        is VoidType -> throw IllegalArgumentException("VoidType has no frame representation")
        is ArrayType -> {
            val elementFrame = frameType(type.elementType)
            val elementDesc = when (elementFrame) {
                is Int -> typeMapper.descriptor(type)
                is String -> "[L$elementFrame;"
                else -> typeMapper.descriptor(type)
            }
            elementDesc
        }
        is StructType -> typeMapper.internalName(type)
        is EnumType -> typeMapper.internalName(type)
        is TraitType -> typeMapper.internalName(type)
        is OptionalType -> "java/lang/Object"
        is PointerType -> "java/lang/Object"
        is FunctionType -> "java/lang/Object"
        is LambdaType -> "java/lang/Object"
        is TypeAliasType -> frameType(type.target)
        is TypeVariable -> "java/lang/Object"
        is UnknownType -> "java/lang/Object"
        is BottomType -> "java/lang/Object"
        is NilLiteralType -> Opcodes.NULL
    }

    public fun entryFrame(params: List<Pair<String, Type>>, isStatic: Boolean): Array<Any?> {
        val frame = mutableListOf<Any?>()
        if (!isStatic) {
            frame.add("this")
        }
        for ((_, type) in params) {
            frame.add(frameType(type))
        }
        return frame.toTypedArray()
    }

    public fun registerFrameType(register: Register): Any = frameType(register.type)

    public fun localsArraySize(params: List<Pair<String, Type>>, isStatic: Boolean): Int {
        var size = if (isStatic) 0 else 1
        for ((_, _) in params) {
            size += 1
        }
        return size
    }

    public fun registerSlotSize(type: Type): Int = 1
}
