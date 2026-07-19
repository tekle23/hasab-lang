package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsQueueTest {

    @Test
    public fun `of creates queue with elements in FIFO order`() {
        val queue = HsQueue.of(1, 2, 3)
        assertEquals(3, queue.size())
        assertEquals(1, queue.peek())
    }

    @Test
    public fun `empty creates empty queue`() {
        val queue = HsQueue.empty()
        assertEquals(0, queue.size())
        assertTrue(queue.isEmpty())
        assertFalse(queue.isNotEmpty())
    }

    @Test
    public fun `offer adds element to back`() {
        val queue = HsQueue.empty()
        assertTrue(queue.offer(1))
        assertEquals(1, queue.size())
        assertEquals(1, queue.peek())
    }

    @Test
    public fun `offer maintains FIFO ordering`() {
        val queue = HsQueue.empty()
        queue.offer(1)
        queue.offer(2)
        queue.offer(3)
        assertEquals(3, queue.size())
        assertEquals(1, queue.peek())
    }

    @Test
    public fun `poll removes and returns front element`() {
        val queue = HsQueue.of(1, 2, 3)
        assertEquals(1, queue.poll())
        assertEquals(2, queue.size())
        assertEquals(2, queue.peek())
    }

    @Test
    public fun `poll returns null when queue is empty`() {
        val queue = HsQueue.empty()
        assertNull(queue.poll())
    }

    @Test
    public fun `peek returns front element without removing`() {
        val queue = HsQueue.of(10, 20)
        assertEquals(10, queue.peek())
        assertEquals(2, queue.size())
    }

    @Test
    public fun `peek returns null when queue is empty`() {
        val queue = HsQueue.empty()
        assertNull(queue.peek())
    }

    @Test
    public fun `add adds element and returns true`() {
        val queue = HsQueue.empty()
        assertTrue(queue.add(42))
        assertEquals(42, queue.peek())
        assertEquals(1, queue.size())
    }

    @Test
    public fun `remove removes and returns front element`() {
        val queue = HsQueue.of("a", "b", "c")
        assertEquals("a", queue.remove())
        assertEquals(2, queue.size())
        assertEquals("b", queue.peek())
    }

    @Test
    public fun `remove throws NoSuchElementException when empty`() {
        val queue = HsQueue.empty()
        var threw = false
        try {
            queue.remove()
        } catch (_: NoSuchElementException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    public fun `contains returns true for present element`() {
        val queue = HsQueue.of(1, 2, 3)
        assertTrue(queue.contains(2))
        assertFalse(queue.contains(99))
    }

    @Test
    public fun `clear empties the queue`() {
        val queue = HsQueue.of(1, 2, 3)
        queue.clear()
        assertEquals(0, queue.size())
        assertTrue(queue.isEmpty())
    }

    @Test
    public fun `poll all elements drains queue`() {
        val queue = HsQueue.of(1, 2, 3)
        assertEquals(1, queue.poll())
        assertEquals(2, queue.poll())
        assertEquals(3, queue.poll())
        assertNull(queue.poll())
        assertTrue(queue.isEmpty())
    }

    @Test
    public fun `FIFO order preserved through mixed operations`() {
        val queue = HsQueue.empty()
        queue.offer(1)
        queue.offer(2)
        assertEquals(1, queue.poll())
        queue.offer(3)
        assertEquals(2, queue.poll())
        assertEquals(3, queue.poll())
        assertTrue(queue.isEmpty())
    }

    @Test
    public fun `toList returns HsList in FIFO order`() {
        val queue = HsQueue.of(1, 2, 3)
        val list = queue.toList()
        assertEquals(3, list.size())
        assertEquals(1, list.get(0))
        assertEquals(2, list.get(1))
        assertEquals(3, list.get(2))
    }

    @Test
    public fun `toArray returns JVM array in order`() {
        val queue = HsQueue.of(1, 2, 3)
        val arr = queue.toArray()
        assertEquals(3, arr.size)
        assertEquals(1, arr[0])
        assertEquals(2, arr[1])
        assertEquals(3, arr[2])
    }

    @Test
    public fun `forEach iterates from front to back`() {
        val queue = HsQueue.of(10, 20, 30)
        val result = mutableListOf<Any?>()
        queue.forEach { result.add(it) }
        assertEquals(10, result[0])
        assertEquals(20, result[1])
        assertEquals(30, result[2])
    }

    @Test
    public fun `null elements are supported`() {
        val queue = HsQueue.empty()
        queue.offer(null)
        queue.offer(1)
        queue.offer(null)
        assertEquals(3, queue.size())
        assertNull(queue.poll())
        assertEquals(1, queue.poll())
        assertNull(queue.poll())
    }

    @Test
    public fun `equals and hashCode work`() {
        val a = HsQueue.of(1, 2, 3)
        val b = HsQueue.of(1, 2, 3)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    public fun `toString contains HsQueue prefix`() {
        val queue = HsQueue.of(1)
        assertTrue(queue.toString().startsWith("HsQueue("))
    }

    @Test
    public fun `single element queue`() {
        val queue = HsQueue.of(42)
        assertEquals(42, queue.peek())
        assertEquals(42, queue.poll())
        assertTrue(queue.isEmpty())
    }
}
