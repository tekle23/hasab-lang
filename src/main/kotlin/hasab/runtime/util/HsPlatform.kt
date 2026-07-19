package hasab.runtime.util

import java.lang.management.ManagementFactory

/**
 * Platform detection and system information utilities for the HASAB runtime.
 *
 * All properties are evaluated once at class-load time and cached.
 */
public object HsPlatform {

    /** The operating system name (e.g. `"Windows 10"`, `"Linux"`). */
    public val osName: String = System.getProperty("os.name") ?: "unknown"

    /** The operating system architecture (e.g. `"amd64"`, `"aarch64"`). */
    public val osArch: String = System.getProperty("os.arch") ?: "unknown"

    /** The Java version string (e.g. `"17.0.1"`). */
    public val javaVersion: String = System.getProperty("java.version") ?: "unknown"

    /** The Kotlin version of the stdlib used at compile time. */
    public val kotlinVersion: String = KotlinVersion.CURRENT.toString()

    /** The user's home directory path. */
    public val userHome: String = System.getProperty("user.home") ?: "unknown"

    /** The current working directory. */
    public val userDir: String = System.getProperty("user.dir") ?: "unknown"

    /** The system temporary directory path. */
    public val tempDir: String = System.getProperty("java.io.tmpdir") ?: "unknown"

    /** The system line separator. */
    public val lineSeparator: String = System.lineSeparator()

    /** The number of processors available to the JVM. */
    public val availableProcessors: Int = Runtime.getRuntime().availableProcessors()

    /** The maximum amount of memory (in bytes) that the JVM will attempt to use. */
    public val maxMemory: Long = Runtime.getRuntime().maxMemory()

    /** The total amount of memory currently available to the JVM (in bytes). */
    public val totalMemory: Long = Runtime.getRuntime().totalMemory()

    /** The amount of free memory in the JVM (in bytes). */
    public val freeMemory: Long = Runtime.getRuntime().freeMemory()

    /** Returns `true` when the OS is Windows. */
    public fun isWindows(): Boolean = osName.lowercase().contains("win")

    /** Returns `true` when the OS is macOS. */
    public fun isMacOS(): Boolean = osName.lowercase().contains("mac")

    /** Returns `true` when the OS is Linux. */
    public fun isLinux(): Boolean = osName.lowercase().contains("linux")

    /** Returns `true` when the OS is a Unix-like system (macOS, Linux, BSD, etc.). */
    public fun isUnix(): Boolean = isLinux() || isMacOS() || osName.lowercase().contains("nix") || osName.lowercase().contains("sunos")

    /** Returns `true` when the JVM is running in 64-bit mode. */
    public fun is64Bit(): Boolean = osArch.lowercase().contains("64")

    /**
     * Returns the value of the environment variable [name], or `null` if unset.
     */
    public fun getEnvironment(name: String): String? = System.getenv(name)

    /**
     * Returns a snapshot of all environment variables.
     */
    public fun getEnvironments(): Map<String, String> = System.getenv().toMap()

    /**
     * Returns the system property [name], or `null` if unset.
     */
    public fun getSystemProperty(name: String): String? = System.getProperty(name)

    /**
     * Requests the JVM to perform garbage collection.
     *
     * This is a hint; the JVM is free to ignore it.
     */
    public fun gc(): Unit = System.gc()

    /**
     * Terminates the JVM with the given [status] code.
     */
    public fun exit(status: Int = 0): Unit = kotlin.system.exitProcess(status)

    /**
     * Returns the current value of the running JVM's high-resolution time source, in nanoseconds.
     */
    public fun nanoTime(): Long = System.nanoTime()

    /**
     * Returns the current wall-clock time in milliseconds since the epoch.
     */
    public fun currentTimeMillis(): Long = System.currentTimeMillis()

    /**
     * Returns an estimate of the uptime of the JVM in milliseconds.
     */
    public fun uptimeMillis(): Long = ManagementFactory.getRuntimeMXBean().uptime
}
