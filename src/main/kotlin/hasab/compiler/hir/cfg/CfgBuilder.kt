package hasab.compiler.hir.cfg

import hasab.compiler.hir.*
import hasab.compiler.types.*

/**
 * Converts a tree-based [HirFnDecl] into a control-flow graph [HirCfgFunction].
 *
 * Each basic block contains a linear sequence of three-address instructions
 * followed by exactly one terminator. Control flow constructs (if, while, for)
 * are lowered into structured block graphs with proper edge wiring.
 */
public class CfgBuilder {

    private var blockCounter = 0
    private var registerCounter = 0
    private val blocks = mutableMapOf<BlockId, HirBasicBlock>()
    private val blockInstructions = mutableMapOf<BlockId, MutableList<HirInstruction>>()
    private var currentBlockId: BlockId = newBlock()

    private fun newBlock(): BlockId {
        val id = BlockId(blockCounter++)
        blockInstructions[id] = mutableListOf()
        return id
    }

    private fun freshRegister(type: Type, hint: String = "r"): Register {
        return Register("$hint$registerCounter", type)
    }

    private fun emit(instruction: HirInstruction) {
        blockInstructions[currentBlockId]!!.add(instruction)
    }

    private fun finishBlock() {
        val instrs = blockInstructions[currentBlockId]!!.toList()
        blocks[currentBlockId] = HirBasicBlock(currentBlockId, instrs)
    }

    private fun startBlock(id: BlockId) {
        currentBlockId = id
    }

    private fun ensureTerminated() {
        val instrs = blockInstructions[currentBlockId]
        if (instrs != null && instrs.isNotEmpty() && !instrs.last().isTerminator()) {
            // Block not terminated - this is a structural issue
        }
    }

    /**
     * Build a [HirCfgFunction] from a [HirFnDecl].
     */
    public fun build(fnDecl: HirFnDecl): HirCfgFunction {
        blockCounter = 0
        registerCounter = 0
        blocks.clear()
        blockInstructions.clear()

        val entryBlockId = newBlock()
        currentBlockId = entryBlockId

        val params = fnDecl.parameters.map { ParamOperand(it.name, it.type) }

        if (fnDecl.body != null) {
            lowerBlock(fnDecl.body)
            val instrs = blockInstructions[currentBlockId]!!
            if (instrs.isEmpty() || !instrs.last().isTerminator()) {
                if (fnDecl.returnType == VoidType) {
                    emit(ReturnInstr(null))
                }
            }
        } else {
            emit(ReturnInstr(null))
        }

        finishBlock()

        return HirCfgFunction(
            name = fnDecl.name,
            parameters = params,
            returnType = fnDecl.returnType,
            blocks = blocks.toMap(),
            entryBlockId = entryBlockId,
        )
    }

    // ---- Statement lowering ----

    private fun lowerBlock(block: HirBlock) {
        for (stmt in block.statements) {
            lowerStatement(stmt)
        }
    }

    private fun lowerStatement(stmt: HirStmt) {
        when (stmt) {
            is HirBlock -> lowerBlock(stmt)
            is HirExprStmt -> lowerExprToTemp(stmt.expression)
            is HirReturnStmt -> {
                val operand = stmt.value?.let { lowerExprToOperand(it) }
                emit(ReturnInstr(operand))
                val newBlock = newBlock()
                finishBlock()
                startBlock(newBlock)
            }
            is HirLetStmt -> {
                val initReg = lowerExprToRegister(stmt.initializer)
                emit(AssignInstr(Register(stmt.name, stmt.type), RegisterOperand(initReg)))
            }
            is HirIfStmt -> lowerIfStmt(stmt)
            is HirWhileStmt -> lowerWhileStmt(stmt)
            is HirForStmt -> lowerForStmt(stmt)
            is HirBreakStmt -> {
                emit(JumpInstr(getBreakTarget()))
                val newBlock = newBlock()
                finishBlock()
                startBlock(newBlock)
            }
            is HirContinueStmt -> {
                emit(JumpInstr(getContinueTarget()))
                val newBlock = newBlock()
                finishBlock()
                startBlock(newBlock)
            }
        }
    }

    // ---- Control flow lowering ----

    private fun lowerIfStmt(stmt: HirIfStmt) {
        val condOperand = lowerExprToOperand(stmt.condition)

        val thenBlockId = newBlock()
        val mergeBlockId = newBlock()

        val elseBlockId = if (stmt.elseBranch != null) {
            newBlock()
        } else {
            mergeBlockId
        }

        emit(BranchInstr(condOperand, thenBlockId, elseBlockId))
        finishBlock()

        startBlock(thenBlockId)
        lowerBlock(stmt.thenBranch)
        val thenInstrs = blockInstructions[currentBlockId]!!
        if (thenInstrs.isEmpty() || !thenInstrs.last().isTerminator()) {
            emit(JumpInstr(mergeBlockId))
        }
        finishBlock()

        if (stmt.elseBranch != null) {
            startBlock(elseBlockId!!)
            lowerStatement(stmt.elseBranch)
            val elseInstrs = blockInstructions[currentBlockId]!!
            if (elseInstrs.isEmpty() || !elseInstrs.last().isTerminator()) {
                emit(JumpInstr(mergeBlockId))
            }
            finishBlock()
        }

        startBlock(mergeBlockId)
    }

