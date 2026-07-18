package hasab.compiler.backend

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.ast.*
import hasab.compiler.types.*

public class JavaSourceGenerator(private val typeCheckDiagnostics: List<TypeDiagnostic> = emptyList()) {

    private lateinit var sb: StringBuilder
    private var indent = 0

    public fun generate(module: Module): String {
        sb = StringBuilder()
        indent = 0

        for (diagnostic in typeCheckDiagnostics) {
            if (diagnostic.severity == DiagnosticSeverity.ERROR) {
                sb.appendLine("// TYPE ERROR: ${diagnostic.message}")
            }
        }

        for (decl in module.declarations) {
            generateDeclaration(decl)
        }

        return sb.toString()
    }

    private fun emit(s: String) { sb.append(s) }
    private fun emitLine(s: String) { sb.appendLine(s) }
    private fun emitIndent() { repeat(indent) { sb.append("    ") } }

    private fun captureExpr(block: () -> Unit): String {
        val saved = sb
        sb = StringBuilder()
        block()
        val result = sb.toString()
        sb = saved
        return result
    }

    private fun generateDeclaration(decl: Decl) {
        when (decl) {
            is FnDecl -> generateFnDecl(decl)
            is StructDecl -> generateStructDecl(decl)
            is EnumDecl -> generateEnumDecl(decl)
            is ImplDecl -> generateImplDecl(decl)
            is TraitDecl -> {}
            is TypeAliasDecl -> generateTypeAlias(decl)
            is ModDecl -> generateModDecl(decl)
            is UseDecl -> {}
            is PubDecl -> generateDeclaration(decl.inner)
        }
    }

    private fun generateFnDecl(decl: FnDecl, isMethod: Boolean = false) {
        val returnType = if (decl.returnType != null) resolveTypeNode(decl.returnType) else ResolvedType.VoidType
        val javaReturnType = TypeMapper.toJavaType(returnType)

        emitIndent()
        emit(if (isMethod) "public " else "public static ")
        emit("$javaReturnType ${decl.name}(")

        val params = decl.parameters.filter { it.name != "self" }
        emit(params.joinToString(", ") { fp ->
            val pt = if (fp.type != null) resolveTypeNode(fp.type) else ResolvedType.VoidType
            "${TypeMapper.toJavaType(pt)} ${sanitizeName(fp.name)}"
        })
        emit(")")

        if (decl.body != null) {
            emitLine(" {")
            indent++
            generateBlock(decl.body)
            indent--
            emitIndent()
            emitLine("}")
        } else {
            emitLine(";")
        }
        emitLine("")
    }

    private fun generateStructDecl(decl: StructDecl) {
        emitIndent()
        emitLine("public class ${decl.name} {")
        indent++

        for (field in decl.fields) {
            val ft = resolveTypeNode(field.type)
            emitIndent()
            emitLine("public ${TypeMapper.toJavaType(ft)} ${sanitizeName(field.name)};")
        }

        if (decl.fields.isNotEmpty()) {
            emitLine("")
            emitIndent()
            emitLine("public ${decl.name}() {}")
            emitLine("")

            val constructorParams = decl.fields.joinToString(", ") { field ->
                val ft = resolveTypeNode(field.type)
                "${TypeMapper.toJavaType(ft)} ${sanitizeName(field.name)}"
            }
            emitIndent()
            emitLine("public ${decl.name}($constructorParams) {")
            indent++
            for (field in decl.fields) {
                emitIndent()
                emitLine("this.${sanitizeName(field.name)} = ${sanitizeName(field.name)};")
            }
            indent--
            emitIndent()
            emitLine("}")
        }

        indent--
        emitIndent()
        emitLine("}")
        emitLine("")
    }

    private fun generateEnumDecl(decl: EnumDecl) {
        emitIndent()
        emitLine("public enum ${decl.name} {")
        indent++

        val variantEntries = decl.variants.map { variant ->
            if (variant.fields.isEmpty()) {
                variant.name
            } else {
                val fieldParams = variant.fields.joinToString(", ") { f ->
                    "${TypeMapper.toJavaType(resolveTypeNode(f.type))} ${sanitizeName(f.name)}"
                }
                "${variant.name}($fieldParams)"
            }
        }
        emit(variantEntries.joinToString(",\n"))
        emitLine(";")
        emitLine("")

        for (variant in decl.variants) {
            if (variant.fields.isNotEmpty()) {
                val fieldParams = variant.fields.joinToString(", ") { f ->
                    "${TypeMapper.toJavaType(resolveTypeNode(f.type))} ${sanitizeName(f.name)}"
                }
                emitIndent()
                emitLine("private final ${variant.name}_Fields ${variant.name.lowercase()}_fields;")
                emitIndent()
                emitLine("${variant.name}($fieldParams) {")
                indent++
                emitIndent()
                emitLine("this.${variant.name.lowercase()}_fields = new ${variant.name}_Fields(${
                    variant.fields.joinToString(", ") { sanitizeName(it.name) }
                });")
                indent--
                emitIndent()
                emitLine("}")
                emitLine("")
            }
        }

