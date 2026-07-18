package hasab.compiler.types

import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.frontend.ast.*

public class TypeChecker(private val module: Module) {

    private val globalEnv = TypeEnvironment()
    private val diagnostics: MutableList<TypeDiagnostic> = mutableListOf()
    private var currentFunctionReturnType: ResolvedType? = null
    private var insideLoop: Boolean = false

    public fun check(): TypeCheckResult {
        BuiltinTypes.registerBuiltins(globalEnv)
        declareTypes(module.declarations)
        checkDeclarations(module.declarations)
        return TypeCheckResult(diagnostics.toList())
    }

    private fun report(typeDiag: TypeDiagnostic) {
        diagnostics.add(typeDiag)
    }

    private fun reportTypeError(message: String, node: AstNode, hint: String? = null) {
        report(TypeError(message, node.range(), node.fileName, hint))
    }

    private fun reportTypeErrorAt(message: String, fileName: String, line: Int, column: Int, startOffset: Int, endOffset: Int, hint: String? = null) {
        val range = SourceRange(SourcePosition(line, column, startOffset), SourcePosition(line, column, endOffset))
        report(TypeError(message, range, fileName, hint))
    }

    private fun resolveTypeNode(node: TypeNode): ResolvedType {
        val resolver = TypeResolver(globalEnv)
        val resolved = resolver.resolve(node)
        diagnostics.addAll(resolver.errors())
        return resolved
    }

    // ── Pass 1: Register type declarations ────────────────────────

    private fun declareTypes(decls: List<Decl>) {
        for (decl in decls) {
            when (decl) {
                is StructDecl -> declareStruct(decl)
                is EnumDecl -> declareEnum(decl)
                is TypeAliasDecl -> declareTypeAlias(decl)
                is TraitDecl -> declareTrait(decl)
                is ModDecl -> {
                    if (decl.body != null) declareTypes(decl.body)
                }
                is PubDecl -> declareTypes(listOf(decl.inner))
                else -> {}
            }
        }
    }

    private fun declareStruct(node: StructDecl) {
        val fields = linkedMapOf<String, ResolvedType>()
        for (field in node.fields) {
            val fieldType = resolveTypeNode(field.type)
            if (fieldType is ResolvedType.ErrorType) continue
            if (fields.containsKey(field.name)) {
                reportTypeErrorAt("Duplicate field '${field.name}' in struct '${node.name}'",
                    field.fileName, field.line, field.column, field.startOffset, field.endOffset)
                continue
            }
            fields[field.name] = fieldType
        }
        globalEnv.define(StructSymbol(node.name, fields))
    }

    private fun declareEnum(node: EnumDecl) {
        val variants = linkedMapOf<String, List<ResolvedType>>()
        for (variant in node.variants) {
            val fieldTypes = variant.fields.map { resolveTypeNode(it.type) }
            variants[variant.name] = fieldTypes
        }
        globalEnv.define(EnumSymbol(node.name, variants))
    }

    private fun declareTypeAlias(node: TypeAliasDecl) {
        val underlying = resolveTypeNode(node.target)
        globalEnv.define(TypeAliasSymbol(node.name, underlying))
    }

    private fun declareTrait(node: TraitDecl) {
        val methods = linkedMapOf<String, ResolvedType>()
        for (method in node.methods) {
            val paramTypes = method.parameters.map { fp ->
                if (fp.type != null) resolveTypeNode(fp.type) else ResolvedType.VoidType
            }
            val returnType = if (method.returnType != null) resolveTypeNode(method.returnType) else ResolvedType.VoidType
            methods[method.name] = ResolvedType.FunctionType(paramTypes, returnType)
        }
        globalEnv.define(TraitSymbol(node.name, methods))
    }

    // ── Pass 2: Check declarations ───────────────────────────────

    private fun checkDeclarations(decls: List<Decl>) {
        for (decl in decls) {
            checkDeclaration(decl)
        }
    }

    private fun checkDeclaration(decl: Decl) {
        when (decl) {
            is FnDecl -> checkFnDecl(decl)
            is StructDecl -> {} // already declared in pass 1
            is EnumDecl -> {} // already declared in pass 1
            is ImplDecl -> checkImplDecl(decl)
            is TraitDecl -> {} // already declared in pass 1
            is TypeAliasDecl -> {} // already declared in pass 1
            is ModDecl -> {
                if (decl.body != null) checkDeclarations(decl.body)
            }
            is UseDecl -> {} // no type checking needed in v1
            is PubDecl -> checkDeclaration(decl.inner)
        }
    }

