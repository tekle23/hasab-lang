package hasab.cli.commands

/**
 * Client for the HASAB Package Registry.
 *
 * In production, this would make HTTP requests to packages.hasab.org.
 * For now, it provides a basic interface with simulated responses.
 */
public class PackageRegistry {

    /** The base URL of the package registry. */
    public val baseUrl: String = "https://packages.hasab.org"

    /** Simulated package catalog. */
    private val knownPackages = mapOf(
        "web" to "1.2.0",
        "json" to "2.1.0",
        "http" to "1.0.3",
        "database" to "1.5.0",
        "ui" to "2.0.0",
        "math" to "1.0.0",
        "crypto" to "1.1.0",
        "testing" to "1.0.0",
        "logging" to "1.2.1",
        "config" to "1.0.0",
    )

    /**
     * Resolves the latest version of a package.
     *
     * @return The version string, or `null` if the package is unknown.
     */
    public fun resolveLatest(packageName: String): String? {
        return knownPackages[packageName.lowercase()]
    }

    /**
     * Checks if a package exists in the registry.
     */
    public fun exists(packageName: String): Boolean {
        return knownPackages.containsKey(packageName.lowercase())
    }

    /**
     * Returns all available packages with their latest versions.
     */
    public fun listPackages(): Map<String, String> = knownPackages.toMap()

    /**
     * Returns the download URL for a specific package version.
     */
    public fun downloadUrl(packageName: String, version: String): String {
        return "$baseUrl/packages/$packageName/$version/download"
    }

    /**
     * Validates that a version string is a valid semver.
     */
    public fun isValidVersion(version: String): Boolean {
        return Regex("^\\d+\\.\\d+\\.\\d+(-\\w+)?(\\+\\w+)?$").matches(version)
    }
}
