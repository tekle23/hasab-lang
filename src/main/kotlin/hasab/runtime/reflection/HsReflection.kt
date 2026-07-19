package hasab.runtime.reflection

import java.lang.reflect.Modifier

/**
 * Reflection utilities for the HASAB runtime.
 *
 * Provides safe, convenient access to JVM reflection APIs
 * for dynamic type inspection, field access, method invocation,
 * and annotation queries.
 */
public object HsReflection {

    /**
     * Returns the fully qualified class name of [obj], or `"null"` if null.
     */
    public fun className(obj: Any?): String =
        obj?.let { it::class.java.name } ?: "null"

    /**
     * Returns the simple class name of [obj], or `"null"` if null.
     */
    public fun simpleClassName(obj: Any?): String =
        obj?.let { it::class.java.simpleName } ?: "null"

    /**
     * Returns the JVM [Class] of [obj], or `null` if null.
     */
    public fun classOf(obj: Any?): Class<*>? = obj?.let { it::class.java }

    /**
     * Returns `true` if [obj] is an instance of [klass].
     */
    public fun isInstance(obj: Any?, klass: Class<*>): Boolean = klass.isInstance(obj)

    /**
     * Returns `true` if [from] can be assigned to [to] (i.e. [to] is a supertype of [from]).
     */
    public fun isAssignable(from: Class<*>, to: Class<*>): Boolean = to.isAssignableFrom(from)

    /**
     * Gets the value of field named [fieldName] from [obj].
     */
    public fun getField(obj: Any?, fieldName: String): Any? {
        if (obj == null) return null
        val field = obj::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(obj)
    }

    /**
     * Sets field named [fieldName] on [obj] to [value].
     */
    public fun setField(obj: Any?, fieldName: String, value: Any?) {
        if (obj == null) return
        val field = obj::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }

    /**
     * Invokes instance method [methodName] on [obj] with the given [args].
     */
    @Suppress("UNCHECKED_CAST")
    public fun invokeMethod(obj: Any?, methodName: String, vararg args: Any?): Any? {
        if (obj == null) return null
        val paramTypes = args.map { arg ->
            when (arg) {
                is Int -> Int::class.javaPrimitiveType!!
                is Long -> Long::class.javaPrimitiveType!!
                is Float -> Float::class.javaPrimitiveType!!
                is Double -> Double::class.javaPrimitiveType!!
                is Boolean -> Boolean::class.javaPrimitiveType!!
                is Byte -> Byte::class.javaPrimitiveType!!
                is Short -> Short::class.javaPrimitiveType!!
                is Char -> Char::class.javaPrimitiveType!!
                else -> arg?.javaClass ?: Any::class.java
            }
        }.toTypedArray()
        val method = obj::class.java.getDeclaredMethod(methodName, *paramTypes)
        method.isAccessible = true
        return method.invoke(obj, *args)
    }

