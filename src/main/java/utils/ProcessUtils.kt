package utils

import models.process.Event
import models.process.LifeCycle
import models.process.Process
import models.process.Trace
import models.process.filtering.ActivityFilterBy
import models.process.filtering.CycleTimeFilterBy
import org.deckfour.xes.`in`.XesXmlParser
import org.deckfour.xes.model.XAttributeTimestamp
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Created by Erdem on 11-Nov-17.
 */
object ProcessUtils {

    fun parseFileToProcess(path: String): Process {
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
        return Process(traces, "Main")
    }

    private val subProcessList = mutableListOf<Process>()

    val subProcesses:List<Process> = subProcessList


    fun addNewSubprocessBasedOnFilter(number: Long, unit:TimeUnit, criterion: CycleTimeFilterBy){
        subProcessList.add(wholeProcess.partitionProcessByCycleTime(number, unit, criterion).first)
    }
    fun addNewSubprocessBasedOnFilter(activityName: String, criterion: ActivityFilterBy){
        subProcessList.add(wholeProcess.partitionProcessByActivityCriterion(activityName, criterion).first)
    }


    private val wholeProcess = parseFileToProcess("/Users/etor/IdeaProjects/deviance_miner/src/PurchasingExample.xes")


    val uniqueActivityNames = wholeProcess.traces.flatMap { it.activities }.map { it.name }.distinct().sorted()

    private fun isStartOrCompleteEvent(lifeCycle: String) = listOf("start", "complete").contains(lifeCycle?.trim()?.toLowerCase())


}