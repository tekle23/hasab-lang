package hasab.compiler.hir

import hasab.compiler.frontend.ast.*
import hasab.compiler.types.*
import hasab.compiler.frontend.ast.ArrayType as AstArrayType
import hasab.compiler.frontend.ast.FunctionType as AstFunctionType
import hasab.compiler.frontend.ast.OptionalType as AstOptionalType
import hasab.compiler.frontend.ast.PointerType as AstPointerType
import hasab.compiler.frontend.ast.VoidType as AstVoidType

/**
 * Lowers a type-checked AST module into an HIR module.
 *
 * Requires a pre-built [TypeEnvironment] with all type bindings
 * (produced by [TypeChecker]).
 *
 * The lowering is a single pass: every AST node is visited exactly once,
 * and the resolved type is read from the environment.
 * Expressions are lowered bottom-up (leaves first), so child types
 * are available before parent types are computed.
 */
public class AstToHirLowering(private val env: TypeEnvironment) {

    public fun lower(module: Module): HirModule {
        val decls = module.declarations.mapNotNull { lowerDecl(it) }
        return HirModule(name = module.name, declarations = decls)
    }

    // ---- Declarations ----

    private fun lowerDecl(decl: Decl): HirDecl? = when (decl) {
        is FnDecl -> lowerFnDecl(decl)
        is StructDecl -> lowerStructDecl(decl)
        is EnumDecl -> lowerEnumDecl(decl)
        is TraitDecl -> lowerTraitDecl(decl)
        is TypeAliasDecl -> lowerTypeAliasDecl(decl)
        is ImplDecl -> lowerImplDecl(decl)
        is ModDecl -> null
        is UseDecl -> HirUseDecl(path = decl.path, isPublic = decl.isPublic)
        is PubDecl -> lowerDecl(decl.inner)
    }

    private fun lowerFnDecl(decl: FnDecl): HirFnDecl {
        val paramTypes = decl.parameters.map { param ->
            param.type?.let { resolveTypeNode(it) } ?: TypeVariable(0)
        }
        val retType = decl.returnType?.let { resolveTypeNode(it) } ?: VoidType
        val fnType = FunctionType(paramTypes, retType)

        var bodyEnv = env.enterScope()
        for (param in decl.parameters) {
            val pt = param.type?.let { resolveTypeNode(it) } ?: TypeVariable(0)
            bodyEnv = bodyEnv.define(param.name, pt)
        }
        val body = decl.body?.let { lowerBlock(it, bodyEnv) }

        return HirFnDecl(
            name = decl.name,
            parameters = decl.parameters.mapIndexed { i, param ->
                HirParam(
                    name = param.name,
                    type = paramTypes[i],
                    isMutable = param.isMutable,
                )
            },
            type = fnType,
            returnType = retType,
            body = body,
            isPublic = decl.isPublic,
        )
    }

    private fun lowerStructDecl(decl: StructDecl): HirStructDecl {
        return HirStructDecl(
            name = decl.name,
            fields = decl.fields.map { field ->
                HirField(
                    name = field.name,
                    type = resolveTypeNode(field.type),
                    isMutable = field.isMutable,
                )
            },
            isPublic = decl.isPublic,
        )
    }

    private fun lowerEnumDecl(decl: EnumDecl): HirEnumDecl {
        return HirEnumDecl(
            name = decl.name,
            variants = decl.variants.map { variant ->
                HirEnumVariant(
                    name = variant.name,
                    fieldTypes = variant.fields.map { resolveTypeNode(it.type) },
                )
            },
            isPublic = decl.isPublic,
        )
    }

    private fun lowerTraitDecl(decl: TraitDecl): HirTraitDecl {
        return HirTraitDecl(
            name = decl.name,
            methods = decl.methods.map { lowerFnDecl(it) },
            isPublic = decl.isPublic,
        )
    }

    private fun lowerTypeAliasDecl(decl: TypeAliasDecl): HirTypeAliasDecl {
        return HirTypeAliasDecl(
            name = decl.name,
            targetType = resolveTypeNode(decl.target),
            isPublic = decl.isPublic,
        )
    }

    private fun lowerImplDecl(decl: ImplDecl): HirImplDecl {
        val targetType = resolveTypeNode(decl.targetType)
        return HirImplDecl(
            targetType = targetType,
            methods = decl.methods.map { lowerFnDecl(it) },
        )
    }

    // ---- Statements ----

    private fun lowerBlock(block: Block, blockEnv: TypeEnvironment): HirBlock {
        var currentEnv = blockEnv
        val stmts = mutableListOf<HirStmt>()
        for (stmt in block.statements) {
            val (hirStmt, newEnv) = lowerStatement(stmt, currentEnv)
            stmts.add(hirStmt)
            currentEnv = newEnv
        }
        return HirBlock(statements = stmts)
    }

