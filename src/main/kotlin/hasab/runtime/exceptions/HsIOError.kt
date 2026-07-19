package hasab.runtime.exceptions

/**
 * Exception thrown when an I/O operation fails.
 *
 * Common causes include file not found, permission denied,
 * disk full, and network errors.
 */
public class HsIOError(
    message: String,
    cause: Throwable? = null
) : HsException(message, cause ?: RuntimeException(message))
