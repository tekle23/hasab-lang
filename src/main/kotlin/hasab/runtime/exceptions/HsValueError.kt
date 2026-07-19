package hasab.runtime.exceptions

/**
 * Exception thrown when a function receives an argument that has the right type
 * but an inappropriate value.
 */
public class HsValueError(message: String) : HsException(message)
