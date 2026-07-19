package hasab.compiler.backend.jvm

import hasab.compiler.types.*

public object JvmTypeMapper {

    public fun descriptor(type: Type): String = when (type) {
        is IntType -> "I"
        is FloatType -> "F"
        is BoolType -> "Z"
        is CharType -> "C"
        is VoidType -> "V"
        is StringType -> "Ljava/lang/String;"
        is ArrayType -> "[${descriptor(type.elementType)}"
        is OptionalType -> "Ljava/lang/Object;"
        is PointerType -> "Ljava/lang/Object;"
        is FunctionType -> "Ljava/lang/Object;"
        is LambdaType -> "Ljava/lang/Object;"
        is StructType -> "L${type.name};"
        is EnumType -> "L${type.name};"
        is TypeAliasType -> descriptor(type.target)
        is TraitType -> "L${type.name};"
        is NilLiteralType -> "Ljava/lang/Object;"
        is UnknownType -> "Ljava/lang/Object;"
        is BottomType -> "Ljava/lang/Object;"
        is TypeVariable -> "Ljava/lang/Object;"
    }

    public fun internalName(type: Type): String = when (type) {
        is StructType -> type.name
        is EnumType -> type.name
        is TraitType -> type.name
        else -> throw IllegalArgumentException("Cannot get internal name for non-class type: $type")
    }

    public fun isPrimitive(type: Type): Boolean = when (type) {
        is IntType, is FloatType, is BoolType, is CharType -> true
        else -> false
    }

    public fun isFloatingPoint(type: Type): Boolean = type is FloatType

    public fun loadOpcode(type: Type): Int = when (type) {
        is IntType, is BoolType, is CharType -> org.objectweb.asm.Opcodes.ILOAD
        is FloatType -> org.objectweb.asm.Opcodes.FLOAD
        else -> org.objectweb.asm.Opcodes.ALOAD
    }

    public fun storeOpcode(type: Type): Int = when (type) {
        is IntType, is BoolType, is CharType -> org.objectweb.asm.Opcodes.ISTORE
        is FloatType -> org.objectweb.asm.Opcodes.FSTORE
        else -> org.objectweb.asm.Opcodes.ASTORE
    }

    public fun returnOpcode(type: Type): Int = when (type) {
        is IntType, is BoolType, is CharType -> org.objectweb.asm.Opcodes.IRETURN
        is FloatType -> org.objectweb.asm.Opcodes.FRETURN
        is VoidType -> org.objectweb.asm.Opcodes.RETURN
        else -> org.objectweb.asm.Opcodes.ARETURN
    }

    public fun newArrayTypeCode(type: Type): Int = when (type) {
        is IntType -> org.objectweb.asm.Opcodes.T_INT
        is FloatType -> org.objectweb.asm.Opcodes.T_FLOAT
        is BoolType -> org.objectweb.asm.Opcodes.T_BOOLEAN
        is CharType -> org.objectweb.asm.Opcodes.T_CHAR
        else -> throw IllegalArgumentException("Use ANEWARRAY for non-primitive type: $type")
    }

    public fun arrayLoadOpcode(type: Type): Int = when (type) {
        is IntType -> org.objectweb.asm.Opcodes.IALOAD
        is FloatType -> org.objectweb.asm.Opcodes.FALOAD
        is BoolType -> org.objectweb.asm.Opcodes.IALOAD
        is CharType -> org.objectweb.asm.Opcodes.CALOAD
        else -> org.objectweb.asm.Opcodes.AALOAD
    }

    public fun arrayStoreOpcode(type: Type): Int = when (type) {
        is IntType -> org.objectweb.asm.Opcodes.IASTORE
        is FloatType -> org.objectweb.asm.Opcodes.FASTORE
        is BoolType -> org.objectweb.asm.Opcodes.IASTORE
        is CharType -> org.objectweb.asm.Opcodes.CASTORE
        else -> org.objectweb.asm.Opcodes.AASTORE
    }
}
