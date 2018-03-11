package utils

import models.chart.ChartPreferences
import models.process.Event
import models.process.LifeCycle
import models.process.Process
import models.process.Trace
import org.deckfour.xes.`in`.XesXmlParser
import org.deckfour.xes.model.XAttributeTimestamp
import org.deckfour.xes.model.XLog
import java.io.InputStream
import java.nio.file.Paths

/**
 * Created by Erdem on 11-Nov-17.
 */
class ProcessUtils {

    var count = 0

    fun parseFileToProcess(pathString: String, name: String) {
        println("Parsing has started")
        val path = Paths.get(pathString)
        val log = XesXmlParser().parse(path.toFile()).first()
        val parsedVariant = fromXESLogToProcess(log, name)
        val clusterVariants = parsedVariant.getClusterVariants(k = chartPreferences.clusterCount)
        variants[name] = clusterVariants
        mainVariants[name] = parsedVariant
    }
    fun parseInputStreamToProcess(iS: InputStream, name: String) {
        println("Parsing has started")
        val log = XesXmlParser().parse(iS).first()
        val parsedVariant = fromXESLogToProcess(log, name)
        val clusterVariants = parsedVariant.getClusterVariants(k = chartPreferences.clusterCount)
        variants[name] = clusterVariants
        mainVariants[name] = parsedVariant
    }



    fun updateGranularity(){
        this.variants.values.flatMap { it }.forEach{it.updateGranularity(chartPreferences.granularityLevel)}
        mainVariants.mapValues { it.value.granularity = chartPreferences.granularityLevel }
    }


    fun updateClusterCount(){
        this.variants = mainVariants.mapValues {it.value.getClusterVariants(chartPreferences.clusterCount) }.toMutableMap()
    }

    fun updateTimeUnit(){
        this.variants.values.flatMap { it }.forEach{it.updateTimeUnits(chartPreferences.timeUnit)}
    }



    var variants: MutableMap<String, List<Process>> = mutableMapOf()
    private val mainVariants: MutableMap<String, Process> = mutableMapOf()
    var chartPreferences = ChartPreferences()

    fun fromXESLogToProcess(log: XLog, name: String): Process {
        val traces: List<Trace> = log.map { logTrace ->
            val events = logTrace.asSequence().filter { isStartOrCompleteEvent(it.attributes["lifecycle:transition"].toString()) }.map{
                Event(it.attributes["concept:name"].toString(),
                        (it.attributes["time:timestamp"] as XAttributeTimestamp).value,
                        LifeCycle.fromString(it.attributes["lifecycle:transition"].toString()))
            }.toList()
            Trace(logTrace.attributes["concept:name"].toString(), events)
        }

        println("Parsing completed")
        return Process(traces, name.split(".").first())
    }

    fun removeVariant(name: String) {
        this.variants.remove(name)
        this.mainVariants.remove(name)
    }



    private fun isStartOrCompleteEvent(lifeCycle: String) = listOf("start", "complete", "null").contains(lifeCycle?.trim()?.toLowerCase())




}