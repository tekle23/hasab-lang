package hasab.runtime.filesystem

import hasab.runtime.exceptions.HsIOError
import java.io.File

/**
 * A managed temporary file that provides automatic cleanup.
 *
 * When [cleanup] is called, the file is deleted from disk.
 * Register the instance with [registerCleanup] to schedule
 * deletion via a shutdown hook.
 *
 * @property path The absolute path of the temporary file.
 */
public class HsTempFile private constructor(
    private val file: File,
    private val deleteOnExit: Boolean
) : AutoCloseable {

    /** The absolute path of the temporary file. */
    public val path: String get() = file.path

    /** Whether the temporary file still exists on disk. */
    public val exists: Boolean get() = file.exists()

    /** The size of the file in bytes. */
    public val size: Long get() = file.length()

    /** Whether this is a directory instead of a file. */
    public val isDirectory: Boolean get() = file.isDirectory

    /**
     * Writes text content to the temporary file.
     *
     * @throws HsIOError if the file cannot be written.
     */
    public fun writeText(content: String) {
        try {
            file.writeText(content, Charsets.UTF_8)
        } catch (e: Exception) {
            throw HsIOError("Failed to write temp file: ${file.path}", e)
        }
    }

    /**
     * Reads text content from the temporary file.
     *
     * @throws HsIOError if the file cannot be read.
     */
    public fun readText(): String {
        try {
            return file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            throw HsIOError("Failed to read temp file: ${file.path}", e)
        }
    }

    /**
     * Writes a byte array to the temporary file.
     *
     * @throws HsIOError if the file cannot be written.
     */
    public fun writeBytes(bytes: ByteArray) {
        try {
            file.writeBytes(bytes)
        } catch (e: Exception) {
            throw HsIOError("Failed to write temp file: ${file.path}", e)
        }
    }

    /**
     * Reads the temporary file as a byte array.
     *
     * @throws HsIOError if the file cannot be read.
     */
    public fun readBytes(): ByteArray {
        try {
            return file.readBytes()
        } catch (e: Exception) {
            throw HsIOError("Failed to read temp file: ${file.path}", e)
        }
    }

    /**
     * Deletes the temporary file from disk.
     *
     * @return `true` if the file was deleted, `false` otherwise.
     */
    public fun cleanup(): Boolean = file.delete()

    /**
     * Registers a JVM shutdown hook that will delete this file on exit.
     */
    public fun registerCleanup() {
        if (deleteOnExit) {
            file.deleteOnExit()
        }
    }

    /**
     * Returns an [HsFile] pointing to the same path.
     */
    public fun toHsFile(): hasab.runtime.io.HsFile = hasab.runtime.io.HsFile(file.absolutePath)

    override fun close() {
        cleanup()
    }

    override fun toString(): String = "HsTempFile(${file.path})"

    public companion object {
        /**
         * Creates a temporary file with the given prefix and suffix.
         *
         * @param prefix The file name prefix.
         * @param suffix The file name suffix (extension).
         * @param deleteOnExit Whether to register a JVM shutdown hook for deletion.
         */
        public fun create(
            prefix: String = "hs",
            suffix: String = ".tmp",
            deleteOnExit: Boolean = true
        ): HsTempFile {
            try {
                val tempFile = File.createTempFile(prefix, suffix)
                if (deleteOnExit) tempFile.deleteOnExit()
                return HsTempFile(tempFile, deleteOnExit)
            } catch (e: Exception) {
                throw HsIOError("Failed to create temporary file", e)
            }
        }

        /**
         * Creates a temporary directory with the given prefix.
         *
         * @param prefix The directory name prefix.
         * @param deleteOnExit Whether to register a JVM shutdown hook for deletion.
         */
        public fun createDirectory(
            prefix: String = "hs",
            deleteOnExit: Boolean = true
        ): HsTempFile {
            try {
                val dir = java.nio.file.Files.createTempDirectory(prefix).toFile()
                if (deleteOnExit) dir.deleteOnExit()
                return HsTempFile(dir, deleteOnExit)
            } catch (e: Exception) {
                throw HsIOError("Failed to create temporary directory", e)
            }
        }
    }
}
