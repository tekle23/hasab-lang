package hasab.runtime.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsConfigTest {

    @Test
    public fun `empty config has size zero`() {
        val config = HsConfig()
        assertEquals(0, config.size())
        assertTrue(config.isEmpty())
    }

    @Test
    public fun `constructor with defaults populates config`() {
        val defaults = mapOf("a" to 1, "b" to "hello")
        val config = HsConfig(defaults)
        assertEquals(2, config.size())
        assertEquals(1, config.getInt("a"))
        assertEquals("hello", config.getString("b"))
    }

    @Test
    public fun `getString returns stored string value`() {
        val config = HsConfig()
        config.set("key", "value")
        assertEquals("value", config.getString("key"))
    }

    @Test
    public fun `getString returns default when key missing`() {
        val config = HsConfig()
        assertEquals("fallback", config.getString("missing", "fallback"))
    }

    @Test
    public fun `getString returns empty string as default`() {
        val config = HsConfig()
        assertEquals("", config.getString("missing"))
    }

    @Test
    public fun `getString converts non-string to string`() {
        val config = HsConfig()
        config.set("key", 42)
        assertEquals("42", config.getString("key"))
    }

    @Test
    public fun `getInt returns stored int value`() {
        val config = HsConfig()
        config.set("key", 42)
        assertEquals(42, config.getInt("key"))
    }

    @Test
    public fun `getInt returns default when key missing`() {
        val config = HsConfig()
        assertEquals(99, config.getInt("missing", 99))
    }

    @Test
    public fun `getInt returns zero as default`() {
        val config = HsConfig()
        assertEquals(0, config.getInt("missing"))
    }

    @Test
    public fun `getInt coerces number types`() {
        val config = HsConfig()
        config.set("key", 3.14)
        assertEquals(3, config.getInt("key"))
    }

    @Test
    public fun `getInt coerces valid string`() {
        val config = HsConfig()
        config.set("key", "123")
        assertEquals(123, config.getInt("key"))
    }

    @Test
    public fun `getInt returns default for invalid string`() {
        val config = HsConfig()
        config.set("key", "not_a_number")
        assertEquals(-1, config.getInt("key", -1))
    }

    @Test
    public fun `getLong returns stored long value`() {
        val config = HsConfig()
        config.set("key", 123456789L)
        assertEquals(123456789L, config.getLong("key"))
    }

    @Test
    public fun `getLong coerces int to long`() {
        val config = HsConfig()
        config.set("key", 42)
        assertEquals(42L, config.getLong("key"))
    }

    @Test
    public fun `getLong returns default when key missing`() {
        val config = HsConfig()
        assertEquals(100L, config.getLong("missing", 100L))
    }

    @Test
    public fun `getFloat returns stored float value`() {
        val config = HsConfig()
        config.set("key", 3.14f)
        assertEquals(3.14f, config.getFloat("key"))
    }

    @Test
    public fun `getFloat returns default when key missing`() {
        val config = HsConfig()
        assertEquals(1.5f, config.getFloat("missing", 1.5f))
    }

    @Test
    public fun `getFloat coerces number type`() {
        val config = HsConfig()
        config.set("key", 42)
        assertEquals(42.0f, config.getFloat("key"))
    }

    @Test
    public fun `getFloat coerces valid string`() {
        val config = HsConfig()
        config.set("key", "2.71")
        assertEquals(2.71f, config.getFloat("key"))
    }

    @Test
    public fun `getFloat returns default for invalid string`() {
        val config = HsConfig()
        config.set("key", "abc")
        assertEquals(0.0f, config.getFloat("key", 0.0f))
    }

    @Test
    public fun `getDouble returns stored double value`() {
        val config = HsConfig()
        config.set("key", 3.14159)
        assertEquals(3.14159, config.getDouble("key"), 0.00001)
    }

    @Test
    public fun `getDouble returns default when key missing`() {
        val config = HsConfig()
        assertEquals(2.0, config.getDouble("missing", 2.0), 0.0)
    }

    @Test
    public fun `getDouble coerces number type`() {
        val config = HsConfig()
        config.set("key", 42)
        assertEquals(42.0, config.getDouble("key"), 0.0)
    }

    @Test
    public fun `getDouble coerces valid string`() {
        val config = HsConfig()
        config.set("key", "1.23")
        assertEquals(1.23, config.getDouble("key"), 0.001)
    }

    @Test
    public fun `getDouble returns default for invalid string`() {
        val config = HsConfig()
        config.set("key", "not_a_double")
        assertEquals(0.0, config.getDouble("key", 0.0), 0.0)
    }

    @Test
    public fun `getBoolean returns stored boolean value`() {
        val config = HsConfig()
        config.set("key", true)
        assertTrue(config.getBoolean("key"))
    }

    @Test
    public fun `getBoolean returns default when key missing`() {
        val config = HsConfig()
        assertTrue(config.getBoolean("missing", true))
    }

    @Test
    public fun `getBoolean returns false as default`() {
        val config = HsConfig()
        assertFalse(config.getBoolean("missing"))
    }

    @Test
    public fun `getBoolean coerces string true`() {
        val config = HsConfig()
        config.set("key", "true")
        assertTrue(config.getBoolean("key"))
    }

    @Test
    public fun `getBoolean coerces string false`() {
        val config = HsConfig()
        config.set("key", "false")
        assertFalse(config.getBoolean("key"))
    }

    @Test
    public fun `getBoolean coerces number to boolean`() {
        val config = HsConfig()
        config.set("key", 1)
        assertTrue(config.getBoolean("key"))
        config.set("key2", 0)
        assertFalse(config.getBoolean("key2"))
    }

    @Test
    public fun `get returns raw value`() {
        val config = HsConfig()
        config.set("key", "value")
        assertEquals("value", config.get("key"))
    }

    @Test
    public fun `get returns null for missing key`() {
        val config = HsConfig()
        assertNull(config.get("missing"))
    }

    @Test
    public fun `getOrNull returns raw value`() {
        val config = HsConfig()
        config.set("key", 42)
        assertEquals(42, config.getOrNull("key"))
    }

    @Test
    public fun `getOrNull returns null for missing key`() {
        val config = HsConfig()
        assertNull(config.getOrNull("missing"))
    }

    @Test
    public fun `set and has work correctly`() {
        val config = HsConfig()
        assertFalse(config.has("key"))
        config.set("key", "value")
        assertTrue(config.has("key"))
    }

    @Test
    public fun `set overwrites existing value`() {
        val config = HsConfig()
        config.set("key", "first")
        config.set("key", "second")
        assertEquals("second", config.getString("key"))
    }

    @Test
    public fun `set null value stores null`() {
        val config = HsConfig()
        config.set("key", null)
        assertTrue(config.has("key"))
        assertNull(config.get("key"))
    }

    @Test
    public fun `remove deletes key`() {
        val config = HsConfig()
        config.set("key", "value")
        assertTrue(config.has("key"))
        config.remove("key")
        assertFalse(config.has("key"))
        assertEquals(0, config.size())
    }

    @Test
    public fun `remove non-existent key does not throw`() {
        val config = HsConfig()
        config.remove("nonexistent")
        assertEquals(0, config.size())
    }

    @Test
    public fun `clear removes all entries`() {
        val config = HsConfig()
        config.set("a", 1)
        config.set("b", 2)
        assertEquals(2, config.size())
        config.clear()
        assertEquals(0, config.size())
        assertTrue(config.isEmpty())
    }

    @Test
    public fun `size reflects number of entries`() {
        val config = HsConfig()
        assertEquals(0, config.size())
        config.set("a", 1)
        assertEquals(1, config.size())
        config.set("b", 2)
        assertEquals(2, config.size())
    }

    @Test
    public fun `isEmpty and isNotEmpty`() {
        val config = HsConfig()
        assertTrue(config.isEmpty())
        assertFalse(config.isNotEmpty())
        config.set("key", "value")
        assertFalse(config.isEmpty())
        assertTrue(config.isNotEmpty())
    }

    @Test
    public fun `keys returns all key names`() {
        val config = HsConfig()
        config.set("x", 1)
        config.set("y", 2)
        val keys = config.keys()
        assertEquals(2, keys.size)
        assertTrue(keys.contains("x"))
        assertTrue(keys.contains("y"))
    }

    @Test
    public fun `toMap returns immutable copy`() {
        val config = HsConfig()
        config.set("a", 1)
        val map = config.toMap()
        assertEquals(1, map["a"])
        config.set("b", 2)
        assertEquals(1, map.size)
    }

    @Test
    public fun `merge combines two configs`() {
        val config1 = HsConfig()
        config1.set("a", 1)
        val config2 = HsConfig()
        config2.set("b", 2)
        config1.merge(config2)
        assertEquals(1, config1.getInt("a"))
        assertEquals(2, config1.getInt("b"))
        assertEquals(2, config1.size())
    }

    @Test
    public fun `merge overwrites existing keys`() {
        val config1 = HsConfig()
        config1.set("key", "old")
        val config2 = HsConfig()
        config2.set("key", "new")
        config1.merge(config2)
        assertEquals("new", config1.getString("key"))
    }

    @Test
    public fun `clone creates independent copy`() {
        val config = HsConfig()
        config.set("key", "value")
        val cloned = config.clone()
        assertEquals("value", cloned.getString("key"))
        config.set("key", "changed")
        assertEquals("value", cloned.getString("key"))
    }

    @Test
    public fun `loadFromProperties parses typed values`() {
        val props = mapOf(
            "str" to "hello",
            "int" to "42",
            "long" to "1234567890123",
            "double" to "3.14",
            "boolTrue" to "true",
            "boolFalse" to "FALSE",
            "unknown" to "abc"
        )
        val config = HsConfig.loadFromProperties(props)
        assertEquals("hello", config.getString("str"))
        assertEquals(42, config.getInt("int"))
        assertEquals(1234567890123L, config.getLong("long"))
        assertEquals(3.14, config.getDouble("double"), 0.001)
        assertTrue(config.getBoolean("boolTrue"))
        assertFalse(config.getBoolean("boolFalse"))
        assertEquals("abc", config.getString("unknown"))
    }

    @Test
    public fun `empty factory returns empty config`() {
        val config = HsConfig.empty()
        assertTrue(config.isEmpty())
        assertEquals(0, config.size())
    }

    @Test
    public fun `toString contains config data`() {
        val config = HsConfig()
        config.set("key", "value")
        val str = config.toString()
        assertTrue(str.contains("HsConfig"))
        assertTrue(str.contains("key"))
    }

    @Test
    public fun `values returns snapshot of all values`() {
        val config = HsConfig()
        config.set("a", 1)
        config.set("b", "hello")
        val values = config.values()
        assertEquals(2, values.size)
        assertEquals(1, values["a"])
        assertEquals("hello", values["b"])
    }
}