    private data class LowerStmtResult(val stmt: HirStmt, val env: TypeEnvironment)

    private fun lowerStatement(stmt: Stmt, currentEnv: TypeEnvironment): LowerStmtResult {
        return when (stmt) {
            is ExprStmt -> {
                LowerStmtResult(HirExprStmt(lowerExpr(stmt.expression, currentEnv)), currentEnv)
            }
            is ReturnStmt -> {
                val value = stmt.value?.let { lowerExpr(it, currentEnv) }
                val retType = value?.type ?: VoidType
                LowerStmtResult(HirReturnStmt(value = value, type = retType), currentEnv)
            }
            is BreakStmt -> LowerStmtResult(HirBreakStmt(), currentEnv)
            is ContinueStmt -> LowerStmtResult(HirContinueStmt(), currentEnv)
            is LetStmt -> {
                val initType = lowerExpr(stmt.initializer, currentEnv).type
                val declaredType = stmt.typeAnnotation?.let { resolveTypeNode(it) }
                val varType = declaredType ?: initType
                val newEnv = currentEnv.define(stmt.name, varType)
                LowerStmtResult(
                    HirLetStmt(
                        name = stmt.name,
                        initializer = lowerExpr(stmt.initializer, currentEnv),
                        isMutable = stmt.isMutable,
                        type = varType,
                    ),
                    newEnv,
                )
            }
            is IfStmt -> {
                val condType = lowerExpr(stmt.condition, currentEnv).type
                val thenBranch = lowerBlock(stmt.thenBranch, currentEnv)
                val elseBranch = stmt.elseBranch?.let { lowerStatement(it, currentEnv)?.stmt }
                LowerStmtResult(
                    HirIfStmt(
                        condition = lowerExpr(stmt.condition, currentEnv),
                        thenBranch = thenBranch,
                        elseBranch = elseBranch,
                        type = condType,
                    ),
                    currentEnv,
                )
            }
            is WhileStmt -> {
                LowerStmtResult(
                    HirWhileStmt(
                        condition = lowerExpr(stmt.condition, currentEnv),
                        body = lowerBlock(stmt.body, currentEnv),
                    ),
                    currentEnv,
                )
            }
            is ForStmt -> {
                val iterableExpr = lowerExpr(stmt.iterable, currentEnv)
                val elementType = when (val iterType = iterableExpr.type) {
                    is ArrayType -> iterType.elementType
                    else -> UnknownType
                }
                val loopEnv = currentEnv.define(stmt.variable, elementType)
                LowerStmtResult(
                    HirForStmt(
                        variable = stmt.variable,
                        variableType = elementType,
                        iterable = iterableExpr,
                        body = lowerBlock(stmt.body, loopEnv),
                    ),
                    currentEnv,
                )
            }
            is Block -> {
                LowerStmtResult(lowerBlock(stmt, currentEnv), currentEnv)
            }
        }
    }

    // ---- Expressions ----