    /**
     * Invokes a static method [methodName] on [klass] with the given [args].
     */
    @Suppress("UNCHECKED_CAST")
    public fun invokeStaticMethod(klass: Class<*>, methodName: String, vararg args: Any?): Any? {
        val method = klass.getDeclaredMethod(methodName, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
        method.isAccessible = true
        return method.invoke(null, *args)
    }

    /**
     * Gets the value of a static field [fieldName] from [klass].
     */
    public fun getStaticField(klass: Class<*>, fieldName: String): Any? {
        val field = klass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(null)
    }

    /**
     * Sets the value of a static field [fieldName] on [klass] to [value].
     */
    public fun setStaticField(klass: Class<*>, fieldName: String, value: Any?) {
        val field = klass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(null, value)
    }

    /**
     * Creates a new instance of [klass] using the constructor matching the given [args].
     */
    @Suppress("UNCHECKED_CAST")
    public fun newInstance(klass: Class<*>, vararg args: Any?): Any? {
        val paramTypes = args.map { it?.javaClass ?: Any::class.java }.toTypedArray()
        val ctor = klass.getDeclaredConstructor(*paramTypes)
        ctor.isAccessible = true
        return ctor.newInstance(*args)
    }

    /**
     * Returns the annotation of type [annotationClass] on [obj], or `null`.
     */
    public fun getAnnotation(obj: Any?, annotationClass: Class<*>): Any? {
        if (obj == null) return null
        return obj::class.java.getAnnotation(annotationClass as Class<Annotation>)
    }

    /**
     * Returns `true` if [obj] has the annotation [annotationClass].
     */
    public fun hasAnnotation(obj: Any?, annotationClass: Class<*>): Boolean {
        if (obj == null) return false
        return obj::class.java.isAnnotationPresent(annotationClass as Class<Annotation>)
    }

    /**
     * Returns a map of annotation class names to annotation instances on [obj].
     */
    public fun getAnnotations(obj: Any?): Map<String, Any?> {
        if (obj == null) return emptyMap()
        return obj::class.java.annotations.associate { it.annotationClass.java.name to it }
    }

    /**
     * Returns `true` if [klass] has the annotation [annotationClass].
     */
    public fun isAnnotationPresent(klass: Class<*>, annotationClass: Class<*>): Boolean =
        klass.isAnnotationPresent(annotationClass as Class<Annotation>)

    /**
     * Returns the names of all declared methods on [obj].
     */
    public fun methods(obj: Any?): List<String> {
        if (obj == null) return emptyList()
        return obj::class.java.methods.map { it.name }.distinct()
    }

    /**
     * Returns a map of field names to values for all declared fields on [obj].
     */
    public fun fields(obj: Any?): Map<String, Any?> {
        if (obj == null) return emptyMap()
        return obj::class.java.declaredFields.associate { field ->
            field.isAccessible = true
            field.name to field.get(obj)
        }
    }

    /**
     * Returns string representations of all declared constructors on [klass].
     */
    public fun constructors(klass: Class<*>): List<String> =
        klass.declaredConstructors.map { it.toString() }

    /**
     * Returns all interfaces implemented by [obj].
     */
    public fun interfaces(obj: Any?): List<Class<*>> {
        if (obj == null) return emptyList()
        return obj::class.java.interfaces.toList()
    }

    /**
     * Returns the superclass of [obj], or `null` if none.
     */
    public fun superclass(obj: Any?): Class<*>? = obj?.let { it::class.java.superclass }

    /**
     * Returns `true` if [obj]'s class is a primitive.
     */
    public fun isPrimitive(obj: Any?): Boolean = obj?.let { it::class.java.isPrimitive } ?: false

    /**
     * Returns `true` if [obj]'s class is an array.
     */
    public fun isArray(obj: Any?): Boolean = obj?.let { it::class.java.isArray } ?: false

    /**
     * Returns `true` if [obj]'s class is an enum.
     */
    public fun isEnum(obj: Any?): Boolean = obj?.let { it::class.java.isEnum } ?: false

    /**
     * Returns `true` if [obj]'s class is an interface.
     */
    public fun isInterface(obj: Any?): Boolean = when (obj) {
        is Class<*> -> obj.isInterface
        else -> obj?.let { it::class.java.isInterface } ?: false
    }

    /**
     * Converts [obj] (which must be an array) to a Kotlin [Array] of [Any?].
     */
    @Suppress("UNCHECKED_CAST")
    public fun asArray(obj: Any?): Array<Any?> {
        if (obj == null) return emptyArray()
        val length = java.lang.reflect.Array.getLength(obj)
        return (0 until length).map { java.lang.reflect.Array.get(obj, it) }.toTypedArray()
    }

    /**
     * Returns all values of the enum [klass].
     */
    @Suppress("UNCHECKED_CAST")
    public fun getEnumValues(klass: Class<*>): Array<Any?> {
        if (!klass.isEnum) return emptyArray()
        @Suppress("UNCHECKED_CAST")
        val constants = klass.enumConstants as? Array<Any?> ?: emptyArray()
        return constants
    }

    /**
     * Returns the name of an enum [value].
     */
    public fun getEnumName(value: Any): String {
        if (value is Enum<*>) return value.name
        return value.toString()
    }
}
