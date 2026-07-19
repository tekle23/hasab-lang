package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange

/**
 * Multi-pass semantic analyzer that orchestrates all semantic components.
 *
 * Passes (in order):
 * 1. Module graph construction — discover modules
 * 2. Declaration collection — build symbol table
 * 3. Import resolution — resolve use/import statements
 * 4. Symbol resolution — verify all references are defined
 * 5. Visibility checking — enforce access modifiers
 * 6. Structural validation — break/continue in loops, etc.
 *
 * Usage:
 * ```
 * val analyzer = SemanticAnalyzer()
 * val model = analyzer.analyze(module)
 * if (model.hasErrors) { ... }
 * ```
 */
public class SemanticAnalyzer {

    private val diagnostics: MutableList<SemanticDiagnostic> = mutableListOf()

    public fun analyze(module: Module): SemanticModel {
        diagnostics.clear()
        val model = SemanticModel.empty()
        val scopeManager = ScopeManager()

        // ---- Pass 1: Module graph ----
        passModuleGraph(module, model)

        // ---- Pass 2: Declaration collection ----
        val collector = DeclarationCollector(scopeManager)
        val (table, declDiags) = collector.collect(module)
        diagnostics.addAll(declDiags)
        model.updateSymbolTable(table)
        model.updateScopeTree(scopeManager.buildScopeTree())

        // ---- Pass 3: Import resolution ----
        val importResolver = ImportResolver(table)
        importResolver.collectModuleNames(module)
        val importDiags = importResolver.resolve(module)
        diagnostics.addAll(importDiags)
        model.updateImports(importResolver.resolvedImports())

        // ---- Pass 4: Symbol resolution ----
        val symbolResolver = SymbolResolver(table, scopeManager)
        val resolveDiags = symbolResolver.resolve(module)
        diagnostics.addAll(resolveDiags)
        model.updateNodeBindings(symbolResolver.nodeBindings)

        // ---- Pass 5: Visibility checking ----
        val visibilityChecker = VisibilityChecker(table)
        val visDiags = visibilityChecker.check(module)
        diagnostics.addAll(visDiags)

        // ---- Pass 6: Structural validation ----
        val structDiags = validateStructure(module)
        diagnostics.addAll(structDiags)

        // ---- Build final model ----
        model.updateDiagnostics(diagnostics.toList())
        return model
    }

    /**
     * Analyze and return the older SemanticResult format for backward compatibility.
     */
    public fun analyzeToResult(module: Module): SemanticResult {
        return analyze(module).toResult()
    }

    // ---- Pass 1: Module graph ----

    private fun passModuleGraph(module: Module, model: SemanticModel) {
        val packageResolver = PackageResolver()
        val pkgDiags = packageResolver.resolve(module)
        diagnostics.addAll(pkgDiags)
        model.updateModuleGraph(packageResolver.moduleGraph())

        // Check for circular dependencies
        val importResolver = ImportResolver(SymbolTable.EMPTY)
        val circularDiags = importResolver.checkCircularDeps(packageResolver.moduleGraph())
        diagnostics.addAll(circularDiags)
    }

    // ---- Pass 6: Structural validation ----

    private fun validateStructure(module: Module): List<SemanticDiagnostic> {
        val result = mutableListOf<SemanticDiagnostic>()
        validateDeclarations(module.declarations, result, insideLoop = false, currentFunctionReturnType = null)
        return result
    }

