package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsSetTest {

    @Test
    public fun `of creates set with elements`() {
        val set = HsSet.of(1, 2, 3)
        assertEquals(3, set.size())
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    public fun `of deduplicates elements`() {
        val set = HsSet.of(1, 2, 2, 3, 3, 3)
        assertEquals(3, set.size())
    }

    @Test
    public fun `empty creates empty set`() {
        val set = HsSet.empty()
        assertEquals(0, set.size())
        assertTrue(set.isEmpty())
        assertFalse(set.isNotEmpty())
    }

    @Test
    public fun `contains returns true for present element`() {
        val set = HsSet.of("a", "b", "c")
        assertTrue(set.contains("b"))
        assertFalse(set.contains("z"))
    }

    @Test
    public fun `containsAll returns true for subset`() {
        val set = HsSet.of(1, 2, 3, 4)
        assertTrue(set.containsAll(listOf(1, 3)))
        assertFalse(set.containsAll(listOf(1, 5)))
    }

    @Test
    public fun `union merges sets`() {
        val a = HsSet.of(1, 2, 3)
        val b = HsSet.of(3, 4, 5)
        val result = a.union(b)
        assertEquals(5, result.size())
        assertTrue(result.contains(1))
        assertTrue(result.contains(3))
        assertTrue(result.contains(5))
    }

    @Test
    public fun `intersect keeps common elements`() {
        val a = HsSet.of(1, 2, 3, 4)
        val b = HsSet.of(2, 4, 6)
        val result = a.intersect(b)
        assertEquals(2, result.size())
        assertTrue(result.contains(2))
        assertTrue(result.contains(4))
        assertFalse(result.contains(1))
    }

    @Test
    public fun `minus returns set difference`() {
        val a = HsSet.of(1, 2, 3, 4)
        val b = HsSet.of(2, 4, 6)
        val result = a.minus(b)
        assertEquals(2, result.size())
        assertTrue(result.contains(1))
        assertTrue(result.contains(3))
        assertFalse(result.contains(2))
    }

    @Test
    public fun `filter keeps matching elements`() {
        val set = HsSet.of(1, 2, 3, 4, 5)
        val filtered = set.filter { (it as Int) > 3 }
        assertEquals(2, filtered.size())
        assertTrue(filtered.contains(4))
        assertTrue(filtered.contains(5))
    }

    @Test
    public fun `filterNot keeps non-matching elements`() {
        val set = HsSet.of(1, 2, 3, 4, 5)
        val filtered = set.filterNot { (it as Int) > 3 }
        assertEquals(3, filtered.size())
        assertTrue(filtered.contains(1))
        assertTrue(filtered.contains(3))
    }

    @Test
    public fun `map transforms elements`() {
        val set = HsSet.of(1, 2, 3)
        val mapped = set.map { (it as Int) * 10 }
        assertEquals(3, mapped.size())
        assertTrue(mapped.contains(10))
        assertTrue(mapped.contains(20))
        assertTrue(mapped.contains(30))
    }

    @Test
    public fun `flatMap flattens iterables`() {
        val set = HsSet.of(1, 2, 3)
        val result = set.flatMap { listOf(it, it) }
        assertEquals(3, result.size())
    }

    @Test
    public fun `any all none count work correctly`() {
        val set = HsSet.of(1, 2, 3, 4, 5)
        assertTrue(set.any { (it as Int) > 4 })
        assertFalse(set.any { (it as Int) > 10 })
        assertTrue(set.all { (it as Int) > 0 })
        assertFalse(set.all { (it as Int) > 3 })
        assertTrue(set.none { (it as Int) > 10 })
        assertFalse(set.none { (it as Int) > 3 })
        assertEquals(3, set.count { (it as Int) > 2 })
    }

    @Test
    public fun `toMutableSet returns independent copy`() {
        val set = HsSet.of(1, 2, 3)
        val mutable = set.toMutableSet()
        mutable.add(4)
        assertEquals(3, set.size())
        assertEquals(4, mutable.size())
    }

    @Test
    public fun `toList returns HsList`() {
        val set = HsSet.of(1, 2, 3)
        val list = set.toList()
        assertEquals(3, list.size())
        assertTrue(list.contains(1))
        assertTrue(list.contains(2))
        assertTrue(list.contains(3))
    }

    @Test
    public fun `toTypedArray returns JVM array`() {
        val set = HsSet.of(1, 2, 3)
        val arr = set.toTypedArray()
        assertEquals(3, arr.size)
    }

    @Test
    public fun `forEach iterates all elements`() {
        val set = HsSet.of(1, 2, 3)
        val result = mutableListOf<Any?>()
        set.forEach { result.add(it) }
        assertEquals(3, result.size)
        assertTrue(result.contains(1))
        assertTrue(result.contains(2))
        assertTrue(result.contains(3))
    }

    @Test
    public fun `equals and hashCode work`() {
        val a = HsSet.of(1, 2, 3)
        val b = HsSet.of(3, 1, 2)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    public fun `joinToString returns elements separated`() {
        val set = HsSet.of("x", "y")
        val str = set.joinToString(", ")
        assertTrue(str.contains("x"))
        assertTrue(str.contains("y"))
        assertTrue(str.contains(", "))
    }

    @Test
    public fun `null elements are supported`() {
        val set = HsSet.of(1, null)
        assertEquals(2, set.size())
        assertTrue(set.contains(null))
        assertTrue(set.contains(1))
    }

    @Test
    public fun `javaSet exposes backing set`() {
        val set = HsSet.of(1, 2, 3)
        assertEquals(3, set.javaSet.size)
        assertTrue(set.javaSet.contains(2))
    }

    @Test
    public fun `empty set operations`() {
        val empty = HsSet.empty()
        val nonEmpty = HsSet.of(1, 2)
        assertEquals(2, empty.union(nonEmpty).size())
        assertEquals(0, empty.intersect(nonEmpty).size())
        assertEquals(0, empty.minus(nonEmpty).size())
    }

    @Test
    public fun `toString contains HsSet prefix`() {
        val set = HsSet.of(1)
        assertTrue(set.toString().startsWith("HsSet("))
    }
}
