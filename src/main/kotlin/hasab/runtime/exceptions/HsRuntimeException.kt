package hasab.runtime.exceptions

/**
 * Exception thrown when a general runtime error occurs during HASAB program execution.
 *
 * This is the standard runtime exception for errors that do not fall
 * into a more specific category.
 */
public class HsRuntimeException : HsException {

    public constructor(message: String) : super(message)

    public constructor(message: String, cause: Throwable) : super(message, cause)
}