    private fun validateDeclarations(
        decls: List<Decl>,
        result: MutableList<SemanticDiagnostic>,
        insideLoop: Boolean,
        currentFunctionReturnType: String?,
    ) {
        for (decl in decls) {
            when (decl) {
                is FnDecl -> {
                    val retType = decl.returnType?.let { typeNodeToString(it) }
                    decl.body?.let {
                        validateBlock(it, result, insideLoop = false, currentFunctionReturnType = retType)
                    }
                }
                is StructDecl -> {}
                is EnumDecl -> {}
                is TraitDecl -> {
                    for (method in decl.methods) {
                        val retType = method.returnType?.let { typeNodeToString(it) }
                        method.body?.let {
                            validateBlock(it, result, insideLoop = false, currentFunctionReturnType = retType)
                        }
                    }
                }
                is TypeAliasDecl -> {}
                is ImplDecl -> {
                    for (method in decl.methods) {
                        val retType = method.returnType?.let { typeNodeToString(it) }
                        method.body?.let {
                            validateBlock(it, result, insideLoop = false, currentFunctionReturnType = retType)
                        }
                    }
                }
                is ModDecl -> decl.body?.let {
                    validateDeclarations(it, result, insideLoop, currentFunctionReturnType)
                }
                is UseDecl -> {}
                is PubDecl -> validateDeclarations(listOf(decl.inner), result, insideLoop, currentFunctionReturnType)
            }
        }
    }

    private fun validateBlock(
        block: Block,
        result: MutableList<SemanticDiagnostic>,
        insideLoop: Boolean,
        currentFunctionReturnType: String?,
    ) {
        for (stmt in block.statements) {
            validateStatement(stmt, result, insideLoop, currentFunctionReturnType)
        }
    }

    private fun validateStatement(
        stmt: Stmt,
        result: MutableList<SemanticDiagnostic>,
        insideLoop: Boolean,
        currentFunctionReturnType: String?,
    ) {
        when (stmt) {
            is ExprStmt -> {}
            is ReturnStmt -> {
                if (stmt.value != null && currentFunctionReturnType == null) {
                    // Returning a value from a void function
                    result.add(SemanticDiagnostic(
                        code = DiagnosticCode.MISSING_RETURN,
                        severity = DiagnosticSeverity.ERROR,
                        message = "Function has no return type but return statement has a value",
                        range = stmt.range(),
                        fileName = stmt.fileName,
                    ))
                }
            }
            is BreakStmt -> {
                if (!insideLoop) {
                    result.add(SemanticDiagnostic(
                        code = DiagnosticCode.BREAK_OUTSIDE_LOOP,
                        severity = DiagnosticSeverity.ERROR,
                        message = "'break' outside of loop",
                        range = stmt.range(),
                        fileName = stmt.fileName,
                    ))
                }
            }
            is ContinueStmt -> {
                if (!insideLoop) {
                    result.add(SemanticDiagnostic(
                        code = DiagnosticCode.CONTINUE_OUTSIDE_LOOP,
                        severity = DiagnosticSeverity.ERROR,
                        message = "'continue' outside of loop",
                        range = stmt.range(),
                        fileName = stmt.fileName,
                    ))
                }
            }
            is LetStmt -> {}
            is IfStmt -> {
                validateBlock(stmt.thenBranch, result, insideLoop, currentFunctionReturnType)
                stmt.elseBranch?.let { validateStatement(it, result, insideLoop, currentFunctionReturnType) }
            }
            is WhileStmt -> {
                validateBlock(stmt.body, result, insideLoop = true, currentFunctionReturnType)
            }
            is ForStmt -> {
                validateBlock(stmt.body, result, insideLoop = true, currentFunctionReturnType)
            }
            is Block -> validateBlock(stmt, result, insideLoop, currentFunctionReturnType)
        }
    }

    private fun typeNodeToString(node: TypeNode): String = when (node) {
        is IdentifierType -> node.name
        is QualifiedType -> node.path.joinToString("::")
        is ArrayType -> "[${typeNodeToString(node.elementType)}]"
        is PointerType -> "*${typeNodeToString(node.elementType)}"
        is OptionalType -> "${typeNodeToString(node.elementType)}?"
        is FunctionType -> {
            val params = node.parameterTypes.joinToString(", ") { typeNodeToString(it) }
            "fn($params) -> ${typeNodeToString(node.returnType)}"
        }
        is VoidType -> "void"
    }
}
