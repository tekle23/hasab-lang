package hasab.runtime.io

import hasab.runtime.exceptions.HsIOError
import java.io.File
import java.nio.file.Files

/**
 * A wrapper around [java.io.File] that provides HASAB file system operations.
 *
 * All paths are resolved relative to the underlying [java.io.File] instance.
 * Errors during file operations are thrown as [HsIOError].
 *
 * @property path The original path string used to construct this file.
 */
public class HsFile(path: String) {

    private val file: File = File(path)

    /** The original path string. */
    public val path: String
        get() = file.path

    /** The name of the file, including extension. */
    public val name: String
        get() = file.name

    /** The file extension, or an empty string if there is none. */
    public val extension: String
        get() = file.extension

    /** The parent directory path, or `null` if this file has no parent. */
    public val parentPath: String?
        get() = file.parent

    /** The absolute path to the file. */
    public val absolutePath: String
        get() = file.absolutePath

    /** Whether this file exists on the file system. */
    public val exists: Boolean
        get() = file.exists()

    /** Whether this path refers to a regular file. */
    public val isFile: Boolean
        get() = file.isFile

    /** Whether this path refers to a directory. */
    public val isDirectory: Boolean
        get() = file.isDirectory

    /** The size of the file in bytes, or `0L` if the file does not exist. */
    public val size: Long
        get() = file.length()

    /** The time the file was last modified in milliseconds since the epoch. */
    public val lastModified: Long
        get() = file.lastModified()

    /** Whether the file is readable. */
    public val isReadable: Boolean
        get() = file.canRead()

    /** Whether the file is writable. */
    public val isWritable: Boolean
        get() = file.canWrite()

    /** Whether the file is hidden. */
    public val isHidden: Boolean
        get() = file.isHidden()

    /**
     * Reads the entire file contents as a string.
     *
     * @throws HsIOError if the file cannot be read.
     */
    public fun readAllText(): String =
        try {
            file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            throw HsIOError("Failed to read file: ${file.path}", e)
        }

    /**
     * Reads the entire file contents as a list of lines.
     *
     * @throws HsIOError if the file cannot be read.
     */
    public fun readAllLines(): List<String> =
        try {
            file.readLines(Charsets.UTF_8)
        } catch (e: Exception) {
            throw HsIOError("Failed to read file: ${file.path}", e)
        }

    /**
     * Reads the entire file contents as a byte array.
     *
     * @throws HsIOError if the file cannot be read.
     */
    public fun readAllBytes(): ByteArray =
        try {
            file.readBytes()
        } catch (e: Exception) {
            throw HsIOError("Failed to read file: ${file.path}", e)
        }

    /**
     * Writes the given text content to the file, overwriting any existing content.
     *
     * @throws HsIOError if the file cannot be written.
     */
    public fun writeAllText(content: String) {
        try {
            file.writeText(content, Charsets.UTF_8)
        } catch (e: Exception) {
            throw HsIOError("Failed to write file: ${file.path}", e)
        }
    }

    /**
     * Writes the given byte array to the file, overwriting any existing content.
     *
     * @throws HsIOError if the file cannot be written.
     */
    public fun writeAllBytes(bytes: ByteArray) {
        try {
            file.writeBytes(bytes)
        } catch (e: Exception) {
            throw HsIOError("Failed to write file: ${file.path}", e)
        }
    }

    /**
     * Appends the given text to the end of the file.
     *
     * @throws HsIOError if the file cannot be written.
     */
    public fun appendText(content: String) {
        try {
            file.appendText(content, Charsets.UTF_8)
        } catch (e: Exception) {
            throw HsIOError("Failed to append to file: ${file.path}", e)
        }
    }

    /**
     * Appends the given text followed by a newline to the end of the file.
     *
     * @throws HsIOError if the file cannot be written.
     */
    public fun appendLine(content: String) {
        try {
            file.appendText(content + System.lineSeparator(), Charsets.UTF_8)
        } catch (e: Exception) {
            throw HsIOError("Failed to append line to file: ${file.path}", e)
        }
    }

