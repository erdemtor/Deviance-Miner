package models

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.util.*

/**
 * Created by Erdem on 11-Nov-17.
 */

data class Event(val name: String, val time: DateTime){
    constructor(name: String, time:String) : this(name,  ISODateTimeFormat.dateTime().parseDateTime(time))
    constructor(name: String, date: Date) : this(name, DateTime(date) )

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other is Event){
            return other.name == this.name
        }

        return false
    }
}