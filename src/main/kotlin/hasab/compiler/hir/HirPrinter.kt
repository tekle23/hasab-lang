package hasab.compiler.hir

/**
 * Pretty-printer for tree-based HIR.
 *
 * Produces human-readable indented output showing the structure of
 * declarations, statements, and expressions with their resolved types.
 */
public class HirPrinter(private val indentStr: String = "  ") {

    /**
     * Print an entire [HirModule] to a string.
     */
    public fun print(module: HirModule): String {
        val sb = StringBuilder()
        sb.appendLine("module ${module.name ?: "<unnamed>"}")
        for (decl in module.declarations) {
            printDecl(decl, sb, 0)
            sb.appendLine()
        }
        return sb.toString()
    }

    private fun printDecl(decl: HirDecl, sb: StringBuilder, depth: Int) {
        val pad = indentStr.repeat(depth)
        when (decl) {
            is HirFnDecl -> {
                val vis = if (decl.isPublic) "pub " else ""
                val params = decl.parameters.joinToString(", ") { "${it.name}: ${it.type.displayName}" }
                val ret = if (decl.returnType != hasab.compiler.types.VoidType) " -> ${decl.returnType.displayName}" else ""
                sb.appendLine("${pad}${vis}fn ${decl.name}($params)$ret")
                if (decl.body != null) {
                    printBlock(decl.body, sb, depth + 1)
                } else {
                    sb.appendLine("${pad}${indentStr}<extern>")
                }
            }
            is HirStructDecl -> {
                val vis = if (decl.isPublic) "pub " else ""
                sb.appendLine("${pad}${vis}struct ${decl.name} {")
                for (field in decl.fields) {
                    val mut = if (field.isMutable) "mut " else ""
                    sb.appendLine("${pad}${indentStr}${mut}${field.name}: ${field.type.displayName}")
                }
                sb.appendLine("${pad}}")
            }
            is HirEnumDecl -> {
                val vis = if (decl.isPublic) "pub " else ""
                sb.appendLine("${pad}${vis}enum ${decl.name} {")
                for (variant in decl.variants) {
                    val fields = if (variant.fieldTypes.isNotEmpty()) {
                        variant.fieldTypes.joinToString(", ") { it.displayName }
                    } else ""
                    sb.appendLine("${pad}${indentStr}${variant.name}($fields)")
                }
                sb.appendLine("${pad}}")
            }
            is HirTraitDecl -> {
                val vis = if (decl.isPublic) "pub " else ""
                sb.appendLine("${pad}${vis}trait ${decl.name} {")
                for (method in decl.methods) {
                    val params = method.parameters.joinToString(", ") { "${it.name}: ${it.type.displayName}" }
                    val ret = if (method.returnType != hasab.compiler.types.VoidType) " -> ${method.returnType.displayName}" else ""
                    sb.appendLine("${pad}${indentStr}fn ${method.name}($params)$ret")
                }
                sb.appendLine("${pad}}")
            }
            is HirTypeAliasDecl -> {
                val vis = if (decl.isPublic) "pub " else ""
                sb.appendLine("${pad}${vis}type ${decl.name} = ${decl.targetType.displayName}")
            }
            is HirImplDecl -> {
                sb.appendLine("${pad}impl ${decl.targetType.displayName} {")
                for (method in decl.methods) {
                    val params = method.parameters.joinToString(", ") { "${it.name}: ${it.type.displayName}" }
                    val ret = if (method.returnType != hasab.compiler.types.VoidType) " -> ${method.returnType.displayName}" else ""
                    sb.appendLine("${pad}${indentStr}fn ${method.name}($params)$ret")
                    method.body?.let { printBlock(it, sb, depth + 2) }
                }
                sb.appendLine("${pad}}")
            }
            is HirUseDecl -> {
                val vis = if (decl.isPublic) "pub " else ""
                sb.appendLine("${pad}${vis}use ${decl.path.joinToString("::")}")
            }
        }
    }

    private fun printBlock(block: HirBlock, sb: StringBuilder, depth: Int) {
        val pad = indentStr.repeat(depth)
        sb.appendLine("${pad}{")
        for (stmt in block.statements) {
            printStmt(stmt, sb, depth + 1)
        }
        sb.appendLine("${pad}}")
    }

    private fun printStmt(stmt: HirStmt, sb: StringBuilder, depth: Int) {
        val pad = indentStr.repeat(depth)
        when (stmt) {
            is HirBlock -> printBlock(stmt, sb, depth)
            is HirExprStmt -> sb.appendLine("${pad}${printExpr(stmt.expression)}")
            is HirReturnStmt -> {
                val value = stmt.value?.let { " ${printExpr(it)}" } ?: ""
                sb.appendLine("${pad}return$value")
            }
            is HirLetStmt -> {
                val mut = if (stmt.isMutable) "mut " else ""
                sb.appendLine("${pad}let $mut${stmt.name} = ${printExpr(stmt.initializer)}")
            }
            is HirIfStmt -> {
                sb.appendLine("${pad}if ${printExpr(stmt.condition)}")
                printBlock(stmt.thenBranch, sb, depth)
                if (stmt.elseBranch != null) {
                    sb.append("${pad}else ")
                    printStmt(stmt.elseBranch, sb, depth)
                }
            }
            is HirWhileStmt -> {
                sb.appendLine("${pad}while ${printExpr(stmt.condition)}")
                printBlock(stmt.body, sb, depth)
            }
            is HirForStmt -> {
                sb.appendLine("${pad}for ${stmt.variable} in ${printExpr(stmt.iterable)}")
                printBlock(stmt.body, sb, depth)
            }
            is HirBreakStmt -> sb.appendLine("${pad}break")
            is HirContinueStmt -> sb.appendLine("${pad}continue")
        }
    }

    private fun printExpr(expr: HirExpr): String = when (expr) {
        is HirIntLiteral -> expr.value
        is HirFloatLiteral -> expr.value
        is HirStringLiteral -> "\"${expr.value}\""
        is HirCharLiteral -> "'${expr.value}'"
        is HirBoolLiteral -> expr.value.toString()
        is HirNilLiteral -> "nil"
        is HirIdentifier -> expr.name
        is HirBinary -> "(${printExpr(expr.left)} ${expr.operator} ${printExpr(expr.right)})"
        is HirUnary -> "(${expr.operator}${printExpr(expr.operand)})"
        is HirCall -> {
            val callee = printExpr(expr.callee)
            val args = expr.arguments.joinToString(", ") { printExpr(it) }
            "$callee($args)"
        }
        is HirIndex -> "${printExpr(expr.callee)}[${printExpr(expr.index)}]"
        is HirFieldAccess -> "${printExpr(expr.callee)}.${expr.fieldName}"
        is HirSafeFieldAccess -> "${printExpr(expr.callee)}?.${expr.fieldName}"
        is HirNullAssert -> "${printExpr(expr.operand)}!!"
        is HirArrayLiteral -> "[${expr.elements.joinToString(", ") { printExpr(it) }}]"
        is HirArrayInit -> ".[${printExpr(expr.size)}]"
        is HirIfExpr -> {
            val elsePart = expr.elseBranch?.let { " else ${printExpr(it)}" } ?: ""
            "if ${printExpr(expr.condition)} { ${printExpr(expr.thenBranch)} }$elsePart"
        }
        is HirAssignment -> "${printExpr(expr.target)} = ${printExpr(expr.value)}"
        is HirCompoundAssignment -> "${printExpr(expr.target)} ${expr.operator}= ${printExpr(expr.value)}"
    }
}
