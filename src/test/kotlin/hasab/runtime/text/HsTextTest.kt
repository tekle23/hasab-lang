package hasab.runtime.text

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

public class HsTextTest {

    // ── length, isEmpty, isBlank ───────────────────────────────

    @Test
    public fun `length of string`() {
        assertEquals(5, HsText.length("hello"))
    }

    @Test
    public fun `length of empty string`() {
        assertEquals(0, HsText.length(""))
    }

    @Test
    public fun `isEmpty on empty string`() {
        assertTrue(HsText.isEmpty(""))
    }

    @Test
    public fun `isEmpty on non-empty string`() {
        assertFalse(HsText.isEmpty("a"))
    }

    @Test
    public fun `isNotEmpty on non-empty string`() {
        assertTrue(HsText.isNotEmpty("x"))
    }

    @Test
    public fun `isBlank on blank string`() {
        assertTrue(HsText.isBlank("   "))
    }

    @Test
    public fun `isBlank on empty string`() {
        assertTrue(HsText.isBlank(""))
    }

    @Test
    public fun `isBlank on non-blank string`() {
        assertFalse(HsText.isBlank(" a "))
    }

    @Test
    public fun `isNotBlank`() {
        assertTrue(HsText.isNotBlank("hello"))
        assertFalse(HsText.isNotBlank(""))
    }

    // ── upper, lower, capitalize ───────────────────────────────

    @Test
    public fun `upper converts to uppercase`() {
        assertEquals("HELLO", HsText.upper("hello"))
    }

    @Test
    public fun `upper on already uppercase`() {
        assertEquals("HELLO", HsText.upper("HELLO"))
    }

    @Test
    public fun `lower converts to lowercase`() {
        assertEquals("hello", HsText.lower("HELLO"))
    }

    @Test
    public fun `lower on already lowercase`() {
        assertEquals("hello", HsText.lower("hello"))
    }

    @Test
    public fun `capitalize first letter`() {
        assertEquals("Hello", HsText.capitalize("hello"))
    }

    @Test
    public fun `capitalize single char`() {
        assertEquals("A", HsText.capitalize("a"))
    }

    @Test
    public fun `decapitalize first letter`() {
        assertEquals("hELLO", HsText.decapitalize("HELLO"))
    }

    @Test
    public fun `capitalize empty string`() {
        assertEquals("", HsText.capitalize(""))
    }

    // ── trim ───────────────────────────────────────────────────

    @Test
    public fun `trim removes surrounding whitespace`() {
        assertEquals("hello", HsText.trim("  hello  "))
    }

    @Test
    public fun `trimStart removes leading whitespace`() {
        assertEquals("hello  ", HsText.trimStart("  hello  "))
    }

    @Test
    public fun `trimEnd removes trailing whitespace`() {
        assertEquals("  hello", HsText.trimEnd("  hello  "))
    }

    // ── padStart, padEnd ───────────────────────────────────────

    @Test
    public fun `padStart adds padding`() {
        assertEquals("007", HsText.padStart("7", 3, '0'))
    }

    @Test
    public fun `padStart no-op when already long enough`() {
        assertEquals("abc", HsText.padStart("abc", 2, '0'))
    }

    @Test
    public fun `padEnd adds padding`() {
        assertEquals("abc  ", HsText.padEnd("abc", 5, ' '))
    }

    @Test
    public fun `padEnd with custom char`() {
        assertEquals("abc...", HsText.padEnd("abc", 6, '.'))
    }

    @Test
    public fun `padStart with default space`() {
        assertEquals("  hi", HsText.padStart("hi", 4))
    }

    // ── center ─────────────────────────────────────────────────

    @Test
    public fun `center pads both sides`() {
        assertEquals(" hello ", HsText.center("hello", 7))
    }

    @Test
    public fun `center with odd padding puts extra on right`() {
        assertEquals("hello ", HsText.center("hello", 6))
    }

    @Test
    public fun `center returns original if too long`() {
        assertEquals("hello", HsText.center("hello", 3))
    }

    // ── repeat ─────────────────────────────────────────────────

    @Test
    public fun `repeat string`() {
        assertEquals("aaa", HsText.repeat("a", 3))
    }

    @Test
    public fun `repeat zero times`() {
        assertEquals("", HsText.repeat("abc", 0))
    }

    // ── reverse ────────────────────────────────────────────────

    @Test
    public fun `reverse string`() {
        assertEquals("olleh", HsText.reverse("hello"))
    }

