package hasab.runtime.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

public class HsArrayTest {

    @Test
    public fun `create builds array of given size`() {
        val arr = HsArray.create(5) { it * 2 }
        assertEquals(5, arr.size)
        assertEquals(0, arr[0])
        assertEquals(2, arr[1])
        assertEquals(8, arr[4])
    }

    @Test
    public fun `createIntArray and createFloatArray and createBoolArray`() {
        assertEquals(3, HsArray.createIntArray(3).size)
        assertEquals(3, HsArray.createFloatArray(3).size)
        assertEquals(3, HsArray.createBoolArray(3).size)
        assertEquals(3, HsArray.createCharArray(3).size)
    }

    @Test
    public fun `createStringArray initializes with empty strings`() {
        val arr = HsArray.createStringArray(3)
        assertEquals(3, arr.size)
        assertEquals("", arr[0])
        assertEquals("", arr[1])
    }

    @Test
    public fun `get and set manipulate elements`() {
        val arr = HsArray.create(3) { 0 }
        HsArray.set(arr, 1, 42)
        assertEquals(0, HsArray.get(arr, 0))
        assertEquals(42, HsArray.get(arr, 1))
    }

    @Test
    public fun `isEmpty and isNotEmpty`() {
        val empty = HsArray.create(0) { 0 }
        assertTrue(HsArray.isEmpty(empty))
        assertFalse(HsArray.isNotEmpty(empty))

        val nonEmpty = HsArray.create(1) { 0 }
        assertFalse(HsArray.isEmpty(nonEmpty))
        assertTrue(HsArray.isNotEmpty(nonEmpty))
    }

    @Test
    public fun `contains finds elements`() {
        val arr = arrayOf<Any?>(1, "hello", 3.0)
        assertTrue(HsArray.contains(arr, "hello"))
        assertFalse(HsArray.contains(arr, "world"))
        assertFalse(HsArray.contains(arr, null))
    }

    @Test
    public fun `indexOf and lastIndexOf`() {
        val arr = arrayOf<Any?>("a", "b", "a", "c")
        assertEquals(0, HsArray.indexOf(arr, "a"))
        assertEquals(2, HsArray.lastIndexOf(arr, "a"))
        assertEquals(-1, HsArray.indexOf(arr, "z"))
    }

    @Test
    public fun `copyOf creates independent copy`() {
        val original = arrayOf<Any?>(1, 2, 3)
        val copy = HsArray.copyOf(original)
        assertEquals(original.toList(), copy.toList())
        HsArray.set(copy, 0, 99)
        assertEquals(1, HsArray.get(original, 0))
    }

    @Test
    public fun `copyOfRange extracts subarray`() {
        val arr = arrayOf<Any?>(0, 1, 2, 3, 4)
        val sub = HsArray.copyOfRange(arr, 1, 4)
        assertEquals(arrayOf<Any?>(1, 2, 3).toList(), sub.toList())
    }

    @Test
    public fun `fill replaces all elements`() {
        val arr = HsArray.create(5) { 0 }
        HsArray.fill(arr, 42)
        assertTrue(arr.all { it == 42 })
    }

    @Test
    public fun `sort orders elements`() {
        val arr = arrayOf<Any?>(3, 1, 4, 1, 5)
        HsArray.sort(arr)
        assertEquals(arrayOf<Any?>(1, 1, 3, 4, 5).toList(), arr.toList())
    }

    @Test
    public fun `reverse reverses in place`() {
        val arr = arrayOf<Any?>(1, 2, 3)
        HsArray.reverse(arr)
        assertEquals(arrayOf<Any?>(3, 2, 1).toList(), arr.toList())
    }

    @Test
    public fun `slice returns subarray`() {
        val arr = arrayOf<Any?>(0, 1, 2, 3, 4)
        val sliced = HsArray.slice(arr, 1..3)
        assertEquals(arrayOf<Any?>(1, 2, 3).toList(), sliced.toList())
    }

