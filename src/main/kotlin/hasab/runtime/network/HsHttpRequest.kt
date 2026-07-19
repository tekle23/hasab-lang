package hasab.runtime.network

/**
 * An immutable HTTP request descriptor.
 *
 * Use [HsHttpRequest.Builder] to construct instances.
 *
 * @property method The HTTP method (GET, POST, etc.).
 * @property url The target URL.
 * @property headers The request headers.
 * @property body The request body, or `null`.
 * @property timeoutMs The request timeout in milliseconds, or `null` for the default.
 */
public class HsHttpRequest private constructor(
    public val method: String,
    public val url: String,
    public val headers: Map<String, String>,
    public val body: ByteArray?,
    public val timeoutMs: Long?
) {
    /** Returns a new builder pre-populated with this request's values. */
    public fun toBuilder(): Builder = Builder().apply {
        method(this@HsHttpRequest.method)
        url(this@HsHttpRequest.url)
        for ((k, v) in headers) header(k, v)
        if (body != null) body(body)
        if (timeoutMs != null) timeout(timeoutMs)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsHttpRequest) return false
        return method == other.method && url == other.url && headers == other.headers &&
            body.contentEquals(other.body) && timeoutMs == other.timeoutMs
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        result = 31 * result + (timeoutMs?.hashCode() ?: 0)
        return result
    }

    /**
     * Builder for constructing [HsHttpRequest] instances.
     */
    public class Builder {
        private var _method: String = "GET"
        private var _url: String = ""
        private val _headers: MutableMap<String, String> = mutableMapOf()
        private var _body: ByteArray? = null
        private var _timeoutMs: Long? = null

        public fun method(method: String): Builder { _method = method.uppercase(); return this }
        public fun url(url: String): Builder { _url = url; return this }
        public fun header(name: String, value: String): Builder { _headers[name] = value; return this }
        public fun headers(headers: Map<String, String>): Builder { _headers.putAll(headers); return this }
        public fun body(body: ByteArray): Builder { _body = body; return this }
        public fun body(body: String): Builder { _body = body.toByteArray(Charsets.UTF_8); return this }
        public fun timeout(ms: Long): Builder { _timeoutMs = ms; return this }

        public fun build(): HsHttpRequest = HsHttpRequest(_method, _url, _headers.toMap(), _body, _timeoutMs)
    }

    public companion object {
        /** Creates a GET request. */
        public fun get(url: String): HsHttpRequest = Builder().method("GET").url(url).build()

        /** Creates a POST request with a string body. */
        public fun post(url: String, body: String): HsHttpRequest =
            Builder().method("POST").url(url).body(body).header("Content-Type", "application/json").build()

        /** Creates a PUT request with a string body. */
        public fun put(url: String, body: String): HsHttpRequest =
            Builder().method("PUT").url(url).body(body).header("Content-Type", "application/json").build()

        /** Creates a DELETE request. */
        public fun delete(url: String): HsHttpRequest = Builder().method("DELETE").url(url).build()

        /** Creates a PATCH request with a string body. */
        public fun patch(url: String, body: String): HsHttpRequest =
            Builder().method("PATCH").url(url).body(body).header("Content-Type", "application/json").build()

        /** Creates a HEAD request. */
        public fun head(url: String): HsHttpRequest = Builder().method("HEAD").url(url).build()
    }
}
