package models

import org.joda.time.DateTime
import org.joda.time.Duration

data class Activity(val name: String, val startTime: DateTime, val endTime: DateTime) {
    constructor(name: String, time: DateTime): this(name,time, time)

    private var enablementTime = startTime

    fun updateEnablementTime(newEnablementTime: DateTime) {
        enablementTime = newEnablementTime;
        waitingTime = Duration(enablementTime, startTime).standardMinutes

    }

    fun isTimeTaking() = endTime != startTime

    val processingTime = Duration(startTime, endTime).standardMinutes
    var waitingTime = Duration(enablementTime, startTime).standardMinutes
}