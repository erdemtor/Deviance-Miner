package models.chart
import org.zkoss.zk.ui.Session

object ChartPreferences {


    private val sessionToPreference = mutableMapOf<Session, Preferences>()

    fun getPreferencesFromSession(s: Session): Preferences = sessionToPreference.getOrPut(s){ Preferences() }

    class Preferences{
        var performanceMeasure: PerformanceMeasure = PerformanceMeasure.CYCLE_TIME
        var aggregationFunction: AggregationFunction = AggregationFunction.SUM
        var percentage = true
    }


}