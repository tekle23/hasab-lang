package hasab.runtime.collections

/**
 * Stack wrapper for the HASAB runtime (LIFO).
 *
 * Wraps an [ArrayDeque] internally to provide last-in-first-out semantics.
 */
public class HsStack internal constructor(
    private val backing: ArrayDeque<Any?>,
) {

    /**
     * Returns the number of elements in this stack.
     */
    public fun size(): Int = backing.size

    /**
     * Returns `true` if this stack contains no elements.
     */
    public fun isEmpty(): Boolean = backing.isEmpty()

    /**
     * Returns `true` if this stack contains one or more elements.
     */
    public fun isNotEmpty(): Boolean = backing.isNotEmpty()

    /**
     * Returns the element at the top of this stack without removing it,
     * or `null` if the stack is empty.
     */
    public fun peek(): Any? = backing.lastOrNull()

    /**
     * Removes and returns the element at the top of this stack.
     *
     * @throws NoSuchElementException if the stack is empty.
     */
    public fun pop(): Any? = backing.removeLast()

    /**
     * Pushes [element] onto the top of this stack.
     */
    public fun push(element: Any?) {
        backing.addLast(element)
    }

    /**
     * Returns the 1-based distance from the top of the stack to [element],
     * or `-1` if [element] is not found.
     */
    public fun search(element: Any?): Int {
        var i = backing.size - 1
        while (i >= 0) {
            if (backing[i] == element) return backing.size - i
            i--
        }
        return -1
    }

    /**
     * Returns `true` if this stack contains [element].
     */
    public fun contains(element: Any?): Boolean = backing.contains(element)

    /**
     * Removes all elements from this stack.
     */
    public fun clear() {
        backing.clear()
    }

    /**
     * Returns a JVM array containing the elements of this stack in LIFO order (top first).
     */
    public fun toArray(): Array<Any?> = backing.reversed().toTypedArray()

    /**
     * Returns an [HsList] containing the elements of this stack in LIFO order (top first).
     */
    public fun toList(): HsList = HsList(backing.reversed())

    /**
     * Returns an [HsList] containing the elements of this stack from bottom to top.
     */
    public fun toReversed(): HsList = HsList(backing.toList())

    /**
     * Performs [action] for each element in this stack, from top to bottom.
     */
    public fun forEach(action: (Any?) -> Unit) {
        backing.reversed().forEach(action)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsStack) return false
        return backing.toList() == other.backing.toList()
    }

    override fun hashCode(): Int = backing.toList().hashCode()

    override fun toString(): String = "HsStack(${backing.reversed()})"

    public companion object {

        /**
         * Creates an [HsStack] from the given [elements].
         *
         * The first element is pushed first (bottom of the stack),
         * the last element ends up on top.
         */
        public fun of(vararg elements: Any?): HsStack = HsStack(ArrayDeque(elements.toList()))

        /**
         * Creates an empty [HsStack].
         */
        public fun empty(): HsStack = HsStack(ArrayDeque())
    }
}
