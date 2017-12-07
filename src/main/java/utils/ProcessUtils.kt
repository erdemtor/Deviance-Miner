package utils

import models.chart.ChartPreferences
import models.process.Event
import models.process.LifeCycle
import models.process.Process
import models.process.Trace
import models.process.filtering.ActivityFilterBy
import models.process.filtering.CycleTimeFilterBy
import org.deckfour.xes.`in`.XesXmlParser
import org.deckfour.xes.model.XAttributeTimestamp
import org.deckfour.xes.model.XLog
import org.zkoss.zhtml.Li
import java.io.InputStream
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Created by Erdem on 11-Nov-17.
 */
class ProcessUtils {

    var count = 0

    fun parseFileToProcess(pathString: String) {
        println("Parsing has started")
        val path = Paths.get(pathString)
        val log = XesXmlParser().parse(path.toFile()).first()
        variants.add(fromXESLogToProcess(log))
    }
    fun parseInputStreamToProcess(iS: InputStream) {
        println("Parsing has started")
        val log = XesXmlParser().parse(iS).first()
        variants.add(fromXESLogToProcess(log))
    }

    val variants: MutableList<Process> = mutableListOf()
    var chartPreferences = ChartPreferences()

    fun fromXESLogToProcess(log: XLog): Process {
        val traces: List<Trace> = log.map { logTrace ->
            val events = logTrace.asSequence().filter { isStartOrCompleteEvent(it.attributes["lifecycle:transition"].toString()) }.map{
                Event(it.attributes["concept:name"].toString(),
                        (it.attributes["time:timestamp"] as XAttributeTimestamp).value,
                        LifeCycle.fromString(it.attributes["lifecycle:transition"].toString()))
            }.toList()
            Trace(logTrace.attributes["concept:name"].toString(), events)
        }

        println("Parsing completed")
        return Process(traces, count++.toString())
    }



    fun getUniqueActivityNames() = this.variants
            .flatMap { it.traces }
            .flatMap { it.activities }
            .map { it.name }
            .distinct()
            .sorted()





    private fun isStartOrCompleteEvent(lifeCycle: String) = listOf("start", "complete").contains(lifeCycle?.trim()?.toLowerCase())




}