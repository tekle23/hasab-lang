package hasab.runtime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

public class HsObjectTest {

    @Test
    public fun `typeName returns Object by default`() {
        val obj = HsObject()
        assertEquals("Object", obj.typeName())
    }

    @Test
    public fun `identityHashCode is consistent for same instance`() {
        val obj = HsObject()
        val h1 = obj.identityHashCode()
        val h2 = obj.identityHashCode()
        assertEquals(h1, h2)
    }

    @Test
    public fun `identityHashCode differs for different instances`() {
        val a = HsObject()
        val b = HsObject()
        assertNotEquals(a.identityHashCode(), b.identityHashCode())
    }

    @Test
    public fun `identityHashCode matches System identityHashCode`() {
        val obj = HsObject()
        assertEquals(System.identityHashCode(obj), obj.identityHashCode())
    }

    @Test
    public fun `companion identityHashCode returns zero for null`() {
        assertEquals(0, HsObject.identityHashCode(null))
    }

    @Test
    public fun `companion identityHashCode matches instance identityHashCode`() {
        val obj = HsObject()
        assertEquals(obj.identityHashCode(), HsObject.identityHashCode(obj))
    }

    @Test
    public fun `equals uses identity comparison`() {
        val obj = HsObject()
        assertTrue(obj.equals(obj))
    }

    @Test
    public fun `equals returns false for different instances`() {
        val a = HsObject()
        val b = HsObject()
        assertEquals(false, a.equals(b))
    }

    @Test
    public fun `equals returns false for null`() {
        val obj = HsObject()
        assertEquals(false, obj.equals(null))
    }

    @Test
    public fun `equals returns false for different type`() {
        val obj = HsObject()
        assertEquals(false, obj.equals("not an HsObject"))
    }

    @Test
    public fun `hashCode uses identity hash`() {
        val obj = HsObject()
        assertEquals(System.identityHashCode(obj), obj.hashCode())
    }

    @Test
    public fun `toString contains typeName and hex hash`() {
        val obj = HsObject()
        val str = obj.toString()
        assertTrue(str.startsWith("Object@"))
        val hexPart = str.removePrefix("Object@")
        assertNotNull(hexPart.toLongOrNull(16))
    }

    @Test
    public fun `subclass can override typeName`() {
        class CustomObject : HsObject() {
            override fun typeName(): String = "Custom"
        }
        val obj = CustomObject()
        assertEquals("Custom", obj.typeName())
        assertTrue(obj.toString().startsWith("Custom@"))
    }

    @Test
    public fun `same reference is equal to itself`() {
        val obj = HsObject()
        assertSame(obj, obj)
    }
}
