package utils

object ChartPreferences {

    var timeAspect: TimeAspect = TimeAspect.ALL
    var statisticalAspect: StatisticalAspect = StatisticalAspect.SUM
    var percentage = true

    inline operator fun component1() = timeAspect
    inline operator fun component2() = statisticalAspect


}