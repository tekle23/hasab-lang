package hasab.runtime.internal

import hasab.runtime.exceptions.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

public class HsInternalTest {

    @Test
    public fun `checkNotNull returns value for non-null`() {
        val result = HsInternal.checkNotNull("hello")
        assertEquals("hello", result)
    }

    @Test
    public fun `checkNotNull returns int for non-null int`() {
        val result = HsInternal.checkNotNull(42)
        assertEquals(42, result)
    }

    @Test
    public fun `checkNotNull throws HsNullPointerException for null`() {
        assertFailsWith<HsNullPointerException> {
            HsInternal.checkNotNull(null)
        }
    }

    @Test
    public fun `checkType returns value for matching type`() {
        val result = HsInternal.checkType("hello", "String")
        assertEquals("hello", result)
    }

    @Test
    public fun `checkType returns null for null value`() {
        val result = HsInternal.checkType(null, "String")
        assertEquals(null, result)
    }

    @Test
    public fun `checkType returns value for Int type`() {
        val result = HsInternal.checkType(42, "Int")
        assertEquals(42, result)
    }

    @Test
    public fun `checkType returns value for Long type`() {
        val result = HsInternal.checkType(42L, "Long")
        assertEquals(42L, result)
    }

    @Test
    public fun `checkType returns value for Float type`() {
        val result = HsInternal.checkType(3.14f, "Float")
        assertEquals(3.14f, result)
    }

    @Test
    public fun `checkType returns value for Double type`() {
        val result = HsInternal.checkType(3.14, "Double")
        assertEquals(3.14, result)
    }

    @Test
    public fun `checkType returns value for Boolean type`() {
        val result = HsInternal.checkType(true, "Boolean")
        assertEquals(true, result)
    }

    @Test
    public fun `checkType returns value for Char type`() {
        val result = HsInternal.checkType('A', "Char")
        assertEquals('A', result)
    }

    @Test
    public fun `checkType returns value for Byte type`() {
        val result = HsInternal.checkType(1.toByte(), "Byte")
        assertEquals(1.toByte(), result)
    }

    @Test
    public fun `checkType returns value for Short type`() {
        val result = HsInternal.checkType(1.toShort(), "Short")
        assertEquals(1.toShort(), result)
    }

    @Test
    public fun `checkType returns value for unknown type name`() {
        val result = HsInternal.checkType("hello", "CustomType")
        assertEquals("hello", result)
    }

    @Test
    public fun `checkType throws HsTypeException for mismatched type`() {
        assertFailsWith<HsTypeException> {
            HsInternal.checkType("hello", "Int")
        }
    }

    @Test
    public fun `checkType throws HsTypeException for String when Int expected`() {
        assertFailsWith<HsTypeException> {
            HsInternal.checkType("not an int", "int")
        }
    }

    @Test
    public fun `typeMismatch throws HsTypeException`() {
        assertFailsWith<HsTypeException> {
            HsInternal.typeMismatch("String", "Int")
        }
    }

    @Test
    public fun `typeMismatch exception message contains expected and actual`() {
        val ex = assertFailsWith<HsTypeException> {
            HsInternal.typeMismatch("Boolean", "String")
        }
        assertTrue(ex.message!!.contains("Boolean"))
        assertTrue(ex.message!!.contains("String"))
    }

    @Test
    public fun `indexOutOfBounds throws HsIndexOutOfBoundsException`() {
        assertFailsWith<HsIndexOutOfBoundsException> {
            HsInternal.indexOutOfBounds(5, 3)
        }
    }

    @Test
    public fun `indexOutOfBounds message contains index and size`() {
        val ex = assertFailsWith<HsIndexOutOfBoundsException> {
            HsInternal.indexOutOfBounds(10, 5)
        }
        assertTrue(ex.message!!.contains("10"))
        assertTrue(ex.message!!.contains("5"))
    }

