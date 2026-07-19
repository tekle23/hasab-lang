package hasab.runtime.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class HsHttpResponseTest {

    @Test
    fun `isSuccessful for 2xx`() {
        val resp = HsHttpResponse(200, "OK", emptyMap(), ByteArray(0), "http://example.com")
        assertTrue(resp.isSuccessful)
        assertFalse(resp.isClientError)
        assertFalse(resp.isServerError)
    }

    @Test
    fun `isRedirect for 3xx`() {
        val resp = HsHttpResponse(301, "Moved", emptyMap(), ByteArray(0), "http://example.com")
        assertTrue(resp.isRedirect)
    }

    @Test
    fun `isClientError for 4xx`() {
        val resp = HsHttpResponse(404, "Not Found", emptyMap(), ByteArray(0), "http://example.com")
        assertTrue(resp.isClientError)
    }

    @Test
    fun `isServerError for 5xx`() {
        val resp = HsHttpResponse(500, "Internal Server Error", emptyMap(), ByteArray(0), "http://example.com")
        assertTrue(resp.isServerError)
    }

    @Test
    fun `bodyAsString decodes body`() {
        val resp = HsHttpResponse(200, "OK", emptyMap(), "hello".toByteArray(), "http://example.com")
        assertEquals("hello", resp.bodyAsString())
    }

    @Test
    fun `header returns first matching header`() {
        val headers = mapOf("Content-Type" to listOf("text/html"))
        val resp = HsHttpResponse(200, "OK", headers, ByteArray(0), "http://example.com")
        assertEquals("text/html", resp.header("Content-Type"))
    }

    @Test
    fun `header is case-insensitive`() {
        val headers = mapOf("Content-Type" to listOf("text/html"))
        val resp = HsHttpResponse(200, "OK", headers, ByteArray(0), "http://example.com")
        assertEquals("text/html", resp.header("content-type"))
    }

    @Test
    fun `header returns null for missing header`() {
        val resp = HsHttpResponse(200, "OK", emptyMap(), ByteArray(0), "http://example.com")
        assertNull(resp.header("X-Missing"))
    }

    @Test
    fun `contentType shortcut works`() {
        val headers = mapOf("Content-Type" to listOf("application/json"))
        val resp = HsHttpResponse(200, "OK", headers, ByteArray(0), "http://example.com")
        assertEquals("application/json", resp.contentType)
    }

    @Test
    fun `contentType empty when missing`() {
        val resp = HsHttpResponse(200, "OK", emptyMap(), ByteArray(0), "http://example.com")
        assertEquals("", resp.contentType)
    }

    @Test
    fun `toString includes status code`() {
        val resp = HsHttpResponse(200, "OK", emptyMap(), ByteArray(0), "http://example.com")
        assertTrue(resp.toString().contains("200"))
    }

    @Test
    fun `equals considers status and body`() {
        val resp1 = HsHttpResponse(200, "OK", emptyMap(), "hi".toByteArray(), "http://example.com")
        val resp2 = HsHttpResponse(200, "OK", emptyMap(), "hi".toByteArray(), "http://example.com")
        assertEquals(resp1, resp2)
    }

    @Test
    fun `not successful for 4xx`() {
        val resp = HsHttpResponse(404, "Not Found", emptyMap(), ByteArray(0), "http://example.com")
        assertFalse(resp.isSuccessful)
    }
}