    private fun checkFnDecl(node: FnDecl, selfType: ResolvedType? = null) {
        val paramTypes = node.parameters.map { fp ->
            if (fp.type != null) resolveTypeNode(fp.type) else ResolvedType.VoidType
        }
        val returnType = if (node.returnType != null) resolveTypeNode(node.returnType) else ResolvedType.VoidType
        val fnType = ResolvedType.FunctionType(paramTypes, returnType)

        val existingSymbol = globalEnv.lookupCurrent(node.name)
        if (existingSymbol != null) {
            reportTypeError("Duplicate declaration of '${node.name}'", node)
            return
        }

        globalEnv.define(FunctionSymbol(node.name, paramTypes, returnType))

        if (node.body != null) {
            val bodyEnv = globalEnv.enterScope()
            for ((i, fp) in node.parameters.withIndex()) {
                if (fp.name == "self") {
                    if (selfType != null && selfType !is ResolvedType.ErrorType) {
                        bodyEnv.define(VariableSymbol("self", selfType, true))
                    }
                    continue
                }
                val paramType = paramTypes[i]
                if (paramType is ResolvedType.ErrorType) continue
                if (bodyEnv.hasCurrent(fp.name)) {
                    reportTypeErrorAt("Duplicate parameter '${fp.name}'",
                        fp.fileName, fp.line, fp.column, fp.startOffset, fp.endOffset)
                    continue
                }
                bodyEnv.define(VariableSymbol(fp.name, paramType, fp.isMutable))
            }

            val prevReturnType = currentFunctionReturnType
            currentFunctionReturnType = returnType
            checkBlock(node.body, bodyEnv)
            currentFunctionReturnType = prevReturnType
        }
    }

    private fun checkImplDecl(node: ImplDecl) {
        val targetType = resolveTypeNode(node.targetType)
        for (method in node.methods) {
            checkFnDecl(method, selfType = targetType)
        }
    }

    // ── Statements ───────────────────────────────────────────────

    private fun checkBlock(block: Block, env: TypeEnvironment) {
        for (stmt in block.statements) {
            checkStatement(stmt, env)
        }
    }

    private fun checkStatement(stmt: Stmt, env: TypeEnvironment) {
        when (stmt) {
            is ExprStmt -> checkExpression(stmt.expression, env)
            is ReturnStmt -> checkReturnStmt(stmt, env)
            is BreakStmt -> {
                if (!insideLoop) {
                    reportTypeError("'break' outside of loop", stmt)
                }
            }
            is ContinueStmt -> {
                if (!insideLoop) {
                    reportTypeError("'continue' outside of loop", stmt)
                }
            }
            is LetStmt -> checkLetStmt(stmt, env)
            is IfStmt -> checkIfStmt(stmt, env)
            is WhileStmt -> checkWhileStmt(stmt, env)
            is ForStmt -> checkForStmt(stmt, env)
            is Block -> checkBlock(stmt, env)
        }
    }

    private fun checkReturnStmt(stmt: ReturnStmt, env: TypeEnvironment) {
        val expectedReturn = currentFunctionReturnType ?: ResolvedType.VoidType

        if (stmt.value != null) {
            val actualType = checkExpression(stmt.value, env)
            if (!isCompatible(actualType, expectedReturn)) {
                reportTypeError(
                    "Return type mismatch: expected '${expectedReturn.displayName()}', got '${actualType.displayName()}'",
                    stmt,
                    hint = "Function declares return type '${expectedReturn.displayName()}'",
                )
            }
        } else {
            if (expectedReturn !is ResolvedType.VoidType) {
                reportTypeError(
                    "Function requires return value of type '${expectedReturn.displayName()}'",
                    stmt,
                    hint = "Add a return expression or change return type to 'void'",
                )
            }
        }
    }

