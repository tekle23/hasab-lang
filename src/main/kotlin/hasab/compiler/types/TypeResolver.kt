package hasab.compiler.types

import hasab.compiler.frontend.ast.ArrayType as AstArrayType
import hasab.compiler.frontend.ast.FunctionType as AstFunctionType
import hasab.compiler.frontend.ast.IdentifierType
import hasab.compiler.frontend.ast.OptionalType as AstOptionalType
import hasab.compiler.frontend.ast.PointerType as AstPointerType
import hasab.compiler.frontend.ast.QualifiedType
import hasab.compiler.frontend.ast.TypeNode
import hasab.compiler.frontend.ast.VoidType as AstVoidType

/**
 * Resolves AST [TypeNode] references to concrete [Type] instances
 * using a [TypeEnvironment].
 *
 * Stateless — all state is held in the environment parameter.
 */
public object TypeResolver {

    /**
     * Resolve an AST [TypeNode] to a [Type].
     *
     * @param node the type annotation AST node
     * @param env the current type environment for name resolution
     * @param onUndefined callback invoked when a type name is not found (optional)
     * @return the resolved [Type], or [UnknownType] if resolution fails
     */
    public fun resolve(
        node: TypeNode,
        env: TypeEnvironment,
        onUndefined: ((String) -> Unit)? = null,
    ): Type = when (node) {
        is IdentifierType -> {
            val resolved = env.lookup(node.name)
            if (resolved != null) resolved
            else {
                onUndefined?.invoke(node.name)
                UnknownType
            }
        }
        is QualifiedType -> {
            val name = node.path.last()
            val resolved = env.lookup(name)
            if (resolved != null) resolved
            else {
                onUndefined?.invoke(name)
                UnknownType
            }
        }
        is AstArrayType -> ArrayType(resolve(node.elementType, env, onUndefined))
        is AstPointerType -> PointerType(resolve(node.elementType, env, onUndefined))
        is AstOptionalType -> OptionalType(resolve(node.elementType, env, onUndefined))
        is AstFunctionType -> {
            val paramTypes = node.parameterTypes.map { resolve(it, env, onUndefined) }
            val retType = resolve(node.returnType, env, onUndefined)
            FunctionType(paramTypes, retType)
        }
        is AstVoidType -> VoidType
    }
}
