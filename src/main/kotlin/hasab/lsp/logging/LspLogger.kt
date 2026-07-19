package hasab.lsp.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.format.DateTimeFormatter

public class LspLogger(
    private val prefix: String = "HasabLSP",
    private val minLevel: Level = Level.INFO,
) {

    public enum class Level { DEBUG, INFO, WARNING, ERROR }

    private val buffer = mutableListOf<LogEntry>()
    private val maxBufferSize = 1000

    @Volatile
    public var onLog: ((LogEntry) -> Unit)? = null

    public fun debug(message: String, throwable: Throwable? = null): Unit = log(Level.DEBUG, message, throwable)
    public fun info(message: String, throwable: Throwable? = null): Unit = log(Level.INFO, message, throwable)
    public fun warn(message: String, throwable: Throwable? = null): Unit = log(Level.WARNING, message, throwable)
    public fun error(message: String, throwable: Throwable? = null): Unit = log(Level.ERROR, message, throwable)

    private fun log(level: Level, message: String, throwable: Throwable?) {
        if (level.ordinal < minLevel.ordinal) return
        val entry = LogEntry(
            timestamp = Instant.now(),
            level = level,
            prefix = prefix,
            message = message,
            stackTrace = throwable?.let { extractStackTrace(it) },
        )
        synchronized(buffer) {
            buffer.add(entry)
            if (buffer.size > maxBufferSize) buffer.removeAt(0)
        }
        onLog?.invoke(entry)
    }

    public fun getRecentLogs(count: Int = 50): List<LogEntry> {
        synchronized(buffer) {
            return buffer.takeLast(count)
        }
    }

    public fun getLogsByLevel(level: Level): List<LogEntry> {
        synchronized(buffer) {
            return buffer.filter { it.level == level }
        }
    }

    public fun clear() {
        synchronized(buffer) { buffer.clear() }
    }

    private fun extractStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

    public data class LogEntry(
        val timestamp: Instant,
        val level: Level,
        val prefix: String,
        val message: String,
        val stackTrace: String?,
    ) {
        override fun toString(): String {
            val ts = DateTimeFormatter.ISO_INSTANT.format(timestamp)
            val trace = stackTrace?.let { "\n$it" } ?: ""
            return "[$ts] [$level] [$prefix] $message$trace"
        }
    }
}
