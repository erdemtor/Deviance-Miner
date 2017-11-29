package models

import java.util.concurrent.TimeUnit

/**
 * Created by Erdem on 11-Nov-17.
 */
data class Process(val traces: List<Trace>) {
    val stats  by lazy {
        ProcessStats(this)
    }
        fun partitionProcessByCycleTime(numericalValue: Long, unit: TimeUnit): Pair<Process, Process> {
            val cycleTimeInSeconds = unit.toSeconds(numericalValue)
            val (normal, deviant) = traces.partition { it.cycleTime < cycleTimeInSeconds }
            return Pair(Process(normal), Process(deviant))
        }
}

