package hasab.runtime.collections

/**
 * Immutable list wrapper for the HASAB runtime.
 *
 * Wraps a [List] of nullable [Any] values and exposes a comprehensive
 * set of read-only operations. Use [HsMutableList] for mutation.
 */
public class HsList internal constructor(
    private val backing: List<Any?>,
) {

    /**
     * Returns the number of elements in this list.
     */
    public fun size(): Int = backing.size

    /**
     * Returns `true` if this list contains no elements.
     */
    public fun isEmpty(): Boolean = backing.isEmpty()

    /**
     * Returns `true` if this list contains one or more elements.
     */
    public fun isNotEmpty(): Boolean = backing.isNotEmpty()

    /**
     * Returns the element at the given [index].
     *
     * @throws IndexOutOfBoundsException if [index] is out of range.
     */
    public fun get(index: Int): Any? = backing[index]

    /**
     * Returns `true` if this list contains [element].
     */
    public fun contains(element: Any?): Boolean = backing.contains(element)

    /**
     * Returns the index of [element], or `-1` if it is not found.
     */
    public fun indexOf(element: Any?): Int = backing.indexOf(element)

    /**
     * Returns the last index of [element], or `-1` if it is not found.
     */
    public fun lastIndexOf(element: Any?): Int = backing.lastIndexOf(element)

    /**
     * Returns a new [HsList] containing elements from [fromIndex] (inclusive)
     * to [toIndex] (exclusive).
     */
    public fun subList(fromIndex: Int, toIndex: Int): HsList =
        HsList(backing.subList(fromIndex, toIndex))

    /**
     * Returns the first element, or `null` if the list is empty.
     */
    public fun first(): Any? = backing.firstOrNull()

    /**
     * Returns the last element, or `null` if the list is empty.
     */
    public fun last(): Any? = backing.lastOrNull()

    /**
     * Returns a new [HsList] with elements in reverse order.
     */
    public fun reversed(): HsList = HsList(backing.reversed())

    /**
     * Returns a new [HsList] with elements sorted in natural order.
     */
    @Suppress("UNCHECKED_CAST")
    public fun sorted(): HsList = HsList((backing as List<Comparable<Any?>>).sortedWith(Comparator.nullsLast(compareBy { it as? Comparable<Any?> })))

    /**
     * Returns a new [HsList] containing only distinct elements.
     */
    public fun distinct(): HsList = HsList(backing.distinct())

    /**
     * Returns a new [HsList] containing only distinct elements as determined by [selector].
     */
    public fun distinctBy(selector: (Any?) -> Any?): HsList = HsList(backing.distinctBy(selector))

    /**
     * Returns a new [HsList] containing the first [n] elements.
     */
    public fun take(n: Int): HsList = HsList(backing.take(n))

    /**
     * Returns a new [HsList] with the first [n] elements removed.
     */
    public fun drop(n: Int): HsList = HsList(backing.drop(n))

    /**
     * Returns a new [HsList] containing elements at the given [indices].
     */
    public fun slice(indices: IntRange): HsList = HsList(backing.slice(indices))

    /**
     * Returns a new [HsList] with elements transformed by [transform].
     */
    public fun map(transform: (Any?) -> Any?): HsList = HsList(backing.map(transform))

    /**
     * Returns a new [HsList] with elements from all iterables produced by [transform].
     */
    public fun flatMap(transform: (Any?) -> Iterable<Any?>): HsList = HsList(backing.flatMap(transform))

    /**
     * Returns a new [HsList] containing only elements matching [predicate].
     */
    public fun filter(predicate: (Any?) -> Boolean): HsList = HsList(backing.filter(predicate))

    /**
     * Returns a new [HsList] containing only elements not matching [predicate].
     */
    public fun filterNot(predicate: (Any?) -> Boolean): HsList = HsList(backing.filterNot(predicate))

    /**
     * Returns a new [HsList] containing only elements that are instances of [klass].
     */
    public fun filterIsInstance(klass: Class<*>): HsList =
        HsList(backing.filter { klass.isInstance(it) })

    /**
     * Performs [action] for each element in this list.
     */
    public fun forEach(action: (Any?) -> Unit) {
        backing.forEach(action)
    }

    /**
     * Performs [action] for each element with its index.
     */
    public fun forEachIndexed(action: (Int, Any?) -> Unit) {
        backing.forEachIndexed { index, element -> action(index, element) }
    }

    /**
     * Accumulates elements starting from the first element using [operation].
     */
    public fun reduce(operation: (Any?, Any?) -> Any?): Any? = backing.reduce(operation)

    /**
     * Accumulates elements starting from [initial] using [operation].
     */
    public fun fold(initial: Any?, operation: (Any?, Any?) -> Any?): Any? = backing.fold(initial, operation)

    /**
     * Returns `true` if at least one element matches [predicate].
     */
    public fun any(predicate: (Any?) -> Boolean): Boolean = backing.any(predicate)

    /**
     * Returns `true` if all elements match [predicate].
     */
    public fun all(predicate: (Any?) -> Boolean): Boolean = backing.all(predicate)

    /**
     * Returns `true` if no elements match [predicate].
     */
    public fun none(predicate: (Any?) -> Boolean): Boolean = backing.none(predicate)

    /**
     * Returns the count of elements matching [predicate].
     */
    public fun count(predicate: (Any?) -> Boolean): Int = backing.count(predicate)

    /**
     * Returns a string representation with elements joined by [separator].
     */
    public fun joinToString(separator: String): String = backing.joinToString(separator)

    /**
     * Returns a mutable copy of this list.
     */
    public fun toMutableList(): HsMutableList = HsMutableList(backing.toMutableList())

    /**
     * Returns a JVM array containing the elements of this list.
     */
    public fun toArray(): Array<Any?> = backing.toTypedArray()

    /**
     * Returns an [HsSet] containing the distinct elements of this list.
     */
    public fun toSet(): HsSet = HsSet(backing.toSet())

    /**
     * Returns a new [HsList] of pairs formed by zipping this list with [other].
     *
     * The resulting list length equals the minimum of both lists.
     */
    public fun zip(other: HsList): HsList = HsList(backing.zip(other.backing))

    /**
     * Returns a new [HsList] containing all elements of this list followed by all elements of [other].
     */
    public fun plus(other: HsList): HsList = HsList(backing + other.backing)

    /**
     * Returns a new [HsList] containing all elements of this list plus [element].
     */
    public fun plus(element: Any?): HsList = HsList(backing + element)

    /**
     * Returns a new [HsList] with the first occurrence of [element] removed.
     */
    public fun minus(element: Any?): HsList = HsList(backing - element)

    /**
     * The underlying JVM list. Read-only access for interop.
     */
    public val javaList: List<Any?> get() = backing

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsList) return false
        return backing == other.backing
    }

    override fun hashCode(): Int = backing.hashCode()

    override fun toString(): String = "HsList($backing)"

    public companion object {

        /**
         * Creates an [HsList] from the given [elements].
         */
        public fun of(vararg elements: Any?): HsList = HsList(elements.toList())

        /**
         * Creates an empty [HsList].
         */
        public fun empty(): HsList = HsList(emptyList())

        /**
         * Wraps an existing JVM [list] as an [HsList].
         */
        public fun fromJavaList(list: List<Any?>): HsList = HsList(list)
    }
}