    @Test
    public fun `reverse empty string`() {
        assertEquals("", HsText.reverse(""))
    }

    // ── contains, startsWith, endsWith ─────────────────────────

    @Test
    public fun `contains finds substring`() {
        assertTrue(HsText.contains("hello world", "world"))
    }

    @Test
    public fun `contains does not find substring`() {
        assertFalse(HsText.contains("hello", "xyz"))
    }

    @Test
    public fun `contains ignoreCase`() {
        assertTrue(HsText.contains("Hello", "ELL", ignoreCase = true))
    }

    @Test
    public fun `startsWith matches prefix`() {
        assertTrue(HsText.startsWith("hello", "hel"))
    }

    @Test
    public fun `startsWith does not match`() {
        assertFalse(HsText.startsWith("hello", "xyz"))
    }

    @Test
    public fun `startsWith ignoreCase`() {
        assertTrue(HsText.startsWith("Hello", "HEL", ignoreCase = true))
    }

    @Test
    public fun `endsWith matches suffix`() {
        assertTrue(HsText.endsWith("hello", "llo"))
    }

    @Test
    public fun `endsWith does not match`() {
        assertFalse(HsText.endsWith("hello", "xyz"))
    }

    @Test
    public fun `endsWith ignoreCase`() {
        assertTrue(HsText.endsWith("Hello", "LLO", ignoreCase = true))
    }

    // ── indexOf, lastIndexOf ───────────────────────────────────

    @Test
    public fun `indexOf finds position`() {
        assertEquals(2, HsText.indexOf("hello", "llo"))
    }

    @Test
    public fun `indexOf not found returns minus 1`() {
        assertEquals(-1, HsText.indexOf("hello", "xyz"))
    }

    @Test
    public fun `indexOf with start index`() {
        assertEquals(3, HsText.indexOf("hello", "l", 3))
    }

    @Test
    public fun `indexOf ignoreCase`() {
        assertEquals(2, HsText.indexOf("HeLLo", "ll", ignoreCase = true))
    }

    @Test
    public fun `lastIndexOf finds last occurrence`() {
        assertEquals(3, HsText.lastIndexOf("hello", "l"))
    }

    @Test
    public fun `lastIndexOf not found`() {
        assertEquals(-1, HsText.lastIndexOf("hello", "z"))
    }

    @Test
    public fun `lastIndexOf ignoreCase`() {
        assertEquals(2, HsText.lastIndexOf("HeLLo", "ll", ignoreCase = true))
    }

    // ── replace, replaceFirst ──────────────────────────────────

    @Test
    public fun `replace all occurrences`() {
        assertEquals("hewwo", HsText.replace("hello", "l", "w"))
    }

    @Test
    public fun `replace with no match`() {
        assertEquals("hello", HsText.replace("hello", "z", "w"))
    }

    @Test
    public fun `replace ignoreCase`() {
        assertEquals("Hewwo", HsText.replace("HeLLo", "l", "w", ignoreCase = true))
    }

    @Test
    public fun `replaceFirst only first occurrence`() {
        assertEquals("hewlo", HsText.replaceFirst("hello", "l", "w"))
    }

    @Test
    public fun `replaceFirst ignoreCase`() {
        assertEquals("Hewlo", HsText.replaceFirst("HeLlo", "l", "w", ignoreCase = true))
    }

    // ── split, join ────────────────────────────────────────────

    @Test
    public fun `split by comma`() {
        assertEquals(listOf("a", "b", "c"), HsText.split("a,b,c", ","))
    }

    @Test
    public fun `split with limit`() {
        assertEquals(listOf("a", "b,c"), HsText.split("a,b,c", ",", 2))
    }

    @Test
    public fun `split no delimiter match`() {
        assertEquals(listOf("hello"), HsText.split("hello", ","))
    }

    @Test
    public fun `join with default separator`() {
        assertEquals("a, b, c", HsText.join(listOf("a", "b", "c")))
    }

    @Test
    public fun `join with custom separator`() {
        assertEquals("a-b-c", HsText.join(listOf("a", "b", "c"), "-"))
    }

    @Test
    public fun `join empty list`() {
        assertEquals("", HsText.join(emptyList()))
    }

    // ── substring ──────────────────────────────────────────────

    @Test
    public fun `substring extracts range`() {
        assertEquals("llo", HsText.substring("hello", 2, 5))
    }

    @Test
    public fun `substring to end`() {
        assertEquals("llo", HsText.substring("hello", 2))
    }

    // ── take, drop ─────────────────────────────────────────────

