package hasab.runtime.reflection

import hasab.runtime.annotations.HsDeprecated
import hasab.runtime.annotations.HsExperimentalApi
import hasab.runtime.annotations.HsAuthor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsReflectionTest {

    private class SampleClass {
        public var value: Int = 0
        private var secret: String = "hidden"

        public fun greet(name: String): String = "Hello, $name"

        public fun add(a: Int, b: Int): Int = a + b
    }

    @HsExperimentalApi
    private class AnnotatedClass

    @Deprecated("old", replaceWith = ReplaceWith("new"))
    private class OldClass

    private enum class Color { RED, GREEN, BLUE }

    @Test
    public fun `className returns full class name`() {
        assertEquals("java.lang.String", HsReflection.className("hello"))
    }

    @Test
    public fun `className returns null for null`() {
        assertEquals("null", HsReflection.className(null))
    }

    @Test
    public fun `simpleClassName returns simple name`() {
        assertEquals("String", HsReflection.simpleClassName("hello"))
    }

    @Test
    public fun `simpleClassName returns null for null`() {
        assertEquals("null", HsReflection.simpleClassName(null))
    }

    @Test
    public fun `classOf returns class`() {
        val clazz = HsReflection.classOf("hello")
        assertNotNull(clazz)
        assertEquals(String::class.java, clazz)
    }

    @Test
    public fun `classOf returns null for null`() {
        assertNull(HsReflection.classOf(null))
    }

    @Test
    public fun `isInstance returns true for matching type`() {
        assertTrue(HsReflection.isInstance("hello", String::class.java))
    }

    @Test
    public fun `isInstance returns false for non-matching type`() {
        assertFalse(HsReflection.isInstance("hello", Int::class.java))
    }

    @Test
    public fun `isInstance returns false for null`() {
        assertFalse(HsReflection.isInstance(null, String::class.java))
    }

    @Test
    public fun `isAssignable returns true for subtype`() {
        assertTrue(HsReflection.isAssignable(ArrayList::class.java, List::class.java))
    }

    @Test
    public fun `isAssignable returns false for unrelated type`() {
        assertFalse(HsReflection.isAssignable(String::class.java, Int::class.java))
    }

    @Test
    public fun `getField retrieves field value`() {
        val obj = SampleClass()
        obj.value = 42
        assertEquals(42, HsReflection.getField(obj, "value"))
    }

    @Test
    public fun `getField retrieves private field`() {
        val obj = SampleClass()
        assertEquals("hidden", HsReflection.getField(obj, "secret"))
    }

    @Test
    public fun `setField sets field value`() {
        val obj = SampleClass()
        HsReflection.setField(obj, "value", 99)
        assertEquals(99, obj.value)
    }

    @Test
    public fun `setField sets private field`() {
        val obj = SampleClass()
        HsReflection.setField(obj, "secret", "revealed")
        assertEquals("revealed", HsReflection.getField(obj, "secret"))
    }

    @Test
    public fun `getField returns null for null obj`() {
        assertNull(HsReflection.getField(null, "anything"))
    }

    @Test
    public fun `setField does nothing for null obj`() {
        HsReflection.setField(null, "anything", "value")
    }

    @Test
    public fun `invokeMethod calls instance method`() {
        val obj = SampleClass()
        val result = HsReflection.invokeMethod(obj, "greet", "World")
        assertEquals("Hello, World", result)
    }

    @Test
    public fun `invokeMethod calls method with multiple params`() {
        val obj = SampleClass()
        val result = HsReflection.invokeMethod(obj, "add", 3, 4)
        assertEquals(7, result)
    }

    @Test
    public fun `invokeMethod returns null for null obj`() {
        assertNull(HsReflection.invokeMethod(null, "greet"))
    }

    @Test
    public fun `methods returns list of method names`() {
        val obj = SampleClass()
        val methodNames = HsReflection.methods(obj)
        assertTrue(methodNames.contains("greet"))
        assertTrue(methodNames.contains("add"))
    }

    @Test
    public fun `methods returns empty list for null`() {
        assertTrue(HsReflection.methods(null).isEmpty())
    }

    @Test
    public fun `fields returns field name value map`() {
        val obj = SampleClass()
        val fields = HsReflection.fields(obj)
        assertTrue(fields.containsKey("value"))
        assertEquals(0, fields["value"])
    }

    @Test
    public fun `fields returns empty map for null`() {
        assertTrue(HsReflection.fields(null).isEmpty())
    }

    @Test
    public fun `isPrimitive returns false for String`() {
        assertFalse(HsReflection.isPrimitive("hello"))
    }

    @Test
    public fun `isPrimitive returns false for null`() {
        assertFalse(HsReflection.isPrimitive(null))
    }

    @Test
    public fun `isArray returns false for String`() {
        assertFalse(HsReflection.isArray("hello"))
    }

    @Test
    public fun `isArray returns true for array`() {
        assertTrue(HsReflection.isArray(arrayOf(1, 2, 3)))
    }

    @Test
    public fun `isEnum returns true for enum`() {
        assertTrue(HsReflection.isEnum(Color.RED))
    }

    @Test
    public fun `isEnum returns false for String`() {
        assertFalse(HsReflection.isEnum("hello"))
    }

    @Test
    public fun `isEnum returns false for null`() {
        assertFalse(HsReflection.isEnum(null))
    }

    @Test
    public fun `isInterface returns false for String`() {
        assertFalse(HsReflection.isInterface("hello"))
    }

    @Test
    public fun `isInterface returns false for null`() {
        assertFalse(HsReflection.isInterface(null))
    }

    @Test
    public fun `isInterface returns true for interface`() {
        assertTrue(HsReflection.isInterface(List::class.java))
    }

    @Test
    public fun `getEnumValues returns all values`() {
        val values = HsReflection.getEnumValues(Color::class.java)
        assertEquals(3, values.size)
        assertTrue(values.contains(Color.RED))
        assertTrue(values.contains(Color.GREEN))
        assertTrue(values.contains(Color.BLUE))
    }

    @Test
    public fun `getEnumValues returns empty for non-enum`() {
        val values = HsReflection.getEnumValues(String::class.java)
        assertTrue(values.isEmpty())
    }

    @Test
    public fun `getEnumName returns name of enum value`() {
        assertEquals("RED", HsReflection.getEnumName(Color.RED))
    }

    @Test
    public fun `getEnumName returns toString for non-enum`() {
        assertEquals("hello", HsReflection.getEnumName("hello"))
    }

    @Test
    public fun `hasAnnotation returns true for annotated class`() {
        val obj = AnnotatedClass()
        assertTrue(HsReflection.hasAnnotation(obj, HsExperimentalApi::class.java))
    }

    @Test
    public fun `hasAnnotation returns false for unannotated class`() {
        val obj = SampleClass()
        assertFalse(HsReflection.hasAnnotation(obj, HsExperimentalApi::class.java))
    }

    @Test
    public fun `hasAnnotation returns false for null`() {
        assertFalse(HsReflection.hasAnnotation(null, HsExperimentalApi::class.java))
    }

    @Test
    public fun `getAnnotation returns annotation instance`() {
        val obj = OldClass()
        val annotation = HsReflection.getAnnotation(obj, Deprecated::class.java)
        assertNotNull(annotation)
    }

    @Test
    public fun `getAnnotation returns null for null obj`() {
        assertNull(HsReflection.getAnnotation(null, Deprecated::class.java))
    }

    @Test
    public fun `getAnnotations returns map of annotations`() {
        val obj = OldClass()
        val annotations = HsReflection.getAnnotations(obj)
        assertTrue(annotations.isNotEmpty())
    }

    @Test
    public fun `getAnnotations returns empty map for null`() {
        assertTrue(HsReflection.getAnnotations(null).isEmpty())
    }

    @Test
    public fun `isAnnotationPresent returns true for present annotation`() {
        assertTrue(HsReflection.isAnnotationPresent(OldClass::class.java, Deprecated::class.java))
    }

    @Test
    public fun `isAnnotationPresent returns false for absent annotation`() {
        assertFalse(HsReflection.isAnnotationPresent(OldClass::class.java, HsExperimentalApi::class.java))
    }

    @Test
    public fun `superclass returns Object for simple class`() {
        val obj = SampleClass()
        val superclass = HsReflection.superclass(obj)
        assertNotNull(superclass)
        assertEquals(Any::class.java, superclass)
    }

    @Test
    public fun `superclass returns null for null`() {
        assertNull(HsReflection.superclass(null))
    }

    @Test
    public fun `interfaces returns list for list implementation`() {
        val obj = ArrayList<String>()
        val ifaces = HsReflection.interfaces(obj)
        assertTrue(ifaces.isNotEmpty())
    }

    @Test
    public fun `interfaces returns empty for null`() {
        assertTrue(HsReflection.interfaces(null).isEmpty())
    }

    @Test
    public fun `constructors returns list`() {
        val ctors = HsReflection.constructors(SampleClass::class.java)
        assertTrue(ctors.isNotEmpty())
    }

    @Test
    public fun `newInstance creates object`() {
        val obj = HsReflection.newInstance(SampleClass::class.java)
        assertNotNull(obj)
        assertTrue(obj is SampleClass)
    }

    @Test
    public fun `asArray converts array`() {
        val arr = arrayOf(1, 2, 3)
        val result = HsReflection.asArray(arr)
        assertEquals(3, result.size)
    }

    @Test
    public fun `asArray returns empty for null`() {
        assertTrue(HsReflection.asArray(null).isEmpty())
    }
}
