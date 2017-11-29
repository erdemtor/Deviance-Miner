package ProcessMiner.models

import ProcessMiner.utils.ChartPreferences
import ProcessMiner.utils.StatisticalAspect.*
import ProcessMiner.utils.TimeAspect.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Created by Erdem on 11-Nov-17.
 */
data class ProcessStats(private val process: Process) {

    private val totalTime = process.traces.map { it.cycleTime }.sum()
    private val totalTimeAveg = process.traces.map { it.cycleTime }.average()
    private val processingTimeTotal = process.traces.map { it.processingTime }.sum()
    private val processingTimeAvg = process.traces.map { it.processingTime }.average()
    private val waitingTimeAvg = process.traces.map { it.waitingTime }.average()
    private val waitingTimeSum = process.traces.map { it.waitingTime }.sum()

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
//            .mapValues {
//        it.value.toBigDecimal()
//                .divide(totalTime.toBigDecimal(), MathContext(5, RoundingMode.HALF_EVEN))
//                .times(100L.toBigDecimal())
//                .toDouble()
//    }
    private val totalAverageActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.average()
            }
//            .mapValues {
//        it.value.toBigDecimal()
//                .divide(totalTimeAveg.toBigDecimal(), MathContext(5, RoundingMode.HALF_EVEN))
//                .times(100L.toBigDecimal())
//                .toDouble()
//    }


    private val processingSumActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.sum().toDouble()
            }
//            .mapValues {
//        it.value.toBigDecimal()
//                .divide(processingTimeTotal.toBigDecimal(), MathContext(5, RoundingMode.HALF_EVEN))
//                .times(100L.toBigDecimal())
//                .toDouble()
//    }
    private val processingMeanActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.processingTime }.average()
            }
//            .mapValues {
//        it.value.toBigDecimal()
//                .divide(processingTimeAvg.toBigDecimal(), MathContext(5, RoundingMode.HALF_EVEN))
//                .times(100L.toBigDecimal())
//                .toDouble()
//    }

    private val waitingSumActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.waitingTime }.sum().toDouble()
            }
//            .mapValues{
//        it.value.toBigDecimal()
//                .divide(waitingTimeSum.toBigDecimal(), MathContext(5, RoundingMode.HALF_EVEN))
//                .times(100L.toBigDecimal())
//                .toDouble() }

        private val waitingAverageActivityToNormalized = process.traces.flatMap { it.timeTakingActivities }
            .groupBy { it.name }
            .mapValues {
                it.value.map { it.waitingTime }.average()
            }
//                .mapValues{
//        it.value.toBigDecimal()
//                .divide(waitingTimeAvg.toBigDecimal(), MathContext(5, RoundingMode.HALF_EVEN))
//                .times(100L.toBigDecimal())
//                .toDouble()
//}





    fun Long.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
    fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
}