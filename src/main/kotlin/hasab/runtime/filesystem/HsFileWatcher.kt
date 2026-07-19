package hasab.runtime.filesystem

import java.io.File
import kotlin.concurrent.thread

/**
 * A simple file system watcher that polls for changes in a directory.
 *
 * @property path The directory path to watch.
 * @property intervalMs Polling interval in milliseconds.
 */
public class HsFileWatcher(
    public val path: String,
    public val intervalMs: Long = 1000L
) {
    private val directory: File = File(path)
    private var running: Boolean = false
    private var watchThread: Thread? = null
    private val callbacks: MutableList<(HsWatchEvent) -> Unit> = mutableListOf()
    private var previousSnapshot: Map<String, Long> = emptyMap()

    /** Returns `true` if the watcher is currently running. */
    public fun isRunning(): Boolean = running

    /** Registers a callback to be invoked when a change is detected. */
    public fun onEvent(callback: (HsWatchEvent) -> Unit) {
        callbacks.add(callback)
    }

    /** Starts watching the directory for changes. */
    public fun start() {
        if (running) return
        running = true
        previousSnapshot = snapshot()
        watchThread = thread(isDaemon = true, name = "hs-filewatcher-$path") {
            while (running) {
                Thread.sleep(intervalMs)
                if (!running) break
                val current = snapshot()
                val events = diff(previousSnapshot, current)
                for (event in events) {
                    for (cb in callbacks) {
                        cb(event)
                    }
                }
                previousSnapshot = current
            }
        }
    }

    /** Stops the watcher. */
    public fun stop() {
        running = false
        watchThread?.interrupt()
        watchThread = null
    }

    private fun snapshot(): Map<String, Long> {
        if (!directory.exists() || !directory.isDirectory) return emptyMap()
        val result = mutableMapOf<String, Long>()
        directory.walkTopDown().forEach { file ->
            result[file.absolutePath] = file.lastModified()
        }
        return result
    }

    private fun diff(old: Map<String, Long>, new: Map<String, Long>): List<HsWatchEvent> {
        val events = mutableListOf<HsWatchEvent>()
        for ((path, _) in new) {
            if (path !in old) {
                events.add(HsWatchEvent(path, HsWatchEvent.Kind.CREATED))
            } else if (old[path] != new[path]) {
                events.add(HsWatchEvent(path, HsWatchEvent.Kind.MODIFIED))
            }
        }
        for ((path, _) in old) {
            if (path !in new) {
                events.add(HsWatchEvent(path, HsWatchEvent.Kind.DELETED))
            }
        }
        return events
    }
}

/**
 * Represents a file system change event.
 *
 * @property path The path of the affected file or directory.
 * @property kind The kind of change that occurred.
 */
public class HsWatchEvent(
    public val path: String,
    public val kind: Kind
) {
    /** The type of change. */
    public enum class Kind { CREATED, MODIFIED, DELETED }

    override fun toString(): String = "HsWatchEvent($kind, $path)"
}
