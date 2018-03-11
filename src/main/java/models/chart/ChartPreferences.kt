package models.chart
class ChartPreferences {


    var performanceMeasure: PerformanceMeasure = PerformanceMeasure.CYCLE_TIME
    var aggregationFunction: AggregationFunction = AggregationFunction.SUM
    var granularityLevel: Granularity = Granularity.ACTIVITY
    var timeUnit: TimeUnit = TimeUnit.MINUTES
    var percentage = true
    var clusterCount: Int = 1
    var stageSize: Int = 2


}