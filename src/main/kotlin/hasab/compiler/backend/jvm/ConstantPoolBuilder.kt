package hasab.compiler.backend.jvm

public class ConstantPoolBuilder {
    private val strings = mutableSetOf<String>()

    public fun addString(value: String): String {
        strings.add(value)
        return value
    }

    public fun addUtf8(value: String): String {
        strings.add(value)
        return value
    }

    public fun addClass(internalName: String): String {
        strings.add(internalName)
        return internalName
    }

    public fun stringCount(): Int = strings.size

    public fun clear() {
        strings.clear()
    }
}
