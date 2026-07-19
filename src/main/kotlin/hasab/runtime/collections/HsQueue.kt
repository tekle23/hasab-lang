package hasab.runtime.collections

/**
 * Queue wrapper for the HASAB runtime (FIFO).
 *
 * Wraps an [ArrayDeque] internally to provide first-in-first-out semantics.
 */
public class HsQueue internal constructor(
    private val backing: ArrayDeque<Any?>,
) {

    /**
     * Returns the number of elements in this queue.
     */
    public fun size(): Int = backing.size

    /**
     * Returns `true` if this queue contains no elements.
     */
    public fun isEmpty(): Boolean = backing.isEmpty()

    /**
     * Returns `true` if this queue contains one or more elements.
     */
    public fun isNotEmpty(): Boolean = backing.isNotEmpty()

    /**
     * Returns the element at the front of this queue without removing it,
     * or `null` if the queue is empty.
     */
    public fun peek(): Any? = backing.firstOrNull()

    /**
     * Removes and returns the element at the front of this queue,
     * or `null` if the queue is empty.
     */
    public fun poll(): Any? = backing.removeFirstOrNull()

    /**
     * Adds [element] to the end of this queue.
     *
     * @return `true` always, for compatibility with [java.util.Queue.offer].
     */
    public fun offer(element: Any?): Boolean = backing.addLast(element).let { true }

    /**
     * Adds [element] to the end of this queue.
     *
     * @return `true` always.
     */
    public fun add(element: Any?): Boolean = backing.addLast(element).let { true }

    /**
     * Removes and returns the element at the front of this queue.
     *
     * @throws NoSuchElementException if the queue is empty.
     */
    public fun remove(): Any? = backing.removeFirst()

    /**
     * Returns `true` if this queue contains [element].
     */
    public fun contains(element: Any?): Boolean = backing.contains(element)

    /**
     * Removes all elements from this queue.
     */
    public fun clear() {
        backing.clear()
    }

    /**
     * Returns a JVM array containing the elements of this queue in order.
     */
    public fun toArray(): Array<Any?> = backing.toTypedArray()

    /**
     * Returns an [HsList] containing the elements of this queue in order.
     */
    public fun toList(): HsList = HsList(backing.toList())

    /**
     * Performs [action] for each element in this queue, from front to back.
     */
    public fun forEach(action: (Any?) -> Unit) {
        backing.forEach(action)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsQueue) return false
        return backing.toList() == other.backing.toList()
    }

    override fun hashCode(): Int = backing.toList().hashCode()

    override fun toString(): String = "HsQueue(${backing.toList()})"

    public companion object {

        /**
         * Creates an [HsQueue] from the given [elements], enqueued in order.
         */
        public fun of(vararg elements: Any?): HsQueue = HsQueue(ArrayDeque(elements.toList()))

        /**
         * Creates an empty [HsQueue].
         */
        public fun empty(): HsQueue = HsQueue(ArrayDeque())
    }
}
