package hasab.compiler.backend.jvm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.util.CheckClassAdapter

public class BytecodeValidator {

    public data class ValidationError(
        val message: String,
        val exception: Exception?,
    )

    public data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError>,
    )

    public fun validate(classBytes: ByteArray): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        return try {
            val cr = ClassReader(classBytes)
            val cw = ClassWriter(0)
            val checkAdapter = CheckClassAdapter(cw, true)
            cr.accept(checkAdapter, 0)
            ValidationResult(true, emptyList())
        } catch (e: Exception) {
            errors.add(ValidationError(e.message ?: "Unknown validation error", e))
            ValidationResult(false, errors)
        }
    }

    public fun validateOrThrow(classBytes: ByteArray) {
        val result = validate(classBytes)
        if (!result.isValid) {
            val messages = result.errors.joinToString("\n") { it.message }
            throw BytecodeValidationException("Bytecode validation failed:\n$messages", result.errors)
        }
    }
}

public class BytecodeValidationException(
    message: String,
    public val errors: List<BytecodeValidator.ValidationError>,
) : RuntimeException(message)
