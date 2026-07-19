package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange

/**
 * First-pass AST visitor that collects all declarations into the symbol table.
 * Does NOT resolve references — only registers names and their metadata.
 */
public class DeclarationCollector(
    private val scopeManager: ScopeManager,
) {
    private var table: SymbolTable = SymbolTable.EMPTY
    private val diagnostics: MutableList<SemanticDiagnostic> = mutableListOf()
    private var currentModule: String? = null

    public fun collect(module: Module): Pair<SymbolTable, List<SemanticDiagnostic>> {
        table = SymbolTable.EMPTY
        collectDeclarations(module.declarations, isTopLevel = true)
        return table to diagnostics.toList()
    }

    private fun report(code: DiagnosticCode, message: String, node: AstNode, hint: String? = null) {
        diagnostics.add(SemanticDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = node.range(),
            fileName = node.fileName,
            hint = hint,
        ))
    }

    private fun reportAt(code: DiagnosticCode, message: String, range: SourceRange, fileName: String, hint: String? = null) {
        diagnostics.add(SemanticDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = range,
            fileName = fileName,
            hint = hint,
        ))
    }

    private fun collectDeclarations(decls: List<Decl>, isTopLevel: Boolean = false) {
        for (decl in decls) {
            collectDecl(decl, isTopLevel)
        }
    }

    private fun collectDecl(decl: Decl, isTopLevel: Boolean) {
        when (decl) {
            is FnDecl -> collectFnDecl(decl, isTopLevel)
            is StructDecl -> collectStructDecl(decl, isTopLevel)
            is EnumDecl -> collectEnumDecl(decl, isTopLevel)
            is TraitDecl -> collectTraitDecl(decl, isTopLevel)
            is TypeAliasDecl -> collectTypeAliasDecl(decl, isTopLevel)
            is ImplDecl -> collectImplDecl(decl)
            is ModDecl -> collectModDecl(decl)
            is UseDecl -> {} // handled by ImportResolver
            is PubDecl -> {
                val inner = decl.inner
                when (inner) {
                    is FnDecl -> collectFnDecl(inner, isTopLevel, visibility = Visibility.PUBLIC)
                    is StructDecl -> collectStructDecl(inner, isTopLevel, visibility = Visibility.PUBLIC)
                    is EnumDecl -> collectEnumDecl(inner, isTopLevel, visibility = Visibility.PUBLIC)
                    is TraitDecl -> collectTraitDecl(inner, isTopLevel, visibility = Visibility.PUBLIC)
                    is TypeAliasDecl -> collectTypeAliasDecl(inner, isTopLevel, visibility = Visibility.PUBLIC)
                    is ModDecl -> collectModDecl(inner, visibility = Visibility.PUBLIC)
                    else -> collectDecl(inner, isTopLevel)
                }
            }
        }
    }

    private fun collectFnDecl(
        node: FnDecl,
        isTopLevel: Boolean,
        visibility: Visibility? = null,
    ) {
        val effectiveVisibility = visibility
            ?: if (node.isPublic) Visibility.PUBLIC else Visibility.MODULE_LOCAL
        if (table.hasCurrent(node.name)) {
            report(
                DiagnosticCode.DUPLICATE_DECLARATION,
                "Duplicate declaration of '${node.name}'",
                node,
                hint = "Function '${node.name}' is already defined in this scope",
            )
            return
        }

        val isExtern = node.body == null
        val childSymbols = mutableListOf<String>()

        // If it has a body, enter function scope and collect local variables
        if (node.body != null) {
            scopeManager.enterScope(ScopeKind.FUNCTION, node.name, node.range(), node.fileName)
            childSymbols.addAll(collectFnParams(node.parameters))
            collectLetBindings(node.body!!)
            scopeManager.addSymbol(node.name)
            scopeManager.exitScope()
        }

        table = table.define(FunctionSymbol(
            name = node.name,
            visibility = effectiveVisibility,
            range = node.range(),
            fileName = node.fileName,
            parameterCount = node.parameters.size,
            isExtern = isExtern,
            docComment = node.docComment,
            parentModule = currentModule,
            childSymbols = childSymbols,
        ))
    }

    private fun reportParamDuplicate(name: String, startOffset: Int, endOffset: Int, fileName: String, line: Int, column: Int) {
        diagnostics.add(SemanticDiagnostic(
            code = DiagnosticCode.DUPLICATE_PARAMETER,
            severity = hasab.compiler.frontend.lexer.DiagnosticSeverity.ERROR,
            message = "Duplicate parameter '$name'",
            range = SourceRange(SourcePosition(line, column, startOffset), SourcePosition(line, column, endOffset)),
            fileName = fileName,
        ))
    }

    private fun collectFnParams(params: List<FunctionParam>): List<String> {
        val names = mutableListOf<String>()
        for (param in params) {
            if (table.hasCurrent(param.name)) {
                reportParamDuplicate(param.name, param.startOffset, param.endOffset, param.fileName, param.line, param.column)
            }
            table = table.define(ParameterSymbol(
                name = param.name,
                visibility = Visibility.MODULE_LOCAL,
                range = SourceRange(
                    SourcePosition(param.line, param.column, param.startOffset),
                    SourcePosition(param.line, param.column, param.endOffset),
                ),
                fileName = param.fileName,
                isMutable = param.isMutable,
                typeAnnotation = param.type?.let { typeNodeToString(it) },
                parentModule = currentModule,
            ))
            names.add(param.name)
            scopeManager.addSymbol(param.name)
        }
        return names
    }

    private fun collectStructDecl(
        node: StructDecl,
        isTopLevel: Boolean,
        visibility: Visibility? = null,
    ) {
        if (table.hasCurrent(node.name)) {
            report(DiagnosticCode.DUPLICATE_DECLARATION, "Duplicate declaration of '${node.name}'", node)
            return
        }

        val effectiveVisibility = visibility
            ?: if (node.isPublic) Visibility.PUBLIC else Visibility.MODULE_LOCAL

        val fieldNames = node.fields.map { it.name }
        table = table.define(StructSymbol(
            name = node.name,
            visibility = effectiveVisibility,
            range = node.range(),
            fileName = node.fileName,
            docComment = node.docComment,
            parentModule = currentModule,
            childSymbols = fieldNames,
        ))
    }

    private fun collectEnumDecl(
        node: EnumDecl,
        isTopLevel: Boolean,
        visibility: Visibility? = null,
    ) {
        if (table.hasCurrent(node.name)) {
            report(DiagnosticCode.DUPLICATE_DECLARATION, "Duplicate declaration of '${node.name}'", node)
            return
        }

        val effectiveVisibility = visibility
            ?: if (node.isPublic) Visibility.PUBLIC else Visibility.MODULE_LOCAL

        val variantNames = node.variants.map { it.name }
        table = table.define(EnumSymbol(
            name = node.name,
            visibility = effectiveVisibility,
            range = node.range(),
            fileName = node.fileName,
            docComment = node.docComment,
            parentModule = currentModule,
            childSymbols = variantNames,
        ))
    }

    private fun collectTraitDecl(
        node: TraitDecl,
        isTopLevel: Boolean,
        visibility: Visibility? = null,
    ) {
        if (table.hasCurrent(node.name)) {
            report(DiagnosticCode.DUPLICATE_DECLARATION, "Duplicate declaration of '${node.name}'", node)
            return
        }

        val effectiveVisibility = visibility
            ?: if (node.isPublic) Visibility.PUBLIC else Visibility.MODULE_LOCAL

        val methodNames = node.methods.map { it.name }
        table = table.define(TraitSymbol(
            name = node.name,
            visibility = effectiveVisibility,
            range = node.range(),
            fileName = node.fileName,
            docComment = node.docComment,
            parentModule = currentModule,
            childSymbols = methodNames,
        ))
    }

    private fun collectTypeAliasDecl(
        node: TypeAliasDecl,
        isTopLevel: Boolean,
        visibility: Visibility? = null,
    ) {
        if (table.hasCurrent(node.name)) {
            report(DiagnosticCode.DUPLICATE_DECLARATION, "Duplicate declaration of '${node.name}'", node)
            return
        }

        val effectiveVisibility = visibility
            ?: if (node.isPublic) Visibility.PUBLIC else Visibility.MODULE_LOCAL

        table = table.define(TypeAliasSymbol(
            name = node.name,
            visibility = effectiveVisibility,
            range = node.range(),
            fileName = node.fileName,
            docComment = node.docComment,
            parentModule = currentModule,
        ))
    }

    private fun collectImplDecl(node: ImplDecl) {
        scopeManager.enterScope(ScopeKind.IMPL, "impl", node.range(), node.fileName)
        for (method in node.methods) {
            collectFnDecl(method, isTopLevel = false)
        }
        scopeManager.exitScope()
    }

    private fun collectLetBindings(block: Block) {
        for (stmt in block.statements) {
            when (stmt) {
                is LetStmt -> {
                    table = table.define(VariableSymbol(
                        name = stmt.name,
                        visibility = Visibility.MODULE_LOCAL,
                        range = stmt.range(),
                        fileName = stmt.fileName,
                        isMutable = stmt.isMutable,
                        typeAnnotation = stmt.typeAnnotation?.let { typeNodeToString(it) },
                        parentModule = currentModule,
                    ))
                    scopeManager.addSymbol(stmt.name)
                }
                is IfStmt -> {
                    collectLetBindings(stmt.thenBranch)
                    stmt.elseBranch?.let {
                        when (it) {
                            is Block -> collectLetBindings(it)
                            is IfStmt -> {} // handled by next iteration
                            else -> {}
                        }
                    }
                }
                is WhileStmt -> collectLetBindings(stmt.body)
                is ForStmt -> collectLetBindings(stmt.body)
                is Block -> collectLetBindings(stmt)
                else -> {}
            }
        }
    }

    private fun collectModDecl(node: ModDecl, visibility: Visibility = Visibility.MODULE_LOCAL) {
        val prevModule = currentModule
        currentModule = if (currentModule != null) "${currentModule}::${node.name}" else node.name

        if (table.hasCurrent(node.name)) {
            report(DiagnosticCode.DUPLICATE_DECLARATION, "Duplicate declaration of module '${node.name}'", node)
        }

        table = table.define(ModuleSymbol(
            name = node.name,
            visibility = visibility,
            range = node.range(),
            fileName = node.fileName,
            parentModule = prevModule,
        ))

        scopeManager.enterScope(ScopeKind.MODULE, node.name, node.range(), node.fileName)
        if (node.body != null) {
            collectDeclarations(node.body)
        }
        scopeManager.exitScope()

        currentModule = prevModule
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
