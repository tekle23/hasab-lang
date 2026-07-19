package hasab.compiler.backend.jvm

import hasab.compiler.hir.cfg.*
import hasab.compiler.types.*
import org.objectweb.asm.Opcodes

public class JvmInstructionEmitter(
    private val methodBuilder: MethodBuilder,
    private val localVars: LocalVariableManager,
    private val typeMapper: JvmTypeMapper,
    private val labelMap: Map<BlockId, org.objectweb.asm.Label>,
    private val mainClassName: String,
) {

    public fun emit(instruction: HirInstruction) {
        when (instruction) {
            is AssignInstr -> {
                loadOperand(instruction.value)
                storeToRegister(instruction.target)
            }
            is BinaryOpInstr -> emitBinaryOp(instruction.target, instruction.operator, instruction.left, instruction.right)
            is UnaryOpInstr -> emitUnaryOp(instruction.target, instruction.operator, instruction.operand)
            is CallInstr -> emitCall(instruction.target, instruction.calleeName, instruction.calleeType, instruction.arguments)
            is LoadFieldInstr -> emitLoadField(instruction.target, instruction.base, instruction.fieldName, instruction.fieldType)
            is LoadIndexInstr -> emitLoadIndex(instruction.target, instruction.base, instruction.index, instruction.elementType)
            is StoreFieldInstr -> emitStoreField(instruction.base, instruction.fieldName, instruction.value)
            is StoreIndexInstr -> emitStoreIndex(instruction.base, instruction.index, instruction.value)
            is ArrayLiteralInstr -> emitArrayLiteral(instruction.target, instruction.elements, instruction.arrayType)
            is ArrayInitInstr -> emitArrayInit(instruction.target, instruction.size, instruction.arrayType)
            is PhiInstr -> emitPhi(instruction.target, instruction.sources)
            is CastInstr -> emitCast(instruction.target, instruction.source, instruction.toType)
            is NullCheckInstr -> {
                loadOperand(instruction.source)
                val nullLabel = org.objectweb.asm.Label()
                methodBuilder.visitJumpInsn(Opcodes.IFNONNULL, nullLabel)
                methodBuilder.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException")
                methodBuilder.visitInsn(Opcodes.DUP)
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "java/lang/NullPointerException",
                    "<init>",
                    "()V",
                    false,
                )
                methodBuilder.visitInsn(Opcodes.ATHROW)
                methodBuilder.visitLabel(nullLabel)
                loadOperand(instruction.source)
                storeToRegister(instruction.target)
            }
            is NullAssertInstr -> {
                loadOperand(instruction.source)
                storeToRegister(instruction.target)
            }
            is ReturnInstr -> emitReturn(instruction.value)
            is BranchInstr -> emitBranch(instruction.condition, instruction.trueBlock, instruction.falseBlock)
            is JumpInstr -> {
                methodBuilder.visitJumpInsn(Opcodes.GOTO, labelMap[instruction.target]
                    ?: throw IllegalArgumentException("No label for block ${instruction.target}"))
            }
            is SwitchInstr -> emitSwitch(instruction.subject, instruction.cases, instruction.defaultBlock)
        }
    }

    public fun emitBlock(block: HirBasicBlock) {
        val blockLabel = labelMap[block.id]
            ?: throw IllegalArgumentException("No label for block ${block.id}")
        methodBuilder.visitLabel(blockLabel)
        for (instruction in block.instructions) {
            emit(instruction)
        }
    }

    internal fun loadOperand(operand: Operand) {
        when (operand) {
            is RegisterOperand -> {
                val slot = localVars.slotFor(operand.register)
                methodBuilder.visitVarInsn(typeMapper.loadOpcode(operand.register.type), slot)
            }
            is ConstOperand -> loadConstant(operand.value, operand.type)
            is ParamOperand -> {
                val slot = localVars.slotForParam(operand.name)
                methodBuilder.visitVarInsn(typeMapper.loadOpcode(operand.type), slot)
            }
        }
    }

    internal fun storeToRegister(register: Register) {
        val slot = localVars.allocateRegister(register)
        methodBuilder.visitVarInsn(typeMapper.storeOpcode(register.type), slot)
    }

    internal fun emitBinaryOp(target: Register, operator: String, left: Operand, right: Operand) {
        loadOperand(left)
        loadOperand(right)
        when (operator) {
            "+" -> {
                if (typeMapper.isFloatingPoint(target.type)) {
                    methodBuilder.visitInsn(Opcodes.FADD)
                } else {
                    methodBuilder.visitInsn(Opcodes.IADD)
                }
            }
            "-" -> {
                if (typeMapper.isFloatingPoint(target.type)) {
                    methodBuilder.visitInsn(Opcodes.FSUB)
                } else {
                    methodBuilder.visitInsn(Opcodes.ISUB)
                }
            }
            "*" -> {
                if (typeMapper.isFloatingPoint(target.type)) {
                    methodBuilder.visitInsn(Opcodes.FMUL)
                } else {
                    methodBuilder.visitInsn(Opcodes.IMUL)
                }
            }
            "/" -> {
                if (typeMapper.isFloatingPoint(target.type)) {
                    methodBuilder.visitInsn(Opcodes.FDIV)
                } else {
                    methodBuilder.visitInsn(Opcodes.IDIV)
                }
            }
            "%" -> {
                if (typeMapper.isFloatingPoint(target.type)) {
                    methodBuilder.visitInsn(Opcodes.FREM)
                } else {
                    methodBuilder.visitInsn(Opcodes.IREM)
                }
            }
            "==" -> {
                val trueLabel = org.objectweb.asm.Label()
                val endLabel = org.objectweb.asm.Label()
                if (typeMapper.isFloatingPoint(left.type)) {
                    methodBuilder.visitInsn(Opcodes.FCMPG)
                }
                methodBuilder.visitJumpInsn(equalityJumpOpcode(operator), trueLabel)
                pushBoolean(false)
                methodBuilder.visitJumpInsn(Opcodes.GOTO, endLabel)
                methodBuilder.visitLabel(trueLabel)
                pushBoolean(true)
                methodBuilder.visitLabel(endLabel)
            }
            "!=" -> {
                val trueLabel = org.objectweb.asm.Label()
                val endLabel = org.objectweb.asm.Label()
                if (typeMapper.isFloatingPoint(left.type)) {
                    methodBuilder.visitInsn(Opcodes.FCMPG)
                }
                methodBuilder.visitJumpInsn(equalityJumpOpcode(operator), trueLabel)
                pushBoolean(false)
                methodBuilder.visitJumpInsn(Opcodes.GOTO, endLabel)
                methodBuilder.visitLabel(trueLabel)
                pushBoolean(true)
                methodBuilder.visitLabel(endLabel)
            }
            "<", ">", "<=", ">=" -> {
                val trueLabel = org.objectweb.asm.Label()
                val endLabel = org.objectweb.asm.Label()
                if (typeMapper.isFloatingPoint(left.type)) {
                    methodBuilder.visitInsn(Opcodes.FCMPG)
                }
                methodBuilder.visitJumpInsn(comparisonJumpOpcode(operator), trueLabel)
                pushBoolean(false)
                methodBuilder.visitJumpInsn(Opcodes.GOTO, endLabel)
                methodBuilder.visitLabel(trueLabel)
                pushBoolean(true)
                methodBuilder.visitLabel(endLabel)
            }
            "&&" -> {
                val falseLabel = org.objectweb.asm.Label()
                val endLabel = org.objectweb.asm.Label()
                methodBuilder.visitJumpInsn(Opcodes.IFEQ, falseLabel)
                loadOperand(right)
                methodBuilder.visitJumpInsn(Opcodes.GOTO, endLabel)
                methodBuilder.visitLabel(falseLabel)
                pushBoolean(false)
                methodBuilder.visitLabel(endLabel)
            }
            "||" -> {
                val trueLabel = org.objectweb.asm.Label()
                val endLabel = org.objectweb.asm.Label()
                methodBuilder.visitJumpInsn(Opcodes.IFNE, trueLabel)
                loadOperand(right)
                methodBuilder.visitJumpInsn(Opcodes.GOTO, endLabel)
                methodBuilder.visitLabel(trueLabel)
                pushBoolean(true)
                methodBuilder.visitLabel(endLabel)
            }
            else -> throw IllegalArgumentException("Unknown binary operator: $operator")
        }
        storeToRegister(target)
    }

    internal fun emitUnaryOp(target: Register, operator: String, operand: Operand) {
        loadOperand(operand)
        when (operator) {
            "-" -> {
                if (typeMapper.isFloatingPoint(target.type)) {
                    methodBuilder.visitInsn(Opcodes.FNEG)
                } else {
                    methodBuilder.visitInsn(Opcodes.INEG)
                }
            }
            "!" -> {
                val trueLabel = org.objectweb.asm.Label()
                val endLabel = org.objectweb.asm.Label()
                methodBuilder.visitJumpInsn(Opcodes.IFEQ, trueLabel)
                pushBoolean(false)
                methodBuilder.visitJumpInsn(Opcodes.GOTO, endLabel)
                methodBuilder.visitLabel(trueLabel)
                pushBoolean(true)
                methodBuilder.visitLabel(endLabel)
            }
            else -> throw IllegalArgumentException("Unknown unary operator: $operator")
        }
        storeToRegister(target)
    }

    internal fun emitCall(target: Register?, calleeName: String, calleeType: Type, arguments: List<Operand>) {
        when (calleeName) {
            "println" -> emitPrintln(arguments)
            "print" -> emitPrint(arguments)
            "len" -> emitLen(arguments)
            else -> emitUserFunctionCall(target, calleeName, calleeType, arguments)
        }
    }

    internal fun emitCast(target: Register, source: Operand, toType: Type) {
        loadOperand(source)
        val fromType = source.type
        when {
            fromType is IntType && toType is FloatType -> methodBuilder.visitInsn(Opcodes.I2F)
            fromType is FloatType && toType is IntType -> methodBuilder.visitInsn(Opcodes.F2I)
            fromType is IntType && toType is CharType -> methodBuilder.visitInsn(Opcodes.I2C)
            fromType is CharType && toType is IntType -> { /* no-op, char is already int in JVM */ }
            fromType is IntType && toType is BoolType -> { /* no-op, bool is int in JVM */ }
            fromType is BoolType && toType is IntType -> { /* no-op */ }
            fromType is IntType && toType is StringType -> {
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/String",
                    "valueOf",
                    "(I)Ljava/lang/String;",
                    false,
                )
            }
            fromType is FloatType && toType is StringType -> {
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/String",
                    "valueOf",
                    "(F)Ljava/lang/String;",
                    false,
                )
            }
            fromType is BoolType && toType is StringType -> {
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/String",
                    "valueOf",
                    "(Z)Ljava/lang/String;",
                    false,
                )
            }
            fromType is CharType && toType is StringType -> {
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/String",
                    "valueOf",
                    "(C)Ljava/lang/String;",
                    false,
                )
            }
            fromType is StringType && toType is IntType -> {
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Integer",
                    "parseInt",
                    "(Ljava/lang/String;)I",
                    false,
                )
            }
            fromType is StringType && toType is FloatType -> {
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Float",
                    "parseFloat",
                    "(Ljava/lang/String;)F",
                    false,
                )
            }
            !typeMapper.isPrimitive(fromType) && !typeMapper.isPrimitive(toType) -> {
                val targetInternalName = when (toType) {
                    is StringType -> "java/lang/String"
                    else -> typeMapper.internalName(toType)
                }
                methodBuilder.visitTypeInsn(Opcodes.CHECKCAST, targetInternalName)
            }
        }
        storeToRegister(target)
    }

    private fun loadConstant(value: Any?, type: Type) {
        when {
            value == null -> methodBuilder.visitInsn(Opcodes.ACONST_NULL)
            type is IntType && value is String -> methodBuilder.visitLdcInsn(value.toInt())
            type is IntType && value is Int -> methodBuilder.visitLdcInsn(value)
            type is FloatType && value is String -> methodBuilder.visitLdcInsn(value.toFloat())
            type is FloatType && value is Float -> methodBuilder.visitLdcInsn(value)
            type is BoolType && value is String -> methodBuilder.visitInsn(if (value.toBoolean()) Opcodes.ICONST_1 else Opcodes.ICONST_0)
            type is BoolType && value is Boolean -> methodBuilder.visitInsn(if (value) Opcodes.ICONST_1 else Opcodes.ICONST_0)
            type is CharType && value is String -> methodBuilder.visitIntInsn(Opcodes.BIPUSH, value[0].code)
            type is CharType && value is Char -> methodBuilder.visitIntInsn(Opcodes.BIPUSH, value.code)
            value is String -> methodBuilder.visitLdcInsn(value)
            else -> throw IllegalArgumentException("Unsupported constant type: ${value::class}")
        }
    }

    private fun pushBoolean(value: Boolean) {
        methodBuilder.visitInsn(if (value) Opcodes.ICONST_1 else Opcodes.ICONST_0)
    }

    private fun emitPrintln(arguments: List<Operand>) {
        methodBuilder.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        if (arguments.isEmpty()) {
            methodBuilder.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "()V",
                false,
            )
        } else {
            val arg = arguments[0]
            loadOperand(arg)
            when (arg.type) {
                is StringType -> methodBuilder.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(Ljava/lang/String;)V",
                    false,
                )
                is IntType -> methodBuilder.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(I)V",
                    false,
                )
                is FloatType -> methodBuilder.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(F)V",
                    false,
                )
                is BoolType -> methodBuilder.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(Z)V",
                    false,
                )
                is CharType -> methodBuilder.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(C)V",
                    false,
                )
                else -> {
                    methodBuilder.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Object")
                    methodBuilder.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(Ljava/lang/Object;)V",
                        false,
                    )
                }
            }
        }
    }

    private fun emitPrint(arguments: List<Operand>) {
        methodBuilder.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        val arg = arguments[0]
        loadOperand(arg)
        when (arg.type) {
            is StringType -> methodBuilder.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "print",
                "(Ljava/lang/String;)V",
                false,
            )
            is IntType -> methodBuilder.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "print",
                "(I)V",
                false,
            )
            is FloatType -> methodBuilder.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "print",
                "(F)V",
                false,
            )
            is BoolType -> methodBuilder.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "print",
                "(Z)V",
                false,
            )
            is CharType -> methodBuilder.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "print",
                "(C)V",
                false,
            )
            else -> {
                methodBuilder.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Object")
                methodBuilder.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "print",
                    "(Ljava/lang/Object;)V",
                    false,
                )
            }
        }
    }

    private fun emitLen(arguments: List<Operand>) {
        val arg = arguments[0]
        loadOperand(arg)
        when (arg.type) {
            is StringType -> methodBuilder.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "length",
                "()I",
                false,
            )
            is ArrayType -> methodBuilder.visitInsn(Opcodes.ARRAYLENGTH)
            else -> throw IllegalArgumentException("len() not supported for type: ${arg.type}")
        }
    }

    private fun emitUserFunctionCall(target: Register?, calleeName: String, calleeType: Type, arguments: List<Operand>) {
        for (arg in arguments) {
            loadOperand(arg)
        }
        val fnType = calleeType as? FunctionType
        val descriptor = if (fnType != null) {
            val paramDesc = fnType.parameterTypes.joinToString("") { typeMapper.descriptor(it) }
            "($paramDesc)${typeMapper.descriptor(fnType.returnType)}"
        } else {
            val paramDesc = arguments.joinToString("") { typeMapper.descriptor(it.type) }
            "($paramDesc)V"
        }
        methodBuilder.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            mainClassName,
            calleeName,
            descriptor,
            false,
        )
        if (target != null && fnType != null && fnType.returnType !is VoidType) {
            storeToRegister(target)
        }
    }

    private fun emitLoadField(target: Register, base: Operand, fieldName: String, fieldType: Type) {
        loadOperand(base)
        val baseType = base.type
        val owner = when (baseType) {
            is StructType -> baseType.name
            is EnumType -> baseType.name
            else -> throw IllegalArgumentException("Cannot load field from non-class type: $baseType")
        }
        methodBuilder.visitFieldInsn(Opcodes.GETFIELD, owner, fieldName, typeMapper.descriptor(fieldType))
        storeToRegister(target)
    }

    private fun emitLoadIndex(target: Register, base: Operand, index: Operand, elementType: Type) {
        loadOperand(base)
        loadOperand(index)
        methodBuilder.visitInsn(typeMapper.arrayLoadOpcode(elementType))
        storeToRegister(target)
    }

    private fun emitStoreField(base: Operand, fieldName: String, value: Operand) {
        loadOperand(base)
        loadOperand(value)
        val baseType = base.type
        val owner = when (baseType) {
            is StructType -> baseType.name
            is EnumType -> baseType.name
            else -> throw IllegalArgumentException("Cannot store field on non-class type: $baseType")
        }
        methodBuilder.visitFieldInsn(Opcodes.PUTFIELD, owner, fieldName, typeMapper.descriptor(value.type))
    }

    private fun emitStoreIndex(base: Operand, index: Operand, value: Operand) {
        loadOperand(base)
        loadOperand(index)
        loadOperand(value)
        methodBuilder.visitInsn(typeMapper.arrayStoreOpcode(value.type))
    }

    private fun emitArrayLiteral(target: Register, elements: List<Operand>, arrayType: Type) {
        val arrayElementType = (arrayType as? ArrayType)?.elementType
            ?: throw IllegalArgumentException("ArrayLiteralInstr target type is not ArrayType: $arrayType")
        methodBuilder.visitIntInsn(Opcodes.BIPUSH, elements.size)
        if (typeMapper.isPrimitive(arrayElementType)) {
            methodBuilder.visitIntInsn(Opcodes.NEWARRAY, typeMapper.newArrayTypeCode(arrayElementType))
        } else {
            val elementInternalName = when (arrayElementType) {
                is StringType -> "java/lang/String"
                else -> typeMapper.internalName(arrayElementType)
            }
            methodBuilder.visitTypeInsn(Opcodes.ANEWARRAY, elementInternalName)
        }
        elements.forEachIndexed { index, element ->
            methodBuilder.visitInsn(Opcodes.DUP)
            methodBuilder.visitIntInsn(Opcodes.BIPUSH, index)
            loadOperand(element)
            methodBuilder.visitInsn(typeMapper.arrayStoreOpcode(arrayElementType))
        }
        storeToRegister(target)
    }

    private fun emitArrayInit(target: Register, size: Operand, arrayType: Type) {
        val arrayElementType = (arrayType as? ArrayType)?.elementType
            ?: throw IllegalArgumentException("ArrayInitInstr target type is not ArrayType: $arrayType")
        loadOperand(size)
        if (typeMapper.isPrimitive(arrayElementType)) {
            methodBuilder.visitIntInsn(Opcodes.NEWARRAY, typeMapper.newArrayTypeCode(arrayElementType))
        } else {
            val elementInternalName = when (arrayElementType) {
                is StringType -> "java/lang/String"
                else -> typeMapper.internalName(arrayElementType)
            }
            methodBuilder.visitTypeInsn(Opcodes.ANEWARRAY, elementInternalName)
        }
        storeToRegister(target)
    }

    private fun emitPhi(target: Register, sources: List<Pair<BlockId, Operand>>) {
        if (sources.isEmpty()) return
        loadOperand(sources.first().second)
        storeToRegister(target)
    }

    private fun emitReturn(value: Operand?) {
        if (value != null) {
            loadOperand(value)
            methodBuilder.visitInsn(typeMapper.returnOpcode(value.type))
        } else {
            methodBuilder.visitInsn(Opcodes.RETURN)
        }
    }

    private fun emitBranch(condition: Operand, trueBlock: BlockId, falseBlock: BlockId) {
        loadOperand(condition)
        val trueLabel = labelMap[trueBlock]
            ?: throw IllegalArgumentException("No label for block $trueBlock")
        val falseLabel = labelMap[falseBlock]
            ?: throw IllegalArgumentException("No label for block $falseBlock")
        methodBuilder.visitJumpInsn(Opcodes.IFNE, trueLabel)
        methodBuilder.visitJumpInsn(Opcodes.GOTO, falseLabel)
    }

    private fun emitSwitch(subject: Operand, cases: List<Pair<Any, BlockId>>, defaultBlock: BlockId) {
        loadOperand(subject)
        val defaultLabel = labelMap[defaultBlock]
            ?: throw IllegalArgumentException("No label for block $defaultBlock")
        val caseValues = cases.map { it.first as Int }.toIntArray()
        val caseLabels = cases.map { labelMap[it.second]
            ?: throw IllegalArgumentException("No label for block ${it.second}") }.toTypedArray()
        methodBuilder.visitTableSwitchInsn(
            caseValues.first(),
            caseValues.last(),
            defaultLabel,
            *caseLabels,
        )
    }

    private fun equalityJumpOpcode(operator: String): Int = when (operator) {
        "==" -> Opcodes.IFEQ
        "!=" -> Opcodes.IFNE
        else -> throw IllegalArgumentException("Not an equality operator: $operator")
    }

    private fun comparisonJumpOpcode(operator: String): Int = when (operator) {
        "<" -> Opcodes.IFLT
        ">" -> Opcodes.IFGT
        "<=" -> Opcodes.IFLE
        ">=" -> Opcodes.IFGE
        else -> throw IllegalArgumentException("Not a comparison operator: $operator")
    }
}
