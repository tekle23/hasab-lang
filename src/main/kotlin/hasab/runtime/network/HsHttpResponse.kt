package hasab.runtime.network

/**
 * An immutable HTTP response received from a server.
 *
 * @property statusCode The HTTP status code (e.g. 200).
 * @property statusMessage The status message (e.g. "OK").
 * @property headers The response headers.
 * @property body The response body as a byte array.
 * @property url The final URL after any redirects.
 */
public class HsHttpResponse(
    public val statusCode: Int,
    public val statusMessage: String,
    public val headers: Map<String, List<String>>,
    public val body: ByteArray,
    public val url: String
) {
    /** Returns `true` if the status code is 2xx. */
    public val isSuccessful: Boolean get() = statusCode in 200..299

    /** Returns `true` if the status code is 3xx. */
    public val isRedirect: Boolean get() = statusCode in 300..399

    /** Returns `true` if the status code is 4xx. */
    public val isClientError: Boolean get() = statusCode in 400..499

    /** Returns `true` if the status code is 5xx. */
    public val isServerError: Boolean get() = statusCode in 500..599

    /** Returns the body decoded as a UTF-8 string. */
    public fun bodyAsString(): String = String(body, Charsets.UTF_8)

    /** Returns the value of the first header with the given [name], or `null`. */
    public fun header(name: String): String? =
        headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value?.firstOrNull()

    /** Returns the content-type header value, or an empty string. */
    public val contentType: String get() = header("Content-Type") ?: ""

    /** Returns the content-length header value, or `-1`. */
    public val contentLength: Long
        get() = header("Content-Length")?.toLongOrNull() ?: body.size.toLong()

    override fun toString(): String = "HsHttpResponse($statusCode $statusMessage, ${body.size} bytes)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsHttpResponse) return false
        return statusCode == other.statusCode && body.contentEquals(other.body)
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + body.contentHashCode()
        return result
    }
}
