package hasab.runtime.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Logging utilities for the HASAB runtime.
 *
 * Provides leveled logging with configurable output and `{}` placeholder formatting.
 * All methods are thread-safe for simple usage patterns.
 */
public object HsLog {

    /**
     * Supported log severity levels in ascending order.
     */
    public enum class LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL
    }

    /**
     * The minimum [LogLevel] that will be emitted.
     * Messages below this level are silently discarded.
     */
    @Suppress("ktlint:standard:property-naming")
    public var currentLevel: LogLevel = LogLevel.INFO

    /**
     * The output sink used to emit formatted log messages.
     * Defaults to standard output.
     */
    public var output: (String) -> Unit = System.out::println

    private val formatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC)

    public fun debug(message: String): Unit =
        logIfEnabled(LogLevel.DEBUG, message)

    public fun debug(message: String, vararg args: Any?): Unit =
        logIfEnabled(LogLevel.DEBUG, message, args)

    public fun info(message: String): Unit =
        logIfEnabled(LogLevel.INFO, message)

    public fun info(message: String, vararg args: Any?): Unit =
        logIfEnabled(LogLevel.INFO, message, args)

    public fun warn(message: String): Unit =
        logIfEnabled(LogLevel.WARNING, message)

    public fun warn(message: String, vararg args: Any?): Unit =
        logIfEnabled(LogLevel.WARNING, message, args)

    public fun error(message: String): Unit =
        logIfEnabled(LogLevel.ERROR, message)

    public fun error(message: String, vararg args: Any?): Unit =
        logIfEnabled(LogLevel.ERROR, message, args)

    public fun error(message: String, throwable: Throwable): Unit =
        logIfEnabled(LogLevel.ERROR, "$message: ${throwable.message}")

    public fun fatal(message: String): Unit =
        logIfEnabled(LogLevel.FATAL, message)

    public fun fatal(message: String, throwable: Throwable): Unit =
        logIfEnabled(LogLevel.FATAL, "$message: ${throwable.message}")

    public fun isDebugEnabled(): Boolean = currentLevel.ordinal <= LogLevel.DEBUG.ordinal

    public fun isInfoEnabled(): Boolean = currentLevel.ordinal <= LogLevel.INFO.ordinal

    public fun isWarnEnabled(): Boolean = currentLevel.ordinal <= LogLevel.WARNING.ordinal

    public fun isErrorEnabled(): Boolean = currentLevel.ordinal <= LogLevel.ERROR.ordinal

    private fun logIfEnabled(level: LogLevel, message: String): Unit {
        if (level.ordinal >= currentLevel.ordinal) {
            output(formatMessage(level.name, message, emptyArray()))
        }
    }

    private fun logIfEnabled(level: LogLevel, message: String, args: Array<out Any?>): Unit {
        if (level.ordinal >= currentLevel.ordinal) {
            output(formatMessage(level.name, message, args))
        }
    }

    private fun formatMessage(level: String, message: String, args: Array<out Any?>): String {
        val sb = StringBuilder()
        sb.append(timestamp())
        sb.append(" [")
        sb.append(level.padEnd(7))
        sb.append("] ")
        var argIndex = 0
        var i = 0
        while (i < message.length) {
            if (i + 1 < message.length && message[i] == '{' && message[i + 1] == '}') {
                if (argIndex < args.size) {
                    sb.append(args[argIndex] ?: "null")
                    argIndex++
                } else {
                    sb.append("{}")
                }
                i += 2
            } else {
                sb.append(message[i])
                i++
            }
        }
        while (argIndex < args.size) {
            sb.append(" ")
            sb.append(args[argIndex] ?: "null")
            argIndex++
        }
        return sb.toString()
    }

    private fun timestamp(): String = formatter.format(Instant.now())
}
