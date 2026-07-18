package hasab.compiler.types

import hasab.compiler.frontend.ast.*

public class TypeResolver(private val env: TypeEnvironment) {

    private val errors: MutableList<TypeDiagnostic> = mutableListOf()

    public fun errors(): List<TypeDiagnostic> = errors.toList()

    public fun resolve(node: TypeNode): ResolvedType {
        return when (node) {
            is IdentifierType -> resolveIdentifier(node)
            is QualifiedType -> resolveQualified(node)
            is ArrayType -> resolveArray(node)
            is PointerType -> resolvePointer(node)
            is OptionalType -> resolveOptional(node)
            is FunctionType -> resolveFunction(node)
            is VoidType -> ResolvedType.VoidType
        }
    }

    private fun resolveIdentifier(node: IdentifierType): ResolvedType {
        val builtin = BuiltinTypes.lookup(node.name)
        if (builtin != null) return builtin

        val symbol = env.lookup(node.name)
        if (symbol != null) return symbol.type

        errors.add(TypeError(
            message = "Unknown type '${node.name}'",
            range = node.range(),
            fileName = node.fileName,
            hint = "Did you forget to declare type '${node.name}'?",
        ))
        return ResolvedType.ErrorType
    }

    private fun resolveQualified(node: QualifiedType): ResolvedType {
        val fullName = node.path.joinToString("::")
        val symbol = env.lookup(fullName)
        if (symbol != null) return symbol.type

        errors.add(TypeError(
            message = "Unknown qualified type '$fullName'",
            range = node.range(),
            fileName = node.fileName,
        ))
        return ResolvedType.ErrorType
    }

    private fun resolveArray(node: ArrayType): ResolvedType {
        val elementType = resolve(node.elementType)
        return ResolvedType.ArrayType(elementType)
    }

    private fun resolvePointer(node: PointerType): ResolvedType {
        val elementType = resolve(node.elementType)
        return ResolvedType.PointerType(elementType)
    }

    private fun resolveOptional(node: OptionalType): ResolvedType {
        val elementType = resolve(node.elementType)
        return ResolvedType.OptionalType(elementType)
    }

    private fun resolveFunction(node: FunctionType): ResolvedType {
        val paramTypes = node.parameterTypes.map { resolve(it) }
        val returnType = resolve(node.returnType)
        return ResolvedType.FunctionType(paramTypes, returnType)
    }
}
