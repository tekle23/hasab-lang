package hasab.runtime.collections

/**
 * Mutable map wrapper for the HASAB runtime.
 *
 * Wraps a [MutableMap] of nullable key-value pairs. Extends the read-only
 * API of [HsMap] with mutation operations.
 */
public class HsMutableMap internal constructor(
    private val backing: MutableMap<Any?, Any?>,
) {

    /**
     * Returns the number of key-value pairs in this map.
     */
    public fun size(): Int = backing.size

    /**
     * Returns `true` if this map contains no key-value pairs.
     */
    public fun isEmpty(): Boolean = backing.isEmpty()

    /**
     * Returns `true` if this map contains one or more key-value pairs.
     */
    public fun isNotEmpty(): Boolean = backing.isNotEmpty()

    /**
     * Returns the value associated with [key], or `null` if the key is not present.
     */
    public fun get(key: Any?): Any? = backing[key]

    /**
     * Returns the value associated with [key], or [defaultValue] if the key is not present.
     */
    public fun getOrDefault(key: Any?, defaultValue: Any?): Any? = backing.getOrDefault(key, defaultValue)

    /**
     * Returns `true` if this map contains the given [key].
     */
    public fun containsKey(key: Any?): Boolean = backing.containsKey(key)

    /**
     * Returns `true` if this map contains the given [value].
     */
    public fun containsValue(value: Any?): Boolean = backing.containsValue(value)

    /**
     * Returns an [HsSet] of all keys in this map.
     */
    public fun keys(): HsSet = HsSet(backing.keys)

    /**
     * Returns an [HsList] of all values in this map.
     */
    public fun values(): HsList = HsList(backing.values.toList())

    /**
     * Returns an [HsList] of all key-value [Map.Entry] pairs in this map.
     */
    public fun entries(): HsList = HsList(backing.entries.toList())

    /**
     * Performs [action] for each key-value pair in this map.
     */
    public fun forEach(action: (Any?, Any?) -> Unit) {
        backing.forEach { (key, value) -> action(key, value) }
    }

    /**
     * Returns a new [HsMap] with values transformed by [transform].
     */
    public fun mapValues(transform: (Map.Entry<Any?, Any?>) -> Any?): HsMap =
        HsMap(backing.mapValues(transform))

    /**
     * Returns a new [HsMap] with keys transformed by [transform].
     */
    public fun mapKeys(transform: (Map.Entry<Any?, Any?>) -> Any?): HsMap =
        HsMap(backing.mapKeys(transform))

    /**
     * Returns a new [HsMap] containing only entries matching [predicate].
     */
    public fun filter(predicate: (Any?, Any?) -> Boolean): HsMap =
        HsMap(backing.filter { (key, value) -> predicate(key, value) })

    /**
     * Returns a new [HsMap] containing only entries not matching [predicate].
     */
    public fun filterNot(predicate: (Any?, Any?) -> Boolean): HsMap =
        HsMap(backing.filterNot { (key, value) -> predicate(key, value) })

    /**
     * Returns a new [HsMap] containing only entries whose keys match [predicate].
     */
    public fun filterKeys(predicate: (Any?) -> Boolean): HsMap =
        HsMap(backing.filterKeys(predicate))

    /**
     * Returns a new [HsMap] containing only entries whose values match [predicate].
     */
    public fun filterValues(predicate: (Any?) -> Boolean): HsMap =
        HsMap(backing.filterValues(predicate))

    /**
     * Returns `true` if at least one entry matches [predicate].
     */
    public fun any(predicate: (Any?, Any?) -> Boolean): Boolean =
        backing.any { (key, value) -> predicate(key, value) }

    /**
     * Returns `true` if all entries match [predicate].
     */
    public fun all(predicate: (Any?, Any?) -> Boolean): Boolean =
        backing.all { (key, value) -> predicate(key, value) }

    /**
     * Returns `true` if no entries match [predicate].
     */
    public fun none(predicate: (Any?, Any?) -> Boolean): Boolean =
        backing.none { (key, value) -> predicate(key, value) }

    /**
     * Returns the count of entries matching [predicate].
     */
    public fun count(predicate: (Any?, Any?) -> Boolean): Int =
        backing.count { (key, value) -> predicate(key, value) }

    /**
     * Returns a mutable copy as an [HsMutableMap].
     */
    public fun toMutableMap(): HsMutableMap = HsMutableMap(backing.toMutableMap())

    /**
     * Returns a new [HsMap] containing all entries from this map and [other].
     *
     * Entries in [other] take precedence for duplicate keys.
     */
    public fun plus(other: HsMap): HsMap = HsMap(backing + other.javaMap)

    /**
     * Returns a new [HsMap] containing all entries from this map plus the given [pair].
     */
    public fun plus(pair: Pair<Any?, Any?>): HsMap = HsMap(backing + pair)

    /**
     * Returns a new [HsMap] with the entry for [key] removed.
     */
    public fun minus(key: Any?): HsMap = HsMap(backing - key)

    /**
     * Returns a string representation with entries joined by [separator].
     */
    public fun joinToString(separator: String): String = backing.entries.joinToString(separator) { "${it.key}=${it.value}" }

    /**
     * Associates [key] with [value] in this map.
     *
     * @return the previous value associated with [key], or `null` if none.
     */
    public fun put(key: Any?, value: Any?): Any? = backing.put(key, value)

    /**
     * Copies all entries from [from] into this map.
     */
    public fun putAll(from: Map<Any?, Any?>) {
        backing.putAll(from)
    }

    /**
     * Removes the entry for [key] from this map.
     *
     * @return the previous value associated with [key], or `null` if none.
     */
    public fun remove(key: Any?): Any? = backing.remove(key)

    /**
     * Removes all entries from this map.
     */
    public fun clear() {
        backing.clear()
    }

    /**
     * Associates [key] with [value] only if [key] is not already present.
     *
     * @return the existing value for [key] if present, or `null` if the entry was inserted.
     */
    public fun putIfAbsent(key: Any?, value: Any?): Any? = backing.putIfAbsent(key, value)

    /**
     * Returns the value for [key], or computes it using [defaultValue] and associates it if absent.
     */
    public fun getOrPut(key: Any?, defaultValue: () -> Any?): Any? =
        backing.getOrPut(key) { defaultValue() }

    /**
     * If the specified [key] is not already associated with a value, associates it
     * with the result of [mappingFunction]. Otherwise, removes the entry.
     */
    public fun computeIfAbsent(key: Any?, mappingFunction: (Any?) -> Any?): Any? =
        backing.computeIfAbsent(key) { mappingFunction(it) }

    /**
     * If the specified [key] is associated with a value, remaps it using [remappingFunction].
     * If the remapping function returns `null`, the entry is removed.
     */
    public fun computeIfPresent(key: Any?, remappingFunction: (Any?, Any?) -> Any?): Any? =
        backing.computeIfPresent(key) { k, v -> remappingFunction(k, v) }

    /**
     * The underlying JVM mutable map. Read-only access for interop.
     */
    public val javaMap: Map<Any?, Any?> get() = backing

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is HsMutableMap) return backing == other.backing
        if (other is HsMap) return backing == other.javaMap
        return false
    }

    override fun hashCode(): Int = backing.hashCode()

    override fun toString(): String = "HsMutableMap($backing)"

    public companion object {

        /**
         * Creates an [HsMutableMap] from the given key-value [pairs].
         */
        public fun of(vararg pairs: Pair<Any?, Any?>): HsMutableMap = HsMutableMap(pairs.toMap().toMutableMap())

        /**
         * Creates an empty [HsMutableMap].
         */
        public fun empty(): HsMutableMap = HsMutableMap(mutableMapOf())
    }
}