    private fun checkLetStmt(stmt: LetStmt, env: TypeEnvironment) {
        val initType = checkExpression(stmt.initializer, env)
        val varType: ResolvedType

        if (stmt.typeAnnotation != null) {
            val annotatedType = resolveTypeNode(stmt.typeAnnotation)
            if (annotatedType is ResolvedType.ErrorType) {
                varType = initType
            } else if (!isCompatible(initType, annotatedType)) {
                reportTypeError(
                    "Type mismatch: variable '${stmt.name}' declared as '${annotatedType.displayName()}' " +
                        "but initialized with '${initType.displayName()}'",
                    stmt,
                )
                varType = annotatedType
            } else {
                varType = annotatedType
            }
        } else {
            varType = initType
            if (varType is ResolvedType.NilType) {
                reportTypeError(
                    "Cannot infer type of '${stmt.name}' from 'nil' expression",
                    stmt,
                    hint = "Add a type annotation: let ${stmt.name}: Type = ...",
                )
            }
        }

        if (env.hasCurrent(stmt.name)) {
            reportTypeError("Variable '${stmt.name}' is already defined in this scope", stmt)
            return
        }

        env.define(VariableSymbol(stmt.name, varType, stmt.isMutable))
    }

    private fun checkIfStmt(stmt: IfStmt, env: TypeEnvironment) {
        val condType = checkExpression(stmt.condition, env)
        if (condType !is ResolvedType.BoolType && condType !is ResolvedType.ErrorType) {
            reportTypeError(
                "Condition must be 'bool', got '${condType.displayName()}'",
                stmt.condition,
            )
        }

        val thenEnv = env.enterScope()
        checkBlock(stmt.thenBranch, thenEnv)

        if (stmt.elseBranch != null) {
            when (stmt.elseBranch) {
                is Block -> {
                    val elseEnv = env.enterScope()
                    checkBlock(stmt.elseBranch, elseEnv)
                }
                is IfStmt -> checkIfStmt(stmt.elseBranch, env)
                else -> checkStatement(stmt.elseBranch, env)
            }
        }
    }

    private fun checkWhileStmt(stmt: WhileStmt, env: TypeEnvironment) {
        val condType = checkExpression(stmt.condition, env)
        if (condType !is ResolvedType.BoolType && condType !is ResolvedType.ErrorType) {
            reportTypeError(
                "Loop condition must be 'bool', got '${condType.displayName()}'",
                stmt.condition,
            )
        }

        val prevInsideLoop = insideLoop
        insideLoop = true
        val bodyEnv = env.enterScope()
        checkBlock(stmt.body, bodyEnv)
        insideLoop = prevInsideLoop
    }

    private fun checkForStmt(stmt: ForStmt, env: TypeEnvironment) {
        val iterableType = checkExpression(stmt.iterable, env)

        val elementType = when (iterableType) {
            is ResolvedType.ArrayType -> iterableType.elementType
            is ResolvedType.StringType -> ResolvedType.CharType
            is ResolvedType.ErrorType -> ResolvedType.ErrorType
            else -> {
                reportTypeError(
                    "Cannot iterate over '${iterableType.displayName()}'",
                    stmt.iterable,
                    hint = "Only arrays and strings are iterable",
                )
                ResolvedType.ErrorType
            }
        }

        val bodyEnv = env.enterScope()
        bodyEnv.define(VariableSymbol(stmt.variable, elementType, true))

        val prevInsideLoop = insideLoop
        insideLoop = true
        checkBlock(stmt.body, bodyEnv)
        insideLoop = prevInsideLoop
    }

    // ── Expressions ──────────────────────────────────────────────

    private fun checkExpression(expr: Expr, env: TypeEnvironment): ResolvedType {
        return when (expr) {
            is IntegerLiteralExpr -> ResolvedType.IntType
            is FloatLiteralExpr -> ResolvedType.FloatType
            is StringLiteralExpr -> ResolvedType.StringType
            is CharLiteralExpr -> ResolvedType.CharType
            is BoolLiteralExpr -> ResolvedType.BoolType
            is NilLiteralExpr -> ResolvedType.NilType
            is IdentifierExpr -> checkIdentifier(expr, env)
            is BinaryExpr -> checkBinaryExpr(expr, env)
            is UnaryExpr -> checkUnaryExpr(expr, env)
            is CallExpr -> checkCallExpr(expr, env)
            is IndexExpr -> checkIndexExpr(expr, env)
            is FieldAccessExpr -> checkFieldAccessExpr(expr, env)
            is ParenExpr -> checkExpression(expr.inner, env)
            is ArrayLiteralExpr -> checkArrayLiteral(expr, env)
            is ArrayInitExpr -> checkArrayInit(expr, env)
            is IfExpr -> checkIfExpr(expr, env)
            is AssignmentExpr -> checkAssignmentExpr(expr, env)
            is CompoundAssignmentExpr -> checkCompoundAssignment(expr, env)
        }
    }

