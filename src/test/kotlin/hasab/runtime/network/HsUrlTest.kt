package hasab.runtime.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class HsUrlTest {

    @Test
    fun `parse url with all components`() {
        val url = HsUrl.parse("https://user:pass@example.com:8080/path?q=1#frag")
        assertEquals("https", url.protocol)
        assertEquals("example.com", url.host)
        assertEquals(8080, url.port)
        assertEquals("/path", url.path)
        assertEquals("q=1", url.query)
        assertEquals("frag", url.fragment)
        assertEquals("user", url.username)
        assertEquals("pass", url.password)
    }

    @Test
    fun `parse simple http url`() {
        val url = HsUrl.parse("http://example.com")
        assertEquals("http", url.protocol)
        assertEquals("example.com", url.host)
        assertEquals(-1, url.port)
    }

    @Test
    fun `isSecure for https`() {
        assertTrue(HsUrl.parse("https://example.com").isSecure)
        assertFalse(HsUrl.parse("http://example.com").isSecure)
    }

    @Test
    fun `effectivePort defaults correctly`() {
        assertEquals(443, HsUrl.parse("https://example.com").effectivePort)
        assertEquals(80, HsUrl.parse("http://example.com").effectivePort)
        assertEquals(8080, HsUrl.parse("http://example.com:8080").effectivePort)
    }

    @Test
    fun `origin returns scheme and host`() {
        val url = HsUrl.parse("https://example.com/path")
        assertEquals("https://example.com", url.origin)
    }

    @Test
    fun `origin includes non-default port`() {
        val url = HsUrl.parse("http://example.com:9090/path")
        assertEquals("http://example.com:9090", url.origin)
    }

    @Test
    fun `pathSegments splits correctly`() {
        val url = HsUrl.parse("https://example.com/a/b/c")
        assertEquals(listOf("a", "b", "c"), url.pathSegments())
    }

    @Test
    fun `pathSegments empty for root`() {
        assertEquals(emptyList(), HsUrl.parse("https://example.com").pathSegments())
    }

    @Test
    fun `withQuery adds parameters`() {
        val url = HsUrl.parse("https://example.com/path")
        val newUrl = url.withQuery(mapOf("key" to "value"))
        assertTrue(newUrl.query.contains("key=value"))
    }

    @Test
    fun `withFragment adds fragment`() {
        val url = HsUrl.parse("https://example.com/path")
        val newUrl = url.withFragment("section1")
        assertEquals("section1", newUrl.fragment)
    }

    @Test
    fun `isValid returns true for valid urls`() {
        assertTrue(HsUrl.isValid("https://example.com"))
        assertTrue(HsUrl.isValid("http://localhost:3000/api"))
    }

    @Test
    fun `isValid returns false for invalid urls`() {
        assertFalse(HsUrl.isValid("not a url"))
        assertFalse(HsUrl.isValid("://invalid"))
    }

    @Test
    fun `build creates url from components`() {
        val url = HsUrl.build("https", "example.com", path = "/api/v1")
        assertEquals("https", url.protocol)
        assertEquals("example.com", url.host)
        assertEquals("/api/v1", url.path)
    }

    @Test
    fun `build with port`() {
        val url = HsUrl.build("http", "localhost", port = 3000)
        assertEquals(3000, url.port)
    }

    @Test
    fun `resolve relative url`() {
        val url = HsUrl.parse("https://example.com/a/b/c")
        val resolved = url.resolve("../d")
        assertTrue(resolved.path.contains("d"))
    }

    @Test
    fun `href returns full url`() {
        val url = HsUrl.parse("https://example.com/path?q=1")
        assertEquals("https://example.com/path?q=1", url.href)
    }

    @Test
    fun `toString returns href`() {
        val url = HsUrl.parse("https://example.com")
        assertEquals(url.href, url.toString())
    }

    @Test
    fun `equals and hashCode work`() {
        val url1 = HsUrl.parse("https://example.com")
        val url2 = HsUrl.parse("https://example.com")
        assertEquals(url1, url2)
        assertEquals(url1.hashCode(), url2.hashCode())
    }

    @Test
    fun `username and password empty for no userInfo`() {
        val url = HsUrl.parse("https://example.com")
        assertEquals("", url.username)
        assertEquals("", url.password)
    }
}
