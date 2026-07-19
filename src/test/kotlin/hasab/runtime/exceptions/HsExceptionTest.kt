package hasab.runtime.exceptions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

public class HsExceptionTest {

    @Test
    public fun `HsException with message`() {
        val ex = HsException("test error")
        assertEquals("test error", ex.message)
        assertEquals("test error", ex.hsMessage)
    }

    @Test
    public fun `HsException with message and cause`() {
        val cause = RuntimeException("root cause")
        val ex = HsException("wrapper", cause)
        assertEquals("wrapper", ex.message)
        assertEquals(cause, ex.cause)
    }

    @Test
    public fun `HsException with no args`() {
        val ex = HsException()
        assertNull(ex.message)
        assertEquals("Unknown error", ex.hsMessage)
    }

    @Test
    public fun `HsException toString formatting`() {
        val ex = HsException("something broke")
        assertEquals("HsException: something broke", ex.toString())
    }

    @Test
    public fun `HsException hsMessage falls back to Unknown error`() {
        val ex = HsException()
        assertEquals("Unknown error", ex.hsMessage)
    }

    @Test
    public fun `HsException is a RuntimeException`() {
        val ex = HsException("test")
        assertTrue(ex is RuntimeException)
    }

    @Test
    public fun `HsRuntimeException message`() {
        val ex = HsRuntimeException("runtime failure")
        assertEquals("runtime failure", ex.message)
    }

    @Test
    public fun `HsRuntimeException inherits HsException`() {
        val ex = HsRuntimeException("err")
        assertTrue(ex is HsException)
    }

    @Test
    public fun `HsRuntimeException toString`() {
        val ex = HsRuntimeException("bad state")
        assertEquals("HsException: bad state", ex.toString())
    }

    @Test
    public fun `HsNullPointerException default message`() {
        val ex = HsNullPointerException()
        assertEquals("Null reference", ex.message)
    }

    @Test
    public fun `HsNullPointerException custom message`() {
        val ex = HsNullPointerException("deref null var")
        assertEquals("deref null var", ex.message)
    }

    @Test
    public fun `HsNullPointerException inherits HsException`() {
        assertTrue(HsNullPointerException() is HsException)
    }

    @Test
    public fun `HsIndexOutOfBoundsException with message`() {
        val ex = HsIndexOutOfBoundsException("index 5 out of range")
        assertEquals("index 5 out of range", ex.message)
    }

    @Test
    public fun `HsIndexOutOfBoundsException create factory`() {
        val ex = HsIndexOutOfBoundsException.create(10, 5)
        assertEquals("Index 10 is out of bounds for size 5", ex.message)
    }

    @Test
    public fun `HsIndexOutOfBoundsException create with zero index`() {
        val ex = HsIndexOutOfBoundsException.create(0, 0)
        assertEquals("Index 0 is out of bounds for size 0", ex.message)
    }

    @Test
    public fun `HsIndexOutOfBoundsException create with negative index`() {
        val ex = HsIndexOutOfBoundsException.create(-1, 3)
        assertEquals("Index -1 is out of bounds for size 3", ex.message)
    }

    @Test
    public fun `HsIndexOutOfBoundsException inherits HsException`() {
        assertTrue(HsIndexOutOfBoundsException("x") is HsException)
    }

    @Test
    public fun `HsTypeException with message`() {
        val ex = HsTypeException("wrong type")
        assertEquals("wrong type", ex.message)
    }

    @Test
    public fun `HsTypeException create factory`() {
        val ex = HsTypeException.create("Int", "String")
        assertEquals("Type mismatch: expected Int, got String", ex.message)
    }

    @Test
    public fun `HsTypeException create with same types`() {
        val ex = HsTypeException.create("Bool", "Bool")
        assertEquals("Type mismatch: expected Bool, got Bool", ex.message)
    }

    @Test
    public fun `HsTypeException inherits HsException`() {
        assertTrue(HsTypeException("x") is HsException)
    }

    @Test
    public fun `HsValueError message`() {
        val ex = HsValueError("invalid value")
        assertEquals("invalid value", ex.message)
    }

    @Test
    public fun `HsValueError inherits HsException`() {
        assertTrue(HsValueError("x") is HsException)
    }

    @Test
    public fun `HsIOError message only`() {
        val ex = HsIOError("io failed")
        assertEquals("io failed", ex.message)
        assertTrue(ex.cause is RuntimeException)
    }

    @Test
    public fun `HsIOError with cause`() {
        val cause = java.io.FileNotFoundException("missing.txt")
        val ex = HsIOError("read failed", cause)
        assertEquals("read failed", ex.message)
        assertEquals(cause, ex.cause)
    }

    @Test
    public fun `HsIOError with null cause creates default`() {
        val ex = HsIOError("write failed", null)
        assertEquals("write failed", ex.message)
        assertTrue(ex.cause is RuntimeException)
        assertEquals("write failed", ex.cause!!.message)
    }

    @Test
    public fun `HsIOError inherits HsException`() {
        assertTrue(HsIOError("x") is HsException)
    }

    @Test
    public fun `HsDivisionByZeroError default message`() {
        val ex = HsDivisionByZeroError()
        assertEquals("Division by zero", ex.message)
    }

    @Test
    public fun `HsDivisionByZeroError custom message`() {
        val ex = HsDivisionByZeroError("mod by zero")
        assertEquals("mod by zero", ex.message)
    }

    @Test
    public fun `HsDivisionByZeroError inherits HsException`() {
        assertTrue(HsDivisionByZeroError() is HsException)
    }

    @Test
    public fun `HsStackOverflowError default message`() {
        val ex = HsStackOverflowError()
        assertEquals("Stack overflow", ex.message)
    }

    @Test
    public fun `HsStackOverflowError custom message`() {
        val ex = HsStackOverflowError("recursion too deep")
        assertEquals("recursion too deep", ex.message)
    }

    @Test
    public fun `HsStackOverflowError inherits HsException`() {
        assertTrue(HsStackOverflowError() is HsException)
    }

    @Test
    public fun `HsNotImplementedError default message`() {
        val ex = HsNotImplementedError()
        assertEquals("Not implemented", ex.message)
    }

    @Test
    public fun `HsNotImplementedError custom message`() {
        val ex = HsNotImplementedError("feature X not done")
        assertEquals("feature X not done", ex.message)
    }

    @Test
    public fun `HsNotImplementedError inherits HsException`() {
        assertTrue(HsNotImplementedError() is HsException)
    }

    @Test
    public fun `exception chaining preserves full cause`() {
        val root = IllegalArgumentException("root")
        val mid = HsRuntimeException("mid", root)
        val top = HsIOError("top", mid)
        assertEquals("top", top.message)
        assertEquals(mid, top.cause)
        assertEquals("mid", top.cause!!.message)
        assertEquals(root, top.cause!!.cause)
    }

    @Test
    public fun `all exceptions are catchable as HsException`() {
        val exceptions: List<HsException> = listOf(
            HsRuntimeException("r"),
            HsNullPointerException(),
            HsIndexOutOfBoundsException("i"),
            HsTypeException("t"),
            HsValueError("v"),
            HsIOError("io"),
            HsDivisionByZeroError(),
            HsStackOverflowError(),
            HsNotImplementedError()
        )
        for (ex in exceptions) {
            val caught = assertFailsWith<HsException> { throw ex }
            assertTrue(caught.hsMessage.isNotEmpty())
        }
    }

    @Test
    public fun `all exceptions are catchable as RuntimeException`() {
        val ex = HsRuntimeException("test")
        assertFailsWith<RuntimeException> { throw ex }
    }
}
