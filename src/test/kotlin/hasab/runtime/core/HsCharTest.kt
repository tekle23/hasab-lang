package hasab.runtime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class HsCharTest {

    @Test
    public fun `toString converts char to string`() {
        assertEquals("A", HsChar.toString('A'))
        assertEquals("0", HsChar.toString('0'))
        assertEquals(" ", HsChar.toString(' '))
    }

    @Test
    public fun `toInt returns code point`() {
        assertEquals(65, HsChar.toInt('A'))
        assertEquals(0, HsChar.toInt('\u0000'))
        assertEquals(48, HsChar.toInt('0'))
    }

    @Test
    public fun `fromInt creates char from code point`() {
        assertEquals('A', HsChar.fromInt(65))
        assertEquals('\u0000', HsChar.fromInt(0))
        assertEquals('0', HsChar.fromInt(48))
    }

    @Test
    public fun `round trip toInt and fromInt`() {
        for (code in 0..127) {
            assertEquals(code, HsChar.toInt(HsChar.fromInt(code)))
        }
    }

    @Test
    public fun `isLetter detects letters`() {
        assertTrue(HsChar.isLetter('a'))
        assertTrue(HsChar.isLetter('Z'))
        assertTrue(HsChar.isLetter('\u00E9'))
        assertFalse(HsChar.isLetter('0'))
        assertFalse(HsChar.isLetter(' '))
    }

    @Test
    public fun `isDigit detects digits`() {
        assertTrue(HsChar.isDigit('0'))
        assertTrue(HsChar.isDigit('9'))
        assertFalse(HsChar.isDigit('a'))
        assertFalse(HsChar.isDigit(' '))
    }

    @Test
    public fun `isLetterOrDigit detects both`() {
        assertTrue(HsChar.isLetterOrDigit('a'))
        assertTrue(HsChar.isLetterOrDigit('5'))
        assertFalse(HsChar.isLetterOrDigit(' '))
        assertFalse(HsChar.isLetterOrDigit('!'))
    }

    @Test
    public fun `isUpperCase and isLowerCase`() {
        assertTrue(HsChar.isUpperCase('A'))
        assertTrue(HsChar.isUpperCase('Z'))
        assertFalse(HsChar.isUpperCase('a'))
        assertFalse(HsChar.isUpperCase('0'))
        assertTrue(HsChar.isLowerCase('a'))
        assertTrue(HsChar.isLowerCase('z'))
        assertFalse(HsChar.isLowerCase('A'))
        assertFalse(HsChar.isLowerCase('0'))
    }

    @Test
    public fun `toUpperCase and toLowerCase convert case`() {
        assertEquals('A', HsChar.toUpperCase('a'))
        assertEquals('A', HsChar.toUpperCase('A'))
        assertEquals('Z', HsChar.toUpperCase('z'))
        assertEquals('a', HsChar.toLowerCase('A'))
        assertEquals('a', HsChar.toLowerCase('a'))
        assertEquals('0', HsChar.toLowerCase('0'))
    }

    @Test
    public fun `isWhitespace detects whitespace`() {
        assertTrue(HsChar.isWhitespace(' '))
        assertTrue(HsChar.isWhitespace('\t'))
        assertTrue(HsChar.isWhitespace('\n'))
        assertFalse(HsChar.isWhitespace('a'))
        assertFalse(HsChar.isWhitespace('0'))
    }

    @Test
    public fun `isControl detects control characters`() {
        assertTrue(HsChar.isControl('\u0000'))
        assertTrue(HsChar.isControl('\u001F'))
        assertFalse(HsChar.isControl('A'))
        assertFalse(HsChar.isControl(' '))
    }

    @Test
    public fun `isPrintable detects printable characters`() {
        assertTrue(HsChar.isPrintable('A'))
        assertTrue(HsChar.isPrintable('0'))
        assertTrue(HsChar.isPrintable('!'))
        assertFalse(HsChar.isPrintable('\u0000'))
        assertFalse(HsChar.isPrintable(' '))
    }

    @Test
    public fun `compare orders by code point`() {
        assertTrue(HsChar.compare('a', 'b') < 0)
        assertEquals(0, HsChar.compare('A', 'A'))
        assertTrue(HsChar.compare('z', 'a') > 0)
        assertTrue(HsChar.compare('0', '9') < 0)
    }
}
