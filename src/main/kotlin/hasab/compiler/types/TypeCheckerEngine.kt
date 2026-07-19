package hasab.compiler.types

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.ast.ArrayType as AstArrayType
import hasab.compiler.frontend.ast.FunctionType as AstFunctionType
import hasab.compiler.frontend.ast.OptionalType as AstOptionalType
import hasab.compiler.frontend.ast.PointerType as AstPointerType
import hasab.compiler.frontend.ast.VoidType as AstVoidType

/**
 * Orchestrates all type-checking components in a multi-pass AST walk.
 *
 * Passes:
 * 1. **Declaration Collection** — Register all top-level type names and function signatures.
 * 2. **Type Resolution** — Resolve all type annotations to concrete [Type] instances.
 * 3. **Expression Inference** — Infer and validate types for all expressions.
 * 4. **Statement Validation** — Validate control flow, assignments, returns.
 * 5. **Trait Implementation** — Verify that structs implement all required trait methods.
 *
 * Delegates to specialized checkers:
 * - [TypeResolver] for type annotations
 * - [NumericPromotionRules] for type promotions
 * - [FunctionCallChecker] for call validation
 * - [FieldAccessChecker] for field access
 * - [NullSafetyChecker] for null safety
 * - [OverloadResolver] for overload resolution
 * - [GenericTypeChecker] for generic types
 * - [TraitImplementationChecker] for trait conformance
 *
 * Produces a [TypedSemanticModel] as output.
 */
public class TypeCheckerEngine {

    private lateinit var diagnostics: DiagnosticCollector
    private lateinit var model: TypedSemanticModel
    private lateinit var genericChecker: GenericTypeChecker
    private lateinit var env: TypeEnvironment
    private var insideLoop = false
    private val mutableVars: MutableSet<String> = hashSetOf()
    private var typeVarCounter = 0

    /** Collected trait declarations for pass 5 validation. */
    private val traitDeclarations: MutableList<TraitDecl> = mutableListOf()

    /** Collected struct declarations for pass 5 validation. */
    private val structDeclarations: MutableList<StructDecl> = mutableListOf()

    /** Collected impl declarations for pass 5 validation. */
    private val implDeclarations: MutableList<ImplDecl> = mutableListOf()

    /**
     * Type-check a module and produce a [TypedSemanticModel].
     *
     * Executes 5 focused passes:
     * 1. Collect declarations and register type names
     * 2. Resolve type annotations
     * 3. Infer and validate expression types
     * 4. Validate statements and control flow
     * 5. Verify trait implementations
     */
    public fun check(module: Module): TypedSemanticModel {
        diagnostics = DiagnosticCollector()
        model = TypedSemanticModel.empty()
        genericChecker = GenericTypeChecker(diagnostics)
        env = TypeEnvironment.root()
        insideLoop = false
        mutableVars.clear()
        typeVarCounter = 0
        traitDeclarations.clear()
        structDeclarations.clear()
        implDeclarations.clear()

        // Pass 1: Collect top-level declarations
        pass1CollectDeclarations(module.declarations)

        // Pass 2-4: Resolve types and validate expressions/statements
        pass2ResolveAndValidate(module.declarations)

        // Pass 5: Validate trait implementations
        pass5ValidateTraitImplementations()

        model.updateEnvironment(env)
        model.addDiagnostics(diagnostics.diagnostics())
        return model.snapshot()
    }

    // ---- Pass 1: Collect Declarations ----

    private fun pass1CollectDeclarations(decls: List<Decl>) {
        for (decl in decls) {
            when (decl) {
                is FnDecl -> pass1FnDecl(decl)
                is StructDecl -> {
                    pass1StructDecl(decl)
                    structDeclarations.add(decl)
                }
                is EnumDecl -> pass1EnumDecl(decl)
                is TraitDecl -> {
                    pass1TraitDecl(decl)
                    traitDeclarations.add(decl)
                }
                is TypeAliasDecl -> pass1TypeAliasDecl(decl)
                is ImplDecl -> {
                    implDeclarations.add(decl)
                }
                is ModDecl -> decl.body?.let { pass1CollectDeclarations(it) }
                is UseDecl -> { /* no-op */ }
                is PubDecl -> pass1CollectDeclarations(listOf(decl.inner))
            }
        }
    }

