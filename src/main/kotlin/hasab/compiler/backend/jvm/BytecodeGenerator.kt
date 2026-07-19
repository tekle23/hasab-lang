package hasab.compiler.backend.jvm

import hasab.compiler.hir.cfg.*
import hasab.compiler.types.*
import org.objectweb.asm.Label

/**
 * Orchestrates bytecode generation for a single HASAB function.
 * Translates a [HirCfgFunction] into JVM bytecode via [ClassBuilder].
 */
public class BytecodeGenerator(
    private val classBuilder: ClassBuilder,
    private val mainClassName: String,
    private val typeMapper: JvmTypeMapper = JvmTypeMapper,
) {

    /**
     * Generate a static JVM method for the given [function].
     */
    public fun generate(function: HirCfgFunction) {
        val localVars = LocalVariableManager(isStatic = true)
        val debugInfo = DebugInfoBuilder("")

        val descriptor = buildMethodDescriptor(function)
        val methodBuilder = classBuilder.addMethod(
            name = function.name,
            descriptor = descriptor,
            access = org.objectweb.asm.Opcodes.ACC_PUBLIC or org.objectweb.asm.Opcodes.ACC_STATIC,
        )
        methodBuilder.methodVisitor.visitCode()

        val startLabel = methodBuilder.newLabel()
        methodBuilder.markLabel(startLabel)

        for (param in function.parameters) {
            localVars.allocateParameter(param.name, param.type)
        }

        val blockLabels = function.blocks.keys.associateWith { methodBuilder.newLabel() }

        val entryLabel = blockLabels[function.entryBlockId]
            ?: throw IllegalStateException("Entry block ${function.entryBlockId} not found")

        methodBuilder.goto(entryLabel)

        val emitter = JvmInstructionEmitter(
            methodBuilder = methodBuilder,
            localVars = localVars,
            typeMapper = typeMapper,
            labelMap = blockLabels,
            mainClassName = mainClassName,
        )

        val sortedBlocks = topologicalSort(function)

        for (blockId in sortedBlocks) {
            val block = function.blocks[blockId] ?: continue
            val blockLabel = blockLabels[blockId] ?: continue
            methodBuilder.markLabel(blockLabel)
            for (instruction in block.instructions) {
                emitter.emit(instruction)
            }
        }

        val endLabel = methodBuilder.newLabel()
        methodBuilder.markLabel(endLabel)

        for ((name, type) in function.parameters.map { it.name to it.type }) {
            val slot = localVars.slotForParam(name)
            debugInfo.emitLocalVariable(
                methodBuilder = methodBuilder,
                name = name,
                descriptor = typeMapper.descriptor(type),
                startLabel = startLabel,
                endLabel = endLabel,
                slot = slot,
            )
        }

        methodBuilder.visitMaxs(0, 0)
        methodBuilder.visitEnd()
    }

    private fun buildMethodDescriptor(function: HirCfgFunction): String {
        val params = function.parameters.joinToString("") { typeMapper.descriptor(it.type) }
        val ret = typeMapper.descriptor(function.returnType)
        return "($params)$ret"
    }

    private fun topologicalSort(function: HirCfgFunction): List<BlockId> {
        val visited = mutableSetOf<BlockId>()
        val result = mutableListOf<BlockId>()

        fun visit(blockId: BlockId) {
            if (!visited.add(blockId)) return
            val block = function.blocks[blockId] ?: return
            when (val term = block.terminator) {
                is BranchInstr -> {
                    visit(term.trueBlock)
                    visit(term.falseBlock)
                }
                is JumpInstr -> visit(term.target)
                is SwitchInstr -> {
                    for ((_, target) in term.cases) visit(target)
                    visit(term.defaultBlock)
                }
                else -> {}
            }
            result.add(blockId)
        }

        visit(function.entryBlockId)

        for (blockId in function.blocks.keys) {
            if (blockId !in visited) {
                result.add(blockId)
            }
        }

        return result
    }
}
