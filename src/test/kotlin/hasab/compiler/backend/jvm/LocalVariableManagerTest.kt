package hasab.compiler.backend.jvm

import hasab.compiler.hir.cfg.Register
import hasab.compiler.types.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocalVariableManagerTest {

    @Test
    fun `static method starts at slot 0`() {
        val manager = LocalVariableManager(isStatic = true)
        val slot = manager.allocateParameter("x", IntType)
        assertEquals(0, slot)
    }

    @Test
    fun `instance method starts at slot 1`() {
        val manager = LocalVariableManager(isStatic = false)
        val slot = manager.allocateParameter("x", IntType)
        assertEquals(1, slot)
    }

    @Test
    fun `parameters get sequential slots in static`() {
        val manager = LocalVariableManager(isStatic = true)
        val slot0 = manager.allocateParameter("x", IntType)
        val slot1 = manager.allocateParameter("y", FloatType)
        assertEquals(0, slot0)
        assertEquals(1, slot1)
    }

    @Test
    fun `parameters get sequential slots in instance`() {
        val manager = LocalVariableManager(isStatic = false)
        val slot0 = manager.allocateParameter("x", IntType)
        val slot1 = manager.allocateParameter("y", FloatType)
        assertEquals(1, slot0)
        assertEquals(2, slot1)
    }

    @Test
    fun `register allocation works`() {
        val manager = LocalVariableManager(isStatic = true)
        manager.allocateParameter("x", IntType)
        val reg = Register("r0", IntType)
        val slot = manager.allocateRegister(reg)
        assertEquals(1, slot)
    }

    @Test
    fun `total slots tracks correctly`() {
        val manager = LocalVariableManager(isStatic = true)
        manager.allocateParameter("x", IntType)
        manager.allocateParameter("y", FloatType)
        manager.allocateRegister(Register("r0", BoolType))
        assertEquals(3, manager.totalSlots())
    }

    @Test
    fun `total slots for instance includes this`() {
        val manager = LocalVariableManager(isStatic = false)
        assertEquals(1, manager.totalSlots())
        manager.allocateParameter("x", IntType)
        assertEquals(2, manager.totalSlots())
    }

    @Test
    fun `reset clears state`() {
        val manager = LocalVariableManager(isStatic = true)
        manager.allocateParameter("x", IntType)
        manager.allocateRegister(Register("r0", IntType))
        manager.reset(isStatic = true)
        assertEquals(0, manager.totalSlots())
    }

    @Test
    fun `reset changes to instance mode`() {
        val manager = LocalVariableManager(isStatic = true)
        manager.reset(isStatic = false)
        val slot = manager.allocateParameter("x", IntType)
        assertEquals(1, slot)
    }

    @Test
    fun `duplicate register returns same slot`() {
        val manager = LocalVariableManager(isStatic = true)
        val reg = Register("r0", IntType)
        val slot1 = manager.allocateRegister(reg)
        val slot2 = manager.allocateRegister(reg)
        assertEquals(slot1, slot2)
    }

    @Test
    fun `slotFor throws for unallocated`() {
        val manager = LocalVariableManager(isStatic = true)
        val reg = Register("unknown", IntType)
        assertFailsWith<IllegalArgumentException> {
            manager.slotFor(reg)
        }
    }

    @Test
    fun `slotForParam throws for unallocated`() {
        val manager = LocalVariableManager(isStatic = true)
        assertFailsWith<IllegalArgumentException> {
            manager.slotForParam("unknown")
        }
    }

    @Test
    fun `slotForParam returns correct slot`() {
        val manager = LocalVariableManager(isStatic = true)
        manager.allocateParameter("x", IntType)
        manager.allocateParameter("y", FloatType)
        assertEquals(0, manager.slotForParam("x"))
        assertEquals(1, manager.slotForParam("y"))
    }

    @Test
    fun `slotFor returns correct slot`() {
        val manager = LocalVariableManager(isStatic = true)
        val reg0 = Register("r0", IntType)
        val reg1 = Register("r1", FloatType)
        manager.allocateRegister(reg0)
        manager.allocateRegister(reg1)
        assertEquals(0, manager.slotFor(reg0))
        assertEquals(1, manager.slotFor(reg1))
    }

    @Test
    fun `isAllocated returns true for allocated register`() {
        val manager = LocalVariableManager(isStatic = true)
        val reg = Register("r0", IntType)
        manager.allocateRegister(reg)
        assertEquals(true, manager.isAllocated(reg))
    }

    @Test
    fun `isAllocated returns false for unallocated register`() {
        val manager = LocalVariableManager(isStatic = true)
        assertEquals(false, manager.isAllocated(Register("r0", IntType)))
    }
}
