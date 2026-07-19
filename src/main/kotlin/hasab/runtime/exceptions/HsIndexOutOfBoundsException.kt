package hasab.runtime.exceptions

/**
 * Exception thrown when an array or list index is outside the valid range.
 */
public class HsIndexOutOfBoundsException(
    message: String
) : HsException(message) {

    public companion object {

        /**
         * Creates a formatted index out of bounds exception.
         *
         * @param index The invalid index that was accessed.
         * @param size The size of the collection or array.
         */
        public fun create(index: Int, size: Int): HsIndexOutOfBoundsException =
            HsIndexOutOfBoundsException(
                "Index $index is out of bounds for size $size"
            )
    }
}
