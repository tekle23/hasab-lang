package hasab.runtime.network

import hasab.runtime.exceptions.HsIOError
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI

/**
 * A simple HTTP client for the HASAB runtime.
 *
 * Wraps [HttpURLConnection] to provide convenience methods
 * for common HTTP operations.
 *
 * @property defaultTimeoutMs The default timeout for requests in milliseconds.
 * @property followRedirects Whether to follow HTTP redirects automatically.
 */
public class HsHttpClient(
    public val defaultTimeoutMs: Long = 30_000L,
    public val followRedirects: Boolean = true
) {
    /**
     * Executes an [HsHttpRequest] and returns an [HsHttpResponse].
     *
     * @param request The request to execute.
     * @return The response from the server.
     * @throws HsIOError if the request fails.
     */
    public fun execute(request: HsHttpRequest): HsHttpResponse {
        try {
            val urlObj = URI.create(request.url).toURL()
            val conn = urlObj.openConnection() as HttpURLConnection
            conn.requestMethod = request.method
            conn.instanceFollowRedirects = followRedirects
            conn.connectTimeout = (request.timeoutMs ?: defaultTimeoutMs).toInt()
            conn.readTimeout = (request.timeoutMs ?: defaultTimeoutMs).toInt()
            conn.doOutput = request.body != null

            for ((name, value) in request.headers) {
                conn.setRequestProperty(name, value)
            }

            if (request.body != null) {
                conn.outputStream.use { it.write(request.body) }
            }

            val status = conn.responseCode
            val message = conn.responseMessage ?: ""
            val responseHeaders = conn.headerFields
                .filterKeys { it != null }
                .mapValues { it.value.filterNotNull() }

            val body = try {
                val stream = if (status in 200..299) conn.inputStream else conn.errorStream
                stream?.use { readAllBytes(it) } ?: ByteArray(0)
            } catch (_: Exception) {
                ByteArray(0)
            }

            return HsHttpResponse(status, message, responseHeaders, body, conn.url.toString())
        } catch (e: HsIOError) {
            throw e
        } catch (e: Exception) {
            throw HsIOError("HTTP request failed: ${request.method} ${request.url}: ${e.message}", e)
        }
    }

    /** Convenience method: sends a GET request and returns the response. */
    public fun get(url: String, headers: Map<String, String> = emptyMap()): HsHttpResponse {
        val request = HsHttpRequest.Builder().method("GET").url(url).headers(headers).build()
        return execute(request)
    }

    /** Convenience method: sends a POST request with a string body and returns the response. */
    public fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): HsHttpResponse {
        val request = HsHttpRequest.Builder().method("POST").url(url).body(body).headers(headers)
            .header("Content-Type", "application/json").build()
        return execute(request)
    }

    /** Convenience method: sends a PUT request with a string body and returns the response. */
    public fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): HsHttpResponse {
        val request = HsHttpRequest.Builder().method("PUT").url(url).body(body).headers(headers)
            .header("Content-Type", "application/json").build()
        return execute(request)
    }

    /** Convenience method: sends a DELETE request and returns the response. */
    public fun delete(url: String, headers: Map<String, String> = emptyMap()): HsHttpResponse {
        val request = HsHttpRequest.Builder().method("DELETE").url(url).headers(headers).build()
        return execute(request)
    }

    /** Convenience method: sends a HEAD request and returns the response. */
    public fun head(url: String, headers: Map<String, String> = emptyMap()): HsHttpResponse {
        val request = HsHttpRequest.Builder().method("HEAD").url(url).headers(headers).build()
        return execute(request)
    }

    private fun readAllBytes(inputStream: java.io.InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(data, 0, data.size).also { bytesRead = it } != -1) {
            buffer.write(data, 0, bytesRead)
        }
        return buffer.toByteArray()
    }
}
