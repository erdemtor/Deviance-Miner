package models.process

import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.alg.flow.EdmondsKarpMFImpl
import utils.ProcessFlowGraph
import utils.WeightedEdge

class StageDecomposer(private val flowGraphAsMap: Map<String, Map<String, Int>>) {
    private val flowGraph: ProcessFlowGraph = createANewFlow()
    private val  sink = flowGraph.vertexSet().minBy { flowGraph.outDegreeOf(it) } ?: ""
    private val source: String = flowGraph.vertexSet().minBy { flowGraph.inDegreeOf(it) } ?: ""
    private val sourceMinCut = GetMinCut(flowGraph).first.toInt()
    private val WT= flowGraph.edgeSet().sumByDouble { flowGraph.getEdgeWeight(it) }

    fun decompose(minStateSize: Int = 2): StageDecomposion {
        val candidateNodes = selectCandidateNodes()
        var currentBestSD = StageDecomposion(mutableListOf(flowGraph.vertexSet()), mutableListOf())
        var newBestSD = StageDecomposion(mutableListOf(flowGraph.vertexSet()), mutableListOf())
        var bestCutPoint = Vertex("", listOf(), 0.0)
        while (!candidateNodes.isEmpty()) {
            for (v in candidateNodes) {
                val cutStage = currentBestSD.findCutStage(v)
                val (preStage, sucStage) = cutGraph(v, cutStage)
                if (preStage.size >= minStateSize && sucStage.size >= minStateSize) {
                    val newSD = reArrangeDecompositon(currentBestSD, cutStage, preStage, sucStage, v.v)
                    if (getModularityScore(newSD) > getModularityScore(newBestSD)){
                        newBestSD = newSD
                        bestCutPoint = v
                    }
                }

            }

            if (newBestSD.stages.size != currentBestSD.stages.size){
                currentBestSD = newBestSD
                candidateNodes.remove(bestCutPoint)
            }else{
                break
            }
        }
        currentBestSD.removeTransitionNodesFromStageNodes()
        return currentBestSD
    }


    private fun getModularityScore(sd: StageDecomposion): Double {
        val sdCopy = sd.copy()
        sdCopy.transitionNodes.forEach {
            it.from.add(it.node)
            it.to.add(it.node)
        }

       return sdCopy.stages.sumByDouble { it.calculateEquation1(this.flowGraph, WT) }
    }




    private fun reArrangeDecompositon(currentBestSD: StageDecomposion, cutStage: Stage, preStage: Stage, sucStage: Stage, transitionNode: String): StageDecomposion {
        val stageDecomposionCopy = currentBestSD.copy()
        stageDecomposionCopy.stages.remove(cutStage)
        stageDecomposionCopy.stages.add(preStage)
        stageDecomposionCopy.stages.add(sucStage)
        val newTransitionNode = TransitionNode(transitionNode, preStage, sucStage)
        stageDecomposionCopy.transitionNodes.add(newTransitionNode)
        return stageDecomposionCopy
    }

    private fun cutGraph(v: Vertex, cutStage: Stage): Pair<Stage, Stage> {
        val graph = createANewFlow()
        graph.removeVertex(v.v)
        v.cutSet.forEach { graph.removeEdge(it.first, it.second) }
        val connectivityInspector = ConnectivityInspector(graph)
        val sourceSubGraph = connectivityInspector.connectedSetOf(source)
        val preStage = cutStage.intersect(sourceSubGraph).plus(v.v) as Stage
        val sucStage = cutStage.minus(preStage) as Stage

        return Pair(preStage, sucStage)
    }


    fun StageDecomposion.findCutStage(v: Vertex): Stage = this.stages.find { it.contains(v.v) } ?: mutableSetOf()


    private fun selectCandidateNodes(): MutableList<Vertex> {
        return flowGraph.vertexSet()
                .asSequence()
                .filter { it != sink && it != source }
                .map{Pair(it, findMinCutWithoutTheGivenVertex(it))}
                .filter{it.second.first < sourceMinCut && it.second.first > 0}
                .map { Vertex(it.first, it.second.second, it.second.first) }
                .toMutableList()
    }

    private fun findMinCutWithoutTheGivenVertex(v: String): Pair<Double, List<Pair<String, String>>> {
        val copyGraph = createANewFlow()
        copyGraph.removeVertex(v)
        return GetMinCut(copyGraph)
    }

    private fun GetMinCut(g: ProcessFlowGraph): Pair<Double, List<Pair<String, String>>> {
        val edmondsKarpMaximumFlow = EdmondsKarpMFImpl(g)
        val minCut = edmondsKarpMaximumFlow.calculateMinCut(source, sink)

        return Pair(minCut, edmondsKarpMaximumFlow.cutEdges.map { Pair(g.getEdgeSource(it), g.getEdgeTarget(it)) })
    }


    private fun createANewFlow(): ProcessFlowGraph {
        val newFlow = ProcessFlowGraph()
        flowGraphAsMap.entries.flatMap { it.value.keys + it.key }.distinct().forEach { newFlow.addVertex(it) }
        flowGraphAsMap.forEach { from, u ->
            u.forEach { to, weight ->
                val weightedEdge = WeightedEdge(weight)
                newFlow.addEdge(from, to, weightedEdge)
                newFlow.setEdgeWeight(weightedEdge, weight.toDouble())
            }
        }
        return newFlow
    }

}


typealias Stage = MutableSet<String>
fun Stage.calculateEquation1(flowGraph : ProcessFlowGraph, WT: Double): Double {


    val innerEdgeWeightSum = sumByDouble { fromNode ->
        sumByDouble { toNode ->
            val edge = flowGraph.getEdge(fromNode, toNode)
            return if (edge != null) {
                flowGraph.getEdgeWeight(edge)
            }else{
                0.0
            }
        }
    }

    val outerEdgeWeightSum  = sumByDouble {
        stageNode ->
        val incomingSums =  flowGraph.incomingEdgesOf(stageNode).map{ flowGraph.getEdgeSource(it)}.filter { outside -> !this.contains(outside) }.sumByDouble {
            outside ->
            val edgeFromOutsideToStageNode = flowGraph.getEdge(outside, stageNode)
            return flowGraph.getEdgeWeight(edgeFromOutsideToStageNode)
        }

        val outGoingSums = flowGraph.outgoingEdgesOf(stageNode).map{ flowGraph.getEdgeTarget(it)}.filter { outside -> !this.contains(outside) }.sumByDouble {
            outside ->
            val edgeFromStageToOutside = flowGraph.getEdge(stageNode, outside)
            return flowGraph.getEdgeWeight(edgeFromStageToOutside)
        }
        return incomingSums + outGoingSums
    }

    return innerEdgeWeightSum/ WT - Math.pow(outerEdgeWeightSum/WT, 2.0)
}
data class TransitionNode(val node: String, val from: Stage, val to: Stage)

data class StageDecomposion(val stages: MutableList<Stage>, val transitionNodes: MutableList<TransitionNode>){
    fun removeTransitionNodesFromStageNodes(){
        transitionNodes.forEach{
            it.from.remove(it.node)
            it.to.remove(it.node)
            if (it.from.size <= it.to.size){
                it.from.add(it.node)
            }else{
                it.to.add(it.node)
            }
        }
    }
}

data class Vertex(val v: String, val cutSet: List<Pair<String, String>>, val minCut: Double)