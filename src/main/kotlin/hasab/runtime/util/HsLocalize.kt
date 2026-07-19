package hasab.runtime.util

import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

/**
 * Localization utility for translating keys into locale-specific strings.
 *
 * Wraps [java.util.ResourceBundle] and provides key-based lookup
 * with optional `MessageFormat` argument substitution.
 */
public class HsLocalize {

    /**
     * The active locale code (e.g. `"en"`, `"fr"`).
     */
    public val locale: String
        get() = _locale

    /**
     * The fallback locale used when a resource bundle for [locale] is not found.
     */
    public var defaultLocale: String = "en"

    private var _locale: String

    private val bundles: MutableMap<String, ResourceBundle> = mutableMapOf()

    /**
     * Creates a localizer for the given [locale].
     */
    public constructor(locale: String = "en") {
        _locale = locale
    }

    /**
     * Translates [key] using the current locale.
     *
     * Returns the raw key if no translation is found.
     */
    public fun translate(key: String): String {
        val bundle = findBundle(key) ?: return key
        return try {
            bundle.getString(key.substringAfterLast('.'))
        } catch (_: MissingResourceException) {
            key
        }
    }

    /**
     * Translates [key] with [args] substituted via [MessageFormat].
     *
     * Returns the raw key if no translation is found.
     */
    public fun translate(key: String, vararg args: Any?): String {
        val raw = translate(key)
        return if (args.isEmpty()) raw else MessageFormat.format(raw, *args)
    }

    /**
     * Returns `true` if a translation for [key] exists in the current locale.
     */
    public fun hasKey(key: String): Boolean {
        val bundle = findBundle(key) ?: return false
        return try {
            bundle.getString(key.substringAfterLast('.'))
            true
        } catch (_: MissingResourceException) {
            false
        }
    }

    /**
     * Returns locale codes for which at least one bundle is loaded.
     */
    public fun getAvailableLocales(): List<String> {
        return bundles.keys.map { it.substringAfterLast('_').ifEmpty { "default" } }.distinct()
    }

    /**
     * Changes the active [locale] and invalidates cached bundles so
     * they are reloaded on the next lookup.
     */
    public fun setLocale(locale: String): Unit {
        _locale = locale
        bundles.clear()
    }

    /**
     * Explicitly loads a resource bundle from [baseName] for the given [locale].
     */
    public fun loadBundle(baseName: String, locale: String): Unit {
        val key = "${baseName}_$locale"
        try {
            val loc = if (locale.isEmpty()) Locale.ROOT else Locale.forLanguageTag(locale.replace('_', '-'))
            bundles[key] = ResourceBundle.getBundle(baseName, loc)
        } catch (_: MissingResourceException) {
            // Bundle not available — skip silently.
        }
    }

    /**
     * Returns all top-level keys from the bundle identified by [baseName]
     * for the current locale.
     */
    public fun getBundleKeys(baseName: String): Set<String> {
        val key = "${baseName}_$_locale"
        val bundle = bundles[key] ?: try {
            val loc = if (_locale.isEmpty()) Locale.ROOT else Locale.forLanguageTag(_locale.replace('_', '-'))
            ResourceBundle.getBundle(baseName, loc).also { bundles[key] = it }
        } catch (_: MissingResourceException) {
            return emptySet()
        }
        return bundle.keys.asSequence().toSet()
    }

    private fun findBundle(key: String): ResourceBundle? {
        val baseName = key.substringBeforeLast('.')
        if (baseName.isEmpty()) return null
        val cacheKey = "${baseName}_$_locale"
        bundles[cacheKey]?.let { return it }
        return try {
            val loc = if (_locale.isEmpty()) Locale.ROOT else Locale.forLanguageTag(_locale.replace('_', '-'))
            ResourceBundle.getBundle(baseName, loc).also { bundles[cacheKey] = it }
        } catch (_: MissingResourceException) {
            try {
                val fallbackLoc = Locale.forLanguageTag(defaultLocale.replace('_', '-'))
                ResourceBundle.getBundle(baseName, fallbackLoc).also { bundles[cacheKey] = it }
            } catch (_: MissingResourceException) {
                null
            }
        }
    }

    public companion object {

        private val defaultInstance: HsLocalize = HsLocalize()

        /**
         * Returns the global [HsLocalize] singleton.
         */
        public fun instance(): HsLocalize = defaultInstance

        /**
         * Shortcut for [instance]`.translate(key)`.
         */
        public fun translate(key: String): String = defaultInstance.translate(key)

        /**
         * Shortcut for [instance]`.translate(key, args)`.
         */
        public fun translate(key: String, vararg args: Any?): String = defaultInstance.translate(key, *args)
    }
}
