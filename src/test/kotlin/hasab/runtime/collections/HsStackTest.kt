package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsStackTest {

    @Test
    public fun `of creates stack with elements`() {
        val stack = HsStack.of(1, 2, 3)
        assertEquals(3, stack.size())
        assertEquals(3, stack.peek())
    }

    @Test
    public fun `of pushes first element to bottom`() {
        val stack = HsStack.of(1, 2, 3)
        assertEquals(3, stack.peek())
    }

    @Test
    public fun `empty creates empty stack`() {
        val stack = HsStack.empty()
        assertEquals(0, stack.size())
        assertTrue(stack.isEmpty())
        assertFalse(stack.isNotEmpty())
    }

    @Test
    public fun `push adds element to top`() {
        val stack = HsStack.empty()
        stack.push(1)
        assertEquals(1, stack.size())
        assertEquals(1, stack.peek())
    }

    @Test
    public fun `push maintains LIFO order`() {
        val stack = HsStack.empty()
        stack.push(1)
        stack.push(2)
        stack.push(3)
        assertEquals(3, stack.peek())
        assertEquals(3, stack.size())
    }

    @Test
    public fun `pop removes and returns top element`() {
        val stack = HsStack.of(1, 2, 3)
        assertEquals(3, stack.pop())
        assertEquals(2, stack.size())
        assertEquals(2, stack.peek())
    }

    @Test
    public fun `pop throws NoSuchElementException when empty`() {
        val stack = HsStack.empty()
        var threw = false
        try {
            stack.pop()
        } catch (_: NoSuchElementException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    public fun `peek returns top element without removing`() {
        val stack = HsStack.of(10, 20)
        assertEquals(20, stack.peek())
        assertEquals(2, stack.size())
    }

    @Test
    public fun `peek returns null when empty`() {
        val stack = HsStack.empty()
        assertNull(stack.peek())
    }

    @Test
    public fun `search finds distance from top`() {
        val stack = HsStack.of(1, 2, 3, 4, 5)
        assertEquals(1, stack.search(5))
        assertEquals(3, stack.search(3))
        assertEquals(5, stack.search(1))
    }

    @Test
    public fun `search returns minus one for missing element`() {
        val stack = HsStack.of(1, 2, 3)
        assertEquals(-1, stack.search(99))
    }

    @Test
    public fun `contains returns true for present element`() {
        val stack = HsStack.of(1, 2, 3)
        assertTrue(stack.contains(2))
        assertFalse(stack.contains(99))
    }

    @Test
    public fun `clear empties the stack`() {
        val stack = HsStack.of(1, 2, 3)
        stack.clear()
        assertEquals(0, stack.size())
        assertTrue(stack.isEmpty())
    }

    @Test
    public fun `LIFO order preserved through mixed operations`() {
        val stack = HsStack.empty()
        stack.push(1)
        stack.push(2)
        assertEquals(2, stack.pop())
        stack.push(3)
        assertEquals(3, stack.pop())
        assertEquals(1, stack.pop())
        assertTrue(stack.isEmpty())
    }

    @Test
    public fun `toList returns HsList in LIFO order top first`() {
        val stack = HsStack.of(1, 2, 3)
        val list = stack.toList()
        assertEquals(3, list.size())
        assertEquals(3, list.get(0))
        assertEquals(2, list.get(1))
        assertEquals(1, list.get(2))
    }

    @Test
    public fun `toReversed returns HsList from bottom to top`() {
        val stack = HsStack.of(1, 2, 3)
        val list = stack.toReversed()
        assertEquals(3, list.size())
        assertEquals(1, list.get(0))
        assertEquals(2, list.get(1))
        assertEquals(3, list.get(2))
    }

    @Test
    public fun `toArray returns LIFO array top first`() {
        val stack = HsStack.of(1, 2, 3)
        val arr = stack.toArray()
        assertEquals(3, arr.size)
        assertEquals(3, arr[0])
        assertEquals(2, arr[1])
        assertEquals(1, arr[2])
    }

    @Test
    public fun `forEach iterates from top to bottom`() {
        val stack = HsStack.of(1, 2, 3)
        val result = mutableListOf<Any?>()
        stack.forEach { result.add(it) }
        assertEquals(3, result[0])
        assertEquals(2, result[1])
        assertEquals(1, result[2])
    }

    @Test
    public fun `search finds closest to top for duplicates`() {
        val stack = HsStack.empty()
        stack.push(1)
        stack.push(2)
        stack.push(1)
        assertEquals(1, stack.search(1))
        assertEquals(2, stack.search(2))
    }

    @Test
    public fun `null elements are supported`() {
        val stack = HsStack.empty()
        stack.push(null)
        stack.push(1)
        stack.push(null)
        assertEquals(3, stack.size())
        assertNull(stack.peek())
        assertNull(stack.pop())
        assertEquals(1, stack.pop())
        assertNull(stack.pop())
    }

    @Test
    public fun `equals and hashCode work`() {
        val a = HsStack.of(1, 2, 3)
        val b = HsStack.of(1, 2, 3)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    public fun `toString contains HsStack prefix`() {
        val stack = HsStack.of(1)
        assertTrue(stack.toString().startsWith("HsStack("))
    }

    @Test
    public fun `single element stack`() {
        val stack = HsStack.of(42)
        assertEquals(42, stack.peek())
        assertEquals(42, stack.pop())
        assertTrue(stack.isEmpty())
    }

    @Test
    public fun `search on empty stack returns minus one`() {
        val stack = HsStack.empty()
        assertEquals(-1, stack.search(1))
    }
}
