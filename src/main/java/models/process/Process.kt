package models.process

import models.process.filtering.ActivityFilterBy
import java.util.concurrent.TimeUnit
import models.process.filtering.ActivityFilterBy.*
import models.process.filtering.CycleTimeFilterBy
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math3.ml.distance.DistanceMeasure

/**
 * Created by Erdem on 11-Nov-17.
 */
data class Process(val traces: List<Trace>, val name: String) {

    val stats: ProcessStats
    val activityIndexMap: Map<String, Int>

    init {
        activityIndexMap = traces.flatMap { it.activities }.map { it.name }.distinct().mapIndexed { index, s -> Pair(s, index) }.toMap()
        stats = ProcessStats(this)

    }

    fun getClusterVariants(k: Int = 3): List<Process> {
        traces.forEach { it.process = this }
        val clusters: List<MutableList<Trace>> = KMeansPlusPlusClusterer<Trace>(k, 10000).cluster(traces).map { it.points }
        return clusters.mapIndexed { index, mutableList -> Process(mutableList, this.name + " " + (index+1)) }
                .apply {
                    forEach { p ->
                        p.traces.forEach { it.process = p }

                    }
                }

    }


    fun partitionProcessByCycleTime(numericalValue: Long, unit: TimeUnit, filterBy: CycleTimeFilterBy): Pair<Process, Process> {
        val cycleTimeInMinutes = unit.toMinutes(numericalValue)
        val (normal, deviant) = when (filterBy) {
            CycleTimeFilterBy.LESS_THAN -> traces.partition { it.cycleTime < cycleTimeInMinutes }
            CycleTimeFilterBy.GREATER_THAN -> traces.partition { it.cycleTime > cycleTimeInMinutes }
        }
        return Pair(Process(normal, filterBy.toString().toLowerCase() + " " + numericalValue + " " + unit.toString().toLowerCase()), Process(deviant, "not_used"))
    }

    fun partitionProcessByActivityCriterion(activityName: String, criterion: ActivityFilterBy): Pair<Process, Process> {
        val (passing, failing) = when (criterion) {
            CONTAINS -> traces.partition { it.containsActivity(activityName) }
            NOT_CONTAINS -> traces.partition { !it.containsActivity(activityName) }
            ENDS_WITH -> traces.partition { it.endsWithActivity(activityName) }
            STARTS_WITH -> traces.partition { it.startsWithActivity(activityName) }
            NOT_ENDS_WITH -> traces.partition { !it.endsWithActivity(activityName) }
        }

        return Pair(Process(passing,
                criterion.toString().toLowerCase() + " " + activityName), Process(failing, "not_used"))
    }


}

