package utils
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.DirectedWeightedMultigraph
import org.jgrapht.graph.SimpleDirectedWeightedGraph

import org.jgrapht.graph.SimpleWeightedGraph

class ProcessFlowGraph :  SimpleDirectedWeightedGraph<String, WeightedEdge>(WeightedEdge::class.java)

class WeightedEdge(val weight: Int) : DefaultWeightedEdge()