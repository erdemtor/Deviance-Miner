package models

import org.joda.time.Duration
import org.nield.kotlinstatistics.Descriptives
import org.nield.kotlinstatistics.descriptiveStatistics

/**
 * Created by Erdem on 11-Nov-17.
 */
data class ProcessStats(val process: Process) {
    val traces = process.traces
    val cycleTimes: List<Long> = traces.asSequence().map(Trace::cycleTime).map({it.standardSeconds}).toList()
    val cycleTimesDouble: DoubleArray = traces.asSequence().map(Trace::cycleTime).map({it.standardSeconds}).map(Long::toDouble).toList().toDoubleArray()
    val cycleTimeStats: Descriptives = cycleTimes.descriptiveStatistics
    val interEventTimes: DoubleArray = traces.flatMap(Trace::interEventTimes).map { it.standardSeconds }.map(Long::toDouble).toDoubleArray()
    val eventBigramStats: Map<Pair<String, String>, Descriptives> = traces.flatMap(Trace::eventBigrams)
            .groupBy({ Pair(it.first.name, it.second.name)})
            .mapValues { it.value.map { Duration(it.first.time, it.second.time).standardSeconds }.descriptiveStatistics }
    val uniqueEvents: Set<String> = traces.flatMap(Trace::events).map(Event::name).toSet()
    val taskOccurrenceStats = traces.flatMap(Trace::events).groupingBy { it.name }.eachCount()
}