    private val breakTargets = ArrayDeque<BlockId>()
    private val continueTargets = ArrayDeque<BlockId>()

    private fun lowerWhileStmt(stmt: HirWhileStmt) {
        val headerBlockId = newBlock()
        val bodyBlockId = newBlock()
        val exitBlockId = newBlock()

        emit(JumpInstr(headerBlockId))
        finishBlock()

        startBlock(headerBlockId)
        val condOperand = lowerExprToOperand(stmt.condition)
        emit(BranchInstr(condOperand, bodyBlockId, exitBlockId))
        finishBlock()

        breakTargets.addLast(exitBlockId)
        continueTargets.addLast(headerBlockId)
        startBlock(bodyBlockId)
        lowerBlock(stmt.body)
        val bodyInstrs = blockInstructions[currentBlockId]!!
        if (bodyInstrs.isEmpty() || !bodyInstrs.last().isTerminator()) {
            emit(JumpInstr(headerBlockId))
        }
        finishBlock()
        breakTargets.removeLast()
        continueTargets.removeLast()

        startBlock(exitBlockId)
    }

    private fun lowerForStmt(stmt: HirForStmt) {
        val indexName = "__idx_${stmt.variable}"
        val indexType = IntType

        emit(AssignInstr(Register(indexName, indexType), ConstOperand(0, IntType)))

        val arrReg = lowerExprToRegister(stmt.iterable)
        val headerBlockId = newBlock()
        val bodyBlockId = newBlock()
        val exitBlockId = newBlock()

        emit(JumpInstr(headerBlockId))
        finishBlock()

        startBlock(headerBlockId)
        val indexReg = Register(indexName, indexType)
        emit(BranchInstr(ConstOperand(true, BoolType), bodyBlockId, exitBlockId))
        finishBlock()

        breakTargets.addLast(exitBlockId)
        continueTargets.addLast(headerBlockId)
        startBlock(bodyBlockId)

        val elemReg = freshRegister(stmt.variableType, stmt.variable)
        emit(LoadIndexInstr(elemReg, RegisterOperand(indexReg), RegisterOperand(arrReg), stmt.variableType))
        emit(AssignInstr(Register(stmt.variable, stmt.variableType), RegisterOperand(elemReg)))

        lowerBlock(stmt.body)

        val oneConst = ConstOperand(1, IntType)
        val newIdxReg = freshRegister(IntType, "idx")
        emit(BinaryOpInstr(newIdxReg, "+", RegisterOperand(indexReg), oneConst))
        emit(AssignInstr(Register(indexName, indexType), RegisterOperand(newIdxReg)))

        val bodyInstrs = blockInstructions[currentBlockId]!!
        if (bodyInstrs.isEmpty() || !bodyInstrs.last().isTerminator()) {
            emit(JumpInstr(headerBlockId))
        }
        finishBlock()
        breakTargets.removeLast()
        continueTargets.removeLast()

        startBlock(exitBlockId)
    }

    private fun getBreakTarget(): BlockId = breakTargets.lastOrNull()
        ?: throw IllegalStateException("break outside of loop")

    private fun getContinueTarget(): BlockId = continueTargets.lastOrNull()
        ?: throw IllegalStateException("continue outside of loop")

    // ---- Expression lowering ----

