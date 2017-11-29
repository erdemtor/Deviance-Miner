package utils

import models.Event
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
            val events = logTrace.asSequence().filter { it.attributes["lifecycle:transition"]?.toString()?.equals("complete") ?: true }.map{
                Event(it.attributes["concept:name"].toString(), (it.attributes["time:timestamp"] as XAttributeTimestamp).value)
            }.toList()
            Trace(logTrace.attributes["concept:name"].toString(), events)
        }

        println("Parsing completed")
        return Process(traces)
    }
}