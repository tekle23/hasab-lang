package hasab.compiler.backend

import hasab.compiler.types.*

public object TypeMapper {

    public fun toJavaType(type: Type): String {
        return when (type) {
            is IntType -> "int"
            is FloatType -> "double"
            is StringType -> "String"
            is BoolType -> "boolean"
            is CharType -> "char"
            is VoidType -> "void"
            is NilLiteralType -> "Object"
            is ArrayType -> "${toJavaType(type.elementType)}[]"
            is PointerType -> "Object"
            is OptionalType -> "Object"
            is FunctionType -> "Object"
            is LambdaType -> "Object"
            is StructType -> type.name
            is EnumType -> type.name
            is TypeAliasType -> toJavaType(type.target)
            is TraitType -> type.name
            is UnknownType -> "Object"
            is BottomType -> "Object"
            is TypeVariable -> "Object"
        }
    }

    public fun toJavaDefault(type: Type): String {
        return when (type) {
            is IntType -> "0"
            is FloatType -> "0.0"
            is StringType -> "\"\""
            is BoolType -> "false"
            is CharType -> "'\\0'"
            is VoidType -> ""
            is NilLiteralType -> "null"
            is ArrayType -> "new ${toJavaType(type.elementType)}[0]"
            is PointerType -> "null"
            is OptionalType -> "null"
            is FunctionType -> "null"
            is LambdaType -> "null"
            is StructType -> "new ${type.name}()"
            is EnumType -> "null"
            is TypeAliasType -> toJavaDefault(type.target)
            is TraitType -> "null"
            is UnknownType -> "null"
            is BottomType -> "null"
            is TypeVariable -> "null"
        }
    }
}
