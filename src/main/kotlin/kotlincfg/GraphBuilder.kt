package main.kotlin.kotlincfg

import kastree.ast.Node
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge

class GraphBuilder(private val ast: Node.File) {
    private val graph = DefaultDirectedGraph<DisplayNode, DefaultEdge>(DefaultEdge::class.java)
    private var cursor: DisplayNode? = null
    private var waitingSources: List<DisplayNode> = arrayListOf()
    private var waitingTargets: List<DisplayNode> = arrayListOf()
    private val generator: IdGenerator = IdGenerator()

    fun build(): DefaultDirectedGraph<DisplayNode, DefaultEdge> {
        val function =  getFirstFunction()
        cursor = toDisplayNode(function)
        graph.addVertex(cursor)
        processFunction(function)
        return graph
    }

    private fun getFirstFunction(): Node.Decl.Func {
        if (ast.decls.size != 1)
            throw Exception("Only single function files are supported")
        return ast.decls[0] as? Node.Decl.Func ?: throw Exception("Only single function files are supported")
    }

    private fun processFunction(function: Node.Decl.Func) {
        when (function.body) {
            is Node.Decl.Func.Body.Expr -> {
                val body = function.body as Node.Decl.Func.Body.Expr
                processExpression(body.expr)
            }
            is Node.Decl.Func.Body.Block -> {
                val body = function.body as Node.Decl.Func.Body.Block
                processBlock(body.block)
            }
        }
    }

    private fun processExpression(expr: Node.Expr) {
        when (expr) {
            is Node.Expr.If -> processIf(expr)
            is Node.Expr.For -> processFor(expr)
            is Node.Expr.While -> processWhile(expr)
            is Node.Expr.When -> processWhen(expr)
            is Node.Expr.Brace -> processBrace(expr)
            is Node.Expr.Return -> processWithoutContinuation(expr)
            is Node.Expr.Break -> processWithoutContinuation(expr)
            is Node.Expr.Continue -> processWithoutContinuation(expr)
            else -> linkNode(expr)
        }
    }

    private fun processIf(ifExpr: Node.Expr.If) {
        linkNode(ifExpr)
        val startPtr = cursor
        processExpression(ifExpr.body)
        val truePtr = cursor
        val waitingSrc = waitingSources
        val waitingTrg = waitingTargets
        waitingSources = arrayListOf()
        waitingTargets = arrayListOf()
        cursor = startPtr
        if (ifExpr.elseBody != null) {
            val elseBody = ifExpr.elseBody as Node.Expr
            processExpression(elseBody)
        }
        val src = mutableListOf<DisplayNode>()
        if (truePtr != null)
            src.add(truePtr)
        for (item in waitingSrc)
            src.add(item)
        for (item in waitingSources)
            src.add(item)
        waitingSources = src
        val trg = mutableListOf<DisplayNode>()
        for (item in waitingTrg)
            trg.add(item)
        for (item in waitingTargets)
            trg.add(item)
        waitingTargets = trg
    }

    private fun processFor(forExpr: Node.Expr.For) {
        linkNode(forExpr)
        val loopStart = cursor
        processExpression(forExpr.body)
        if (loopStart != null && cursor != null) {
            graph.addEdge(cursor, loopStart)
            linkWaiting(loopStart)
        }
        cursor = loopStart
    }

    private fun processWhile(whileExpr: Node.Expr.While) {
        if (whileExpr.doWhile) {
            processDoWhile(whileExpr)
        } else {
            linkNode(whileExpr)
            val whileNode = cursor
            processExpression(whileExpr.body)
            if (cursor != null) {
                graph.addEdge(cursor, whileNode)
                linkWaiting(whileNode)
            }
            cursor = whileNode
        }
    }

    private fun processDoWhile(whileExpr: Node.Expr.While) {
        val node: DisplayNode = toDisplayNode(whileExpr)
        graph.addVertex(node)
        val lst = mutableListOf(node)
        for (item in waitingSources)
            lst.add(item)
        waitingSources = lst
        processExpression(whileExpr.body)
        if (cursor != null) {
            graph.addEdge(cursor, node)
            linkWaiting(node)
        }
        cursor = node
    }

