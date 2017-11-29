package utils

import models.Event
import models.LifeCycle
import models.Process
import models.Trace
import org.deckfour.xes.`in`.XesXmlParser
import org.deckfour.xes.model.XAttributeTimestamp
import java.nio.file.Paths

/**
 * Created by Erdem on 11-Nov-17.
 */
object LogParser {

    fun parseFileToProcess(path: String): Process{
        println("Parsing has started")
        val log = XesXmlParser().parse(Paths.get(path).toFile()).first()
        val traces: List<Trace> = log.map { logTrace ->
            val events = logTrace.asSequence().filter { isStartOrCompleteEvent(it.attributes["lifecycle:transition"].toString()) }.map{
                Event(it.attributes["concept:name"].toString(),
                        (it.attributes["time:timestamp"] as XAttributeTimestamp).value,
                        LifeCycle.fromString(it.attributes["lifecycle:transition"].toString()))
            }.toList()
            Trace(logTrace.attributes["concept:name"].toString(), events)
        }

        println("Parsing completed")
        return Process(traces)
    }

    val p = parseFileToProcess("/Users/etor/IdeaProjects/deviance_miner/src/PurchasingExample.xes")


    private fun isStartOrCompleteEvent(lifeCycle: String) = listOf("start", "complete").contains(lifeCycle?.trim()?.toLowerCase())
}