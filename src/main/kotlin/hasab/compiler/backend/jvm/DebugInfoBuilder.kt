package hasab.compiler.backend.jvm

import org.objectweb.asm.Label

public class DebugInfoBuilder(private val sourceFileName: String) {

    private val lineLabels = mutableMapOf<Int, Label>()

    public fun labelForLine(lineNumber: Int): Label {
        return lineLabels.getOrPut(lineNumber) { Label() }
    }

    public fun getLineLabels(): Map<Int, Label> = lineLabels.toMap()

    public fun emitLineNumbers(methodBuilder: MethodBuilder) {
        for ((line, label) in lineLabels.toSortedMap()) {
            methodBuilder.visitLabel(label)
            methodBuilder.visitLineNumber(line, label)
        }
    }

    public fun emitLocalVariable(
        methodBuilder: MethodBuilder,
        name: String,
        descriptor: String,
        startLabel: Label,
        endLabel: Label,
        slot: Int,
    ) {
        methodBuilder.visitLocalVariable(name, descriptor, null, startLabel, endLabel, slot)
    }

    public fun reset() {
        lineLabels.clear()
    }
}
