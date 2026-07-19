package hasab.runtime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

public class HsIntTest {

    @Test
    public fun `MAX_VALUE and MIN_VALUE are correct`() {
        assertEquals(Int.MAX_VALUE, HsInt.MAX_VALUE)
        assertEquals(Int.MIN_VALUE, HsInt.MIN_VALUE)
    }

    @Test
    public fun `toString converts integer to string`() {
        assertEquals("0", HsInt.toString(0))
        assertEquals("42", HsInt.toString(42))
        assertEquals("-7", HsInt.toString(-7))
    }

    @Test
    public fun `parseInt parses valid integers`() {
        assertEquals(42, HsInt.parseInt("42"))
        assertEquals(-1, HsInt.parseInt("-1"))
        assertEquals(0, HsInt.parseInt("0"))
        assertEquals(Int.MAX_VALUE, HsInt.parseInt("2147483647"))
        assertEquals(Int.MIN_VALUE, HsInt.parseInt("-2147483648"))
    }

    @Test
    public fun `parseInt throws on invalid input`() {
        assertFailsWith<NumberFormatException> { HsInt.parseInt("abc") }
        assertFailsWith<NumberFormatException> { HsInt.parseInt("") }
        assertFailsWith<NumberFormatException> { HsInt.parseInt("3.14") }
    }

    @Test
    public fun `parseIntOrNull returns null on invalid input`() {
        assertNull(HsInt.parseIntOrNull("abc"))
        assertNull(HsInt.parseIntOrNull(""))
        assertNull(HsInt.parseIntOrNull("3.14"))
    }

    @Test
    public fun `parseIntOrNull returns value on valid input`() {
        assertEquals(42, HsInt.parseIntOrNull("42"))
        assertEquals(-7, HsInt.parseIntOrNull("-7"))
        assertEquals(0, HsInt.parseIntOrNull("0"))
    }

    @Test
    public fun `toChar converts to character`() {
        assertEquals('A', HsInt.toChar(65))
        assertEquals('\u0000', HsInt.toChar(0))
    }

    @Test
    public fun `toFloat converts to float`() {
        assertEquals(3.0f, HsInt.toFloat(3))
        assertEquals(-1.5f, HsInt.toFloat(-1) - 0.5f, 0.001f)
    }

    @Test
    public fun `toLong converts to long`() {
        assertEquals(42L, HsInt.toLong(42))
        assertEquals((-2147483647).toLong(), HsInt.toLong(Int.MIN_VALUE + 1))
    }

    @Test
    public fun `toByteArray and fromByteArray round trip`() {
        val bytes = HsInt.toByteArray(1)
        assertEquals(4, bytes.size)
        assertEquals(1, HsInt.fromByteArray(bytes))

        assertEquals(0, HsInt.fromByteArray(HsInt.toByteArray(0)))
        assertEquals(256, HsInt.fromByteArray(HsInt.toByteArray(256)))
        assertEquals(Int.MAX_VALUE, HsInt.fromByteArray(HsInt.toByteArray(Int.MAX_VALUE)))
        assertEquals(Int.MIN_VALUE, HsInt.fromByteArray(HsInt.toByteArray(Int.MIN_VALUE)))
    }

    @Test
    public fun `fromByteArray requires at least 4 bytes`() {
        assertFailsWith<IllegalArgumentException> { HsInt.fromByteArray(byteArrayOf(1, 2, 3)) }
    }

    @Test
    public fun `compare returns correct ordering`() {
        assertTrue(HsInt.compare(1, 2) < 0)
        assertEquals(0, HsInt.compare(5, 5))
        assertTrue(HsInt.compare(10, 3) > 0)
    }

    @Test
    public fun `coerceIn clamps to range`() {
        assertEquals(5, HsInt.coerceIn(5, 0, 10))
        assertEquals(0, HsInt.coerceIn(-5, 0, 10))
        assertEquals(10, HsInt.coerceIn(15, 0, 10))
    }

    @Test
    public fun `coerceAtLeast and coerceAtMost`() {
        assertEquals(10, HsInt.coerceAtLeast(5, 10))
        assertEquals(15, HsInt.coerceAtLeast(15, 10))
        assertEquals(5, HsInt.coerceAtMost(5, 10))
        assertEquals(10, HsInt.coerceAtMost(15, 10))
    }

    @Test
    public fun `digits returns individual digits`() {
        assertEquals(listOf(0), HsInt.digits(0))
        assertEquals(listOf(1, 2, 3), HsInt.digits(123))
        assertEquals(listOf(4, 2), HsInt.digits(-42))
        assertEquals(listOf(2, 1, 4, 7, 4, 8, 3, 6, 4, 8), HsInt.digits(Int.MIN_VALUE))
    }

    @Test
    public fun `isEven and isOdd`() {
        assertTrue(HsInt.isEven(0))
        assertTrue(HsInt.isEven(2))
        assertFalse(HsInt.isEven(1))
        assertFalse(HsInt.isOdd(0))
        assertTrue(HsInt.isOdd(1))
        assertTrue(HsInt.isOdd(-3))
    }

    @Test
    public fun `isPositive and isNegative`() {
        assertTrue(HsInt.isPositive(1))
        assertFalse(HsInt.isPositive(0))
        assertFalse(HsInt.isPositive(-1))
        assertTrue(HsInt.isNegative(-1))
        assertFalse(HsInt.isNegative(0))
        assertFalse(HsInt.isNegative(1))
    }

    @Test
    public fun `clamp is alias for coerceIn`() {
        assertEquals(5, HsInt.clamp(5, 0, 10))
        assertEquals(0, HsInt.clamp(-5, 0, 10))
        assertEquals(10, HsInt.clamp(15, 0, 10))
    }
}
