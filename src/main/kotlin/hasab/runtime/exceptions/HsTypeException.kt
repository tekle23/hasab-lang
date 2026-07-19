package hasab.runtime.exceptions

/**
 * Exception thrown when an operation is performed on a value of the wrong type.
 */
public class HsTypeException(
    message: String
) : HsException(message) {

    public companion object {

        /**
         * Creates a formatted type mismatch exception.
         *
         * @param expected The type that was expected.
         * @param actual The type that was encountered.
         */
        public fun create(expected: String, actual: String): HsTypeException =
            HsTypeException(
                "Type mismatch: expected $expected, got $actual"
            )
    }
}
