package hasab.runtime.exceptions

/**
 * Exception thrown when a division or modulo operation is attempted with a zero divisor.
 */
public class HsDivisionByZeroError(
    message: String = "Division by zero"
) : HsException(message)