    private fun processWhen(whenExpr: Node.Expr.When) {
        linkNode(whenExpr)
        val whenNode = cursor
        val outs: MutableList<DisplayNode> = mutableListOf()
        for (expr in whenExpr.entries) {
            val sources: MutableList<DisplayNode> = mutableListOf()
            for (cond in expr.conds) {
                cursor = whenNode
                linkNode(when (cond) {
                    is Node.Expr.When.Cond.Expr -> cond.expr
                    is Node.Expr.When.Cond.In -> cond.expr
                    is Node.Expr.When.Cond.Is -> cond.type
                })
                val source = cursor
                if (source != null)
                    sources.add(source)
            }
            waitingSources = sources
            processExpression(expr.body)
            val out = cursor
            if (out != null) {
                outs.add(out)
            }
        }
        waitingSources = outs
    }

    private fun processBrace(braceExpr: Node.Expr.Brace) {
        if (braceExpr.block != null){
            val block = braceExpr.block as Node.Block
            processBlock(block)
        }
    }

    private fun processWithoutContinuation(expression: Node.Expr) {
        linkNode(expression)
        cursor = null
    }

    private fun processBreakContinue(expression: Node.Expr) {
        linkNode(expression)
        val waiter = cursor
        cursor = null
        if (waiter != null)
            waitingSources = arrayListOf(waiter)
    }

    private fun processBlock(block: Node.Block) {
        for (stmt: Node.Stmt in block.stmts) {
            processStatement(stmt)
        }
    }

    private fun processDeclaration(decl: Node.Decl) {
        when (decl) {
            is Node.Decl.Property -> linkNode(decl)
            else -> throw Exception("Only propertyDeclarations are implemented")
        }
    }

    private fun processStatement(stmt: Node.Stmt) {
        when (stmt) {
            is Node.Stmt.Decl -> processDeclaration(stmt.decl)
            is Node.Stmt.Expr -> processExpression(stmt.expr)
        }
    }

    private fun linkNode(node: Node) {
        val node: DisplayNode = toDisplayNode(node)
        graph.addVertex(node)
        if (cursor != null) {
            graph.addEdge(cursor, node)
        }
        linkWaiting(node)
        cursor = node
    }

    private fun linkWaiting(target: DisplayNode?) {
        if (!waitingSources.isEmpty()) {
            for (waitingSource: DisplayNode in waitingSources) {
                if (target != null)
                    graph.addEdge(waitingSource, target)
            }
            waitingSources = arrayListOf()
        }
        if (!waitingTargets.isEmpty()) {
            for (waitingTarget: DisplayNode in waitingTargets) {
                if (target != null)
                    graph.addEdge(target, waitingTarget)
            }
            waitingTargets = arrayListOf()
        }
    }

    private fun toDisplayNode(node: Node): DisplayNode {
        val text = when (node) {
            is Node.Decl -> declToString(node)
            is Node.Expr -> exprToString(node)
            is Node.Type -> typeToString(node)
            else -> "${node::class}-${generator.next()}"
        }
        return DisplayNode(text, generator.next())
    }

    private fun typeToString(node: Node.Type?): String {
        if (node == null)
            return ""
        val typeRef = node.ref
        return when (typeRef) {
            is Node.TypeRef.Simple -> typeRef.pieces.joinToString { it.name }
            is Node.TypeRef.Paren -> "Typeref - Paren"
            is Node.TypeRef.Func -> "Typeref - Func"
            is Node.TypeRef.Nullable -> "Typeref - Nullable"
            is Node.TypeRef.Dynamic -> "Typeref - Dynamic"
        }
    }

    private fun propToString(prop: Node.Decl.Property?): String {
        if (prop == null)
            return ""
        val name = if (prop.vars[0] != null) prop.vars[0]?.name else "var${generator.next()}"
        val type = prop.vars.joinToString { if (it != null) typeToString(it.type) else "" }
        val value = if (prop.expr != null) exprToString(prop.expr) else ""
        return "$name${if (type != "") " :${type}" else ""}${if (value != "") " := ${value}" else ""}"
    }

    private fun varToString(variable: Node.Decl.Property.Var?): String {
        if (variable == null)
            return ""
        val name = variable.name
        val type = if (variable.type != null) ": ${variable.type}" else ""
        return "${name}${type}"
    }

    private fun condToString(cond: Node.Expr.When.Cond): String {
        return when (cond) {
            is Node.Expr.When.Cond.Expr -> "(==)${exprToString(cond.expr)}"
            is Node.Expr.When.Cond.In -> "${if (cond.not) "!in" else "in"}${cond.expr}"
            is Node.Expr.When.Cond.Is -> "${if (cond.not) "!is" else "is"}${typeToString(cond.type)}"
        }
    }

