package models.process

import models.process.filtering.ActivityFilterBy
import java.util.concurrent.TimeUnit
import models.process.filtering.ActivityFilterBy.*
import models.process.filtering.CycleTimeFilterBy

/**
 * Created by Erdem on 11-Nov-17.
 */
data class Process(val traces: List<Trace>, val name: String) {
    val stats  by lazy {
        ProcessStats(this)
    }
        fun partitionProcessByCycleTime(numericalValue: Long, unit: TimeUnit, filterBy : CycleTimeFilterBy): Pair<Process, Process> {
            val cycleTimeInMinutes = unit.toMinutes(numericalValue)
            val (normal, deviant) = when(filterBy){
                CycleTimeFilterBy.LESS_THAN -> traces.partition { it.cycleTime < cycleTimeInMinutes }
                CycleTimeFilterBy.GREATER_THAN -> traces.partition { it.cycleTime > cycleTimeInMinutes }
            }
            return Pair(Process(normal,  filterBy.toString().toLowerCase() + " " + numericalValue + " " + unit.toString().toLowerCase()), Process(deviant, "not_used"))
        }

    fun partitionProcessByActivityCriterion(activityName: String, criterion: ActivityFilterBy): Pair<Process, Process> {
        val(passing, failing) = when(criterion){
            CONTAINS -> traces.partition{it.containsActivity(activityName)}
            NOT_CONTAINS -> traces.partition{!it.containsActivity(activityName)}
            ENDS_WITH -> traces.partition { it.endsWithActivity(activityName)  }
            STARTS_WITH -> traces.partition { it.startsWithActivity(activityName)  }
            NOT_ENDS_WITH -> traces.partition { !it.endsWithActivity(activityName)  }
        }

        return Pair(Process(passing,
                criterion.toString().toLowerCase() + " " + activityName), Process(failing, "not_used"))
    }


}

