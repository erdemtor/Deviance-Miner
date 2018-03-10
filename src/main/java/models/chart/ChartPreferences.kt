package models.chart

import org.zkoss.zk.ui.Session

class ChartPreferences {


    var performanceMeasure: PerformanceMeasure = PerformanceMeasure.CYCLE_TIME
    var aggregationFunction: AggregationFunction = AggregationFunction.SUM
    var timeUnit: TimeUnit = TimeUnit.MINUTES
    var percentage = true
    var clusterCount: Int = 1
    var stageSize: Int = 2


}