    private fun exprToString(expr: Node.Expr?): String {
        if (expr == null)
            return ""
        return when (expr) {
            is Node.Expr.StringTmpl -> expr.elems.joinToString { it: Node.Expr.StringTmpl.Elem ->
                when (it) {
                    is Node.Expr.StringTmpl.Elem.Regular -> "\'${it.str}\'"
                    is Node.Expr.StringTmpl.Elem.ShortTmpl -> it.str
                    is Node.Expr.StringTmpl.Elem.UnicodeEsc -> it.digits
                    is Node.Expr.StringTmpl.Elem.RegularEsc -> it.char.toString()
                    is Node.Expr.StringTmpl.Elem.LongTmpl -> exprToString(it.expr)
                }
            }
            is Node.Expr.Const -> expr.value
            is Node.Expr.When.Cond -> condToString(expr)
            is Node.Expr.When -> "when ${exprToString(expr.expr)}"
            is Node.Expr.Return -> "return ${exprToString(expr.expr)}"
            is Node.Expr.While -> "while ${exprToString(expr.expr)}"
            is Node.Expr.BinaryOp -> {
                val oper = expr.oper
                return "${exprToString(expr.lhs)} ${when(oper) {
                    is Node.Expr.BinaryOp.Oper.Infix -> oper.str
                    is Node.Expr.BinaryOp.Oper.Token -> oper.token.str
                }} ${exprToString(expr.rhs)}"
            }
            is Node.Expr.UnaryOp -> if (expr.prefix) "${expr.oper.token.str}${exprToString(expr.expr)}"
                else "${exprToString(expr.expr)}${expr.oper.token.str}"
            is Node.Expr.If -> "if ${exprToString(expr.expr)}"
            is Node.Expr.Try -> "try ${expr.block.stmts.joinToString { stmt -> when(stmt) {
                is Node.Stmt.Decl -> declToString(stmt.decl)
                is Node.Stmt.Expr -> exprToString(stmt.expr)
            } }}"
            is Node.Expr.For -> "for ${varToString(expr.vars[0])} in ${exprToString(expr.inExpr)}"
            is Node.Expr.TypeOp -> "${expr.lhs} ${expr.oper} ${expr.rhs}"
            is Node.Expr.DoubleColonRef.Callable -> expr.name
            is Node.Expr.DoubleColonRef.Class -> "Double colon reference class"
            is Node.Expr.Paren -> exprToString(expr.expr)
            is Node.Expr.Brace -> "block should not be in the tree"
            is Node.Expr.Brace.Param -> "block should not be in the tree"
            is Node.Expr.This -> "this${expr.label ?: ""}"
            is Node.Expr.Super -> "super${expr.label ?: ""}"
            is Node.Expr.Object -> "objects not implemented"
            is Node.Expr.Throw -> "throw ${exprToString(expr.expr)}"
            is Node.Expr.Continue -> "continue ${expr.label ?: ""}"
            is Node.Expr.Break -> "break ${expr.label ?: ""}"
            is Node.Expr.CollLit -> expr.exprs.joinToString { exprToString(it) }
            is Node.Expr.Name -> expr.name
            is Node.Expr.Labeled -> "${expr.label}: ${exprToString(expr.expr)}"
            is Node.Expr.Annotated -> "annotated ${exprToString(expr.expr)}"
            is Node.Expr.Call -> "${exprToString(expr.expr)}(${expr.args.joinToString { exprToString(it.expr)}})"
            is Node.Expr.ArrayAccess -> "${exprToString(expr.expr)}[${expr.indices.joinToString { exprToString(it) }}]"
        }
    }

    private fun declToString (decl: Node.Decl): String {
        return when (decl) {
            is Node.Decl.Property -> propToString(decl)
            is Node.Decl.Structured -> "structured"
            is Node.Decl.Init -> "init"
            is Node.Decl.Func -> "function ${decl.name}(${decl.params.joinToString(",") { it.name }})"
            is Node.Decl.TypeAlias -> "type alias"
            is Node.Decl.Constructor -> "constructor"
            is Node.Decl.EnumEntry -> "enum entry"
        }
    }
}

class IdGenerator {
    private var cntr: Int = 0

    fun next(): Int {
        return cntr++
    }
}