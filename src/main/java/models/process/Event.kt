package models.process

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.util.*

/**
 * Created by Erdem on 11-Nov-17.
 */

data class Event(val name: String, val time: DateTime, val lifeCycle: LifeCycle){
    constructor(name: String, time:String, lifeCycle: LifeCycle) : this(name,  ISODateTimeFormat.dateTime().parseDateTime(time), lifeCycle)
    constructor(name: String, date: Date, lifeCycle: LifeCycle) : this(name, DateTime(date), lifeCycle )

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other is Event){
            return other.name == this.name
        }

        return false
    }
}