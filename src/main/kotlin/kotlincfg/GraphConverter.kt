package main.kotlin.kotlincfg

import guru.nidi.graphviz.engine.Format
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import java.io.File
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser


fun exportToDot(graph: DefaultDirectedGraph<DisplayNode, DefaultEdge>, fileName: String) {
    val dotFile: File = File(fileName);
    dotFile.printWriter().use { outputStream ->
        outputStream.println("digraph cfg {" +
                "{${graph.vertexSet().joinToString("; ") {displayNode -> nodeText(displayNode)}}}")
        graph.edgeSet().forEach { edge: DefaultEdge ->
            outputStream.println("${nodeText(graph.getEdgeSource(edge))} -> ${nodeText(graph.getEdgeTarget(edge))}")
        }
        outputStream.println("}")
    }
}

fun nodeText(node: DisplayNode): String {
    return "\"${node.id}. ${node.text}\""
}

fun dotFileToImage(sourceFileName: String, resultFileName: String) {
    val g = Parser.read(File(sourceFileName))
    Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(File(resultFileName))
}
