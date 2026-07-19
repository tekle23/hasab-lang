package hasab.runtime.network

import java.net.URI
import java.net.URL

/**
 * URL parsing and manipulation utilities for the HASAB runtime.
 *
 * @property raw The original URL string.
 */
public class HsUrl(raw: String) {

    private val uri: URI = URI.create(raw)
    private val url: URL = uri.toURL()

    /** The full URL string. */
    public val href: String get() = url.toString()

    /** The protocol scheme (e.g. `"https"`). */
    public val protocol: String get() = url.protocol

    /** The host name. */
    public val host: String get() = url.host ?: ""

    /** The port number, or `-1` if none was specified. */
    public val port: Int get() = url.port

    /** The path component. */
    public val path: String get() = url.path ?: ""

    /** The query string (without the leading `?`), or an empty string. */
    public val query: String get() = url.query ?: ""

    /** The fragment identifier (without the leading `#`), or an empty string. */
    public val fragment: String get() = url.ref ?: ""

    /** The username, or an empty string. */
    public val username: String get() = uri.userInfo?.substringBefore(':') ?: ""

    /** The password, or an empty string. */
    public val password: String get() = uri.userInfo?.substringAfter(':') ?: ""

    /** The effective port, defaulting to the standard port for the scheme. */
    public val effectivePort: Int
        get() = if (port != -1) port else when (protocol) {
            "https" -> 443
            "ftp" -> 21
            else -> 80
        }

    /** Returns `true` if the protocol is `"https"`. */
    public val isSecure: Boolean get() = protocol == "https"

    /** Returns the origin (scheme + host + port). */
    public val origin: String get() = "$protocol://$host${if (effectivePort != 80 && effectivePort != 443) ":$effectivePort" else ""}"

    /** Returns a new [HsUrl] with the given query parameters appended. */
    public fun withQuery(params: Map<String, String>): HsUrl {
        val qs = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val newRaw = "$protocol://$host${if (port != -1) ":$port" else ""}$path?$qs${if (fragment.isNotEmpty()) "#$fragment" else ""}"
        return HsUrl(newRaw)
    }

    /** Returns a new [HsUrl] with the given fragment. */
    public fun withFragment(newFragment: String): HsUrl {
        val base = href.substringBefore('#')
        return HsUrl("$base#$newFragment")
    }

    /** Resolves [relativeUrl] against this URL. */
    public fun resolve(relativeUrl: String): HsUrl {
        val resolved = url.toURI().resolve(relativeUrl).toURL()
        return HsUrl(resolved.toString())
    }

    /** Returns the path segments split by `/`. */
    public fun pathSegments(): List<String> =
        path.split("/").filter { it.isNotEmpty() }

    /** Returns `true` if the URL matches the given pattern. */
    public fun matches(pattern: Regex): Boolean = pattern.matches(href)

    override fun toString(): String = href

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsUrl) return false
        return href == other.href
    }

    override fun hashCode(): Int = href.hashCode()

    public companion object {
        /** Parses a URL string. Throws [IllegalArgumentException] if invalid. */
        public fun parse(raw: String): HsUrl = HsUrl(raw)

        /** Returns `true` if [url] is a valid URL. */
        public fun isValid(url: String): Boolean = try {
            URI.create(url); true
        } catch (_: Exception) {
            false
        }

        /** Creates a URL from its components. */
        public fun build(
            protocol: String,
            host: String,
            port: Int = -1,
            path: String = "",
            query: String? = null,
            fragment: String? = null
        ): HsUrl {
            val portStr = if (port != -1) ":$port" else ""
            val queryStr = if (query != null) "?$query" else ""
            val fragStr = if (fragment != null) "#$fragment" else ""
            return HsUrl("$protocol://$host$portStr$path$queryStr$fragStr")
        }
    }
}
