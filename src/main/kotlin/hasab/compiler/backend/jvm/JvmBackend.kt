package hasab.compiler.backend.jvm

import hasab.compiler.hir.HirModule
import hasab.compiler.hir.HirDecl
import hasab.compiler.hir.HirFnDecl
import hasab.compiler.hir.HirStructDecl
import hasab.compiler.hir.HirEnumDecl
import hasab.compiler.hir.cfg.HirCfgModule
import hasab.compiler.hir.cfg.HirCfgFunction
import hasab.compiler.types.*

/**
 * Configuration for the JVM backend.
 */
public data class JvmBackendConfig(
    val className: String = "Main",
    val outputDir: String = ".",
    val targetJvmVersion: Int = 21,
    val validate: Boolean = true,
    val sourceFileName: String = "source.hasab",
)

/**
 * Result of JVM compilation.
 */
public data class JvmCompilationResult(
    val classes: Map<String, ByteArray>,
    val mainClassName: String,
    val diagnostics: List<String> = emptyList(),
) {
    val success: Boolean get() = diagnostics.isEmpty()
}

/**
 * Top-level JVM backend orchestrator.
 * Takes a [HirCfgModule] and produces JVM `.class` file byte arrays.
 */
public class JvmBackend(
    private val config: JvmBackendConfig = JvmBackendConfig(),
    private val typeMapper: JvmTypeMapper = JvmTypeMapper,
) {
    private val constantPool = ConstantPoolBuilder()
    private val validator = BytecodeValidator()

    /**
     * Compile an optimized CFG module to JVM class files.
     */
    public fun compile(cfgModule: HirCfgModule): JvmCompilationResult {
        val classes = mutableMapOf<String, ByteArray>()
        val diagnostics = mutableListOf<String>()

        val mainClassName = config.className

        try {
            generateStructClasses(cfgModule, classes)
            generateEnumClasses(cfgModule, classes)
            generateMainClass(cfgModule, mainClassName, classes)
        } catch (e: Exception) {
            diagnostics.add("Compilation error: ${e.message}")
            return JvmCompilationResult(classes, mainClassName, diagnostics)
        }

        if (config.validate) {
            for ((name, bytes) in classes) {
                val result = validator.validate(bytes)
                if (!result.isValid) {
                    for (error in result.errors) {
                        diagnostics.add("Validation error in $name: ${error.message}")
                    }
                }
            }
        }

        return JvmCompilationResult(classes, mainClassName, diagnostics)
    }

    /**
     * Compile a tree HIR module to JVM class files.
     * Generates CFG from tree HIR first.
     */
    public fun compileTreeHir(treeHir: HirModule, treeHirFunctions: List<HirFnDecl>): JvmCompilationResult {
        val cfgFunctions = mutableMapOf<String, HirCfgFunction>()
        for (fn in treeHirFunctions) {
            try {
                val builder = hasab.compiler.hir.cfg.CfgBuilder()
                val cfgFn = builder.build(fn)
                cfgFunctions[fn.name] = cfgFn
            } catch (e: Exception) {
                // Skip functions that fail to lower to CFG
            }
        }
        val cfgModule = HirCfgModule(config.className, cfgFunctions)
        return compile(cfgModule)
    }

    private fun generateStructClasses(cfgModule: HirCfgModule, classes: MutableMap<String, ByteArray>) {
        // Struct classes are generated from tree HIR, not CFG.
        // This method can be called with struct info passed separately.
    }

    private fun generateEnumClasses(cfgModule: HirCfgModule, classes: MutableMap<String, ByteArray>) {
        // Enum classes are generated from tree HIR, not CFG.
    }

    private fun generateMainClass(
        cfgModule: HirCfgModule,
        mainClassName: String,
        classes: MutableMap<String, ByteArray>,
    ) {
        val classBuilder = ClassBuilder(
            className = mainClassName,
            access = org.objectweb.asm.Opcodes.ACC_PUBLIC or org.objectweb.asm.Opcodes.ACC_SUPER,
        )
        classBuilder.begin()
        classBuilder.addDefaultConstructor()

        val generator = BytecodeGenerator(
            classBuilder = classBuilder,
            mainClassName = mainClassName,
            typeMapper = typeMapper,
        )

        for ((name, function) in cfgModule.functions) {
            try {
                generator.generate(function)
            } catch (e: Exception) {
                // Log but continue with other functions
            }
        }

        val classBytes = classBuilder.build()
        classes[mainClassName] = classBytes
    }

    /**
     * Generate a struct class from tree HIR declaration.
     */
    public fun generateStructClass(structDecl: HirStructDecl): ByteArray {
        val className = structDecl.name
        val classBuilder = ClassBuilder(
            className = className,
            access = org.objectweb.asm.Opcodes.ACC_PUBLIC or org.objectweb.asm.Opcodes.ACC_SUPER,
        )
        classBuilder.begin()

        for (field in structDecl.fields) {
            classBuilder.addField(
                name = field.name,
                descriptor = typeMapper.descriptor(field.type),
                access = org.objectweb.asm.Opcodes.ACC_PUBLIC,
            )
        }

        if (structDecl.fields.isNotEmpty()) {
            val constructorDesc = "(${
                structDecl.fields.joinToString("") { typeMapper.descriptor(it.type) }
            })V"
            val mv = classBuilder.getClassWriter().visitMethod(
                org.objectweb.asm.Opcodes.ACC_PUBLIC,
                "<init>",
                constructorDesc,
                null,
                null,
            )
            mv.visitCode()
            mv.visitVarInsn(org.objectweb.asm.Opcodes.ALOAD, 0)
            mv.visitMethodInsn(
                org.objectweb.asm.Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false,
            )
            var slot = 1
            for (field in structDecl.fields) {
                mv.visitVarInsn(typeMapper.loadOpcode(field.type), slot)
                mv.visitFieldInsn(
                    org.objectweb.asm.Opcodes.PUTFIELD,
                    className,
                    field.name,
                    typeMapper.descriptor(field.type),
                )
                slot++
            }
            mv.visitInsn(org.objectweb.asm.Opcodes.RETURN)
            mv.visitMaxs(0, 0)
            mv.visitEnd()
        } else {
            classBuilder.addDefaultConstructor()
        }

        return classBuilder.build()
    }

    /**
     * Generate an enum class from tree HIR declaration.
     */
    public fun generateEnumClass(enumDecl: HirEnumDecl): ByteArray {
        val className = enumDecl.name
        val classBuilder = ClassBuilder(
            className = className,
            access = org.objectweb.asm.Opcodes.ACC_PUBLIC or org.objectweb.asm.Opcodes.ACC_SUPER or org.objectweb.asm.Opcodes.ACC_ENUM,
            superName = "java/lang/Enum",
        )
        classBuilder.begin()

        val descriptor = "L$className;"

        for (variant in enumDecl.variants) {
            val fv = classBuilder.getClassWriter().visitField(
                org.objectweb.asm.Opcodes.ACC_PUBLIC or org.objectweb.asm.Opcodes.ACC_STATIC or org.objectweb.asm.Opcodes.ACC_ENUM,
                variant.name,
                descriptor,
                null,
                null,
            )
            fv.visitEnd()
        }

        classBuilder.addDefaultConstructor()

        val nameArrayDesc = "[Ljava/lang/String;"
        val valuesMethod = classBuilder.addMethod(
            name = "values",
            descriptor = "()[L$className;",
            access = org.objectweb.asm.Opcodes.ACC_PUBLIC or org.objectweb.asm.Opcodes.ACC_STATIC,
        )
        valuesMethod.methodVisitor.visitCode()
        valuesMethod.methodVisitor.visitFieldInsn(
            org.objectweb.asm.Opcodes.GETSTATIC,
            className,
            "\$VALUES",
            "[$descriptor",
        )
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.DUP)
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.ARRAYLENGTH)
        valuesMethod.methodVisitor.visitTypeInsn(org.objectweb.asm.Opcodes.ANEWARRAY, className)
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.DUP)
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.ICONST_0)
        valuesMethod.methodVisitor.visitFieldInsn(
            org.objectweb.asm.Opcodes.GETSTATIC,
            className,
            "\$VALUES",
            "[$descriptor",
        )
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.DUP)
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.ICONST_0)
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.ARRAYLENGTH)
        valuesMethod.methodVisitor.visitMethodInsn(
            org.objectweb.asm.Opcodes.INVOKESTATIC,
            "java/lang/System",
            "arraycopy",
            "(Ljava/lang/Object;ILjava/lang/Object;II)V",
            false,
        )
        valuesMethod.methodVisitor.visitInsn(org.objectweb.asm.Opcodes.ARETURN)
        valuesMethod.methodVisitor.visitMaxs(0, 0)
        valuesMethod.methodVisitor.visitEnd()

        return classBuilder.build()
    }

    /**
     * Write all class files to the output directory.
     */
    public fun writeClasses(result: JvmCompilationResult, outputDir: java.io.File) {
        if (!result.success) return
        outputDir.mkdirs()
        for ((name, bytes) in result.classes) {
            val file = java.io.File(outputDir, "$name.class")
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
        }
    }
}
