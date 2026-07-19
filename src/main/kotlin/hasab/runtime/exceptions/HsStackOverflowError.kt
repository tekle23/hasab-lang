package hasab.runtime.exceptions

/**
 * Exception thrown when the call stack overflows due to excessive recursion.
 */
public class HsStackOverflowError(
    message: String = "Stack overflow"
) : HsException(message)
