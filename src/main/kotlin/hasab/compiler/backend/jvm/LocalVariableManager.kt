package hasab.compiler.backend.jvm

import hasab.compiler.hir.cfg.Register
import hasab.compiler.types.Type

public class LocalVariableManager(private var isStatic: Boolean = true) {

    private val registerToSlot: MutableMap<Register, Int> = mutableMapOf()
    private val paramNameToSlot: MutableMap<String, Int> = mutableMapOf()
    private var nextSlot: Int = if (isStatic) 0 else 1

    public fun allocateParameter(name: String, type: Type): Int {
        val slot = nextSlot++
        paramNameToSlot[name] = slot
        return slot
    }

    public fun allocateRegister(register: Register): Int {
        registerToSlot[register]?.let { return it }
        val slot = nextSlot++
        registerToSlot[register] = slot
        return slot
    }

    public fun slotFor(register: Register): Int =
        registerToSlot[register]
            ?: paramNameToSlot[register.name]
            ?: throw IllegalArgumentException("Register ${register.name} not allocated")

    public fun slotForParam(name: String): Int =
        paramNameToSlot[name]
            ?: throw IllegalArgumentException("Parameter $name not allocated")

    public fun totalSlots(): Int = nextSlot

    public fun reset(isStatic: Boolean) {
        this.isStatic = isStatic
        registerToSlot.clear()
        paramNameToSlot.clear()
        nextSlot = if (isStatic) 0 else 1
    }

    public fun isAllocated(register: Register): Boolean =
        register in registerToSlot
}
