package hasab.runtime.exceptions

/**
 * Exception thrown when an attempt is made to dereference a null reference.
 *
 * This corresponds to null pointer errors in the HASAB runtime.
 */
public class HsNullPointerException(
    message: String = "Null reference"
) : HsException(message)