    private fun checkIdentifier(expr: IdentifierExpr, env: TypeEnvironment): ResolvedType {
        val symbol = env.lookup(expr.name)
        if (symbol == null) {
            reportTypeError("Undefined variable '${expr.name}'", expr,
                hint = "Did you forget to declare '${expr.name}'?")
            return ResolvedType.ErrorType
        }
        return when (symbol) {
            is FunctionSymbol -> ResolvedType.FunctionType(symbol.parameterTypes, symbol.returnType)
            else -> symbol.type
        }
    }

    private fun checkBinaryExpr(expr: BinaryExpr, env: TypeEnvironment): ResolvedType {
        val leftType = checkExpression(expr.left, env)
        val rightType = checkExpression(expr.right, env)

        if (leftType is ResolvedType.ErrorType || rightType is ResolvedType.ErrorType) {
            return ResolvedType.ErrorType
        }

        return when (expr.operator) {
            "+", "-", "*", "/", "%" -> {
                if (leftType is ResolvedType.IntType && rightType is ResolvedType.IntType) {
                    ResolvedType.IntType
                } else if (leftType is ResolvedType.FloatType && rightType is ResolvedType.FloatType) {
                    ResolvedType.FloatType
                } else if (expr.operator == "+" && leftType is ResolvedType.StringType && rightType is ResolvedType.StringType) {
                    ResolvedType.StringType
                } else {
                    reportTypeError(
                        "Operator '${expr.operator}' cannot be applied to '${leftType.displayName()}' and '${rightType.displayName()}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            "==", "!=" -> {
                if (isComparable(leftType, rightType)) ResolvedType.BoolType
                else {
                    reportTypeError(
                        "Cannot compare '${leftType.displayName()}' and '${rightType.displayName()}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            "<", ">", "<=", ">=" -> {
                if (isOrdered(leftType, rightType)) ResolvedType.BoolType
                else {
                    reportTypeError(
                        "Cannot compare '${leftType.displayName()}' and '${rightType.displayName()}' with '${expr.operator}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            "&&", "||" -> {
                if (leftType is ResolvedType.BoolType && rightType is ResolvedType.BoolType) {
                    ResolvedType.BoolType
                } else {
                    reportTypeError(
                        "Operator '${expr.operator}' requires 'bool', got '${leftType.displayName()}' and '${rightType.displayName()}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            "&", "|", "^" -> {
                if (leftType is ResolvedType.IntType && rightType is ResolvedType.IntType) {
                    ResolvedType.IntType
                } else {
                    reportTypeError(
                        "Bitwise operator '${expr.operator}' requires 'int', got '${leftType.displayName()}' and '${rightType.displayName()}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            "<<", ">>" -> {
                if (leftType is ResolvedType.IntType && rightType is ResolvedType.IntType) {
                    ResolvedType.IntType
                } else {
                    reportTypeError(
                        "Shift operator '${expr.operator}' requires 'int', got '${leftType.displayName()}' and '${rightType.displayName()}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            "..", "..=" -> {
                if (leftType is ResolvedType.IntType && rightType is ResolvedType.IntType) {
                    ResolvedType.ArrayType(ResolvedType.IntType)
                } else {
                    reportTypeError(
                        "Range operator '${expr.operator}' requires 'int', got '${leftType.displayName()}' and '${rightType.displayName()}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            else -> {
                reportTypeError("Unknown operator '${expr.operator}'", expr)
                ResolvedType.ErrorType
            }
        }
    }

    private fun checkUnaryExpr(expr: UnaryExpr, env: TypeEnvironment): ResolvedType {
        val operandType = checkExpression(expr.operand, env)
        if (operandType is ResolvedType.ErrorType) return ResolvedType.ErrorType

        return when (expr.operator) {
            "-" -> {
                if (operandType is ResolvedType.IntType || operandType is ResolvedType.FloatType) {
                    operandType
                } else {
                    reportTypeError("Unary '-' cannot be applied to '${operandType.displayName()}'", expr)
                    ResolvedType.ErrorType
                }
            }
            "!" -> {
                if (operandType is ResolvedType.BoolType) {
                    ResolvedType.BoolType
                } else {
                    reportTypeError("Logical NOT '!' requires 'bool', got '${operandType.displayName()}'", expr)
                    ResolvedType.ErrorType
                }
            }
            "~" -> {
                if (operandType is ResolvedType.IntType) {
                    ResolvedType.IntType
                } else {
                    reportTypeError("Bitwise NOT '~' requires 'int', got '${operandType.displayName()}'", expr)
                    ResolvedType.ErrorType
                }
            }
            else -> {
                reportTypeError("Unknown unary operator '${expr.operator}'", expr)
                ResolvedType.ErrorType
            }
        }
    }

    private fun checkCallExpr(expr: CallExpr, env: TypeEnvironment): ResolvedType {
        val calleeType = checkExpression(expr.callee, env)
        if (calleeType is ResolvedType.ErrorType) return ResolvedType.ErrorType

        if (calleeType is ResolvedType.FunctionType) {
            if (expr.arguments.size != calleeType.parameterTypes.size) {
                reportTypeError(
                    "Expected ${calleeType.parameterTypes.size} arguments, got ${expr.arguments.size}",
                    expr,
                )
                return calleeType.returnType
            }
            for ((i, arg) in expr.arguments.withIndex()) {
                val argType = checkExpression(arg, env)
                val paramType = calleeType.parameterTypes[i]
                if (!isCompatible(argType, paramType)) {
                    reportTypeError(
                        "Argument ${i + 1}: expected '${paramType.displayName()}', got '${argType.displayName()}'",
                        arg,
                    )
                }
            }
            return calleeType.returnType
        }

        reportTypeError("Cannot call '${calleeType.displayName()}' — not a function", expr)
        return ResolvedType.ErrorType
    }

    private fun checkIndexExpr(expr: IndexExpr, env: TypeEnvironment): ResolvedType {
        val calleeType = checkExpression(expr.callee, env)
        val indexType = checkExpression(expr.index, env)

        if (calleeType is ResolvedType.ErrorType) return ResolvedType.ErrorType

        if (indexType !is ResolvedType.IntType && indexType !is ResolvedType.ErrorType) {
            reportTypeError("Array index must be 'int', got '${indexType.displayName()}'", expr.index)
        }

        return when (calleeType) {
            is ResolvedType.ArrayType -> calleeType.elementType
            is ResolvedType.PointerType -> calleeType.elementType
            else -> {
                reportTypeError("Cannot index into '${calleeType.displayName()}'", expr)
                ResolvedType.ErrorType
            }
        }
    }

    private fun checkFieldAccessExpr(expr: FieldAccessExpr, env: TypeEnvironment): ResolvedType {
        val calleeType = checkExpression(expr.callee, env)
        if (calleeType is ResolvedType.ErrorType) return ResolvedType.ErrorType

        return when (calleeType) {
            is ResolvedType.StructType -> {
                val fieldType = calleeType.fields[expr.fieldName]
                if (fieldType == null) {
                    reportTypeError(
                        "Struct '${calleeType.name}' has no field '${expr.fieldName}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                } else {
                    fieldType
                }
            }
            else -> {
                reportTypeError("Cannot access field '${expr.fieldName}' on '${calleeType.displayName()}'", expr)
                ResolvedType.ErrorType
            }
        }
    }

    private fun checkArrayLiteral(expr: ArrayLiteralExpr, env: TypeEnvironment): ResolvedType {
        if (expr.elements.isEmpty()) {
            reportTypeError(
                "Cannot infer array type from empty literal",
                expr,
                hint = "Use array init syntax: [Type; size]",
            )
            return ResolvedType.ErrorType
        }

        val firstType = checkExpression(expr.elements[0], env)
        for (i in 1 until expr.elements.size) {
            val elemType = checkExpression(expr.elements[i], env)
            if (!isCompatible(elemType, firstType)) {
                reportTypeError(
                    "Array element ${i + 1}: expected '${firstType.displayName()}', got '${elemType.displayName()}'",
                    expr.elements[i],
                )
            }
        }
        return ResolvedType.ArrayType(firstType)
    }

    private fun checkArrayInit(expr: ArrayInitExpr, env: TypeEnvironment): ResolvedType {
        val sizeType = checkExpression(expr.size, env)
        if (sizeType !is ResolvedType.IntType && sizeType !is ResolvedType.ErrorType) {
            reportTypeError("Array size must be 'int', got '${sizeType.displayName()}'", expr.size)
        }

        if (expr.elementType != null) {
            val elemType = resolveTypeNode(expr.elementType)
            return ResolvedType.ArrayType(elemType)
        }

        reportTypeError("Array init requires element type", expr)
        return ResolvedType.ErrorType
    }

    private fun checkIfExpr(expr: IfExpr, env: TypeEnvironment): ResolvedType {
        val condType = checkExpression(expr.condition, env)
        if (condType !is ResolvedType.BoolType && condType !is ResolvedType.ErrorType) {
            reportTypeError("Condition must be 'bool', got '${condType.displayName()}'", expr.condition)
        }

        val thenType = checkExpression(expr.thenBranch, env)

        if (expr.elseBranch != null) {
            val elseType = checkExpression(expr.elseBranch, env)
            if (isCompatible(thenType, elseType)) return thenType
            if (isCompatible(elseType, thenType)) return elseType
            reportTypeError(
                "If expression branches have incompatible types: '${thenType.displayName()}' vs '${elseType.displayName()}'",
                expr,
            )
            return ResolvedType.ErrorType
        }

        return ResolvedType.OptionalType(thenType)
    }

    private fun checkAssignmentExpr(expr: AssignmentExpr, env: TypeEnvironment): ResolvedType {
        val targetType = checkExpression(expr.target, env)
        val valueType = checkExpression(expr.value, env)

        if (targetType is ResolvedType.ErrorType || valueType is ResolvedType.ErrorType) {
            return ResolvedType.ErrorType
        }

        if (!isLValue(expr.target, env)) {
            reportTypeError("Cannot assign to non-assignable expression", expr.target)
            return ResolvedType.ErrorType
        }

        if (!isCompatible(valueType, targetType)) {
            reportTypeError(
                "Type mismatch: cannot assign '${valueType.displayName()}' to '${targetType.displayName()}'",
                expr,
            )
        }

        return targetType
    }

    private fun checkCompoundAssignment(expr: CompoundAssignmentExpr, env: TypeEnvironment): ResolvedType {
        val targetType = checkExpression(expr.target, env)
        val valueType = checkExpression(expr.value, env)

        if (targetType is ResolvedType.ErrorType || valueType is ResolvedType.ErrorType) {
            return ResolvedType.ErrorType
        }

        if (!isLValue(expr.target, env)) {
            reportTypeError("Cannot assign to non-assignable expression", expr.target)
            return ResolvedType.ErrorType
        }

        return when (expr.operator) {
            "+=", "-=", "*=", "/=", "%=" -> {
                if ((targetType is ResolvedType.IntType || targetType is ResolvedType.FloatType) &&
                    (valueType is ResolvedType.IntType || targetType == valueType)
                ) {
                    targetType
                } else {
                    reportTypeError(
                        "Operator '${expr.operator}' cannot be applied to '${targetType.displayName()}' and '${valueType.displayName()}'",
                        expr,
                    )
                    ResolvedType.ErrorType
                }
            }
            else -> {
                reportTypeError("Unknown compound assignment operator '${expr.operator}'", expr)
                ResolvedType.ErrorType
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private fun isLValue(expr: Expr, env: TypeEnvironment): Boolean {
        return when (expr) {
            is IdentifierExpr -> {
                val symbol = env.lookup(expr.name)
                symbol is VariableSymbol && symbol.isMutable
            }
            is FieldAccessExpr -> isLValue(expr.callee, env)
            is IndexExpr -> isLValue(expr.callee, env)
            else -> false
        }
    }

    private fun isCompatible(actual: ResolvedType, expected: ResolvedType): Boolean {
        if (actual == expected) return true
        if (actual is ResolvedType.ErrorType || expected is ResolvedType.ErrorType) return true
        if (actual is ResolvedType.NilType && expected is ResolvedType.OptionalType) return true
        if (actual is ResolvedType.NilType) return false
        if (actual is ResolvedType.IntType && expected is ResolvedType.FloatType) return true
        if (actual is ResolvedType.TypeAlias) return isCompatible(actual.underlying, expected)
        if (expected is ResolvedType.TypeAlias) return isCompatible(actual, expected.underlying)
        return false
    }

    private fun isComparable(left: ResolvedType, right: ResolvedType): Boolean {
        if (left is ResolvedType.ErrorType || right is ResolvedType.ErrorType) return true
        return left == right
    }

    private fun isOrdered(left: ResolvedType, right: ResolvedType): Boolean {
        if (left is ResolvedType.ErrorType || right is ResolvedType.ErrorType) return true
        return (left is ResolvedType.IntType && right is ResolvedType.IntType) ||
            (left is ResolvedType.FloatType && right is ResolvedType.FloatType) ||
            (left is ResolvedType.StringType && right is ResolvedType.StringType) ||
            (left is ResolvedType.CharType && right is ResolvedType.CharType)
    }
}
