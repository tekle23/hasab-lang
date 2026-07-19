package hasab.runtime.exceptions

/**
 * Base exception class for all HASAB runtime errors.
 *
 * All HASAB-specific exceptions inherit from this class, providing
 * a unified error handling mechanism with consistent message formatting.
 */
public open class HsException : RuntimeException {

    /**
     * The HASAB-specific error message.
     *
     * Returns the original message if available, or "Unknown error" as a fallback.
     */
    public val hsMessage: String
        get() = message ?: "Unknown error"

    /**
     * Creates an exception with the specified error message.
     */
    public constructor(message: String) : super(message)

    /**
     * Creates an exception with the specified error message and cause.
     */
    public constructor(message: String, cause: Throwable) : super(message, cause)

    /**
     * Creates an exception with no message or cause.
     */
    public constructor() : super()

    override fun toString(): String = "HsException: $hsMessage"
}
