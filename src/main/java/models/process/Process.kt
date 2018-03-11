package models.process

import models.chart.Granularity
import models.chart.TimeUnit
import models.process.filtering.ActivityFilterBy
import models.process.filtering.ActivityFilterBy.*
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer
import org.apache.commons.math3.ml.distance.CanberraDistance

/**
 * Created by Erdem on 11-Nov-17.
 */
data class Process(val traces: List<Trace>, val name: String, var granularity: Granularity = Granularity.ACTIVITY) {


    val activityIndexMap: Map<String, Int> = traces.flatMap { it.activities }.map { it.name }.distinct().mapIndexed { index, s -> Pair(s, index) }.toMap()
    val flowGraphAsMap: Map<String, Map<String, Int>> =  this.traces.flatMap { it.getActivityBigrams() }
            .groupBy { it.first }
            .mapValues { it.value.groupingBy { it.second }.eachCount() }
    val stageDecomposer: StageDecomposer = StageDecomposer(flowGraphAsMap)
    var stats: ProcessStats = ProcessStats(this)
    var minimumStageSize: Int = -1

    fun updateTimeUnits(newUnit: TimeUnit){
        this.traces.forEach{it.updateTimeUnit(newUnit)}
        updateStats()
    }


    fun getClusterVariants(k: Int = 3): List<Process> {
        traces.forEach { it.process = this }
        val clusters: List<MutableList<Trace>> = KMeansPlusPlusClusterer<Trace>(k, 10000, CanberraDistance()).cluster(traces).map { it.points }
        return clusters
                .mapIndexed { index, mutableList ->
                    Process(mutableList, this.name + " " + (index+1) + " - (" + mutableList.size  + " cases)", this.granularity) }
                .apply {
                    forEach { p ->
                        p.traces.forEach { it.process = p }

                    }
                }
    }

    fun updateGranularity(granularity: Granularity){
        when(granularity){
            Granularity.STAGE -> updateGranularityToStage(2)
            Granularity.ACTIVITY -> updateGranularityToActivity()
        }
        updateStats()
    }
    private fun updateGranularityToActivity(){
        this.granularity = Granularity.ACTIVITY
    }

    private fun updateGranularityToStage(minimumStageSize: Int){
        this.granularity = Granularity.STAGE
        if (minimumStageSize != this.minimumStageSize){
            val (stages, _) = this.stageDecomposer.decompose(minimumStageSize)
            this.traces.forEach {
                it.activities.forEach{
                    activity ->
                    var stageID = stages.indexOfFirst { it.contains(activity.name) } + 1
                    activity.belongsToStage = "Stage: " +stageID
                }
            }
            this.minimumStageSize = minimumStageSize
        }

    }

    private fun updateStats(){
        stats= ProcessStats(this)
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

