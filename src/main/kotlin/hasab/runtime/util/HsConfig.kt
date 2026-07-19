package hasab.runtime.util

import java.io.File
import java.util.Properties

/**
 * Configuration utility wrapping a mutable string-keyed map.
 *
 * Values can be of any type. Convenience getters perform
 * type coercion when the stored value does not exactly match
 * the requested type.
 */
public class HsConfig {

    private val store: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Creates an empty configuration.
     */
    public constructor()

    /**
     * Creates a configuration pre-populated with [defaults].
     */
    public constructor(defaults: Map<String, Any?>) {
        store.putAll(defaults)
    }

    public fun getString(key: String, defaultValue: String = ""): String =
        when (val v = store[key]) {
            null -> defaultValue
            is String -> v
            else -> v.toString()
        }

    public fun getInt(key: String, defaultValue: Int = 0): Int =
        when (val v = store[key]) {
            null -> defaultValue
            is Int -> v
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }

    public fun getLong(key: String, defaultValue: Long = 0L): Long =
        when (val v = store[key]) {
            null -> defaultValue
            is Long -> v
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }

    public fun getFloat(key: String, defaultValue: Float = 0f): Float =
        when (val v = store[key]) {
            null -> defaultValue
            is Float -> v
            is Number -> v.toFloat()
            is String -> v.toFloatOrNull() ?: defaultValue
            else -> defaultValue
        }

    public fun getDouble(key: String, defaultValue: Double = 0.0): Double =
        when (val v = store[key]) {
            null -> defaultValue
            is Double -> v
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: defaultValue
            else -> defaultValue
        }

    public fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        when (val v = store[key]) {
            null -> defaultValue
            is Boolean -> v
            is String -> v.lowercase().toBooleanStrictOrNull() ?: defaultValue
            is Number -> v.toInt() != 0
            else -> defaultValue
        }

    public fun get(key: String): Any? = store[key]

    public fun getOrNull(key: String): Any? = store[key]

    public fun set(key: String, value: Any?): Unit {
        store[key] = value
    }

    public fun has(key: String): Boolean = store.containsKey(key)

    public fun remove(key: String): Unit {
        store.remove(key)
    }

    public fun clear(): Unit {
        store.clear()
    }

    public fun size(): Int = store.size

    public fun isEmpty(): Boolean = store.isEmpty()

    public fun isNotEmpty(): Boolean = store.isNotEmpty()

    public fun keys(): Set<String> = store.keys.toSet()

    public fun values(): Map<String, Any?> = store.toMap()

    public fun toMap(): Map<String, Any?> = store.toMap()

    public fun merge(other: HsConfig): Unit {
        store.putAll(other.store)
    }

    public fun clone(): HsConfig {
        val copy = HsConfig()
        copy.store.putAll(store)
        return copy
    }

    public companion object {

        /**
         * Loads configuration from a properties file at [path].
         *
         * Keys use `.` as a hierarchy separator in the file but are stored flat.
         */
        public fun loadFromFile(path: String): HsConfig {
            val file = File(path)
            if (!file.exists()) return HsConfig()
            val props = Properties()
            file.inputStream().use { props.load(it) }
            val config = HsConfig()
            for (key in props.stringPropertyNames()) {
                config.store[key] = parseValue(props.getProperty(key))
            }
            return config
        }

        /**
         * Loads configuration from a classpath resource at [resourcePath].
         */
        public fun loadFromResource(resourcePath: String): HsConfig {
            val stream = HsConfig::class.java.classLoader.getResourceAsStream(resourcePath)
                ?: return HsConfig()
            val props = Properties()
            stream.use { props.load(it) }
            val config = HsConfig()
            for (key in props.stringPropertyNames()) {
                config.store[key] = parseValue(props.getProperty(key))
            }
            return config
        }

        /**
         * Creates configuration from a [properties] map, parsing string values
         * into their likely types.
         */
        public fun loadFromProperties(properties: Map<String, String>): HsConfig {
            val config = HsConfig()
            for ((key, value) in properties) {
                config.store[key] = parseValue(value)
            }
            return config
        }

        /**
         * Returns an empty configuration.
         */
        public fun empty(): HsConfig = HsConfig()

        private fun parseValue(value: String): Any? {
            val trimmed = value.trim()
            if (trimmed.equals("true", ignoreCase = true)) return true
            if (trimmed.equals("false", ignoreCase = true)) return false
            trimmed.toIntOrNull()?.let { return it }
            trimmed.toLongOrNull()?.let { return it }
            trimmed.toDoubleOrNull()?.let { return it }
            return value
        }
    }

    override fun toString(): String = "HsConfig($store)"
}
