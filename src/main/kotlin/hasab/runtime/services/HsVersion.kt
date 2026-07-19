package hasab.runtime.services

import java.time.LocalDate

/**
 * Version information for the HASAB runtime.
 */
public object HsVersion {

    /** The full semantic version string. */
    public val version: String = "1.0.0"

    /** The human-readable name of this runtime. */
    public val name: String = "HASAB Runtime"

    /** The date this version was built. */
    public val buildDate: String = LocalDate.now().toString()

    /** The Java version used at build time. */
    public val javaVersion: String = System.getProperty("java.version") ?: "unknown"

    /** The Kotlin version used at build time. */
    public val kotlinVersion: String = KotlinVersion.CURRENT.toString()

    /**
     * Returns a multi-line string describing this runtime version.
     */
    public fun info(): String = buildString {
        appendLine("$name v$version")
        appendLine("Built:    $buildDate")
        appendLine("Java:     $javaVersion")
        append("Kotlin:   $kotlinVersion")
    }

    /**
     * Returns the major component of the semantic version (e.g. `1` from `"1.2.3"`).
     */
    public fun majorVersion(): Int = parseSegment(0)

    /**
     * Returns the minor component of the semantic version (e.g. `2` from `"1.2.3"`).
     */
    public fun minorVersion(): Int = parseSegment(1)

    /**
     * Returns the patch component of the semantic version (e.g. `3` from `"1.2.3"`).
     */
    public fun patchVersion(): Int = parseSegment(2)

    /**
     * Returns `true` when this runtime satisfies at least the given [requiredVersion].
     *
     * Comparison is lexicographic on the `major.minor.patch` segments.
     */
    public fun isCompatibleWith(requiredVersion: String): Boolean {
        val required = requiredVersion.split('.')
            .mapNotNull { it.toIntOrNull() }
            .ifEmpty { return false }
        val current = version.split('.')
            .mapNotNull { it.toIntOrNull() }
            .ifEmpty { return false }

        for (i in 0 until maxOf(required.size, current.size)) {
            val r = required.getOrElse(i) { 0 }
            val c = current.getOrElse(i) { 0 }
            if (c > r) return true
            if (c < r) return false
        }
        return true
    }

    override fun toString(): String = "$name v$version"

    private fun parseSegment(index: Int): Int =
        version.split('.').getOrElse(index) { "0" }.toIntOrNull() ?: 0
}
