package hasab.runtime.core

/**
 * Static utility methods for operating on HASAB arrays.
 *
 * HASAB arrays are represented as JVM arrays. This object provides
 * collection-like operations that work on the generic [Array<Any?>] type,
 * as well as factory methods for typed primitive arrays.
 */
public object HsArray {

    /** Creates a new array of [size] elements, initializing each with [init]. */
    public fun create(size: Int, init: (Int) -> Any?): Array<Any?> = Array(size, init)

    /** Creates a new [IntArray] of the given [size], initialized to zeros. */
    public fun createIntArray(size: Int): IntArray = IntArray(size)

    /** Creates a new [FloatArray] of the given [size], initialized to zeros. */
    public fun createFloatArray(size: Int): FloatArray = FloatArray(size)

    /** Creates a new [BooleanArray] of the given [size], initialized to `false`. */
    public fun createBoolArray(size: Int): BooleanArray = BooleanArray(size)

    /** Creates a new [CharArray] of the given [size], initialized to null characters. */
    public fun createCharArray(size: Int): CharArray = CharArray(size)

    /** Creates a new array of [String] of the given [size], initialized to empty strings. */
    public fun createStringArray(size: Int): Array<String> = Array(size) { "" }

    /** Returns the element at [index] in [array]. */
    public fun get(array: Array<Any?>, index: Int): Any? = array[index]

    /** Sets the element at [index] in [array] to [value]. */
    public fun set(array: Array<Any?>, index: Int, value: Any?) {
        array[index] = value
    }

    /** Returns the size (length) of [array]. */
    public fun size(array: Array<Any?>): Int = array.size

    /** Returns `true` if [array] is empty. */
    public fun isEmpty(array: Array<Any?>): Boolean = array.isEmpty()

    /** Returns `true` if [array] is not empty. */
    public fun isNotEmpty(array: Array<Any?>): Boolean = array.isNotEmpty()

    /** Returns `true` if [array] contains [value]. */
    public fun contains(array: Array<Any?>, value: Any?): Boolean = array.contains(value)

    /** Returns the index of the first occurrence of [value] in [array], or `-1` if not found. */
    public fun indexOf(array: Array<Any?>, value: Any?): Int = array.indexOf(value)

    /** Returns the index of the last occurrence of [value] in [array], or `-1` if not found. */
    public fun lastIndexOf(array: Array<Any?>, value: Any?): Int = array.lastIndexOf(value)

    /** Returns a shallow copy of [array]. */
    public fun copyOf(array: Array<Any?>): Array<Any?> = array.copyOf()

    /** Returns a copy of [array] from [fromIndex] (inclusive) to [toIndex] (exclusive). */
    public fun copyOfRange(array: Array<Any?>, fromIndex: Int, toIndex: Int): Array<Any?> =
        array.copyOfRange(fromIndex, toIndex)

    /** Fills [array] with [value]. */
    public fun fill(array: Array<Any?>, value: Any?) {
        array.fill(value)
    }

    /** Sorts [array] in natural order. */
    public fun sort(array: Array<Any?>) {
        @Suppress("UNCHECKED_CAST")
        val comparableArray = Array(array.size) { array[it] as? Comparable<Any?> }
        comparableArray.sortWith(compareBy { it })
        for (i in array.indices) {
            @Suppress("UNCHECKED_CAST")
            array[i] = comparableArray[i] as Any?
        }
    }

    /** Reverses [array] in place. */
    public fun reverse(array: Array<Any?>) {
        array.reverse()
    }

    /** Returns a slice of [array] over the given [indices]. */
    public fun slice(array: Array<Any?>, indices: IntRange): Array<Any?> = array.slice(indices).toTypedArray()

    /** Returns a new array with [transform] applied to each element. */
    public fun map(array: Array<Any?>, transform: (Any?) -> Any?): Array<Any?> = array.map(transform).toTypedArray()

    /** Returns a new array containing only elements that satisfy [predicate]. */
    public fun filter(array: Array<Any?>, predicate: (Any?) -> Boolean): Array<Any?> =
        array.filter(predicate).toTypedArray()

    /** Performs [action] on each element. */
    public fun forEach(array: Array<Any?>, action: (Any?) -> Unit) {
        array.forEach(action)
    }

    /** Reduces [array] to a single value using [operation]. */
    public fun reduce(array: Array<Any?>, operation: (Any?, Any?) -> Any?): Any? = array.reduce(operation)

    /** Folds [array] into a single value starting from [initial] using [operation]. */
    public fun fold(array: Array<Any?>, initial: Any?, operation: (Any?, Any?) -> Any?): Any? =
        array.fold(initial, operation)

    /** Joins all elements into a single string separated by [separator]. */
    public fun joinToString(array: Array<Any?>, separator: String): String = array.joinToString(separator)

    /** Returns a [List] containing all elements of [array]. */
    public fun toList(array: Array<Any?>): List<Any?> = array.toList()

    /** Returns an [Array] containing all elements of [list]. */
    public fun fromList(list: List<Any?>): Array<Any?> = list.toTypedArray()

    /** Flattens an array of arrays into a single array. */
    public fun flatten(arrays: Array<Array<Any?>>): Array<Any?> = arrays.flatMap { it.toList() }.toTypedArray()

    /** Returns a new array containing all elements of [a] followed by all elements of [b]. */
    public fun concat(a: Array<Any?>, b: Array<Any?>): Array<Any?> = arrayOf(*a, *b)

    /** Returns a new array containing only distinct elements from [array], preserving order. */
    public fun distinct(array: Array<Any?>): Array<Any?> = array.distinct().toTypedArray()

    /** Returns the first [n] elements of [array]. */
    public fun take(array: Array<Any?>, n: Int): Array<Any?> = array.take(n).toTypedArray()

    /** Returns all elements of [array] except the first [n]. */
    public fun drop(array: Array<Any?>, n: Int): Array<Any?> = array.drop(n).toTypedArray()

    /** Returns the first element of [array]. Throws [NoSuchElementException] if empty. */
    public fun first(array: Array<Any?>): Any? = array.first()

    /** Returns the last element of [array]. Throws [NoSuchElementException] if empty. */
    public fun last(array: Array<Any?>): Any? = array.last()

    /** Returns a random element from [array]. Throws [NoSuchElementException] if empty. */
    public fun random(array: Array<Any?>): Any? = array.random()
}