    /**
     * Copies this file to the specified destination.
     *
     * @param destination The target [HsFile] to copy to.
     * @return `true` if the copy succeeded, `false` otherwise.
     */
    public fun copyTo(destination: HsFile): Boolean =
        try {
            file.copyTo(destination.file, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }

    /**
     * Moves this file to the specified destination.
     *
     * @param destination The target [HsFile] to move to.
     * @return `true` if the move succeeded, `false` otherwise.
     */
    public fun moveTo(destination: HsFile): Boolean =
        try {
            file.renameTo(destination.file)
        } catch (e: Exception) {
            false
        }

    /**
     * Deletes this file or empty directory.
     *
     * @return `true` if the file was deleted, `false` otherwise.
     */
    public fun delete(): Boolean = file.delete()

    /**
     * Creates a new, empty file if one does not already exist.
     *
     * @return `true` if the file was created, `false` if it already exists.
     * @throws HsIOError if the file could not be created.
     */
    public fun createNewFile(): Boolean =
        try {
            file.createNewFile()
        } catch (e: Exception) {
            throw HsIOError("Failed to create file: ${file.path}", e)
        }

    /**
     * Creates this directory, including any necessary but nonexistent parent directories.
     *
     * @return `true` if the directory was created, `false` otherwise.
     */
    public fun mkdir(): Boolean = file.mkdir()

    /**
     * Creates this directory, creating all nonexistent parent directories first.
     *
     * @return `true` if the directory was created, `false` otherwise.
     */
    public fun mkdirs(): Boolean = file.mkdirs()

    /**
     * Lists all files and directories within this directory.
     *
     * @return An array of [HsFile] objects, or an empty array if this is not a directory.
     */
    public fun listFiles(): Array<HsFile> {
        val children = file.listFiles() ?: return emptyArray()
        return children.map { HsFile(it.absolutePath) }.toTypedArray()
    }

    /**
     * Lists files within this directory that have the specified extension.
     *
     * @param extension The file extension to filter by (without the leading dot).
     * @return An array of matching [HsFile] objects.
     */
    public fun listFilesFiltered(extension: String): Array<HsFile> {
        val children = file.listFiles() ?: return emptyArray()
        return children
            .filter { it.extension == extension }
            .map { HsFile(it.absolutePath) }
            .toTypedArray()
    }

    /**
     * Resolves a relative path against this file's path.
     *
     * @param relativePath The relative path to resolve.
     * @return A new [HsFile] representing the resolved path.
     */
    public fun resolve(relativePath: String): HsFile =
        HsFile(File(file, relativePath).absolutePath)

    /**
     * Returns the relative path from the other file to this file.
     *
     * @param other The base file from which the relative path is computed.
     * @return The relative path string.
     */
    public fun relativeTo(other: HsFile): String =
        file.relativeTo(other.file).path

    /**
     * Reads each line of the file and invokes the given action.
     *
     * @param action The function to call with each line.
     * @throws HsIOError if the file cannot be read.
     */
    public fun forEachLine(action: (String) -> Unit) {
        try {
            file.forEachLine(Charsets.UTF_8, action = action)
        } catch (e: HsIOError) {
            throw e
        } catch (e: Exception) {
            throw HsIOError("Failed to read file: ${file.path}", e)
        }
    }

    /**
     * Reads all lines from the file.
     *
     * This is an alias for [readAllLines].
     *
     * @throws HsIOError if the file cannot be read.
     */
    public fun readLines(): List<String> = readAllLines()

    override fun toString(): String = "HsFile($path)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsFile) return false
        return file.absolutePath == other.file.absolutePath
    }

    override fun hashCode(): Int = file.absolutePath.hashCode()

    public companion object {

        /**
         * Creates an [HsFile] from a path string.
         *
         * @param path The file path.
         * @return A new [HsFile] instance.
         */
        public fun fromPath(path: String): HsFile = HsFile(path)

        /**
         * Creates a temporary file with the given prefix and suffix.
         *
         * @param prefix The prefix for the temporary file name.
         * @param suffix The suffix (extension) for the temporary file name.
         * @return A new [HsFile] pointing to the created temporary file.
         * @throws HsIOError if the temporary file could not be created.
         */
        public fun tempFile(
            prefix: String = "hs",
            suffix: String = ".tmp"
        ): HsFile =
            try {
                val tempFile = File.createTempFile(prefix, suffix)
                tempFile.deleteOnExit()
                HsFile(tempFile.absolutePath)
            } catch (e: Exception) {
                throw HsIOError("Failed to create temporary file", e)
            }

        /**
         * Creates a temporary directory with the given prefix.
         *
         * @param prefix The prefix for the temporary directory name.
         * @return A new [HsFile] pointing to the created temporary directory.
         * @throws HsIOError if the temporary directory could not be created.
         */
        public fun createTempDirectory(prefix: String = "hs"): HsFile =
            try {
                val dir = Files.createTempDirectory(prefix)
                HsFile(dir.toAbsolutePath().toString())
            } catch (e: Exception) {
                throw HsIOError("Failed to create temporary directory", e)
            }
    }
}
