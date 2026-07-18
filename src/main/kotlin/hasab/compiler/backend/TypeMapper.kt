package hasab.compiler.backend

import hasab.compiler.types.ResolvedType

public object TypeMapper {

    public fun toJavaType(type: ResolvedType): String {
        return when (type) {
            is ResolvedType.IntType -> "int"
            is ResolvedType.FloatType -> "double"
            is ResolvedType.StringType -> "String"
            is ResolvedType.BoolType -> "boolean"
            is ResolvedType.CharType -> "char"
            is ResolvedType.VoidType -> "void"
            is ResolvedType.NilType -> "Object"
            is ResolvedType.ArrayType -> "${toJavaType(type.elementType)}[]"
            is ResolvedType.PointerType -> "Object"
            is ResolvedType.OptionalType -> "Object"
            is ResolvedType.FunctionType -> "Object"
            is ResolvedType.StructType -> type.name
            is ResolvedType.EnumType -> type.name
            is ResolvedType.TypeAlias -> toJavaType(type.underlying)
            is ResolvedType.TraitType -> type.name
            is ResolvedType.ErrorType -> "Object"
        }
    }

    public fun toJavaDefault(type: ResolvedType): String {
        return when (type) {
            is ResolvedType.IntType -> "0"
            is ResolvedType.FloatType -> "0.0"
            is ResolvedType.StringType -> "\"\""
            is ResolvedType.BoolType -> "false"
            is ResolvedType.CharType -> "'\\0'"
            is ResolvedType.VoidType -> ""
            is ResolvedType.NilType -> "null"
            is ResolvedType.ArrayType -> "new ${toJavaType(type.elementType)}[0]"
            is ResolvedType.PointerType -> "null"
            is ResolvedType.OptionalType -> "null"
            is ResolvedType.FunctionType -> "null"
            is ResolvedType.StructType -> "new ${type.name}()"
            is ResolvedType.EnumType -> "null"
            is ResolvedType.TypeAlias -> toJavaDefault(type.underlying)
            is ResolvedType.TraitType -> "null"
            is ResolvedType.ErrorType -> "null"
        }
    }
}