    private fun lowerExpr(expr: Expr, env: TypeEnvironment): HirExpr = when (expr) {
        is IntegerLiteralExpr -> HirIntLiteral(value = expr.value)
        is FloatLiteralExpr -> HirFloatLiteral(value = expr.value)
        is StringLiteralExpr -> HirStringLiteral(value = expr.value)
        is CharLiteralExpr -> HirCharLiteral(value = expr.value)
        is BoolLiteralExpr -> HirBoolLiteral(value = expr.value)
        is NilLiteralExpr -> {
            val resolved = env.lookup("nil") ?: NilLiteralType
            HirNilLiteral(type = resolved)
        }
        is IdentifierExpr -> {
            val resolvedType = env.lookup(expr.name) ?: UnknownType
            HirIdentifier(name = expr.name, type = resolvedType)
        }
        is BinaryExpr -> {
            val left = lowerExpr(expr.left, env)
            val right = lowerExpr(expr.right, env)
            val resultType = inferBinaryResultType(expr.operator, left.type, right.type)
            HirBinary(left = left, operator = expr.operator, right = right, type = resultType)
        }
        is UnaryExpr -> {
            val operand = lowerExpr(expr.operand, env)
            val resultType = inferUnaryResultType(expr.operator, operand.type)
            HirUnary(operator = expr.operator, operand = operand, type = resultType)
        }
        is CallExpr -> {
            val callee = lowerExpr(expr.callee, env)
            val args = expr.arguments.map { lowerExpr(it, env) }
            val returnType = when (val ct = callee.type) {
                is FunctionType -> ct.returnType
                else -> UnknownType
            }
            HirCall(callee = callee, arguments = args, type = returnType)
        }
        is IndexExpr -> {
            val callee = lowerExpr(expr.callee, env)
            val index = lowerExpr(expr.index, env)
            val resultType = when (val ct = callee.type) {
                is ArrayType -> OptionalType(ct.elementType)
                is StringType -> CharType
                else -> UnknownType
            }
            HirIndex(callee = callee, index = index, type = resultType)
        }
        is FieldAccessExpr -> {
            val callee = lowerExpr(expr.callee, env)
            val fieldType = when (val ct = callee.type) {
                is StructType -> ct.fieldByName(expr.fieldName)?.type ?: UnknownType
                is PointerType -> (ct.elementType as? StructType)?.fieldByName(expr.fieldName)?.type ?: UnknownType
                else -> UnknownType
            }
            HirFieldAccess(callee = callee, fieldName = expr.fieldName, type = fieldType)
        }
        is SafeFieldAccessExpr -> {
            val callee = lowerExpr(expr.callee, env)
            val unwrappedType = when (val ct = callee.type) {
                is OptionalType -> ct.elementType
                is PointerType -> ct.elementType
                else -> ct
            }
            val fieldType = when (val ut = unwrappedType) {
                is StructType -> ut.fieldByName(expr.fieldName)?.type ?: UnknownType
                is PointerType -> (ut.elementType as? StructType)?.fieldByName(expr.fieldName)?.type ?: UnknownType
                else -> UnknownType
            }
            HirSafeFieldAccess(callee = callee, fieldName = expr.fieldName, type = OptionalType(fieldType))
        }
        is NullAssertExpr -> {
            val operand = lowerExpr(expr.operand, env)
            val opType = operand.type
            val unwrappedType = when (opType) {
                is OptionalType -> opType.elementType
                else -> opType
            }
            HirNullAssert(operand = operand, type = unwrappedType)
        }
        is ParenExpr -> lowerExpr(expr.inner, env)
        is ArrayLiteralExpr -> {
            val elements = expr.elements.map { lowerExpr(it, env) }
            val elemType = elements.firstOrNull()?.type ?: TypeVariable(0)
            HirArrayLiteral(elements = elements, type = ArrayType(elemType))
        }
        is ArrayInitExpr -> {
            val sizeExpr = lowerExpr(expr.size, env)
            val elemType = expr.elementType?.let { resolveTypeNode(it) } ?: TypeVariable(0)
            HirArrayInit(size = sizeExpr, type = ArrayType(elemType))
        }
        is IfExpr -> {
            val cond = lowerExpr(expr.condition, env)
            val thenExpr = lowerExpr(expr.thenBranch, env)
            val elseExpr = expr.elseBranch?.let { lowerExpr(it, env) }
            val resultType = when {
                elseExpr == null -> OptionalType(thenExpr.type)
                else -> thenExpr.type
            }
            HirIfExpr(condition = cond, thenBranch = thenExpr, elseBranch = elseExpr, type = resultType)
        }
        is AssignmentExpr -> {
            val target = lowerExpr(expr.target, env)
            val value = lowerExpr(expr.value, env)
            HirAssignment(target = target, value = value, type = target.type)
        }
        is CompoundAssignmentExpr -> {
            val target = lowerExpr(expr.target, env)
            val value = lowerExpr(expr.value, env)
            HirCompoundAssignment(target = target, operator = expr.operator, value = value, type = target.type)
        }
    }

    // ---- Type resolution ----

    private fun resolveTypeNode(node: TypeNode): Type = when (node) {
        is IdentifierType -> env.lookup(node.name) ?: UnknownType
        is QualifiedType -> env.lookup(node.path.last()) ?: UnknownType
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

    // ---- Binary / unary type inference (simplified, mirrors TypeChecker) ----

    private fun inferBinaryResultType(op: String, left: Type, right: Type): Type = when (op) {
        "+", "-", "*", "/", "%" -> when {
            left == IntType && right == IntType -> IntType
            left == FloatType && right == FloatType -> FloatType
            left == IntType && right == FloatType -> FloatType
            left == FloatType && right == IntType -> FloatType
            left == StringType && right == StringType -> StringType
            else -> UnknownType
        }
        "==", "!=", "<", ">", "<=", ">=" -> BoolType
        "&&", "||" -> BoolType
        "&", "|", "^", "<<", ">>" -> IntType
        else -> UnknownType
    }

    private fun inferUnaryResultType(op: String, operand: Type): Type = when (op) {
        "-", "+" -> operand
        "!" -> BoolType
        "*" -> when (operand) {
            is PointerType -> operand.elementType
            else -> UnknownType
        }
        "&" -> PointerType(operand)
        else -> UnknownType
    }
}
