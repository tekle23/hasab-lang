package hasab.runtime.filesystem

import java.io.File
import java.nio.file.Paths

/**
 * Cross-platform path manipulation utilities for the HASAB runtime.
 *
 * Provides static methods for building, normalizing, and querying
 * file paths in a platform-agnostic manner.
 */
public object HsPath {

    /** The system-dependent path separator character. */
    public val separator: String = File.separator

    /** The system-dependent line separator. */
    public val lineSeparator: String = System.lineSeparator()

    /** Returns the normalized form of [path] with redundant separators removed. */
    public fun normalize(path: String): String = Paths.get(path).normalize().toString()

    /** Returns the absolute form of [path]. */
    public fun toAbsolute(path: String): String = File(path).absolutePath

    /** Returns the canonical form of [path], resolving symlinks and `.`/`..`. */
    public fun toCanonical(path: String): String = File(path).canonicalPath

    /** Returns the file name (last component) of [path]. */
    public fun getFileName(path: String): String = File(path).name

    /** Returns the parent directory of [path], or `null` if there is none. */
    public fun getParent(path: String): String? = File(path).parent

    /** Returns the file extension of [path], or an empty string if there is none. */
    public fun getExtension(path: String): String = File(path).extension

    /** Returns the file name without its extension. */
    public fun getBaseName(path: String): String = File(path).nameWithoutExtension

    /** Returns `true` if [path] is an absolute path. */
    public fun isAbsolute(path: String): Boolean = File(path).isAbsolute

    /** Returns `true` if [path] is a relative path. */
    public fun isRelative(path: String): Boolean = !File(path).isAbsolute

    /** Returns `true` if [child] is a descendant of [parent]. */
    public fun isChildOf(child: String, parent: String): Boolean {
        val childPath = File(child).canonicalPath
        val parentPath = File(parent).canonicalPath
        return childPath.startsWith(parentPath + separator) || childPath.startsWith(parentPath + "/")
    }

    /** Joins multiple path components with the path separator. */
    public fun join(vararg parts: String): String {
        if (parts.isEmpty()) return ""
        return File(parts[0]).let { base ->
            parts.drop(1).fold(base) { acc, part -> File(acc, part) }.path
        }
    }

    /** Resolves [relativePath] against [basePath]. */
    public fun resolve(basePath: String, relativePath: String): String =
        File(File(basePath), relativePath).path

    /** Returns the relative path from [base] to [target]. */
    public fun relativize(base: String, target: String): String =
        File(base).toPath().relativize(File(target).toPath()).toString()

    /** Changes the extension of [path] to [newExtension]. */
    public fun changeExtension(path: String, newExtension: String): String {
        val file = File(path)
        val baseName = file.nameWithoutExtension
        val parent = file.parent
        return if (parent != null) {
            File(parent, "$baseName.$newExtension").path
        } else {
            "$baseName.$newExtension"
        }
    }

    /** Returns `true` if [path] has the given [extension] (case-insensitive). */
    public fun hasExtension(path: String, extension: String): Boolean =
        File(path).extension.equals(extension, ignoreCase = true)

    /** Lists all root directories on the filesystem (e.g., `C:\`, `/`). */
    public fun roots(): List<String> = File.listRoots().map { it.path }

    /** Returns the temporary directory path. */
    public fun tempDir(): String = System.getProperty("java.io.tmpdir") ?: "/tmp"

    /** Returns the user's home directory path. */
    public fun homeDir(): String = System.getProperty("user.home") ?: "~"
}