    @Test
    public fun `map transforms elements`() {
        val arr = arrayOf<Any?>(1, 2, 3)
        val result = HsArray.map(arr) { (it as Int) * 10 }
        assertEquals(arrayOf<Any?>(10, 20, 30).toList(), result.toList())
    }

    @Test
    public fun `filter keeps matching elements`() {
        val arr = arrayOf<Any?>(1, 2, 3, 4, 5)
        val result = HsArray.filter(arr) { (it as Int) % 2 == 0 }
        assertEquals(arrayOf<Any?>(2, 4).toList(), result.toList())
    }

    @Test
    public fun `forEach executes action on each element`() {
        val arr = arrayOf<Any?>(1, 2, 3)
        var sum = 0
        HsArray.forEach(arr) { sum += it as Int }
        assertEquals(6, sum)
    }

    @Test
    public fun `reduce combines elements`() {
        val arr = arrayOf<Any?>(1, 2, 3, 4)
        val result = HsArray.reduce(arr) { a, b -> (a as Int) + (b as Int) }
        assertEquals(10, result)
    }

    @Test
    public fun `fold combines with initial value`() {
        val arr = arrayOf<Any?>(1, 2, 3)
        val result = HsArray.fold(arr, 10) { acc, elem -> (acc as Int) + (elem as Int) }
        assertEquals(16, result)
    }

    @Test
    public fun `joinToString joins with separator`() {
        val arr = arrayOf<Any?>(1, 2, 3)
        assertEquals("1, 2, 3", HsArray.joinToString(arr, ", "))
        assertEquals("123", HsArray.joinToString(arr, ""))
    }

    @Test
    public fun `toList and fromList round trip`() {
        val list = listOf<Any?>(1, "two", 3.0)
        val arr = HsArray.fromList(list)
        assertEquals(list, HsArray.toList(arr))
    }

    @Test
    public fun `flatten merges nested arrays`() {
        val nested = arrayOf(
            arrayOf<Any?>(1, 2),
            arrayOf<Any?>(3),
            arrayOf<Any?>(4, 5)
        )
        val flat = HsArray.flatten(nested)
        assertEquals(arrayOf<Any?>(1, 2, 3, 4, 5).toList(), flat.toList())
    }

    @Test
    public fun `concat joins two arrays`() {
        val a = arrayOf<Any?>(1, 2)
        val b = arrayOf<Any?>(3, 4)
        val result = HsArray.concat(a, b)
        assertEquals(arrayOf<Any?>(1, 2, 3, 4).toList(), result.toList())
    }

    @Test
    public fun `distinct removes duplicates`() {
        val arr = arrayOf<Any?>(1, 2, 1, 3, 2)
        val result = HsArray.distinct(arr)
        assertEquals(arrayOf<Any?>(1, 2, 3).toList(), result.toList())
    }

    @Test
    public fun `take and drop`() {
        val arr = arrayOf<Any?>(1, 2, 3, 4, 5)
        assertEquals(arrayOf<Any?>(1, 2, 3).toList(), HsArray.take(arr, 3).toList())
        assertEquals(arrayOf<Any?>(4, 5).toList(), HsArray.drop(arr, 3).toList())
        assertEquals(arrayOf<Any?>().toList(), HsArray.take(arr, 0).toList())
        assertEquals(arrayOf<Any?>().toList(), HsArray.drop(arr, 5).toList())
    }

    @Test
    public fun `first and last`() {
        val arr = arrayOf<Any?>(10, 20, 30)
        assertEquals(10, HsArray.first(arr))
        assertEquals(30, HsArray.last(arr))
    }

    @Test
    public fun `first and last throw on empty`() {
        val empty = arrayOf<Any?>()
        assertFailsWith<NoSuchElementException> { HsArray.first(empty) }
        assertFailsWith<NoSuchElementException> { HsArray.last(empty) }
    }

    @Test
    public fun `random returns element from array`() {
        val arr = arrayOf<Any?>("only")
        assertEquals("only", HsArray.random(arr))
    }
}
