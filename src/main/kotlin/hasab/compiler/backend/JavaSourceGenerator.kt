package hasab.compiler.backend

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.ast.*
import hasab.compiler.types.*
import hasab.compiler.frontend.ast.ArrayType as AstArrayType
import hasab.compiler.frontend.ast.FunctionType as AstFunctionType
import hasab.compiler.frontend.ast.OptionalType as AstOptionalType
import hasab.compiler.frontend.ast.PointerType as AstPointerType
import hasab.compiler.frontend.ast.VoidType as AstVoidType

public class JavaSourceGenerator(private val typeCheckDiagnostics: List<TypeDiagnostic> = emptyList()) {

    private lateinit var sb: StringBuilder
    private var indent = 0
    private var sourceMap: SourceMap? = null
    private var generatedFile: String = ""
    private var currentLine: Int = 1
    private var typeEnv: hasab.compiler.types.TypeEnvironment = hasab.compiler.types.TypeEnvironment.root()
    private var typedModel: hasab.compiler.types.TypedSemanticModel = hasab.compiler.types.TypedSemanticModel.empty()
    private val implMethodNames = mutableSetOf<String>()

    public fun generate(module: Module, className: String = "Main"): String {
        sb = StringBuilder()
        indent = 0
        currentLine = 1

        for (diagnostic in typeCheckDiagnostics) {
            if (diagnostic.severity == DiagnosticSeverity.ERROR) {
                sb.appendLine("// TYPE ERROR: ${diagnostic.message}")
                currentLine++
            }
        }

        emitLine("import java.util.Objects;")
        emitLine("")
        emitLine("public class $className {")
        indent++
        for (decl in module.declarations) {
            generateDeclaration(decl)
        }
        emitBuiltinHelpers()
        indent--
        emitIndent()
        emitLine("}")

        return sb.toString()
    }

    public fun generate(
        module: Module,
        typeCheckResult: hasab.compiler.types.TypeCheckResult,
        sourceMap: SourceMap,
        sourceFile: String,
        generatedFileName: String? = null,
    ): String {
        this.sourceMap = sourceMap
        this.generatedFile = generatedFileName ?: sourceFile.replace(".has", ".java")
        this.typeEnv = typeCheckResult.environment
        this.typedModel = typeCheckResult.typedModel
        val className = sourceFile
            .removeSuffix(".has")
            .removeSuffix(".hasab")
            .substringAfterLast("/")
            .substringAfterLast("\\")
            .replace(Regex("[^A-Za-z0-9_]"), "_")
            .let { if (it.isEmpty() || it[0].isDigit()) "_$it" else it }

        sb = StringBuilder()
        indent = 0
        currentLine = 1

        for (diagnostic in typeCheckResult.diagnostics) {
            if (diagnostic.severity == DiagnosticSeverity.ERROR) {
                sb.appendLine("// TYPE ERROR: ${diagnostic.message}")
                currentLine++
            }
        }

        emitLine("import java.util.Objects;")
        emitLine("")
        emitLine("public class $className {")
        indent++
        for (decl in module.declarations) {
            generateDeclaration(decl)
        }
        emitBuiltinHelpers()
        indent--
        emitIndent()
        emitLine("}")

        this.sourceMap = null
        return sb.toString()
    }

