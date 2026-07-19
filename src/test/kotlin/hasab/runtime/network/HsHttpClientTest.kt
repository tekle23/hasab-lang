package hasab.runtime.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HsHttpClientTest {

    @Test
    fun `client default timeout`() {
        val client = HsHttpClient()
        assertEquals(30_000L, client.defaultTimeoutMs)
    }

    @Test
    fun `client custom timeout`() {
        val client = HsHttpClient(defaultTimeoutMs = 5000L)
        assertEquals(5000L, client.defaultTimeoutMs)
    }

    @Test
    fun `client follow redirects default`() {
        val client = HsHttpClient()
        assertTrue(client.followRedirects)
    }

    @Test
    fun `client follow redirects disabled`() {
        val client = HsHttpClient(followRedirects = false)
        assertEquals(false, client.followRedirects)
    }

    @Test
    fun `get request builds correctly`() {
        val client = HsHttpClient()
        val request = HsHttpRequest.get("http://localhost:1")
        assertEquals("GET", request.method)
    }

    @Test
    fun `post request builds correctly`() {
        val client = HsHttpClient()
        val request = HsHttpRequest.post("http://localhost:1", "body")
        assertEquals("POST", request.method)
    }
}
