package models.process

import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.alg.flow.EdmondsKarpMFImpl
import utils.ProcessFlowGraph
import utils.WeightedEdge

class StageDecomposer(val flowGraphAsMap: Map<String, Map<String, Int>>) {
    val flowGraph: ProcessFlowGraph
    val sink: String
    val source: String
    val sourceMinCut: Int
    val minStateSize = 4
    val WT: Double

    init {
        flowGraph = createANewFlow()
        source = flowGraph.vertexSet().minBy { flowGraph.inDegreeOf(it) } ?: ""
        sink = flowGraph.vertexSet().minBy { flowGraph.outDegreeOf(it) } ?: ""
        WT = flowGraph.edgeSet().sumByDouble { flowGraph.getEdgeWeight(it) }
        sourceMinCut = GetMinCut(flowGraph).first.toInt()
        val decomposed = decomposeIt()
    }



    fun decomposeIt(): StageDecomposion {
        val candidateNodes = selectCandidateNodes()
        var currentBestSD = StageDecomposion(mutableListOf(flowGraph.vertexSet()), mutableListOf())
        var newBestSD = StageDecomposion(mutableListOf(flowGraph.vertexSet()), mutableListOf())
        var bestCutPoint = Vertex("", listOf())
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
        return currentBestSD
    }


    fun getModularityScore(sd: StageDecomposion): Double {
        val sdCopy = sd.copy()
        sdCopy.transitionNodes.forEach {
            it.from.add(it.node)
            it.to.add(it.node)
        }

       return sdCopy.stages.sumByDouble { it.calculateEquation1() }
    }


    fun Stage.calculateEquation1(): Double {


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

    fun reArrangeDecompositon(currentBestSD: StageDecomposion, cutStage: Stage, preStage: Stage, sucStage: Stage, transitionNode: String): StageDecomposion {
        val stageDecomposionCopy = currentBestSD.copy()
        stageDecomposionCopy.stages.remove(cutStage)
        stageDecomposionCopy.stages.add(preStage)
        stageDecomposionCopy.stages.add(sucStage)
        val newTransitionNode = TransitionNode(transitionNode, preStage, sucStage)
        stageDecomposionCopy.transitionNodes.add(newTransitionNode)
        return stageDecomposionCopy
    }

    fun cutGraph(v: Vertex, cutStage: Stage): Pair<Stage, Stage> {
        val graph = createANewFlow()
        graph.removeVertex(v.v)
        v.cutSet.forEach { graph.removeEdge(it.first, it.second) }
        val connectivityInspector = ConnectivityInspector(graph)
        val sourceSubGraph = connectivityInspector.connectedSetOf(source)
        val preStage = cutStage.plus(sourceSubGraph) as Stage
        val sucStage = cutStage.minus(sourceSubGraph) as Stage

        return Pair(preStage, sucStage)
    }


    fun StageDecomposion.findCutStage(v: Vertex): Stage = this.stages.find { it.contains(v.v) } ?: mutableSetOf()


    fun selectCandidateNodes(): MutableList<Vertex> {
        return flowGraph.vertexSet()
                .asSequence()
                .filter { it != sink && it != source }
                .map { Vertex(it, findMinCutWithoutTheGivenVertex(it).second) }
                .toMutableList()
    }

    fun findMinCutWithoutTheGivenVertex(v: String): Pair<Double, List<Pair<String, String>>> {
        val copyGraph = createANewFlow()
        copyGraph.removeVertex(v)
        return GetMinCut(copyGraph)
    }

    fun GetMinCut(g: ProcessFlowGraph): Pair<Double, List<Pair<String, String>>> {
        val edmondsKarpMaximumFlow = EdmondsKarpMFImpl(g)
        val minCut = edmondsKarpMaximumFlow.calculateMinCut(source, sink)

        return Pair(minCut, edmondsKarpMaximumFlow.cutEdges.map { Pair(g.getEdgeSource(it), g.getEdgeTarget(it)) })
    }


    fun createANewFlow(): ProcessFlowGraph {
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

data class TransitionNode(val node: String, val from: Stage, val to: Stage)

data class StageDecomposion(val stages: MutableList<Stage>, val transitionNodes: MutableList<TransitionNode>)

data class Vertex(val v: String, val cutSet: List<Pair<String, String>>)