    @Test
    public fun `nullReference throws HsNullPointerException`() {
        assertFailsWith<HsNullPointerException> {
            HsInternal.nullReference()
        }
    }

    @Test
    public fun `nullReference exception has default message`() {
        val ex = assertFailsWith<HsNullPointerException> {
            HsInternal.nullReference()
        }
        assertTrue(ex.message!!.isNotEmpty())
    }

    @Test
    public fun `divisionByZero throws HsDivisionByZeroError`() {
        assertFailsWith<HsDivisionByZeroError> {
            HsInternal.divisionByZero()
        }
    }

    @Test
    public fun `notImplemented throws HsNotImplementedError`() {
        assertFailsWith<HsNotImplementedError> {
            HsInternal.notImplemented("coroutines")
        }
    }

    @Test
    public fun `notImplemented message contains feature name`() {
        val ex = assertFailsWith<HsNotImplementedError> {
            HsInternal.notImplemented("pattern matching")
        }
        assertTrue(ex.message!!.contains("pattern matching"))
    }

    @Test
    public fun `valueError throws HsValueError`() {
        assertFailsWith<HsValueError> {
            HsInternal.valueError("invalid input")
        }
    }

    @Test
    public fun `valueError message matches input`() {
        val ex = assertFailsWith<HsValueError> {
            HsInternal.valueError("out of range")
        }
        assertEquals("out of range", ex.message)
    }

    @Test
    public fun `runtimeError throws HsRuntimeException`() {
        assertFailsWith<HsRuntimeException> {
            HsInternal.runtimeError("something went wrong")
        }
    }

    @Test
    public fun `runtimeError message matches input`() {
        val ex = assertFailsWith<HsRuntimeException> {
            HsInternal.runtimeError("unexpected state")
        }
        assertEquals("unexpected state", ex.message)
    }

    @Test
    public fun `ioError throws HsIOError`() {
        assertFailsWith<HsIOError> {
            HsInternal.ioError("file not found")
        }
    }

    @Test
    public fun `ioError with cause wraps throwable`() {
        val cause = RuntimeException("root cause")
        val ex = assertFailsWith<HsIOError> {
            HsInternal.ioError("read failed", cause)
        }
        assertEquals("read failed", ex.message)
        assertTrue(ex.cause is RuntimeException)
    }

    @Test
    public fun `ioError without cause has default`() {
        val ex = assertFailsWith<HsIOError> {
            HsInternal.ioError("write failed")
        }
        assertEquals("write failed", ex.message)
    }

    @Test
    public fun `formatStacktrace returns non-empty string`() {
        val ex = RuntimeException("test error")
        val trace = HsInternal.formatStacktrace(ex)
        assertTrue(trace.isNotEmpty())
        assertTrue(trace.contains("test error"))
        assertTrue(trace.contains("RuntimeException"))
    }

    @Test
    public fun `safeToString returns string representation`() {
        assertEquals("hello", HsInternal.safeToString("hello"))
        assertEquals("42", HsInternal.safeToString(42))
    }

    @Test
    public fun `safeToString returns null placeholder for null`() {
        assertEquals("<null>", HsInternal.safeToString(null))
    }

    @Test
    public fun `stackOverflow throws HsStackOverflowError`() {
        assertFailsWith<HsStackOverflowError> {
            HsInternal.stackOverflow()
        }
    }

    @Test
    public fun `all exception types extend HsException`() {
        assertFailsWith<HsException> { HsInternal.nullReference() }
        assertFailsWith<HsException> { HsInternal.divisionByZero() }
        assertFailsWith<HsException> { HsInternal.notImplemented("x") }
        assertFailsWith<HsException> { HsInternal.valueError("x") }
        assertFailsWith<HsException> { HsInternal.runtimeError("x") }
        assertFailsWith<HsException> { HsInternal.ioError("x") }
        assertFailsWith<HsException> { HsInternal.stackOverflow() }
    }
}