    @Test
    public fun `take first n chars`() {
        assertEquals("hel", HsText.take("hello", 3))
    }

    @Test
    public fun `take more than length`() {
        assertEquals("hello", HsText.take("hello", 10))
    }

    @Test
    public fun `takeLast n chars`() {
        assertEquals("llo", HsText.takeLast("hello", 3))
    }

    @Test
    public fun `drop first n chars`() {
        assertEquals("lo", HsText.drop("hello", 3))
    }

    @Test
    public fun `drop more than length`() {
        assertEquals("", HsText.drop("hello", 10))
    }

    @Test
    public fun `dropLast n chars`() {
        assertEquals("hel", HsText.dropLast("hello", 2))
    }

    // ── chunked ────────────────────────────────────────────────

    @Test
    public fun `chunked splits into groups`() {
        assertEquals(listOf("he", "ll", "o"), HsText.chunked("hello", 2))
    }

    // ── lines ──────────────────────────────────────────────────

    @Test
    public fun `lines splits multiline`() {
        assertEquals(listOf("a", "b", "c"), HsText.lines("a\nb\nc"))
    }

    @Test
    public fun `lines single line`() {
        assertEquals(listOf("hello"), HsText.lines("hello"))
    }

    // ── matches (regex) ────────────────────────────────────────

    @Test
    public fun `matches full string regex`() {
        assertTrue(HsText.matches("hello", "hello"))
    }

    @Test
    public fun `matches partial is false`() {
        assertFalse(HsText.matches("hello", "ell"))
    }

    @Test
    public fun `matches digit pattern`() {
        assertTrue(HsText.matches("12345", "\\d+"))
    }

    @Test
    public fun `matches rejects non match`() {
        assertFalse(HsText.matches("abc", "\\d+"))
    }

    // ── replaceRegex ───────────────────────────────────────────

    @Test
    public fun `replaceRegex replaces matching`() {
        assertEquals("h3ll3", HsText.replaceRegex("hello", "[eo]", "3"))
    }

    @Test
    public fun `replaceRegex replaces all vowels`() {
        assertEquals("h3llo", HsText.replaceRegex("hello", "e", "3"))
        assertEquals("hell0", HsText.replaceRegex("hello", "o", "0"))
    }

    @Test
    public fun `replaceRegex no match`() {
        assertEquals("hello", HsText.replaceRegex("hello", "\\d", "0"))
    }

    // ── findAll ────────────────────────────────────────────────

    @Test
    public fun `findAll extracts all matches`() {
        assertEquals(listOf("123", "456"), HsText.findAll("abc123def456", "\\d+"))
    }

    @Test
    public fun `findAll no matches`() {
        assertEquals(emptyList(), HsText.findAll("hello", "\\d+"))
    }

    // ── isAlpha, isNumeric, isAlphaNumeric ─────────────────────

    @Test
    public fun `isAlpha true for letters`() {
        assertTrue(HsText.isAlpha("abcXYZ"))
    }

    @Test
    public fun `isAlpha false for digits`() {
        assertFalse(HsText.isAlpha("abc123"))
    }

    @Test
    public fun `isAlpha false for empty`() {
        assertFalse(HsText.isAlpha(""))
    }

    @Test
    public fun `isNumeric true for digits`() {
        assertTrue(HsText.isNumeric("12345"))
    }

    @Test
    public fun `isNumeric false for letters`() {
        assertFalse(HsText.isNumeric("12a34"))
    }

    @Test
    public fun `isNumeric false for empty`() {
        assertFalse(HsText.isNumeric(""))
    }

    @Test
    public fun `isAlphaNumeric true for mixed`() {
        assertTrue(HsText.isAlphaNumeric("abc123"))
    }

    @Test
    public fun `isAlphaNumeric false for special chars`() {
        assertFalse(HsText.isAlphaNumeric("abc!123"))
    }

    @Test
    public fun `isAlphaNumeric false for empty`() {
        assertFalse(HsText.isAlphaNumeric(""))
    }

    // ── isUpperCase, isLowerCase ───────────────────────────────

    @Test
    public fun `isUpperCase true`() {
        assertTrue(HsText.isUpperCase("HELLO"))
    }

    @Test
    public fun `isUpperCase false`() {
        assertFalse(HsText.isUpperCase("Hello"))
    }

    @Test
    public fun `isLowerCase true`() {
        assertTrue(HsText.isLowerCase("hello"))
    }

    @Test
    public fun `isLowerCase false`() {
        assertFalse(HsText.isLowerCase("Hello"))
    }

