package main.kotlin.kotlincfg

import kastree.ast.psi.Parser
import java.io.File
import java.time.LocalDateTime

fun main(args: Array<String>) {

    val fileName = args[0]
    val file = File(fileName)
    if (!file.exists()){
        println("File does not exist")
        return
    }
    val codeStr = file.readText()
    val fileAst = Parser.parseFile(codeStr)
    var builder = GraphBuilder(fileAst)
    val graph = builder.build()
    val dotName = "/tmp/dot.dot"
    val imgName = "/tmp/graph-${LocalDateTime.now()}.png"
    exportToDot(graph, dotName)
    dotFileToImage(dotName, imgName)
}
