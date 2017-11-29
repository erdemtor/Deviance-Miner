package models

import org.joda.time.Duration

/**
 * Created by Erdem on 11-Nov-17.
 */
data class Trace(val id: String, val events: List<Event>){
    val cycleTime by lazy {
        Duration(events.first().time, events.last().time)
    }
    val eventBigrams by lazy {
        this.events.zip(this.events.drop(1))
    }
    val interEventTimes by lazy {
        this.eventBigrams.map({ Duration(it.first.time, it.second.time) })
    }

    fun getEventsInOrder(): String{
        return events.map { it.name }.joinToString(separator = "")
    }
}