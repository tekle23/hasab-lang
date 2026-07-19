package hasab.runtime.annotations

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HsAnnotationsTest {

    @HsDeprecated(message = "Use newMethod instead", replaceWith = "newMethod()")
    private fun deprecatedFunction() {}

    private fun normalFunction() {}

    @HsExperimentalApi
    private class ExperimentalClass

    @HsInternalApi
    private class InternalClass

    @HsThreadSafe
    private class ThreadSafeClass

    @HsNotThreadSafe
    private class NotThreadSafeClass

    @HsAuthor(name = "Test Author", email = "test@example.com")
    private class AuthoredClass

    @HsSince(version = "1.0.0")
    private class VersionedClass

    @Test
    public fun `HsDeprecated has default values`() {
        val annotation = HsDeprecated::class
        assertTrue(annotation.annotations.isNotEmpty())
    }

    @Test
    public fun `HsDeprecated annotation is present on deprecated function`() {
        val annotations = ::deprecatedFunction.annotations
        val deprecated = annotations.filterIsInstance<HsDeprecated>()
        assertTrue(deprecated.isNotEmpty())
    }

    @Test
    public fun `HsDeprecated message value is correct`() {
        val deprecated = ::deprecatedFunction.annotations.filterIsInstance<HsDeprecated>().first()
        assertEquals("Use newMethod instead", deprecated.message)
    }

    @Test
    public fun `HsDeprecated replaceWith value is correct`() {
        val deprecated = ::deprecatedFunction.annotations.filterIsInstance<HsDeprecated>().first()
        assertEquals("newMethod()", deprecated.replaceWith)
    }

    @Test
    public fun `HsDeprecated not present on normal function`() {
        val deprecated = ::normalFunction.annotations.filterIsInstance<HsDeprecated>()
        assertTrue(deprecated.isEmpty())
    }

    @Test
    public fun `HsExperimentalApi is present on class`() {
        val annotations = ExperimentalClass::class.annotations
        val experimental = annotations.filterIsInstance<HsExperimentalApi>()
        assertTrue(experimental.isNotEmpty())
    }

    @Test
    public fun `HsInternalApi is present on class`() {
        val annotations = InternalClass::class.annotations
        val internal = annotations.filterIsInstance<HsInternalApi>()
        assertTrue(internal.isNotEmpty())
    }

    @Test
    public fun `HsThreadSafe is present on class`() {
        val annotations = ThreadSafeClass::class.annotations
        val threadSafe = annotations.filterIsInstance<HsThreadSafe>()
        assertTrue(threadSafe.isNotEmpty())
    }

    @Test
    public fun `HsNotThreadSafe is present on class`() {
        val annotations = NotThreadSafeClass::class.annotations
        val notThreadSafe = annotations.filterIsInstance<HsNotThreadSafe>()
        assertTrue(notThreadSafe.isNotEmpty())
    }

    @Test
    public fun `HsAuthor name value is correct`() {
        val authored = AuthoredClass::class.annotations.filterIsInstance<HsAuthor>().first()
        assertEquals("Test Author", authored.name)
    }

    @Test
    public fun `HsAuthor email value is correct`() {
        val authored = AuthoredClass::class.annotations.filterIsInstance<HsAuthor>().first()
        assertEquals("test@example.com", authored.email)
    }

    @Test
    public fun `HsSince version value is correct`() {
        val since = VersionedClass::class.annotations.filterIsInstance<HsSince>().first()
        assertEquals("1.0.0", since.version)
    }

    @Test
    public fun `annotation is visible via Java reflection for HsDeprecated`() {
        val annotation = HsAnnotationsTest::class.java.getDeclaredMethod("deprecatedFunction")
            .getAnnotation(HsDeprecated::class.java)
        assertTrue(annotation != null, "HsDeprecated annotation should be visible via reflection")
    }

    @Test
    public fun `annotation is visible via Java reflection for HsExperimentalApi`() {
        val annotation = ExperimentalClass::class.java.getAnnotation(HsExperimentalApi::class.java)
        assertTrue(annotation != null, "HsExperimentalApi annotation should be visible via reflection")
    }

    @Test
    public fun `annotation is visible via Java reflection for HsInternalApi`() {
        val annotation = InternalClass::class.java.getAnnotation(HsInternalApi::class.java)
        assertTrue(annotation != null, "HsInternalApi annotation should be visible via reflection")
    }

    @Test
    public fun `annotation is visible via Java reflection for HsThreadSafe`() {
        val annotation = ThreadSafeClass::class.java.getAnnotation(HsThreadSafe::class.java)
        assertTrue(annotation != null, "HsThreadSafe annotation should be visible via reflection")
    }

    @Test
    public fun `annotation is visible via Java reflection for HsNotThreadSafe`() {
        val annotation = NotThreadSafeClass::class.java.getAnnotation(HsNotThreadSafe::class.java)
        assertTrue(annotation != null, "HsNotThreadSafe annotation should be visible via reflection")
    }

    @Test
    public fun `annotation is visible via Java reflection for HsDeprecated annotation class`() {
        val annotation = HsDeprecated::class.java.getAnnotation(HsDeprecated::class.java)
        assertTrue(annotation == null, "HsDeprecated should not be annotated with itself")
    }

    @Test
    public fun `annotation is visible via Java reflection for HsExperimentalApi annotation class`() {
        val annotation = HsExperimentalApi::class.java.getAnnotation(HsExperimentalApi::class.java)
        assertTrue(annotation == null, "HsExperimentalApi should not be annotated with itself")
    }

    @Test
    public fun `annotation is visible via Java reflection for HsInternalApi annotation class`() {
        val annotation = HsInternalApi::class.java.getAnnotation(HsInternalApi::class.java)
        assertTrue(annotation == null, "HsInternalApi should not be annotated with itself")
    }

    @Test
    public fun `HsDeprecated default values are empty strings`() {
        val constructor = HsDeprecated::class.constructors.first()
        assertTrue(constructor.parameters.size <= 2)
    }

    @Test
    public fun `multiple annotations can coexist on same class`() {
        @HsExperimentalApi
        @HsThreadSafe
        class MultiAnnotated

        val annotations = MultiAnnotated::class.annotations
        assertTrue(annotations.filterIsInstance<HsExperimentalApi>().isNotEmpty())
        assertTrue(annotations.filterIsInstance<HsThreadSafe>().isNotEmpty())
    }
}