    @Test
    public fun `isUpperCase empty is false`() {
        assertFalse(HsText.isUpperCase(""))
    }

    // ── compareTo ──────────────────────────────────────────────

    @Test
    public fun `compareTo less than`() {
        assertTrue(HsText.compareTo("apple", "banana") < 0)
    }

    @Test
    public fun `compareTo greater than`() {
        assertTrue(HsText.compareTo("banana", "apple") > 0)
    }

    @Test
    public fun `compareTo equal`() {
        assertEquals(0, HsText.compareTo("hello", "hello"))
    }

    @Test
    public fun `compareTo ignoreCase`() {
        assertEquals(0, HsText.compareTo("Hello", "hello", ignoreCase = true))
    }

    // ── commonPrefix, commonSuffix ─────────────────────────────

    @Test
    public fun `commonPrefix finds shared prefix`() {
        assertEquals("hel", HsText.commonPrefix("hello", "help"))
    }

    @Test
    public fun `commonPrefix no common`() {
        assertEquals("", HsText.commonPrefix("abc", "xyz"))
    }

    @Test
    public fun `commonPrefix identical strings`() {
        assertEquals("hello", HsText.commonPrefix("hello", "hello"))
    }

    @Test
    public fun `commonSuffix finds shared suffix`() {
        assertEquals("ello", HsText.commonSuffix("hello", "jello"))
    }

    @Test
    public fun `commonSuffix no common`() {
        assertEquals("", HsText.commonSuffix("abc", "xyz"))
    }

    @Test
    public fun `commonSuffix identical strings`() {
        assertEquals("hello", HsText.commonSuffix("hello", "hello"))
    }

    // ── indent ─────────────────────────────────────────────────

    @Test
    public fun `indent adds spaces`() {
        assertEquals("  hello", HsText.indent("hello", 2))
    }

    @Test
    public fun `indent multiline`() {
        assertEquals(" a\n b", HsText.indent("a\nb", 1))
    }

    // ── escapeHtml, unescapeHtml ───────────────────────────────

    @Test
    public fun `escapeHtml encodes special chars`() {
        assertEquals("&amp;&lt;&gt;&quot;&#39;", HsText.escapeHtml("&<>\"'"))
    }

    @Test
    public fun `unescapeHtml decodes special chars`() {
        assertEquals("&<>\"'", HsText.unescapeHtml("&amp;&lt;&gt;&quot;&#39;"))
    }

    @Test
    public fun `escapeHtml unescapeHtml roundtrip`() {
        val original = "Tom & Jerry <\"said\"> it's here"
        assertEquals(original, HsText.unescapeHtml(HsText.escapeHtml(original)))
    }

    // ── truncate ───────────────────────────────────────────────

    @Test
    public fun `truncate shortens long text`() {
        assertEquals("He...", HsText.truncate("Hello World", 5))
    }

    @Test
    public fun `truncate does not shorten within limit`() {
        assertEquals("hi", HsText.truncate("hi", 10))
    }

    @Test
    public fun `truncate exact length`() {
        assertEquals("hello", HsText.truncate("hello", 5))
    }

    @Test
    public fun `truncate with custom suffix`() {
        assertEquals("Hel~~", HsText.truncate("Hello World", 5, "~~"))
    }

    // ── levenshteinDistance ────────────────────────────────────

    @Test
    public fun `levenshtein same strings is zero`() {
        assertEquals(0, HsText.levenshteinDistance("hello", "hello"))
    }

    @Test
    public fun `levenshtein single insertion`() {
        assertEquals(1, HsText.levenshteinDistance("cat", "cats"))
    }

    @Test
    public fun `levenshtein single deletion`() {
        assertEquals(1, HsText.levenshteinDistance("cats", "cat"))
    }

    @Test
    public fun `levenshtein single substitution`() {
        assertEquals(1, HsText.levenshteinDistance("cat", "cot"))
    }

    @Test
    public fun `levenshtein completely different`() {
        assertEquals(3, HsText.levenshteinDistance("abc", "xyz"))
    }

    @Test
    public fun `levenshtein empty strings`() {
        assertEquals(0, HsText.levenshteinDistance("", ""))
    }

    @Test
    public fun `levenshtein one empty`() {
        assertEquals(5, HsText.levenshteinDistance("hello", ""))
    }

    @Test
    public fun `levenshtein known value`() {
        assertEquals(3, HsText.levenshteinDistance("kitten", "sitting"))
    }
}
