package hasab.runtime.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class HsHttpRequestTest {

    @Test
    fun `get creates GET request`() {
        val req = HsHttpRequest.get("https://example.com")
        assertEquals("GET", req.method)
        assertEquals("https://example.com", req.url)
        assertNull(req.body)
    }

    @Test
    fun `post creates POST request with body`() {
        val req = HsHttpRequest.post("https://example.com", """{"key":"value"}""")
        assertEquals("POST", req.method)
        assertNotNull(req.body)
    }

    @Test
    fun `put creates PUT request`() {
        val req = HsHttpRequest.put("https://example.com", "data")
        assertEquals("PUT", req.method)
        assertNotNull(req.body)
    }

    @Test
    fun `delete creates DELETE request`() {
        val req = HsHttpRequest.delete("https://example.com")
        assertEquals("DELETE", req.method)
    }

    @Test
    fun `patch creates PATCH request`() {
        val req = HsHttpRequest.patch("https://example.com", "data")
        assertEquals("PATCH", req.method)
    }

    @Test
    fun `head creates HEAD request`() {
        val req = HsHttpRequest.head("https://example.com")
        assertEquals("HEAD", req.method)
    }

    @Test
    fun `builder sets custom method`() {
        val req = HsHttpRequest.Builder()
            .method("OPTIONS")
            .url("https://example.com")
            .build()
        assertEquals("OPTIONS", req.method)
    }

    @Test
    fun `builder sets headers`() {
        val req = HsHttpRequest.Builder()
            .url("https://example.com")
            .header("Authorization", "Bearer token")
            .build()
        assertEquals("Bearer token", req.headers["Authorization"])
    }

    @Test
    fun `builder sets multiple headers`() {
        val req = HsHttpRequest.Builder()
            .url("https://example.com")
            .headers(mapOf("A" to "1", "B" to "2"))
            .build()
        assertEquals("1", req.headers["A"])
        assertEquals("2", req.headers["B"])
    }

    @Test
    fun `builder sets timeout`() {
        val req = HsHttpRequest.Builder()
            .url("https://example.com")
            .timeout(5000)
            .build()
        assertEquals(5000, req.timeoutMs)
    }

    @Test
    fun `toBuilder round trips`() {
        val original = HsHttpRequest.Builder()
            .method("POST")
            .url("https://example.com")
            .body("body")
            .header("X-Custom", "value")
            .build()
        val rebuilt = original.toBuilder().build()
        assertEquals(original.method, rebuilt.method)
        assertEquals(original.url, rebuilt.url)
        assertEquals(original.headers, rebuilt.headers)
    }

    @Test
    fun `method is uppercased`() {
        val req = HsHttpRequest.Builder()
            .method("get")
            .url("https://example.com")
            .build()
        assertEquals("GET", req.method)
    }

    @Test
    fun `body string is encoded to bytes`() {
        val req = HsHttpRequest.post("https://example.com", "hello")
        assertEquals("hello", String(req.body!!))
    }
}
