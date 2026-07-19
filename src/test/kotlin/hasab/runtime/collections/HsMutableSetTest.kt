package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsMutableSetTest {

    @Test
    public fun `of creates mutable set with elements`() {
        val set = HsMutableSet.of(1, 2, 3)
        assertEquals(3, set.size())
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    public fun `of deduplicates elements`() {
        val set = HsMutableSet.of(1, 2, 2, 3)
        assertEquals(3, set.size())
    }

    @Test
    public fun `empty creates empty mutable set`() {
        val set = HsMutableSet.empty()
        assertEquals(0, set.size())
        assertTrue(set.isEmpty())
    }

    @Test
    public fun `add returns true when element is new`() {
        val set = HsMutableSet.empty()
        assertTrue(set.add(1))
        assertEquals(1, set.size())
        assertTrue(set.contains(1))
    }

    @Test
    public fun `add returns false when element already exists`() {
        val set = HsMutableSet.of(1)
        assertFalse(set.add(1))
        assertEquals(1, set.size())
    }

    @Test
    public fun `addAll adds multiple new elements`() {
        val set = HsMutableSet.of(1)
        assertTrue(set.addAll(listOf(2, 3, 4)))
        assertEquals(4, set.size())
    }

    @Test
    public fun `addAll returns false when no new elements added`() {
        val set = HsMutableSet.of(1, 2)
        assertFalse(set.addAll(listOf(1, 2)))
        assertEquals(2, set.size())
    }

    @Test
    public fun `remove returns true when element found`() {
        val set = HsMutableSet.of(1, 2, 3)
        assertTrue(set.remove(2))
        assertEquals(2, set.size())
        assertFalse(set.contains(2))
    }

    @Test
    public fun `remove returns false when element not found`() {
        val set = HsMutableSet.of(1, 2)
        assertFalse(set.remove(99))
        assertEquals(2, set.size())
    }

    @Test
    public fun `removeAll removes matching elements`() {
        val set = HsMutableSet.of(1, 2, 3, 4, 5)
        assertTrue(set.removeAll(listOf(2, 4)))
        assertEquals(3, set.size())
        assertFalse(set.contains(2))
        assertFalse(set.contains(4))
        assertTrue(set.contains(1))
    }

    @Test
    public fun `retainAll keeps only matching elements`() {
        val set = HsMutableSet.of(1, 2, 3, 4, 5)
        assertTrue(set.retainAll(listOf(2, 3, 6)))
        assertEquals(2, set.size())
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    public fun `clear empties the set`() {
        val set = HsMutableSet.of(1, 2, 3)
        set.clear()
        assertEquals(0, set.size())
        assertTrue(set.isEmpty())
    }

    @Test
    public fun `contains and containsAll work`() {
        val set = HsMutableSet.of(1, 2, 3)
        assertTrue(set.contains(2))
        assertFalse(set.contains(5))
        assertTrue(set.containsAll(listOf(1, 2)))
        assertFalse(set.containsAll(listOf(1, 5)))
    }

    @Test
    public fun `union returns HsSet`() {
        val a = HsMutableSet.of(1, 2)
        val b = HsSet.of(2, 3)
        val result = a.union(b)
        assertEquals(3, result.size())
        assertTrue(result.contains(1))
        assertTrue(result.contains(2))
        assertTrue(result.contains(3))
    }

    @Test
    public fun `intersect returns HsSet`() {
        val a = HsMutableSet.of(1, 2, 3)
        val b = HsSet.of(2, 3, 4)
        val result = a.intersect(b)
        assertEquals(2, result.size())
        assertTrue(result.contains(2))
        assertTrue(result.contains(3))
    }

    @Test
    public fun `minus returns HsSet`() {
        val a = HsMutableSet.of(1, 2, 3, 4)
        val b = HsSet.of(2, 4)
        val result = a.minus(b)
        assertEquals(2, result.size())
        assertTrue(result.contains(1))
        assertTrue(result.contains(3))
    }

    @Test
    public fun `filter returns HsSet`() {
        val set = HsMutableSet.of(1, 2, 3, 4, 5)
        val filtered = set.filter { (it as Int) > 3 }
        assertEquals(2, filtered.size())
        assertTrue(filtered.contains(4))
        assertTrue(filtered.contains(5))
    }

    @Test
    public fun `filterNot returns HsSet`() {
        val set = HsMutableSet.of(1, 2, 3, 4, 5)
        val filtered = set.filterNot { (it as Int) > 3 }
        assertEquals(3, filtered.size())
    }

    @Test
    public fun `map returns HsSet with transformed elements`() {
        val set = HsMutableSet.of(1, 2, 3)
        val mapped = set.map { (it as Int) * 10 }
        assertEquals(3, mapped.size())
        assertTrue(mapped.contains(10))
        assertTrue(mapped.contains(20))
        assertTrue(mapped.contains(30))
    }

    @Test
    public fun `flatMap returns HsSet`() {
        val set = HsMutableSet.of(1, 2)
        val result = set.flatMap { listOf(it, it) }
        assertEquals(2, result.size())
    }

    @Test
    public fun `any all none count work`() {
        val set = HsMutableSet.of(1, 2, 3, 4, 5)
        assertTrue(set.any { (it as Int) > 4 })
        assertFalse(set.any { (it as Int) > 10 })
        assertTrue(set.all { (it as Int) > 0 })
        assertEquals(3, set.count { (it as Int) > 2 })
    }

    @Test
    public fun `toMutableSet returns independent copy`() {
        val set = HsMutableSet.of(1, 2, 3)
        val copy = set.toMutableSet()
        copy.add(4)
        assertEquals(3, set.size())
        assertEquals(4, copy.size())
    }

    @Test
    public fun `toList returns HsList`() {
        val set = HsMutableSet.of(1, 2, 3)
        val list = set.toList()
        assertEquals(3, list.size())
        assertTrue(list.contains(1))
    }

    @Test
    public fun `toTypedArray returns JVM array`() {
        val set = HsMutableSet.of(1, 2, 3)
        val arr = set.toTypedArray()
        assertEquals(3, arr.size)
    }

    @Test
    public fun `forEach iterates all elements`() {
        val set = HsMutableSet.of(1, 2, 3)
        val result = mutableListOf<Any?>()
        set.forEach { result.add(it) }
        assertEquals(3, result.size)
    }

    @Test
    public fun `null elements are supported`() {
        val set = HsMutableSet.empty()
        set.add(null)
        set.add(1)
        assertEquals(2, set.size())
        assertTrue(set.contains(null))
    }

    @Test
    public fun `equals with HsSet`() {
        val mutable = HsMutableSet.of(1, 2, 3)
        val immutable = HsSet.of(3, 1, 2)
        assertTrue(mutable == immutable)
    }

    @Test
    public fun `javaSet exposes backing set`() {
        val set = HsMutableSet.of(1, 2)
        assertEquals(2, set.javaSet.size)
        assertTrue(set.javaSet.contains(1))
    }

    @Test
    public fun `toString contains HsMutableSet prefix`() {
        val set = HsMutableSet.of(1)
        assertTrue(set.toString().startsWith("HsMutableSet("))
    }

    @Test
    public fun `set remains mutable after operations`() {
        val set = HsMutableSet.of(1, 2, 3)
        set.remove(2)
        set.add(4)
        assertEquals(3, set.size())
        assertFalse(set.contains(2))
        assertTrue(set.contains(4))
    }

    @Test
    public fun `joinToString returns elements separated`() {
        val set = HsMutableSet.of("a", "b")
        val str = set.joinToString("-")
        assertTrue(str.contains("a"))
        assertTrue(str.contains("b"))
        assertTrue(str.contains("-"))
    }
}
