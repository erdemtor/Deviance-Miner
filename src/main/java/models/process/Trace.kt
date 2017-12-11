package models.process

import com.google.common.collect.Lists
import org.apache.commons.math3.ml.clustering.Clusterable
import org.joda.time.Duration

/**
 * Created by Erdem on 11-Nov-17.
 */
data class Trace(val id: String) : Clusterable{

    override fun getPoint(): DoubleArray {
        val doubleArray = DoubleArray(process.activityIndexMap.size)
        this.activities.groupingBy { it.name }.eachCount().forEach{
            name, count ->
                doubleArray[process.activityIndexMap[name]!!] = count.toDouble()
        }
        return doubleArray
    }

    lateinit var events: List<Event>
    lateinit var activities: List<Activity>
    lateinit var process: Process

    constructor( id :String,  events: List<Event>): this(id) {
        this.events = events.sortedBy { it.time }
        this.activities = events.groupBy { it.name }.entries.flatMap {
            val name = it.key
            Lists.partition(it.value.sortedBy { it.time },2)
                    .map {
                        if (it.size ==2)
                            Activity(name, startTime = it[0].time, endTime = it[1].time)
                        else
                            Activity(name, time = it[0].time)
                    }
        }.sortedBy { it.startTime }

        activities.zip(activities.drop(1)).forEach {
            it.second.updateEnablementTime(it.first.endTime)
        }

    }



    val cycleTime by lazy {
        Duration(events.first().time, events.last().time).standardMinutes
    }
    val processingTime by lazy {  activities.map { it.processingTime }.sum() }

    val waitingTime  by lazy { activities.map { it.waitingTime }.sum() }

    fun containsActivity(name: String) = activities.asSequence().map { it.name }.contains(name)
    fun endsWithActivity(name: String) = activities.last().name == name
    fun startsWithActivity(name: String) = activities.first().name == name

    fun firstIndexOfActivity(name: String) =  activities.indexOfFirst { it.name == name }





    val timeTakingActivities by lazy { this.activities.filter(Activity::isTimeTaking) }

    fun getEventsInOrder(): String = events.joinToString(separator = "") { it.name }
}