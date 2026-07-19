package hasab.runtime.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class HsStringTest {

    @Test
    public fun `length returns string length`() {
        assertEquals(0, HsString.length(""))
        assertEquals(1, HsString.length("a"))
        assertEquals(5, HsString.length("hello"))
    }

    @Test
    public fun `charAt returns correct character`() {
        assertEquals('h', HsString.charAt("hello", 0))
        assertEquals('o', HsString.charAt("hello", 4))
    }

    @Test
    public fun `substring returns correct range`() {
        assertEquals("ell", HsString.substring("hello", 1, 4))
        assertEquals("hello", HsString.substring("hello", 0, 5))
        assertEquals("", HsString.substring("hello", 2, 2))
    }

    @Test
    public fun `toUpperCase and toLowerCase`() {
        assertEquals("HELLO", HsString.toUpperCase("hello"))
        assertEquals("HELLO", HsString.toUpperCase("Hello"))
        assertEquals("hello", HsString.toLowerCase("HELLO"))
        assertEquals("hello", HsString.toLowerCase("Hello"))
    }

    @Test
    public fun `trim removes whitespace`() {
        assertEquals("hello", HsString.trim("  hello  "))
        assertEquals("hello", HsString.trim("hello"))
        assertEquals("a b", HsString.trim("  a b  "))
        assertEquals("", HsString.trim("   "))
    }

    @Test
    public fun `startsWith and endsWith`() {
        assertTrue(HsString.startsWith("hello", "hel"))
        assertTrue(HsString.startsWith("hello", "hello"))
        assertFalse(HsString.startsWith("hello", "world"))
        assertTrue(HsString.endsWith("hello", "llo"))
        assertTrue(HsString.endsWith("hello", "hello"))
        assertFalse(HsString.endsWith("hello", "world"))
    }

    @Test
    public fun `contains finds substring`() {
        assertTrue(HsString.contains("hello world", "world"))
        assertFalse(HsString.contains("hello", "world"))
        assertTrue(HsString.contains("hello", ""))
    }

    @Test
    public fun `indexOf and lastIndexOf`() {
        assertEquals(6, HsString.indexOf("hello world", "world"))
        assertEquals(-1, HsString.indexOf("hello", "world"))
        assertEquals(0, HsString.indexOf("hello", "hel"))
        assertEquals(3, HsString.lastIndexOf("abcabc", "abc"))
    }

    @Test
    public fun `replace substitutes all occurrences`() {
        assertEquals("hxllo", HsString.replace("hello", "e", "x"))
        assertEquals("aaa", HsString.replace("aba", "b", "a"))
        assertEquals("hello", HsString.replace("hello", "x", "y"))
    }

    @Test
    public fun `split divides by delimiter`() {
        assertContentEquals(arrayOf("a", "b", "c"), HsString.split("a,b,c", ","))
        assertContentEquals(arrayOf("hello"), HsString.split("hello", ","))
        assertContentEquals(arrayOf("a", "b", ""), HsString.split("a,b,", ","))
    }

    @Test
    public fun `join concatenates with delimiter`() {
        assertEquals("a,b,c", HsString.join(arrayOf("a", "b", "c"), ","))
        assertEquals("abc", HsString.join(arrayOf("a", "b", "c"), ""))
        assertEquals("a", HsString.join(arrayOf("a"), "-"))
    }

    @Test
    public fun `isEmpty and isBlank`() {
        assertTrue(HsString.isEmpty(""))
        assertFalse(HsString.isEmpty("a"))
        assertTrue(HsString.isBlank(""))
        assertTrue(HsString.isBlank("   "))
        assertTrue(HsString.isBlank("\t\n"))
        assertFalse(HsString.isBlank("a"))
    }

    @Test
    public fun `repeat repeats string`() {
        assertEquals("aaa", HsString.repeat("a", 3))
        assertEquals("", HsString.repeat("hello", 0))
        assertEquals("hello", HsString.repeat("hello", 1))
    }

    @Test
    public fun `reverse reverses string`() {
        assertEquals("olleh", HsString.reverse("hello"))
        assertEquals("", HsString.reverse(""))
        assertEquals("a", HsString.reverse("a"))
    }

    @Test
    public fun `compareTo compares lexicographically`() {
        assertTrue(HsString.compareTo("abc", "abd") < 0)
        assertEquals(0, HsString.compareTo("abc", "abc"))
        assertTrue(HsString.compareTo("abd", "abc") > 0)
    }

    @Test
    public fun `format produces formatted string`() {
        assertEquals("hello 42", HsString.format("hello %d", 42))
        assertEquals("a and b", HsString.format("%s and %s", "a", "b"))
    }

    @Test
    public fun `valueOf converts to string`() {
        assertEquals("42", HsString.valueOf(42))
        assertEquals("true", HsString.valueOf(true))
        assertEquals("hello", HsString.valueOf("hello"))
        assertEquals("null", HsString.valueOf(null))
    }

    @Test
    public fun `parseBoolean parses correctly`() {
        assertTrue(HsString.parseBoolean("true"))
        assertTrue(HsString.parseBoolean("TRUE"))
        assertTrue(HsString.parseBoolean("True"))
        assertFalse(HsString.parseBoolean("false"))
        assertFalse(HsString.parseBoolean("abc"))
    }

    @Test
    public fun `parseInt and parseFloat parse numbers`() {
        assertEquals(42, HsString.parseInt("42"))
        assertEquals(-7, HsString.parseInt("-7"))
        assertEquals(3.14f, HsString.parseFloat("3.14"))
    }

    @Test
    public fun `toCharArray converts to char array`() {
        assertContentEquals(charArrayOf('h', 'e', 'l', 'l', 'o'), HsString.toCharArray("hello"))
        assertContentEquals(charArrayOf(), HsString.toCharArray(""))
    }

    @Test
    public fun `encode and decode round trip`() {
        val original = "hello 世界"
        val encoded = HsString.encode(original)
        val decoded = HsString.decode(encoded)
        assertEquals(original, decoded)
        assertEquals(12, encoded.size)
    }

    @Test
    public fun `isAlpha detects letters`() {
        assertTrue(HsString.isAlpha("hello"))
        assertTrue(HsString.isAlpha("ABC"))
        assertFalse(HsString.isAlpha("hello123"))
        assertFalse(HsString.isAlpha(""))
        assertFalse(HsString.isAlpha("hello world"))
    }

    @Test
    public fun `isNumeric detects digits`() {
        assertTrue(HsString.isNumeric("12345"))
        assertTrue(HsString.isNumeric("0"))
        assertFalse(HsString.isNumeric("12a34"))
        assertFalse(HsString.isNumeric(""))
        assertFalse(HsString.isNumeric("12 34"))
    }

    @Test
    public fun `isAlphaNumeric detects letters and digits`() {
        assertTrue(HsString.isAlphaNumeric("abc123"))
        assertTrue(HsString.isAlphaNumeric("hello"))
        assertTrue(HsString.isAlphaNumeric("42"))
        assertFalse(HsString.isAlphaNumeric("hello world"))
        assertFalse(HsString.isAlphaNumeric("abc!"))
        assertFalse(HsString.isAlphaNumeric(""))
    }
}
