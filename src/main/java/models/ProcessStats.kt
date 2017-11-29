package models

import utils.ChartPreferences
import utils.StatisticalAspect.MEAN
import utils.StatisticalAspect.SUM
import utils.TimeAspect.*

/**
 * Created by Erdem on 11-Nov-17.
 */
data class ProcessStats(private val process: Process) {


    fun getActivityTimeMap(pref: ChartPreferences): Map<String, Double>? {
        if (pref.timeAspect == PROCESSING) {
            if (pref.statisticalAspect == SUM) {
                return processingSumActivityToNormalized
            } else if (pref.statisticalAspect == MEAN) {
                return processingMeanActivityToNormalized
            }
        } else if (pref.timeAspect == WAITING) {
            if (pref.statisticalAspect == SUM) {
                return waitingSumActivityToNormalized
            } else if (pref.statisticalAspect == MEAN) {
                return waitingAverageActivityToNormalized
            }

        } else if (pref.timeAspect == ALL) {
            if (pref.statisticalAspect == SUM) {
                return totalSumActivityToNormalized
            } else if (pref.statisticalAspect == MEAN) {
                return totalAverageActivityToNormalized
            }
        }
        return null
    }


    private val totalSumActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.sum().toDouble()
            }

    private val totalAverageActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.average()
            }


    private val processingSumActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.sum().toDouble()
            }

    private val processingMeanActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.average()
            }


    private val waitingSumActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.waitingTime }.sum().toDouble()
            }


    private val waitingAverageActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.waitingTime }.average()
            }

}