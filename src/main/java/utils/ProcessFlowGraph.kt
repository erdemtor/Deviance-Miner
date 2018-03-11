package utils

import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

class ProcessFlowGraph :  SimpleDirectedWeightedGraph<String, WeightedEdge>(WeightedEdge::class.java)

class WeightedEdge(val weight: Int) : DefaultWeightedEdge()