    private fun lowerExprToRegister(expr: HirExpr): Register {
        val reg = freshRegister(expr.type, "tmp")
        when (expr) {
            is HirIntLiteral -> emit(AssignInstr(reg, ConstOperand(expr.value, expr.type)))
            is HirFloatLiteral -> emit(AssignInstr(reg, ConstOperand(expr.value, expr.type)))
            is HirStringLiteral -> emit(AssignInstr(reg, ConstOperand(expr.value, expr.type)))
            is HirCharLiteral -> emit(AssignInstr(reg, ConstOperand(expr.value, expr.type)))
            is HirBoolLiteral -> emit(AssignInstr(reg, ConstOperand(expr.value, expr.type)))
            is HirNilLiteral -> emit(AssignInstr(reg, ConstOperand(null, expr.type)))
            is HirIdentifier -> emit(AssignInstr(reg, RegisterOperand(Register(expr.name, expr.type))))
            is HirBinary -> {
                val left = lowerExprToOperand(expr.left)
                val right = lowerExprToOperand(expr.right)
                emit(BinaryOpInstr(reg, expr.operator, left, right))
            }
            is HirUnary -> {
                val operand = lowerExprToOperand(expr.operand)
                emit(UnaryOpInstr(reg, expr.operator, operand))
            }
            is HirCall -> {
                val calleeName = extractCalleeName(expr.callee)
                val args = expr.arguments.map { lowerExprToOperand(it) }
                emit(CallInstr(reg, calleeName, expr.callee.type, args))
            }
            is HirIndex -> {
                val base = lowerExprToOperand(expr.callee)
                val index = lowerExprToOperand(expr.index)
                val elemType = when (val ct = expr.callee.type) {
                    is ArrayType -> ct.elementType
                    is StringType -> CharType
                    else -> UnknownType
                }
                emit(LoadIndexInstr(reg, base, index, elemType))
            }
            is HirFieldAccess -> {
                val base = lowerExprToOperand(expr.callee)
                emit(LoadFieldInstr(reg, base, expr.fieldName, expr.type))
            }
            is HirSafeFieldAccess -> {
                val base = lowerExprToOperand(expr.callee)
                emit(LoadFieldInstr(reg, base, expr.fieldName, expr.type))
            }
            is HirNullAssert -> {
                val operand = lowerExprToOperand(expr.operand)
                emit(NullAssertInstr(reg, operand))
            }
            is HirArrayLiteral -> {
                val elements = expr.elements.map { lowerExprToOperand(it) }
                emit(ArrayLiteralInstr(reg, elements, expr.type))
            }
            is HirArrayInit -> {
                val size = lowerExprToOperand(expr.size)
                emit(ArrayInitInstr(reg, size, expr.type))
            }
            is HirIfExpr -> {
                val condOperand = lowerExprToOperand(expr.condition)
                val thenBlockId = newBlock()
                val elseBlockId = newBlock()
                val mergeBlockId = newBlock()

                emit(BranchInstr(condOperand, thenBlockId, elseBlockId))
                finishBlock()

                startBlock(thenBlockId)
                val thenReg = lowerExprToRegister(expr.thenBranch)
                emit(AssignInstr(reg, RegisterOperand(thenReg)))
                emit(JumpInstr(mergeBlockId))
                finishBlock()

                startBlock(elseBlockId)
                if (expr.elseBranch != null) {
                    val elseReg = lowerExprToRegister(expr.elseBranch)
                    emit(AssignInstr(reg, RegisterOperand(elseReg)))
                } else {
                    emit(AssignInstr(reg, ConstOperand(null, expr.type)))
                }
                emit(JumpInstr(mergeBlockId))
                finishBlock()

                startBlock(mergeBlockId)
            }
            is HirAssignment -> {
                val value = lowerExprToOperand(expr.value)
                when (val target = expr.target) {
                    is HirIdentifier -> emit(AssignInstr(Register(target.name, target.type), value))
                    is HirFieldAccess -> {
                        val base = lowerExprToOperand(target.callee)
                        emit(StoreFieldInstr(base, target.fieldName, value))
                    }
                    is HirIndex -> {
                        val base = lowerExprToOperand(target.callee)
                        val idx = lowerExprToOperand(target.index)
                        emit(StoreIndexInstr(base, idx, value))
                    }
                    else -> emit(AssignInstr(reg, value))
                }
                emit(AssignInstr(reg, value))
            }
            is HirCompoundAssignment -> {
                val value = lowerExprToOperand(expr.value)
                when (val target = expr.target) {
                    is HirIdentifier -> {
                        val currentReg = RegisterOperand(Register(target.name, target.type))
                        val resultReg = freshRegister(target.type, "comp")
                        emit(BinaryOpInstr(resultReg, expr.operator, currentReg, value))
                        emit(AssignInstr(Register(target.name, target.type), RegisterOperand(resultReg)))
                    }
                    else -> {
                        val base = lowerExprToOperand(target)
                        val resultReg = freshRegister(expr.type, "comp")
                        emit(BinaryOpInstr(resultReg, expr.operator, base, value))
                    }
                }
                emit(AssignInstr(reg, ConstOperand(0, expr.type)))
            }
        }
        return reg
    }

    private fun lowerExprToOperand(expr: HirExpr): Operand = when (expr) {
        is HirIntLiteral -> ConstOperand(expr.value, expr.type)
        is HirFloatLiteral -> ConstOperand(expr.value, expr.type)
        is HirStringLiteral -> ConstOperand(expr.value, expr.type)
        is HirCharLiteral -> ConstOperand(expr.value, expr.type)
        is HirBoolLiteral -> ConstOperand(expr.value, expr.type)
        is HirNilLiteral -> ConstOperand(null, expr.type)
        is HirIdentifier -> RegisterOperand(Register(expr.name, expr.type))
        else -> {
            val reg = lowerExprToRegister(expr)
            RegisterOperand(reg)
        }
    }

    private fun lowerExprToTemp(expr: HirExpr) {
        lowerExprToRegister(expr)
    }

    private fun extractCalleeName(callee: HirExpr): String = when (callee) {
        is HirIdentifier -> callee.name
        else -> "<anonymous>"
    }
}
