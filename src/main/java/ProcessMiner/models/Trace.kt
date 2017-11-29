package ProcessMiner.models

import com.google.common.collect.Lists
import org.joda.time.Duration

/**
 * Created by Erdem on 11-Nov-17.
 */
data class Trace(val id: String){

    lateinit var events: List<Event>
    lateinit var activities: List<Activity>
    constructor( id :String,  events: List<Event>): this(id) {
        this.events = events
        this.activities = events.groupBy { it.name }.entries.flatMap {
            val name = it.key
            Lists.partition(it.value,2)
                    .map {
                        if (it.size ==2)
                            Activity(name, startTime = it[0].time, endTime = it[1].time)
                        else
                            Activity(name,time = it[0].time)
                    }
        }.sortedBy { it.startTime }

        activities.zip(activities.drop(1)).forEach {
            it.second.updateEnablementTime(it.first.endTime)
        }


    }
    val cycleTime by lazy {
        Duration(events.first().time, events.last().time).standardSeconds
    }
    val processingTime by lazy {  activities.map { it.processingTime }.sum() }

    val waitingTime  by lazy { activities.map { it.waitingTime }.sum() }









    val timeTakingActivities by lazy { this.activities.filter(Activity::isTimeTaking) }

    fun getEventsInOrder(): String = events.joinToString(separator = "") { it.name }
}