package hasab.runtime.core

/**
 * Base class for all HASAB runtime objects.
 *
 * Every HASAB value that is not a JVM primitive is represented
 * as a subclass of [HsObject]. Provides identity-based hashing,
 * equality, and string conversion semantics for the runtime.
 */
public open class HsObject {

    /**
     * The HASAB type name of this object.
     *
     * Subclasses should override this to return the type name
     * as it appears in HASAB source code.
     */
    public open fun typeName(): String = "Object"

    /**
     * Returns the JVM identity hash code for this object.
     *
     * Two calls to [identityHashCode] on the same reference
     * will return the same value.
     */
    public fun identityHashCode(): Int = System.identityHashCode(this)

    override fun hashCode(): Int = System.identityHashCode(this)

    override fun equals(other: Any?): Boolean = this === other

    override fun toString(): String = "${typeName()}@${Integer.toHexString(hashCode())}"

    public companion object {

        /**
         * Returns the JVM identity hash code for [obj].
         *
         * If [obj] is `null`, returns `0`.
         */
        public fun identityHashCode(obj: Any?): Int = System.identityHashCode(obj)
    }
}
