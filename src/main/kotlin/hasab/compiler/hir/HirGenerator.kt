package hasab.compiler.hir

import hasab.compiler.frontend.ast.Module
import hasab.compiler.hir.cfg.*
import hasab.compiler.types.TypeCheckResult

/**
 * Orchestrates HIR generation: AST → Tree HIR → CFG HIR.
 *
 * This is the top-level entry point for HIR generation.
 * It coordinates the existing [AstToHirLowering] with the new
 * [CfgBuilder] to produce both representations.
 */
public class HIRGenerator(
    private val sourceMap: HirSourceMap = HirSourceMap(),
) {
    private val cfgBuilder = CfgBuilder()
    private val validator = HirValidator()

    /**
     * Result of HIR generation.
     */
    public data class GenerationResult(
        val treeHir: HirModule,
        val cfgModule: HirCfgModule,
        val sourceMap: HirSourceMap,
        val diagnostics: List<HirValidationError>,
    ) {
        val hasErrors: Boolean get() = diagnostics.isNotEmpty()
    }

    /**
     * Generate both tree HIR and CFG HIR from a type-checked AST module.
     */
    public fun generate(typeCheckResult: TypeCheckResult, module: Module): GenerationResult {
        // Step 1: Lower AST to tree HIR
        val lowering = AstToHirLowering(typeCheckResult.environment)
        val treeHir = lowering.lower(module)

        // Step 2: Validate tree HIR
        val diagnostics = validator.validate(treeHir)

        // Step 3: Build CFG for each function
        val cfgFunctions = mutableMapOf<String, HirCfgFunction>()
        for (decl in treeHir.declarations) {
            if (decl is HirFnDecl && decl.body != null) {
                val cfgFn = cfgBuilder.build(decl)
                cfgFunctions[decl.name] = cfgFn
            }
        }

        val cfgModule = HirCfgModule(
            name = treeHir.name,
            functions = cfgFunctions,
        )

        return GenerationResult(
            treeHir = treeHir,
            cfgModule = cfgModule,
            sourceMap = sourceMap,
            diagnostics = diagnostics,
        )
    }

    /**
     * Generate CFG HIR from an already-lowered tree HIR module.
     * Useful for re-generating CFG after tree HIR transformations.
     */
    public fun generateCfg(treeHir: HirModule): HirCfgModule {
        val cfgFunctions = mutableMapOf<String, HirCfgFunction>()
        for (decl in treeHir.declarations) {
            if (decl is HirFnDecl && decl.body != null) {
                val cfgFn = cfgBuilder.build(decl)
                cfgFunctions[decl.name] = cfgFn
            }
        }
        return HirCfgModule(name = treeHir.name, functions = cfgFunctions)
    }
}
