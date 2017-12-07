package models.chart

import org.zkoss.zk.ui.Session

class ChartPreferences {


    var performanceMeasure: PerformanceMeasure = PerformanceMeasure.CYCLE_TIME
    var aggregationFunction: AggregationFunction = AggregationFunction.SUM
    var percentage = true


}