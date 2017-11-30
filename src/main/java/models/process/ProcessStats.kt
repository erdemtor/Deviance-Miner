package models.process

import models.chart.ChartPreferences
import models.chart.StatisticalAspect.*
import models.chart.TimeAspect.*
import org.nield.kotlinstatistics.Descriptives
import org.nield.kotlinstatistics.descriptiveStatistics

/**
 * Created by Erdem on 11-Nov-17.
 */
data class ProcessStats(private val process: Process) {


    fun getActivityTimeMap(pref: ChartPreferences): Map<String, Double>? {
        return when {
            pref.timeAspect == PROCESSING -> when {
                ChartPreferences.statisticalAspect == SUM -> return processingActivityStats.mapValues { it.value.sum }
                ChartPreferences.statisticalAspect == MEAN -> return processingActivityStats.mapValues { it.value.mean }
                ChartPreferences.statisticalAspect == OCCURRENCE_COUNT -> return processingActivityStats.mapValues { it.value.size.toDouble() }
                else -> {
                    null
                }

            }
            pref.timeAspect == WAITING -> when {
                ChartPreferences.statisticalAspect == SUM -> return waitingActivityStats.mapValues { it.value.sum }
                ChartPreferences.statisticalAspect == MEAN -> return waitingActivityStats.mapValues { it.value.mean }
                ChartPreferences.statisticalAspect == OCCURRENCE_COUNT -> return waitingActivityStats.mapValues { it.value.size.toDouble() }
                else -> {
                    null
                }
            }
            pref.timeAspect == ALL -> when {
                pref.statisticalAspect == SUM -> return totalActivityStats.mapValues { it.value.sum }
                pref.statisticalAspect == MEAN -> return totalActivityStats.mapValues { it.value.mean }
                pref.statisticalAspect == OCCURRENCE_COUNT -> return totalActivityStats.mapValues { it.value.size.toDouble() }
                else -> {
                    null
                }
            }
            else -> {
                null
            }
        }

    }

    fun getActivityDescriptivesMap(pref: ChartPreferences): Map<String, Descriptives>? {
        return when {
            pref.timeAspect == PROCESSING -> processingActivityStats
            pref.timeAspect == WAITING -> waitingActivityStats
            pref.timeAspect == ALL -> totalActivityStats
            else -> null
        }
    }


    val totalActivityStats = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.descriptiveStatistics
            }


    private val processingActivityStats = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.descriptiveStatistics
            }


    private val waitingActivityStats = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.waitingTime }.descriptiveStatistics
            }

    private val allUniqueActivityNames = process.traces.flatMap { it.activities }.map { it.name }.distinct()

    val activityMFOI = allUniqueActivityNames.map {
        activityName -> Pair(activityName, process.traces.asSequence()
            .filter { it.containsActivity(activityName) }
            .map { it.firstIndexOfActivity(activityName) }
            .average())
    }.sortedByDescending { it.second }.toMap()


}