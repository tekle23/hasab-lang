package hasab.runtime.collections

/**
 * Mutable set wrapper for the HASAB runtime.
 *
 * Wraps a [MutableSet] of nullable [Any] values. Extends the read-only
 * API of [HsSet] with mutation operations.
 */
public class HsMutableSet internal constructor(
    private val backing: MutableSet<Any?>,
) {

    /**
     * Returns the number of elements in this set.
     */
    public fun size(): Int = backing.size

    /**
     * Returns `true` if this set contains no elements.
     */
    public fun isEmpty(): Boolean = backing.isEmpty()

    /**
     * Returns `true` if this set contains one or more elements.
     */
    public fun isNotEmpty(): Boolean = backing.isNotEmpty()

    /**
     * Returns `true` if this set contains [element].
     */
    public fun contains(element: Any?): Boolean = backing.contains(element)

    /**
     * Returns `true` if this set contains all elements in [elements].
     */
    public fun containsAll(elements: Collection<Any?>): Boolean = backing.containsAll(elements)

    /**
     * Performs [action] for each element in this set.
     */
    public fun forEach(action: (Any?) -> Unit) {
        backing.forEach(action)
    }

    /**
     * Returns a new [HsSet] containing only elements matching [predicate].
     */
    public fun filter(predicate: (Any?) -> Boolean): HsSet = HsSet(backing.filter(predicate).toSet())

    /**
     * Returns a new [HsSet] containing only elements not matching [predicate].
     */
    public fun filterNot(predicate: (Any?) -> Boolean): HsSet = HsSet(backing.filterNot(predicate).toSet())

    /**
     * Returns a new [HsSet] with elements transformed by [transform].
     */
    public fun map(transform: (Any?) -> Any?): HsSet = HsSet(backing.map(transform).toSet())

    /**
     * Returns a new [HsSet] with elements from all iterables produced by [transform].
     */
    public fun flatMap(transform: (Any?) -> Iterable<Any?>): HsSet =
        HsSet(backing.flatMap(transform).toSet())

    /**
     * Returns a new [HsSet] containing all elements in this set and [other] (set union).
     */
    public fun union(other: HsSet): HsSet = HsSet(backing.union(other.javaSet))

    /**
     * Returns a new [HsSet] containing only elements present in both this set and [other] (set intersection).
     */
    public fun intersect(other: HsSet): HsSet = HsSet(backing.intersect(other.javaSet))

    /**
     * Returns a new [HsSet] containing elements in this set but not in [other] (set difference).
     */
    public fun minus(other: HsSet): HsSet = HsSet(backing.minus(other.javaSet))

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
     * Returns a mutable copy as an [HsMutableSet].
     */
    public fun toMutableSet(): HsMutableSet = HsMutableSet(backing.toMutableSet())

    /**
     * Returns an [HsList] containing the elements of this set.
     */
    public fun toList(): HsList = HsList(backing.toList())

    /**
     * Returns a JVM array containing the elements of this set.
     */
    public fun toTypedArray(): Array<Any?> = backing.toTypedArray()

    /**
     * Returns a string representation with elements joined by [separator].
     */
    public fun joinToString(separator: String): String = backing.joinToString(separator)

    /**
     * Appends [element] to this set.
     *
     * @return `true` if the element was not already present.
     */
    public fun add(element: Any?): Boolean = backing.add(element)

    /**
     * Appends all [elements] to this set.
     *
     * @return `true` if the set was modified as a result.
     */
    public fun addAll(elements: Collection<Any?>): Boolean = backing.addAll(elements)

    /**
     * Removes [element] from this set.
     *
     * @return `true` if the element was found and removed.
     */
    public fun remove(element: Any?): Boolean = backing.remove(element)

    /**
     * Removes all elements that are also in [elements].
     *
     * @return `true` if the set was modified as a result.
     */
    public fun removeAll(elements: Collection<Any?>): Boolean = backing.removeAll(elements.toSet())

    /**
     * Retains only elements that are also in [elements].
     *
     * @return `true` if the set was modified as a result.
     */
    public fun retainAll(elements: Collection<Any?>): Boolean = backing.retainAll(elements.toSet())

    /**
     * Removes all elements from this set.
     */
    public fun clear() {
        backing.clear()
    }

    /**
     * The underlying JVM mutable set. Read-only access for interop.
     */
    public val javaSet: Set<Any?> get() = backing

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is HsMutableSet) return backing == other.backing
        if (other is HsSet) return backing == other.javaSet
        return false
    }

    override fun hashCode(): Int = backing.hashCode()

    override fun toString(): String = "HsMutableSet($backing)"

    public companion object {

        /**
         * Creates an [HsMutableSet] from the given [elements].
         */
        public fun of(vararg elements: Any?): HsMutableSet = HsMutableSet(elements.toMutableSet())

        /**
         * Creates an empty [HsMutableSet].
         */
        public fun empty(): HsMutableSet = HsMutableSet(mutableSetOf())
    }
}