    private fun emitBuiltinHelpers() {
        emitIndent()
        emitLine("private static int len(Object obj) {")
        indent++
        emitIndent()
        emitLine("if (obj instanceof Object[]) return ((Object[]) obj).length;")
        emitIndent()
        emitLine("if (obj instanceof int[]) return ((int[]) obj).length;")
        emitIndent()
        emitLine("if (obj instanceof double[]) return ((double[]) obj).length;")
        emitIndent()
        emitLine("if (obj instanceof boolean[]) return ((boolean[]) obj).length;")
        emitIndent()
        emitLine("if (obj instanceof char[]) return ((char[]) obj).length;")
        emitIndent()
        emitLine("if (obj instanceof long[]) return ((long[]) obj).length;")
        emitIndent()
        emitLine("if (obj instanceof String) return ((String) obj).length();")
        emitIndent()
        emitLine("throw new UnsupportedOperationException(\"len() not supported for \" + obj.getClass());")
        indent--
        emitIndent()
        emitLine("}")
        emitIndent()
        emitLine("private static int abs(int x) { return Math.abs(x); }")
        emitIndent()
        emitLine("private static double sqrt(double x) { return Math.sqrt(x); }")
        emitIndent()
        emitLine("private static double pow(double base, double exp) { return Math.pow(base, exp); }")
        emitIndent()
        emitLine("private static int min(int a, int b) { return Math.min(a, b); }")
        emitIndent()
        emitLine("private static int max(int a, int b) { return Math.max(a, b); }")
        emitIndent()
        emitLine("private static String str(Object obj) { return String.valueOf(obj); }")
        emitIndent()
        emitLine("private static void print(Object obj) { System.out.print(obj); }")
        emitIndent()
        emitLine("private static int now() { return (int) System.currentTimeMillis(); }")
        emitIndent()
        emitLine("private static int to_int(Object obj) { if (obj instanceof Number) return ((Number) obj).intValue(); if (obj instanceof String) return Integer.parseInt((String) obj); throw new IllegalArgumentException(\"Cannot convert \" + obj.getClass() + \" to int\"); }")
        emitIndent()
        emitLine("private static double to_float(Object obj) { if (obj instanceof Number) return ((Number) obj).doubleValue(); if (obj instanceof String) return Double.parseDouble((String) obj); throw new IllegalArgumentException(\"Cannot convert \" + obj.getClass() + \" to float\"); }")
        emitIndent()
        emitLine("private static String typeof(Object obj) { return obj == null ? \"nil\" : obj.getClass().getSimpleName(); }")
        emitIndent()
        emitLine("private static void _hs_assert(boolean cond) { if (!cond) throw new AssertionError(\"Assertion failed\"); }")
        emitIndent()
        emitLine("private static String substring(String s, int start, int end) { return s.substring(start, end); }")
        emitIndent()
        emitLine("private static boolean contains(String s, String sub) { return s.contains(sub); }")
        emitIndent()
        emitLine("private static String trim(String s) { return s.trim(); }")
        emitIndent()
        emitLine("private static String upper(String s) { return s.toUpperCase(); }")
        emitIndent()
        emitLine("private static String lower(String s) { return s.toLowerCase(); }")
        emitIndent()
        emitLine("private static String reverse(String s) { return new StringBuilder(s).reverse().toString(); }")
        emitIndent()
        emitLine("private static String replace(String s, String from, String to) { return s.replace(from, to); }")
        emitIndent()
        emitLine("private static String[] split(String s, String delim) { return s.split(delim); }")
        emitIndent()
        emitLine("private static boolean starts_with(String s, String prefix) { return s.startsWith(prefix); }")
        emitIndent()
        emitLine("private static boolean ends_with(String s, String suffix) { return s.endsWith(suffix); }")
    }

    private fun emitWithSourceMap(node: AstNode, block: () -> Unit) {
        val sm = sourceMap
        if (sm != null) {
            val startLine = currentLine
            block()
            val endLine = currentLine
            sm.record(
                generatedFile = generatedFile,
                generatedLine = startLine,
                sourceFile = node.fileName,
                sourceLine = node.line,
                sourceCol = node.column,
                sourceEndLine = node.line,
                sourceEndCol = node.column,
            )
            if (endLine > startLine) {
                for (line in (startLine + 1)..endLine) {
                    sm.record(
                        generatedFile = generatedFile,
                        generatedLine = line,
                        sourceFile = node.fileName,
                        sourceLine = node.line,
                        sourceCol = node.column,
                    )
                }
            }
        } else {
            block()
        }
    }

    private fun emit(s: String) { sb.append(s) }
    private fun emitLine(s: String) { sb.appendLine(s); currentLine++ }
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