    private fun pass1FnDecl(decl: FnDecl) {
        val paramTypes = decl.parameters.map { param ->
            param.type?.let { TypeResolver.resolve(it, env) } ?: TypeVariable(nextVarId())
        }
        val retType = decl.returnType?.let { TypeResolver.resolve(it, env) } ?: VoidType
        val fnType = FunctionType(paramTypes, retType)
        env = env.define(decl.name, fnType)
        model.recordType(decl, fnType)
        model.addFunctionOverload(decl.name, fnType)
    }

    private fun pass1StructDecl(decl: StructDecl) {
        val structType = StructType(decl.name, emptyList())
        env = env.define(decl.name, structType)
        model.recordType(decl, structType)
    }

    private fun pass1EnumDecl(decl: EnumDecl) {
        val enumType = EnumType(decl.name, emptyList())
        env = env.define(decl.name, enumType)
        model.recordType(decl, enumType)
    }

    private fun pass1TraitDecl(decl: TraitDecl) {
        val traitType = TraitType(decl.name, emptyList())
        env = env.define(decl.name, traitType)
        model.recordType(decl, traitType)
    }

    private fun pass1TypeAliasDecl(decl: TypeAliasDecl) {
        val aliasType = TypeAliasType(decl.name, UnknownType)
        env = env.define(decl.name, aliasType)
        model.recordType(decl, aliasType)
    }

    // ---- Pass 2-4: Resolve and Validate ----

    private fun pass2ResolveAndValidate(decls: List<Decl>) {
        for (decl in decls) {
            when (decl) {
                is FnDecl -> resolveAndValidateFnDecl(decl)
                is StructDecl -> resolveAndValidateStructDecl(decl)
                is EnumDecl -> resolveAndValidateEnumDecl(decl)
                is TraitDecl -> resolveAndValidateTraitDecl(decl)
                is TypeAliasDecl -> resolveAndValidateTypeAliasDecl(decl)
                is ImplDecl -> resolveAndValidateImplDecl(decl)
                is ModDecl -> decl.body?.let { pass2ResolveAndValidate(it) }
                is UseDecl -> { /* no-op */ }
                is PubDecl -> pass2ResolveAndValidate(listOf(decl.inner))
            }
        }
    }

    private fun resolveAndValidateFnDecl(decl: FnDecl) {
        val paramTypes = decl.parameters.map { param ->
            param.type?.let { resolveType(it) } ?: TypeVariable(nextVarId())
        }
        val retType = decl.returnType?.let { resolveType(it) } ?: VoidType
        val fnType = FunctionType(paramTypes, retType)

        env = env.define(decl.name, fnType)
        model.recordType(decl, fnType)

        val savedEnv = env
        env = env.enterScope()
        for ((i, param) in decl.parameters.withIndex()) {
            env = env.define(param.name, paramTypes[i])
            if (param.isMutable || param.name == "self") {
                mutableVars.add(param.name)
                model.addMutableVar(param.name)
            }
        }
        decl.body?.let { checkBlock(it, retType) }
        env = savedEnv
    }

    private fun resolveAndValidateStructDecl(decl: StructDecl) {
        val fields = decl.fields.map { field ->
            StructTypeField(field.name, resolveType(field.type), field.isMutable)
        }
        val structType = StructType(decl.name, fields)
        env = env.define(decl.name, structType)
        model.recordType(decl, structType)
    }

    private fun resolveAndValidateEnumDecl(decl: EnumDecl) {
        val variants = decl.variants.map { variant ->
            val fieldTypes = variant.fields.map { resolveType(it.type) }
            EnumTypeVariant(variant.name, fieldTypes)
        }
        val enumType = EnumType(decl.name, variants)
        env = env.define(decl.name, enumType)
        model.recordType(decl, enumType)
    }

    private fun resolveAndValidateTraitDecl(decl: TraitDecl) {
        val methods = decl.methods.map { method ->
            val paramTypes = method.parameters.map { param ->
                param.type?.let { resolveType(it) } ?: TypeVariable(nextVarId())
            }
            val retType = method.returnType?.let { resolveType(it) } ?: VoidType
            TraitTypeMethod(method.name, paramTypes, retType)
        }
        val traitType = TraitType(decl.name, methods)
        env = env.define(decl.name, traitType)
        model.recordType(decl, traitType)
    }

    private fun resolveAndValidateTypeAliasDecl(decl: TypeAliasDecl) {
        val target = resolveType(decl.target)
        val aliasType = TypeAliasType(decl.name, target)
        env = env.define(decl.name, aliasType)
        model.recordType(decl, aliasType)
    }

