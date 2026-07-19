package hasab.runtime.filesystem

import hasab.runtime.exceptions.HsIOError
import hasab.runtime.io.HsFile
import java.io.File

/**
 * A wrapper around [java.io.File] providing HASAB directory operations.
 *
 * @property path The directory path.
 */
public class HsDirectory(path: String) {

    private val dir: File = File(path)

    /** The directory path. */
    public val path: String get() = dir.path

    /** The absolute directory path. */
    public val absolutePath: String get() = dir.absolutePath

    /** Whether this directory exists. */
    public val exists: Boolean get() = dir.exists() && dir.isDirectory

    /** Whether this directory is empty. */
    public val isEmpty: Boolean get() = exists && (dir.listFiles()?.isEmpty() ?: true)

    /** The number of files and subdirectories in this directory. */
    public val size: Long get() = if (exists) countRecursive(dir) else 0L

    /** The number of direct children. */
    public val childCount: Int get() = dir.listFiles()?.size ?: 0

    /** Creates this directory. Returns `true` if created. */
    public fun create(): Boolean = dir.mkdirs()

    /** Deletes this directory and all its contents recursively. Returns `true` if deleted. */
    public fun deleteRecursively(): Boolean = dir.deleteRecursively()

    /** Lists all files and subdirectories in this directory. */
    public fun listFiles(): List<HsFile> {
        if (!exists) return emptyList()
        return dir.listFiles()?.map { HsFile(it.absolutePath) } ?: emptyList()
    }

    /** Lists only files (not subdirectories) in this directory. */
    public fun listFilesOnly(): List<HsFile> {
        if (!exists) return emptyList()
        return dir.listFiles()?.filter { it.isFile }?.map { HsFile(it.absolutePath) } ?: emptyList()
    }

    /** Lists only subdirectories in this directory. */
    public fun listDirectories(): List<HsDirectory> {
        if (!exists) return emptyList()
        return dir.listFiles()?.filter { it.isDirectory }?.map { HsDirectory(it.absolutePath) } ?: emptyList()
    }

    /** Lists all files recursively in this directory. */
    public fun walkFiles(): List<HsFile> {
        if (!exists) return emptyList()
        return dir.walkTopDown().filter { it.isFile }.map { HsFile(it.absolutePath) }.toList()
    }

    /** Lists all subdirectories recursively in this directory. */
    public fun walkDirectories(): List<HsDirectory> {
        if (!exists) return emptyList()
        return dir.walkTopDown().filter { it.isDirectory }.map { HsDirectory(it.absolutePath) }.toList()
    }

    /** Creates a file within this directory. */
    public fun createFile(fileName: String): HsFile {
        val file = File(dir, fileName)
        try {
            file.createNewFile()
        } catch (e: Exception) {
            throw HsIOError("Failed to create file: ${file.path}", e)
        }
        return HsFile(file.absolutePath)
    }

    /** Creates a subdirectory within this directory. */
    public fun createSubdirectory(dirName: String): HsDirectory {
        val sub = File(dir, dirName)
        sub.mkdirs()
        return HsDirectory(sub.absolutePath)
    }

    /** Copies this directory to [destination] recursively. */
    public fun copyTo(destination: HsDirectory): Boolean {
        return try {
            dir.copyRecursively(destination.dir, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Returns the relative path from this directory to [file]. */
    public fun relativize(file: HsFile): String =
        dir.toPath().relativize(File(file.path).toPath()).toString()

    override fun toString(): String = "HsDirectory($path)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsDirectory) return false
        return dir.canonicalPath == other.dir.canonicalPath
    }

    override fun hashCode(): Int = dir.canonicalPath.hashCode()

    private fun countRecursive(file: File): Long {
        var count = 0L
        val files = file.listFiles() ?: return 0
        for (f in files) {
            count++
            if (f.isDirectory) {
                count += countRecursive(f)
            }
        }
        return count
    }

    public companion object {
        /** Creates an [HsDirectory] from a path string. */
        public fun fromPath(path: String): HsDirectory = HsDirectory(path)

        /** Creates a temporary directory with the given prefix. */
        public fun tempDir(prefix: String = "hs"): HsDirectory {
            val dir = File(System.getProperty("java.io.tmpdir"), "${prefix}_${System.nanoTime()}")
            dir.mkdirs()
            dir.deleteOnExit()
            return HsDirectory(dir.absolutePath)
        }
    }
}
