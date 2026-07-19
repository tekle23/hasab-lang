package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsListTest {

    @Test
    public fun `of creates list with elements`() {
        val list = HsList.of(1, "hello", 3.0)
        assertEquals(3, list.size())
        assertEquals(1, list.get(0))
        assertEquals("hello", list.get(1))
        assertEquals(3.0, list.get(2))
    }

    @Test
    public fun `empty creates list with size zero`() {
        val list = HsList.empty()
        assertEquals(0, list.size())
        assertTrue(list.isEmpty())
        assertFalse(list.isNotEmpty())
    }

    @Test
    public fun `get throws IndexOutOfBoundsException for out of range`() {
        val list = HsList.of(1, 2, 3)
        var threw = false
        try {
            list.get(5)
        } catch (_: IndexOutOfBoundsException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    public fun `contains returns true for existing element`() {
        val list = HsList.of("a", "b", "c")
        assertTrue(list.contains("b"))
        assertFalse(list.contains("z"))
    }

    @Test
    public fun `indexOf returns correct index`() {
        val list = HsList.of(10, 20, 30, 20)
        assertEquals(1, list.indexOf(20))
        assertEquals(-1, list.indexOf(99))
    }

    @Test
    public fun `lastIndexOf returns last occurrence`() {
        val list = HsList.of(10, 20, 30, 20)
        assertEquals(3, list.lastIndexOf(20))
        assertEquals(-1, list.lastIndexOf(99))
    }

    @Test
    public fun `first and last return correct elements`() {
        val list = HsList.of(1, 2, 3)
        assertEquals(1, list.first())
        assertEquals(3, list.last())
    }

    @Test
    public fun `first and last return null for empty list`() {
        val list = HsList.empty()
        assertNull(list.first())
        assertNull(list.last())
    }

    @Test
    public fun `reversed returns elements in reverse order`() {
        val list = HsList.of(1, 2, 3)
        val reversed = list.reversed()
        assertEquals(3, reversed.size())
        assertEquals(3, reversed.get(0))
        assertEquals(2, reversed.get(1))
        assertEquals(1, reversed.get(2))
    }

    @Test
    public fun `distinct removes duplicates`() {
        val list = HsList.of(1, 2, 2, 3, 1)
        val distinct = list.distinct()
        assertEquals(3, distinct.size())
        assertTrue(distinct.contains(1))
        assertTrue(distinct.contains(2))
        assertTrue(distinct.contains(3))
    }

    @Test
    public fun `take returns first n elements`() {
        val list = HsList.of(1, 2, 3, 4, 5)
        val taken = list.take(3)
        assertEquals(3, taken.size())
        assertEquals(1, taken.get(0))
        assertEquals(3, taken.get(2))
    }

    @Test
    public fun `drop returns all but first n elements`() {
        val list = HsList.of(1, 2, 3, 4, 5)
        val dropped = list.drop(2)
        assertEquals(3, dropped.size())
        assertEquals(3, dropped.get(0))
        assertEquals(5, dropped.get(2))
    }

    @Test
    public fun `subList returns correct range`() {
        val list = HsList.of(1, 2, 3, 4, 5)
        val sub = list.subList(1, 4)
        assertEquals(3, sub.size())
        assertEquals(2, sub.get(0))
        assertEquals(4, sub.get(2))
    }

    @Test
    public fun `map transforms elements`() {
        val list = HsList.of(1, 2, 3)
        val mapped = list.map { (it as Int) * 10 }
        assertEquals(3, mapped.size())
        assertEquals(10, mapped.get(0))
        assertEquals(20, mapped.get(1))
        assertEquals(30, mapped.get(2))
    }

    @Test
    public fun `filter keeps matching elements`() {
        val list = HsList.of(1, 2, 3, 4, 5, 6)
        val filtered = list.filter { (it as Int) % 2 == 0 }
        assertEquals(3, filtered.size())
        assertTrue(filtered.contains(2))
        assertTrue(filtered.contains(4))
        assertTrue(filtered.contains(6))
    }

    @Test
    public fun `filterNot keeps non-matching elements`() {
        val list = HsList.of(1, 2, 3, 4)
        val filtered = list.filterNot { (it as Int) > 2 }
        assertEquals(2, filtered.size())
        assertEquals(1, filtered.get(0))
        assertEquals(2, filtered.get(1))
    }

    @Test
    public fun `reduce accumulates from first element`() {
        val list = HsList.of(1, 2, 3, 4)
        val result = list.reduce { acc, el -> (acc as Int) + (el as Int) }
        assertEquals(10, result)
    }

    @Test
    public fun `fold accumulates with initial value`() {
        val list = HsList.of(1, 2, 3)
        val result = list.fold(100) { acc, el -> (acc as Int) + (el as Int) }
        assertEquals(106, result)
    }

    @Test
    public fun `any all none and count work correctly`() {
        val list = HsList.of(1, 2, 3, 4, 5)
        assertTrue(list.any { (it as Int) > 4 })
        assertFalse(list.any { (it as Int) > 10 })
        assertTrue(list.all { (it as Int) > 0 })
        assertFalse(list.all { (it as Int) > 3 })
        assertTrue(list.none { (it as Int) > 10 })
        assertFalse(list.none { (it as Int) > 3 })
        assertEquals(3, list.count { (it as Int) > 2 })
    }

    @Test
    public fun `plus list concatenates`() {
        val a = HsList.of(1, 2)
        val b = HsList.of(3, 4)
        val result = a.plus(b)
        assertEquals(4, result.size())
        assertEquals(1, result.get(0))
        assertEquals(4, result.get(3))
    }

    @Test
    public fun `minus removes first occurrence`() {
        val list = HsList.of(1, 2, 3, 2)
        val result = list.minus(2)
        assertEquals(3, result.size())
        assertEquals(1, result.get(0))
        assertEquals(3, result.get(1))
        assertEquals(2, result.get(2))
    }

    @Test
    public fun `toMutableList returns independent mutable copy`() {
        val list = HsList.of(1, 2, 3)
        val mutable = list.toMutableList()
        mutable.add(4)
        assertEquals(3, list.size())
        assertEquals(4, mutable.size())
    }

    @Test
    public fun `equals and hashCode work`() {
        val a = HsList.of(1, 2, 3)
        val b = HsList.of(1, 2, 3)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    public fun `joinToString returns comma separated`() {
        val list = HsList.of(1, 2, 3)
        assertEquals("1, 2, 3", list.joinToString(", "))
    }

    @Test
    public fun `zip pairs elements from both lists`() {
        val a = HsList.of(1, 2, 3)
        val b = HsList.of("a", "b")
        val zipped = a.zip(b)
        assertEquals(2, zipped.size())
    }

    @Test
    public fun `fromJavaList wraps existing list`() {
        val javaList = listOf<Any?>(10, null, "text")
        val hsList = HsList.fromJavaList(javaList)
        assertEquals(3, hsList.size())
        assertEquals(10, hsList.get(0))
        assertNull(hsList.get(1))
        assertEquals("text", hsList.get(2))
    }

    @Test
    public fun `null elements are supported`() {
        val list = HsList.of(1, null, 3)
        assertEquals(3, list.size())
        assertNull(list.get(1))
        assertTrue(list.contains(null))
        assertEquals(1, list.indexOf(null))
    }

    @Test
    public fun `forEach and forEachIndexed iterate correctly`() {
        val list = HsList.of("a", "b", "c")
        val result = mutableListOf<Any?>()
        list.forEach { result.add(it) }
        assertEquals(3, result.size)
        assertEquals("a", result[0])
        assertEquals("b", result[1])
        assertEquals("c", result[2])

        val indexed = mutableListOf<Pair<Int, Any?>>()
        list.forEachIndexed { index, element -> indexed.add(index to element) }
        assertEquals(0 to "a", indexed[0])
        assertEquals(2 to "c", indexed[2])
    }

    @Test
    public fun `toSet returns HsSet with distinct elements`() {
        val list = HsList.of(1, 2, 2, 3)
        val set = list.toSet()
        assertEquals(3, set.size())
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    public fun `sorted returns sorted list`() {
        val list = HsList.of(3, 1, 2)
        val sorted = list.sorted()
        assertEquals(1, sorted.get(0))
        assertEquals(2, sorted.get(1))
        assertEquals(3, sorted.get(2))
    }

    @Test
    public fun `distinctBy deduplicates by selector`() {
        val list = HsList.of(1, 2, 3, 4, 5, 6)
        val result = list.distinctBy { (it as Int) % 2 }
        assertEquals(2, result.size())
    }

    @Test
    public fun `flatMap flattens iterables`() {
        val list = HsList.of(1, 2, 3)
        val result = list.flatMap { listOf(it, it) }
        assertEquals(6, result.size())
        assertEquals(1, result.get(0))
        assertEquals(1, result.get(1))
        assertEquals(3, result.get(4))
        assertEquals(3, result.get(5))
    }

    @Test
    public fun `slice returns elements at given indices`() {
        val list = HsList.of(10, 20, 30, 40, 50)
        val sliced = list.slice(1..3)
        assertEquals(3, sliced.size())
        assertEquals(20, sliced.get(0))
        assertEquals(40, sliced.get(2))
    }

    @Test
    public fun `toArray returns JVM array`() {
        val list = HsList.of(1, 2, 3)
        val arr = list.toArray()
        assertEquals(3, arr.size)
        assertEquals(1, arr[0])
        assertEquals(3, arr[2])
    }

    @Test
    public fun `plus element appends single element`() {
        val list = HsList.of(1, 2)
        val result = list.plus(3)
        assertEquals(3, result.size())
        assertEquals(3, result.get(2))
    }

    @Test
    public fun `javaList exposes backing list`() {
        val list = HsList.of(1, 2, 3)
        assertEquals(3, list.javaList.size)
        assertEquals(listOf(1, 2, 3), list.javaList)
    }
}