    private fun generateFnDecl(decl: FnDecl, isMethod: Boolean = false, selfTypeName: String? = null) {
        emitWithSourceMap(decl) {
            val returnType = if (decl.returnType != null) resolveTypeNode(decl.returnType) else VoidType
            val javaReturnType = TypeMapper.toJavaType(returnType)

            emitIndent()
            emit(if (isMethod) "public " else "public static ")
            emit("$javaReturnType ${sanitizeName(decl.name)}(")

            val hasSelf = decl.parameters.any { it.name == "self" }
            val isMain = decl.name == "main" && !isMethod
            val params = if (isMain) {
                emptyList()
            } else if (hasSelf && selfTypeName != null) {
                decl.parameters
            } else {
                decl.parameters.filter { it.name != "self" }
            }
            if (isMain) {
                emit("String[] args")
            } else {
                emit(params.joinToString(", ") { fp ->
                    val pt = if (fp.name == "self" && selfTypeName != null) {
                        typeEnv.lookup(selfTypeName) ?: VoidType
                    } else if (fp.type != null) {
                        resolveTypeNode(fp.type)
                    } else {
                        VoidType
                    }
                    "${TypeMapper.toJavaType(pt)} ${sanitizeName(fp.name)}"
                })
            }
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
    }

    private fun generateStructDecl(decl: StructDecl) {
        emitWithSourceMap(decl) {
            emitIndent()
            emitLine("public static class ${decl.name} {")
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
    }

    private fun generateEnumDecl(decl: EnumDecl) {
        emitWithSourceMap(decl) {
            val hasDataVariants = decl.variants.any { it.fields.isNotEmpty() }

            if (!hasDataVariants) {
                emitIndent()
                emitLine("public enum ${decl.name} {")
                indent++
                emit(decl.variants.joinToString(", ") { it.name })
                emitLine(";")
                indent--
                emitIndent()
                emitLine("}")
                emitLine("")
            } else {
                emitIndent()
                emitLine("public static class ${decl.name} {")
                indent++
                emitIndent()
                emitLine("public final String _variant;")
                indent--
                emitLine("")

                emitIndent()
                emitLine("private ${decl.name}(String variant) { this._variant = variant; }")
                emitLine("")

                for (variant in decl.variants) {
                    val params = variant.fields.joinToString(", ") { f ->
                        "${TypeMapper.toJavaType(resolveTypeNode(f.type))} ${sanitizeName(f.name)}"
                    }
                    emitIndent()
                    emitLine("public static ${decl.name} ${variant.name}($params) { return new ${decl.name}(\"${variant.name}\"); }")
                }

                indent--
                emitIndent()
                emitLine("}")
                emitLine("")
            }
        }
    }

    private fun generateImplDecl(decl: ImplDecl) {
        val structName = when (decl.targetType) {
            is IdentifierType -> decl.targetType.name
            else -> null
        }
        for (method in decl.methods) {
            implMethodNames.add(method.name)
            generateFnDecl(method, isMethod = false, selfTypeName = structName)
        }
    }

    private fun generateTypeAlias(decl: TypeAliasDecl) {
        val target = resolveTypeNode(decl.target)
        emitIndent()
        emitLine("// type alias ${decl.name} = ${TypeMapper.toJavaType(target)}")
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
                if (expr.operator == ".." || expr.operator == "..=") {
                    val typedType = typedModel.typeOf(expr)
                    if (typedType is ArrayType && typedType.elementType == IntType) {
                        emit("new int[]{")
                        emit(captureExpr { generateExpression(expr.left) })
                        emit(", ")
                        emit(captureExpr { generateExpression(expr.right) })
                        emit("}")
                    } else {
                        emit("new Object[]{")
                        emit(captureExpr { generateExpression(expr.left) })
                        emit(", ")
                        emit(captureExpr { generateExpression(expr.right) })
                        emit("}")
                    }
                } else {
                    generateExpression(expr.left)
                    emit(" ${expr.operator} ")
                    generateExpression(expr.right)
                }
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
                val typedType = typedModel.typeOf(expr)
                if (typedType is ArrayType) {
                    emit("new ${TypeMapper.toJavaType(typedType.elementType)}[]{")
                } else {
                    val elemType = if (expr.elements.isNotEmpty()) inferExpressionType(expr.elements[0]) else StructType("Object", emptyList())
                    emit("new ${TypeMapper.toJavaType(elemType)}[]{")
                }
                emit(expr.elements.joinToString(", ") { captureExpr { generateExpression(it) } })
                emit("}")
            }
            is ArrayInitExpr -> {
                val elemType = if (expr.elementType != null) resolveTypeNode(expr.elementType)
                else StructType("Object", emptyList())
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
            is SafeFieldAccessExpr -> {
                emit("(")
                generateExpression(expr.callee)
                emit(" != null ? ")
                generateExpression(expr.callee)
                emit(".${sanitizeName(expr.fieldName)} : null)")
            }
            is NullAssertExpr -> {
                emit("Objects.requireNonNull(")
                generateExpression(expr.operand)
                emit(")")
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

        if (expr.callee is IdentifierExpr && (expr.callee as IdentifierExpr).name == "print") {
            emit("System.out.print(")
            emit(expr.arguments.joinToString(", ") { captureExpr { generateExpression(it) } })
            emit(")")
            return
        }

        if (expr.callee is IdentifierExpr) {
            val name = (expr.callee as IdentifierExpr).name
            if (isKnownStruct(name)) {
                emit("new $name(")
                emit(expr.arguments.joinToString(", ") { captureExpr { generateExpression(it) } })
                emit(")")
                return
            }
        }

        if (expr.callee is FieldAccessExpr && (expr.callee as FieldAccessExpr).fieldName in implMethodNames) {
            val fae = expr.callee as FieldAccessExpr
            emit("${sanitizeName(fae.fieldName)}(")
            emit(captureExpr { generateExpression(fae.callee) })
            if (expr.arguments.isNotEmpty()) {
                emit(", ")
                emit(expr.arguments.joinToString(", ") { captureExpr { generateExpression(it) } })
            }
            emit(")")
            return
        }

        generateExpression(expr.callee)
        emit("(")
        emit(expr.arguments.joinToString(", ") { captureExpr { generateExpression(it) } })
        emit(")")
    }

    private fun isKnownStruct(name: String): Boolean {
        return typeEnv.lookup(name) is hasab.compiler.types.StructType
    }

    // ── Helpers ──────────────────────────────────────────────────

    private fun sanitizeName(name: String): String {
        return when (name) {
            "new" -> "isNew"
            "this" -> "self"
            "super" -> "isSuper"
            "class" -> "isClass"
            "void" -> "isVoid"
            "assert" -> "_hs_assert"
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

    private fun resolveTypeNode(node: TypeNode): Type {
        return when (node) {
            is IdentifierType -> typeEnv.lookup(node.name) ?: StructType(node.name, emptyList())
            is QualifiedType -> StructType(node.path.joinToString("::"), emptyList())
            is AstArrayType -> ArrayType(resolveTypeNode(node.elementType))
            is AstPointerType -> PointerType(resolveTypeNode(node.elementType))
            is AstOptionalType -> OptionalType(resolveTypeNode(node.elementType))
            is AstFunctionType -> {
                FunctionType(
                    node.parameterTypes.map { resolveTypeNode(it) },
                    resolveTypeNode(node.returnType),
                )
            }
            is AstVoidType -> VoidType
        }
    }

    private fun inferExpressionType(expr: Expr): Type {
        val typedType = typedModel.typeOf(expr)
        if (typedType != null) return typedType
        val objectType = StructType("Object", emptyList())
        return when (expr) {
            is IntegerLiteralExpr -> IntType
            is FloatLiteralExpr -> FloatType
            is StringLiteralExpr -> StringType
            is CharLiteralExpr -> CharType
            is BoolLiteralExpr -> BoolType
            is NilLiteralExpr -> NilLiteralType
            is IdentifierExpr -> typeEnv.lookup(expr.name) ?: objectType
            is BinaryExpr -> {
                val left = inferExpressionType(expr.left)
                when (expr.operator) {
                    "==", "!=", "<", ">", "<=", ">=", "&&", "||" -> BoolType
                    else -> left
                }
            }
            is ParenExpr -> inferExpressionType(expr.inner)
            is ArrayLiteralExpr -> {
                if (expr.elements.isNotEmpty()) ArrayType(inferExpressionType(expr.elements[0]))
                else ArrayType(objectType)
            }
            is IfExpr -> {
                val thenType = inferExpressionType(expr.thenBranch)
                if (expr.elseBranch != null) inferExpressionType(expr.elseBranch)
                else OptionalType(thenType)
            }
            is IndexExpr -> {
                val calleeType = inferExpressionType(expr.callee)
                if (calleeType is ArrayType) calleeType.elementType
                else objectType
            }
            is CallExpr -> {
                val callee = expr.callee
                if (callee is IdentifierExpr) {
                    val fnType = typeEnv.lookup(callee.name)
                    if (fnType is FunctionType) fnType.returnType
                    else objectType
                } else objectType
            }
            else -> objectType
        }
    }
}