    private fun resolveAndValidateImplDecl(decl: ImplDecl) {
        val targetType = resolveType(decl.targetType)
        for (method in decl.methods) {
            val paramTypes = method.parameters.map { param ->
                if (param.name == "self" && param.type == null) targetType
                else param.type?.let { resolveType(it) } ?: TypeVariable(nextVarId())
            }
            val retType = method.returnType?.let { resolveType(it) } ?: VoidType
            val fnType = FunctionType(paramTypes, retType)

            env = env.define(method.name, fnType)
            model.recordType(method, fnType)

            val savedEnv = env
            env = env.enterScope()
            for (param in method.parameters) {
                val paramType = if (param.name == "self" && param.type == null) targetType
                else param.type?.let { resolveType(it) } ?: TypeVariable(nextVarId())
                env = env.define(param.name, paramType)
                if (param.isMutable || param.name == "self") {
                    mutableVars.add(param.name)
                    model.addMutableVar(param.name)
                }
            }
            method.body?.let { checkBlock(it, retType) }
            env = savedEnv
        }
    }

    // ---- Pass 5: Trait Implementation Validation ----

    private fun pass5ValidateTraitImplementations() {
        for (impl in implDeclarations) {
            val targetType = resolveType(impl.targetType)
            if (targetType !is TraitType) continue

            val implementedMethods = impl.methods.map { it.name }.toSet()
            val requiredMethods = targetType.methods

            for (required in requiredMethods) {
                if (required.name !in implementedMethods) {
                    diagnostics.report(
                        TypeDiagnosticCode.MISSING_TRAIT_METHOD,
                        "Struct '${(targetType as? TraitType)?.name ?: targetType.displayName}' " +
                            "is missing required method '${required.name}' from trait '${targetType.displayName}'",
                        impl.range(),
                        impl.fileName,
                        suggestion = "Add method: ${required.name}(${required.parameterTypes.joinToString(", ") { it.displayName }}) -> ${required.returnType.displayName}",
                    )
                } else {
                    val implMethod = impl.methods.find { it.name == required.name }
                    if (implMethod != null) {
                        val implParamTypes = implMethod.parameters.map { param ->
                            if (param.name == "self") targetType
                            else param.type?.let { resolveType(it) } ?: UnknownType
                        }
                        val implRetType = implMethod.returnType?.let { resolveType(it) } ?: VoidType

                        if (implParamTypes.size != required.parameterTypes.size ||
                            !implParamTypes.zip(required.parameterTypes).all { (a, b) -> a == b } ||
                            implRetType != required.returnType
                        ) {
                            diagnostics.report(
                                TypeDiagnosticCode.TRAIT_METHOD_SIGNATURE,
                                "Method '${required.name}' signature does not match trait requirement",
                                implMethod.range(),
                                implMethod.fileName,
                                expectedType = FunctionType(required.parameterTypes, required.returnType),
                                foundType = FunctionType(implParamTypes, implRetType),
                                suggestion = "Expected: fn ${required.name}(${required.parameterTypes.joinToString(", ") { it.displayName }}) -> ${required.returnType.displayName}",
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- Block / Statement checking (same as before) ----

    private fun checkBlock(block: Block, currentFnReturnType: Type?) {
        val savedEnv = env
        env = env.enterScope()
        for (stmt in block.statements) {
            checkStatement(stmt, currentFnReturnType)
        }
        env = savedEnv
    }

    private fun checkStatement(stmt: Stmt, currentFnReturnType: Type?) {
        when (stmt) {
            is ExprStmt -> {
                val type = inferExpr(stmt.expression)
                model.recordType(stmt.expression, type)
                model.recordType(stmt, type)
            }
            is ReturnStmt -> checkReturn(stmt, currentFnReturnType)
            is BreakStmt -> {
                if (!insideLoop) {
                    diagnostics.report(
                        TypeDiagnosticCode.CANNOT_ITERATE,
                        "'break' outside of loop",
                        stmt.range(),
                        stmt.fileName,
                        hint = "Move 'break' inside a 'while' or 'for' loop",
                    )
                }
                model.recordType(stmt, VoidType)
            }
            is ContinueStmt -> {
                if (!insideLoop) {
                    diagnostics.report(
                        TypeDiagnosticCode.CANNOT_ITERATE,
                        "'continue' outside of loop",
                        stmt.range(),
                        stmt.fileName,
                        hint = "Move 'continue' inside a 'while' or 'for' loop",
                    )
                }
                model.recordType(stmt, VoidType)
            }
            is LetStmt -> checkLet(stmt)
            is IfStmt -> checkIfStmt(stmt, currentFnReturnType)
            is WhileStmt -> checkWhile(stmt, currentFnReturnType)
            is ForStmt -> checkFor(stmt, currentFnReturnType)
            is Block -> checkBlock(stmt, currentFnReturnType)
        }
    }

    private fun checkReturn(stmt: ReturnStmt, currentFnReturnType: Type?) {
        if (stmt.value != null) {
            val valueType = inferExpr(stmt.value)
            model.recordType(stmt.value, valueType)
            if (currentFnReturnType == null || currentFnReturnType == VoidType) {
                diagnostics.report(
                    TypeDiagnosticCode.RETURN_TYPE_MISMATCH,
                    "Cannot return a value from a void function",
                    stmt.range(),
                    stmt.fileName,
                    expectedType = VoidType,
                    foundType = valueType,
                    hint = "Remove the return value, or declare a return type",
                )
            } else {
                NullSafetyChecker.checkReturnType(valueType, currentFnReturnType, stmt.range(), stmt.fileName, diagnostics)
            }
            model.recordType(stmt, valueType)
        } else {
            val expected = currentFnReturnType ?: VoidType
            if (expected != VoidType) {
                diagnostics.report(
                    TypeDiagnosticCode.RETURN_TYPE_MISMATCH,
                    "Expected return type '${expected.displayName}', got 'void'",
                    stmt.range(),
                    stmt.fileName,
                    expectedType = expected,
                    foundType = VoidType,
                )
            }
            model.recordType(stmt, VoidType)
        }
    }

    private fun checkLet(stmt: LetStmt) {
        if (env.hasCurrent(stmt.name)) {
                diagnostics.report(
                    TypeDiagnosticCode.UNDEFINED_VARIABLE,
                    "Variable '${stmt.name}' is already defined in this scope",
                    stmt.range(),
                    stmt.fileName,
                    hint = "Use a different variable name, or remove the duplicate declaration",
                )
        }

        val initType = inferExpr(stmt.initializer)
        model.recordType(stmt.initializer, initType)

        NullSafetyChecker.checkNilWithoutAnnotation(
            initType, stmt.typeAnnotation != null, stmt.range(), stmt.fileName, diagnostics
        )

        val declaredType = stmt.typeAnnotation?.let { resolveType(it) }
        if (declaredType != null && initType != UnknownType && declaredType != UnknownType) {
            if (!areTypesCompatible(initType, declaredType)) {
                diagnostics.report(
                    TypeDiagnosticCode.CANNOT_ASSIGN_TYPE,
                    "Cannot assign '${initType.displayName}' to variable of type '${declaredType.displayName}'",
                    stmt.range(),
                    stmt.fileName,
                    expectedType = declaredType,
                    foundType = initType,
                )
            }
        }

        val varType = declaredType ?: initType
        env = env.define(stmt.name, varType)
        if (stmt.isMutable) {
            mutableVars.add(stmt.name)
            model.addMutableVar(stmt.name)
        }
        model.recordType(stmt, varType)
    }

    private fun checkIfStmt(stmt: IfStmt, currentFnReturnType: Type?) {
        val condType = inferExpr(stmt.condition)
        model.recordType(stmt.condition, condType)
        NullSafetyChecker.checkCondition(stmt.condition, condType, diagnostics)

        checkBlock(stmt.thenBranch, currentFnReturnType)
        stmt.elseBranch?.let { checkStatement(it, currentFnReturnType) }
        model.recordType(stmt, VoidType)
    }

    private fun checkWhile(stmt: WhileStmt, currentFnReturnType: Type?) {
        val condType = inferExpr(stmt.condition)
        model.recordType(stmt.condition, condType)
        NullSafetyChecker.checkCondition(stmt.condition, condType, diagnostics)

        val savedLoop = insideLoop
        insideLoop = true
        checkBlock(stmt.body, currentFnReturnType)
        insideLoop = savedLoop
        model.recordType(stmt, VoidType)
    }

    private fun checkFor(stmt: ForStmt, currentFnReturnType: Type?) {
        val iterableType = inferExpr(stmt.iterable)
        model.recordType(stmt.iterable, iterableType)

        val elementType = when (iterableType) {
            is ArrayType -> iterableType.elementType
            else -> {
                diagnostics.report(
                    TypeDiagnosticCode.CANNOT_ITERATE,
                    "Cannot iterate over '${iterableType.displayName}'",
                    stmt.iterable.range(),
                    stmt.fileName,
                    expectedType = ArrayType(TypeVariable(nextVarId())),
                    foundType = iterableType,
                    hint = "Only arrays can be iterated with 'for'",
                )
                UnknownType
            }
        }

        val savedEnv = env
        env = env.define(stmt.variable, elementType)
        val savedLoop = insideLoop
        insideLoop = true
        checkBlock(stmt.body, currentFnReturnType)
        insideLoop = savedLoop
        env = savedEnv
        model.recordType(stmt, VoidType)
    }

    // ---- Expression type inference ----

    private fun inferExpr(expr: Expr): Type {
        val type = when (expr) {
            is IntegerLiteralExpr -> IntType
            is FloatLiteralExpr -> FloatType
            is StringLiteralExpr -> StringType
            is CharLiteralExpr -> CharType
            is BoolLiteralExpr -> BoolType
            is NilLiteralExpr -> NilLiteralType
            is IdentifierExpr -> inferIdentifier(expr)
            is BinaryExpr -> inferBinary(expr)
            is UnaryExpr -> inferUnary(expr)
            is CallExpr -> inferCall(expr)
            is IndexExpr -> inferIndex(expr)
            is FieldAccessExpr -> inferFieldAccess(expr)
            is SafeFieldAccessExpr -> inferSafeFieldAccess(expr)
            is NullAssertExpr -> inferNullAssert(expr)
            is ParenExpr -> inferExpr(expr.inner)
            is ArrayLiteralExpr -> inferArrayLiteral(expr)
            is ArrayInitExpr -> inferArrayInit(expr)
            is IfExpr -> inferIfExpr(expr)
            is AssignmentExpr -> inferAssignment(expr)
            is CompoundAssignmentExpr -> inferCompoundAssignment(expr)
        }
        model.recordType(expr, type)
        return type
    }

    private fun inferIdentifier(expr: IdentifierExpr): Type {
        return env.lookup(expr.name) ?: run {
            diagnostics.report(
                TypeDiagnosticCode.UNDEFINED_VARIABLE,
                "Undefined variable '${expr.name}'",
                expr.range(),
                expr.fileName,
                hint = "Declare the variable before use, or check for typos",
            )
            UnknownType
        }
    }

    private fun inferBinary(expr: BinaryExpr): Type {
        val leftType = inferExpr(expr.left)
        val rightType = inferExpr(expr.right)
        val resultType = NumericPromotionRules.binaryResultType(expr.operator, leftType, rightType)

        return if (resultType != null) {
            resultType
        } else {
            diagnostics.report(
                TypeDiagnosticCode.ARITH_TYPE_ERROR,
                "Cannot apply '${expr.operator}' to '${leftType.displayName}' and '${rightType.displayName}'",
                expr.range(),
                expr.fileName,
                expectedType = leftType,
                foundType = rightType,
            )
            UnknownType
        }
    }

    private fun inferUnary(expr: UnaryExpr): Type {
        val operandType = inferExpr(expr.operand)
        val resultType = NumericPromotionRules.unaryResultType(expr.operator, operandType)

        return if (resultType != null) {
            resultType
        } else {
            diagnostics.report(
                TypeDiagnosticCode.UNARY_TYPE_ERROR,
                "Cannot apply '${expr.operator}' to '${operandType.displayName}'",
                expr.range(),
                expr.fileName,
                foundType = operandType,
            )
            UnknownType
        }
    }

    private fun inferCall(expr: CallExpr): Type {
        val calleeType = inferExpr(expr.callee)
        return FunctionCallChecker.checkCall(expr, calleeType, diagnostics)
    }

    private fun inferIndex(expr: IndexExpr): Type {
        val calleeType = inferExpr(expr.callee)
        val indexType = inferExpr(expr.index)

        if (indexType != IntType && indexType != UnknownType) {
            diagnostics.report(
                TypeDiagnosticCode.TYPE_MISMATCH,
                "Index must be 'int', got '${indexType.displayName}'",
                expr.index.range(),
                expr.index.fileName,
                expectedType = IntType,
                foundType = indexType,
            )
        }

        return when (calleeType) {
            is ArrayType -> calleeType.elementType
            is StringType -> CharType
            else -> {
                diagnostics.report(
                    TypeDiagnosticCode.NOT_INDEXABLE,
                    "Type '${calleeType.displayName}' is not indexable",
                    expr.range(),
                    expr.fileName,
                    foundType = calleeType,
                    hint = "Only arrays and strings can be indexed with '[]'",
                )
                UnknownType
            }
        }
    }

    private fun inferFieldAccess(expr: FieldAccessExpr): Type {
        val calleeType = inferExpr(expr.callee)
        return FieldAccessChecker.checkFieldAccess(expr, calleeType, diagnostics)
    }

    private fun inferSafeFieldAccess(expr: SafeFieldAccessExpr): Type {
        val calleeType = inferExpr(expr.callee)
        val unwrappedType = when (calleeType) {
            is OptionalType -> calleeType.elementType
            is PointerType -> calleeType.elementType
            else -> calleeType
        }
        return when (unwrappedType) {
            is StructType -> {
                val field = unwrappedType.fieldByName(expr.fieldName)
                if (field != null) OptionalType(field.type) else {
                    diagnostics.report(
                        TypeDiagnosticCode.NO_SUCH_FIELD,
                        "Struct '${unwrappedType.name}' has no field '${expr.fieldName}'",
                        expr.range(),
                        expr.fileName,
                        hint = if (unwrappedType.fields.isNotEmpty()) {
                            "Available fields: ${unwrappedType.fields.joinToString(", ") { it.name }}"
                        } else null,
                    )
                    UnknownType
                }
            }
            is PointerType -> {
                val innerStruct = unwrappedType.elementType as? StructType
                val field = innerStruct?.fieldByName(expr.fieldName)
                if (field != null) OptionalType(field.type) else {
                    diagnostics.report(
                        TypeDiagnosticCode.NO_SUCH_FIELD,
                        "Cannot access field '${expr.fieldName}' via safe navigation",
                        expr.range(),
                        expr.fileName,
                        hint = if (innerStruct != null && innerStruct.fields.isNotEmpty()) {
                            "Available fields: ${innerStruct.fields.joinToString(", ") { it.name }}"
                        } else "Check the struct definition for available fields",
                    )
                    UnknownType
                }
            }
            else -> {
                diagnostics.report(
                    TypeDiagnosticCode.NO_SUCH_FIELD,
                    "Cannot use safe navigation on '${calleeType.displayName}'",
                    expr.range(),
                    expr.fileName,
                    hint = "Safe navigation (?.) can only be used on struct, optional, or pointer types",
                )
                UnknownType
            }
        }
    }

    private fun inferNullAssert(expr: NullAssertExpr): Type {
        val operandType = inferExpr(expr.operand)
        return when (operandType) {
            is OptionalType -> operandType.elementType
            is NilLiteralType -> {
                diagnostics.report(
                    TypeDiagnosticCode.CANNOT_ASSIGN_TYPE,
                    "Cannot assert non-null on a value of type 'nil'",
                    expr.range(),
                    expr.fileName,
                    hint = "Ensure the value is not null before asserting",
                )
                UnknownType
            }
            else -> operandType
        }
    }

    private fun inferArrayLiteral(expr: ArrayLiteralExpr): Type {
        if (expr.elements.isEmpty()) return ArrayType(TypeVariable(nextVarId()))
        val elementType = inferExpr(expr.elements[0])
        for (i in 1 until expr.elements.size) {
            val elemType = inferExpr(expr.elements[i])
            if (!areTypesCompatible(elemType, elementType)) {
                diagnostics.report(
                    TypeDiagnosticCode.TYPE_MISMATCH,
                    "Array element ${i + 1}: expected '${elementType.displayName}', got '${elemType.displayName}'",
                    expr.elements[i].range(),
                    expr.fileName,
                    expectedType = elementType,
                    foundType = elemType,
                )
            }
        }
        return ArrayType(elementType)
    }

    private fun inferArrayInit(expr: ArrayInitExpr): Type {
        val sizeType = inferExpr(expr.size)
        if (sizeType != IntType && sizeType != UnknownType) {
            diagnostics.report(
                TypeDiagnosticCode.TYPE_MISMATCH,
                "Array size must be 'int', got '${sizeType.displayName}'",
                expr.size.range(),
                expr.fileName,
                expectedType = IntType,
                foundType = sizeType,
            )
        }
        val elementType = expr.elementType?.let { resolveType(it) } ?: TypeVariable(nextVarId())
        return ArrayType(elementType)
    }

    private fun inferIfExpr(expr: IfExpr): Type {
        val condType = inferExpr(expr.condition)
        NullSafetyChecker.checkCondition(expr.condition, condType, diagnostics)

        val thenType = inferExpr(expr.thenBranch)
        val elseType = expr.elseBranch?.let { inferExpr(it) }
        return when {
            elseType == null -> OptionalType(thenType)
            areTypesCompatible(thenType, elseType) -> NumericPromotionRules.commonBranchType(thenType, elseType)
            areTypesCompatible(elseType, thenType) -> NumericPromotionRules.commonBranchType(elseType, thenType)
            else -> {
                diagnostics.report(
                    TypeDiagnosticCode.TYPE_MISMATCH,
                    "If branches have incompatible types: '${thenType.displayName}' vs '${elseType.displayName}'",
                    expr.range(),
                    expr.fileName,
                    expectedType = thenType,
                    foundType = elseType,
                )
                thenType
            }
        }
    }

    private fun inferAssignment(expr: AssignmentExpr): Type {
        val targetType = inferExpr(expr.target)
        val valueType = inferExpr(expr.value)

        if (expr.target is IdentifierExpr && expr.target.name !in mutableVars) {
            diagnostics.report(
                TypeDiagnosticCode.MUTABILITY_VIOLATION,
                "Cannot assign to immutable variable '${expr.target.name}'",
                expr.range(),
                expr.fileName,
                hint = "Declare with 'mut' to allow reassignment",
            )
        }

        if (targetType != UnknownType && valueType != UnknownType) {
            if (!areTypesCompatible(valueType, targetType)) {
                diagnostics.report(
                    TypeDiagnosticCode.CANNOT_ASSIGN_TYPE,
                    "Cannot assign '${valueType.displayName}' to '${targetType.displayName}'",
                    expr.range(),
                    expr.fileName,
                    expectedType = targetType,
                    foundType = valueType,
                )
            }
        }

        return targetType
    }

    private fun inferCompoundAssignment(expr: CompoundAssignmentExpr): Type {
        val targetType = inferExpr(expr.target)
        val valueType = inferExpr(expr.value)

        if (expr.target is IdentifierExpr && expr.target.name !in mutableVars) {
            diagnostics.report(
                TypeDiagnosticCode.MUTABILITY_VIOLATION,
                "Cannot assign to immutable variable '${expr.target.name}'",
                expr.range(),
                expr.fileName,
                hint = "Declare with 'mut' to allow reassignment",
            )
        }

        if (targetType != UnknownType && valueType != UnknownType) {
            if (!areTypesCompatible(valueType, targetType)) {
                diagnostics.report(
                    TypeDiagnosticCode.CANNOT_ASSIGN_TYPE,
                    "Cannot assign '${valueType.displayName}' to '${targetType.displayName}'",
                    expr.range(),
                    expr.fileName,
                    expectedType = targetType,
                    foundType = valueType,
                )
            }
        }

        return targetType
    }

    // ---- Helpers ----

    private fun resolveType(node: TypeNode): Type {
        return TypeResolver.resolve(node, env) { name ->
            diagnostics.report(
                TypeDiagnosticCode.UNDEFINED_TYPE,
                "Unknown type '$name'",
                node.range(),
                node.fileName,
                hint = "Import the type or check for typos",
            )
        }
    }

    private fun areTypesCompatible(source: Type, target: Type): Boolean {
        if (source.isAssignableTo(target)) return true
        if (NumericPromotionRules.canPromote(source, target)) return true
        if (TypeCompatibilityMatrix.isImplicitlyConvertible(source, target)) return true
        // Unwrap type aliases for comparison
        val rawSource = if (source is TypeAliasType) source.target else source
        val rawTarget = if (target is TypeAliasType) target.target else target
        if (rawSource != source || rawTarget != target) {
            if (areTypesCompatible(rawSource, rawTarget)) return true
        }
        if (target is OptionalType) return source is NilLiteralType || source.isAssignableTo(target.elementType)
        if (source is OptionalType) return source.elementType.isAssignableTo(target)
        return false
    }

    private fun nextVarId(): Int = typeVarCounter++
}