        for (variant in decl.variants) {
            if (variant.fields.isNotEmpty()) {
                emitIndent()
                emitLine("public static class ${variant.name}_Fields {")
                indent++
                for (field in variant.fields) {
                    emitIndent()
                    emitLine("public final ${TypeMapper.toJavaType(resolveTypeNode(field.type))} ${sanitizeName(field.name)};")
                }
                val fp = variant.fields.joinToString(", ") { f ->
                    "${TypeMapper.toJavaType(resolveTypeNode(f.type))} ${sanitizeName(f.name)}"
                }
                emitIndent()
                emitLine("public ${variant.name}_Fields($fp) {")
                indent++
                for (field in variant.fields) {
                    emitIndent()
                    emitLine("this.${sanitizeName(field.name)} = ${sanitizeName(field.name)};")
                }
                indent--
                emitIndent()
                emitLine("}")
                indent--
                emitIndent()
                emitLine("}")
                emitLine("")
            }
        }

        indent--
        emitIndent()
        emitLine("}")
        emitLine("")
    }

    private fun generateImplDecl(decl: ImplDecl) {
        for (method in decl.methods) {
            generateFnDecl(method, isMethod = true)
        }
    }

    private fun generateTypeAlias(decl: TypeAliasDecl) {
        val target = resolveTypeNode(decl.target)
        emitIndent()
        emitLine("public static class ${decl.name} extends ${TypeMapper.toJavaType(target)} {}")
        emitLine("")
    }

    private fun generateModDecl(decl: ModDecl) {
        emitIndent()
        emitLine("// mod ${decl.name}")
        if (decl.body != null) {
            for (inner in decl.body) {
                generateDeclaration(inner)
            }
        }
    }

    private fun generateBlock(block: Block) {
        for (stmt in block.statements) {
            generateStatement(stmt)
        }
    }

    private fun generateStatement(stmt: Stmt) {
        when (stmt) {
            is ExprStmt -> {
                emitIndent()
                generateExpression(stmt.expression)
                emitLine(";")
            }
            is ReturnStmt -> {
                emitIndent()
                emit("return ")
                if (stmt.value != null) generateExpression(stmt.value)
                emitLine(";")
            }
            is BreakStmt -> { emitIndent(); emitLine("break;") }
            is ContinueStmt -> { emitIndent(); emitLine("continue;") }
            is LetStmt -> generateLetStmt(stmt)
            is IfStmt -> generateIfStmt(stmt)
            is WhileStmt -> generateWhileStmt(stmt)
            is ForStmt -> generateForStmt(stmt)
            is Block -> {
                emitIndent()
                emitLine("{")
                indent++
                generateBlock(stmt)
                indent--
                emitIndent()
                emitLine("}")
            }
        }
    }

    private fun generateLetStmt(stmt: LetStmt) {
        val varType = if (stmt.typeAnnotation != null) resolveTypeNode(stmt.typeAnnotation)
        else inferExpressionType(stmt.initializer)

        emitIndent()
        emit("${TypeMapper.toJavaType(varType)} ${sanitizeName(stmt.name)} = ")
        generateExpression(stmt.initializer)
        emitLine(";")
    }

    private fun generateIfStmt(stmt: IfStmt) {
        emitIndent()
        emit("if (")
        generateExpression(stmt.condition)
        emitLine(") {")
        indent++
        generateBlock(stmt.thenBranch)
        indent--

        if (stmt.elseBranch != null) {
            when (stmt.elseBranch) {
                is Block -> {
                    emitIndent()
                    emitLine("} else {")
                    indent++
                    generateBlock(stmt.elseBranch as Block)
                    indent--
                }
                is IfStmt -> {
                    emitIndent()
                    emit("} else ")
                    generateIfStmt(stmt.elseBranch as IfStmt)
                    return
                }
                else -> {}
            }
        }

        emitIndent()
        emitLine("}")
    }

    private fun generateWhileStmt(stmt: WhileStmt) {
        emitIndent()
        emit("while (")
        generateExpression(stmt.condition)
        emitLine(") {")
        indent++
        generateBlock(stmt.body)
        indent--
        emitIndent()
        emitLine("}")
    }

    private fun generateForStmt(stmt: ForStmt) {
        emitIndent()
        emit("for (var ${sanitizeName(stmt.variable)} : ")
        generateExpression(stmt.iterable)
        emitLine(") {")
        indent++
        generateBlock(stmt.body)
        indent--
        emitIndent()
        emitLine("}")
    }

    private fun generateExpression(expr: Expr) {
        when (expr) {
            is IntegerLiteralExpr -> emit(expr.value)
            is FloatLiteralExpr -> emit(expr.value)
            is StringLiteralExpr -> emit("\"${escapeString(expr.value)}\"")
            is CharLiteralExpr -> emit("'${escapeString(expr.value)}'")
            is BoolLiteralExpr -> emit(if (expr.value) "true" else "false")
            is NilLiteralExpr -> emit("null")
            is IdentifierExpr -> emit(sanitizeName(expr.name))
            is BinaryExpr -> {
                generateExpression(expr.left)
                emit(" ${expr.operator} ")
                generateExpression(expr.right)
            }
            is UnaryExpr -> {
                emit(expr.operator)
                generateExpression(expr.operand)
            }
            is CallExpr -> generateCallExpr(expr)
            is IndexExpr -> {
                generateExpression(expr.callee)
                emit("[")
                generateExpression(expr.index)
                emit("]")
            }
            is FieldAccessExpr -> {
                generateExpression(expr.callee)
                emit(".${sanitizeName(expr.fieldName)}")
            }
            is ParenExpr -> {
                emit("(")
                generateExpression(expr.inner)
                emit(")")
            }
            is ArrayLiteralExpr -> {
                emit("new Object[]{")
                emit(expr.elements.joinToString(", ") { captureExpr { generateExpression(it) } })
                emit("}")
            }
            is ArrayInitExpr -> {
                val elemType = if (expr.elementType != null) resolveTypeNode(expr.elementType)
                else ResolvedType.StructType("Object", linkedMapOf())
                emit("new ${TypeMapper.toJavaType(elemType)}[")
                generateExpression(expr.size)
                emit("]")
            }
            is IfExpr -> {
                emit("(")
                generateExpression(expr.condition)
                emit(" ? ")
                generateExpression(expr.thenBranch)
                emit(" : ")
                if (expr.elseBranch != null) generateExpression(expr.elseBranch) else emit("null")
                emit(")")
            }
            is AssignmentExpr -> {
                generateExpression(expr.target)
                emit(" = ")
                generateExpression(expr.value)
            }
            is CompoundAssignmentExpr -> {
                generateExpression(expr.target)
                emit(" ${expr.operator} ")
                generateExpression(expr.value)
            }
        }
    }

    private fun generateCallExpr(expr: CallExpr) {
        if (expr.callee is IdentifierExpr && (expr.callee as IdentifierExpr).name == "println") {
            emit("System.out.println(")
            emit(expr.arguments.joinToString(", ") { captureExpr { generateExpression(it) } })
            emit(")")
            return
        }

        generateExpression(expr.callee)
        emit("(")
        emit(expr.arguments.joinToString(", ") { captureExpr { generateExpression(it) } })
        emit(")")
    }

    // ── Helpers ──────────────────────────────────────────────────

    private fun sanitizeName(name: String): String {
        return when (name) {
            "new" -> "isNew"
            "this" -> "self"
            "super" -> "isSuper"
            "class" -> "isClass"
            "void" -> "isVoid"
            "int", "float", "string", "bool", "char" -> "v_$name"
            else -> name
        }
    }

    private fun escapeString(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun resolveTypeNode(node: TypeNode): ResolvedType {
        return when (node) {
            is IdentifierType -> BuiltinTypes.lookup(node.name) ?: ResolvedType.StructType(node.name, linkedMapOf())
            is QualifiedType -> ResolvedType.StructType(node.path.joinToString("::"), linkedMapOf())
            is hasab.compiler.frontend.ast.ArrayType -> ResolvedType.ArrayType(resolveTypeNode(node.elementType))
            is PointerType -> ResolvedType.PointerType(resolveTypeNode(node.elementType))
            is OptionalType -> ResolvedType.OptionalType(resolveTypeNode(node.elementType))
            is hasab.compiler.frontend.ast.FunctionType -> {
                ResolvedType.FunctionType(
                    node.parameterTypes.map { resolveTypeNode(it) },
                    resolveTypeNode(node.returnType),
                )
            }
            is VoidType -> ResolvedType.VoidType
        }
    }

    private fun inferExpressionType(expr: Expr): ResolvedType {
        val objectType = ResolvedType.StructType("Object", linkedMapOf())
        return when (expr) {
            is IntegerLiteralExpr -> ResolvedType.IntType
            is FloatLiteralExpr -> ResolvedType.FloatType
            is StringLiteralExpr -> ResolvedType.StringType
            is CharLiteralExpr -> ResolvedType.CharType
            is BoolLiteralExpr -> ResolvedType.BoolType
            is NilLiteralExpr -> ResolvedType.NilType
            is IdentifierExpr -> objectType
            is BinaryExpr -> {
                val left = inferExpressionType(expr.left)
                when (expr.operator) {
                    "==", "!=", "<", ">", "<=", ">=", "&&", "||" -> ResolvedType.BoolType
                    else -> left
                }
            }
            is ParenExpr -> inferExpressionType(expr.inner)
            is ArrayLiteralExpr -> {
                if (expr.elements.isNotEmpty()) ResolvedType.ArrayType(inferExpressionType(expr.elements[0]))
                else ResolvedType.ArrayType(objectType)
            }
            is IfExpr -> {
                val thenType = inferExpressionType(expr.thenBranch)
                if (expr.elseBranch != null) inferExpressionType(expr.elseBranch)
                else ResolvedType.OptionalType(thenType)
            }
            else -> objectType
        }
    }
}
