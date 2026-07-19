package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsMutableListTest {

    @Test
    public fun `of creates mutable list with elements`() {
        val list = HsMutableList.of(1, 2, 3)
        assertEquals(3, list.size())
        assertEquals(1, list.get(0))
        assertEquals(3, list.get(2))
    }

    @Test
    public fun `empty creates empty mutable list`() {
        val list = HsMutableList.empty()
        assertEquals(0, list.size())
        assertTrue(list.isEmpty())
    }

    @Test
    public fun `create with capacity creates empty list`() {
        val list = HsMutableList.create(10)
        assertEquals(0, list.size())
        assertTrue(list.isEmpty())
    }

    @Test
    public fun `add appends element`() {
        val list = HsMutableList.of(1, 2)
        assertTrue(list.add(3))
        assertEquals(3, list.size())
        assertEquals(3, list.get(2))
    }

    @Test
    public fun `add at index inserts element`() {
        val list = HsMutableList.of(1, 3)
        list.add(1, 2)
        assertEquals(3, list.size())
        assertEquals(2, list.get(1))
        assertEquals(3, list.get(2))
    }

    @Test
    public fun `addAll appends multiple elements`() {
        val list = HsMutableList.of(1)
        assertTrue(list.addAll(listOf(2, 3, 4)))
        assertEquals(4, list.size())
        assertEquals(4, list.get(3))
    }

    @Test
    public fun `remove removes first occurrence`() {
        val list = HsMutableList.of(1, 2, 3, 2)
        assertTrue(list.remove(2))
        assertEquals(3, list.size())
        assertEquals(3, list.get(1))
    }

    @Test
    public fun `remove returns false for absent element`() {
        val list = HsMutableList.of(1, 2, 3)
        assertFalse(list.remove(99))
        assertEquals(3, list.size())
    }

    @Test
    public fun `removeAt removes and returns element`() {
        val list = HsMutableList.of(10, 20, 30)
        val removed = list.removeAt(1)
        assertEquals(20, removed)
        assertEquals(2, list.size())
        assertEquals(30, list.get(1))
    }

    @Test
    public fun `set replaces element and returns old`() {
        val list = HsMutableList.of(1, 2, 3)
        val old = list.set(1, 99)
        assertEquals(2, old)
        assertEquals(99, list.get(1))
        assertEquals(3, list.size())
    }

    @Test
    public fun `clear empties the list`() {
        val list = HsMutableList.of(1, 2, 3)
        list.clear()
        assertEquals(0, list.size())
        assertTrue(list.isEmpty())
    }

    @Test
    public fun `sort sorts with comparator`() {
        val list = HsMutableList.of(3, 1, 4, 1, 5)
        list.sort(compareBy { it as Int })
        assertEquals(1, list.get(0))
        assertEquals(1, list.get(1))
        assertEquals(3, list.get(2))
        assertEquals(4, list.get(3))
        assertEquals(5, list.get(4))
    }

    @Test
    public fun `reverse reverses in place`() {
        val list = HsMutableList.of(1, 2, 3, 4)
        list.reverse()
        assertEquals(4, list.get(0))
        assertEquals(3, list.get(1))
        assertEquals(2, list.get(2))
        assertEquals(1, list.get(3))
    }

    @Test
    public fun `shuffle changes order`() {
        val list = HsMutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        list.shuffle()
        assertEquals(10, list.size())
        assertTrue(list.contains(1))
        assertTrue(list.contains(10))
    }

    @Test
    public fun `removeAll removes all matching elements`() {
        val list = HsMutableList.of(1, 2, 3, 4, 5)
        assertTrue(list.removeAll(listOf(2, 4)))
        assertEquals(3, list.size())
        assertFalse(list.contains(2))
        assertFalse(list.contains(4))
    }

    @Test
    public fun `retainAll keeps only matching elements`() {
        val list = HsMutableList.of(1, 2, 3, 4, 5)
        assertTrue(list.retainAll(listOf(2, 3, 6)))
        assertEquals(2, list.size())
        assertTrue(list.contains(2))
        assertTrue(list.contains(3))
    }

    @Test
    public fun `get throws IndexOutOfBoundsException`() {
        val list = HsMutableList.of(1)
        var threw = false
        try {
            list.get(5)
        } catch (_: IndexOutOfBoundsException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    public fun `filter returns HsList not HsMutableList`() {
        val list = HsMutableList.of(1, 2, 3, 4, 5)
        val filtered = list.filter { (it as Int) > 2 }
        assertEquals(3, filtered.size())
        assertTrue(filtered.contains(3))
        assertTrue(filtered.contains(4))
        assertTrue(filtered.contains(5))
    }

    @Test
    public fun `map returns transformed HsList`() {
        val list = HsMutableList.of(1, 2, 3)
        val mapped = list.map { (it as Int) * 2 }
        assertEquals(2, mapped.get(0))
        assertEquals(4, mapped.get(1))
        assertEquals(6, mapped.get(2))
    }

    @Test
    public fun `toMutableList returns independent copy`() {
        val list = HsMutableList.of(1, 2, 3)
        val copy = list.toMutableList()
        copy.add(4)
        assertEquals(3, list.size())
        assertEquals(4, copy.size())
    }

    @Test
    public fun `list remains mutable after operations`() {
        val list = HsMutableList.of(1, 2, 3)
        list.add(4)
        list.remove(1)
        assertEquals(3, list.size())
        assertFalse(list.contains(1))
        assertTrue(list.contains(4))
    }

    @Test
    public fun `null elements are supported`() {
        val list = HsMutableList.empty()
        list.add(null)
        list.add(1)
        list.add(null)
        assertEquals(3, list.size())
        assertNull(list.get(0))
        assertEquals(1, list.get(1))
        assertNull(list.get(2))
    }

    @Test
    public fun `zip returns HsList`() {
        val a = HsMutableList.of(1, 2, 3)
        val b = HsList.of("a", "b")
        val zipped = a.zip(b)
        assertEquals(2, zipped.size())
    }

    @Test
    public fun `plus returns HsList`() {
        val a = HsMutableList.of(1, 2)
        val b = HsList.of(3, 4)
        val result = a.plus(b)
        assertEquals(4, result.size())
        assertEquals(1, result.get(0))
        assertEquals(4, result.get(3))
    }

    @Test
    public fun `minus returns HsList`() {
        val list = HsMutableList.of(1, 2, 3)
        val result = list.minus(2)
        assertEquals(2, result.size())
        assertTrue(result.contains(1))
        assertTrue(result.contains(3))
        assertFalse(result.contains(2))
    }

    @Test
    public fun `toArray returns JVM array`() {
        val list = HsMutableList.of(1, 2, 3)
        val arr = list.toArray()
        assertEquals(3, arr.size)
        assertEquals(1, arr[0])
        assertEquals(3, arr[2])
    }

    @Test
    public fun `toSet returns HsSet with distinct elements`() {
        val list = HsMutableList.of(1, 2, 2, 3)
        val set = list.toSet()
        assertEquals(3, set.size())
    }

    @Test
    public fun `addAll at index inserts at position`() {
        val list = HsMutableList.of(1, 4, 5)
        list.addAll(1, listOf(2, 3))
        assertEquals(5, list.size())
        assertEquals(2, list.get(1))
        assertEquals(3, list.get(2))
        assertEquals(4, list.get(3))
    }

    @Test
    public fun `javaList exposes backing list`() {
        val list = HsMutableList.of(1, 2, 3)
        assertEquals(3, list.javaList.size)
        assertEquals(1, list.javaList[0])
    }

    @Test
    public fun `removeAt throws IndexOutOfBoundsException for invalid index`() {
        val list = HsMutableList.of(1)
        var threw = false
        try {
            list.removeAt(10)
        } catch (_: IndexOutOfBoundsException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    public fun `set throws IndexOutOfBoundsException for invalid index`() {
        val list = HsMutableList.of(1)
        var threw = false
        try {
            list.set(5, 10)
        } catch (_: IndexOutOfBoundsException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    public fun `sorted returns HsList`() {
        val list = HsMutableList.of(3, 1, 2)
        val sorted = list.sorted()
        assertEquals(1, sorted.get(0))
        assertEquals(2, sorted.get(1))
        assertEquals(3, sorted.get(2))
    }

    @Test
    public fun `distinct returns HsList`() {
        val list = HsMutableList.of(1, 1, 2, 3, 3, 3)
        val distinct = list.distinct()
        assertEquals(3, distinct.size())
    }

    @Test
    public fun `equals with HsList`() {
        val mutable = HsMutableList.of(1, 2, 3)
        val immutable = HsList.of(1, 2, 3)
        assertTrue(mutable == immutable)
    }
}
