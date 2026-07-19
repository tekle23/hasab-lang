package hasab.runtime.exceptions

/**
 * Exception thrown when a function or feature has not yet been implemented.
 */
public class HsNotImplementedError(
    message: String = "Not implemented"
) : HsException(message)
