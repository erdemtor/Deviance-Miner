package models.process

import models.chart.TimeUnit
import org.joda.time.DateTime
import org.joda.time.Duration

data class Activity(val name: String, val startTime: DateTime, val endTime: DateTime) {
    constructor(name: String, time: DateTime): this(name,time, time)

    var belongsToStage: String = ""
    private var enablementTime = startTime
    var timeUnit: TimeUnit = TimeUnit.MINUTES

    fun updateEnablementTime(newEnablementTime: DateTime) {
        enablementTime = newEnablementTime;
        waitingTime = Duration(enablementTime, startTime).convertTo(timeUnit)
    }

    fun updateTimeUnit(newUnit: TimeUnit) {
        timeUnit = newUnit
        waitingTime = Duration(enablementTime, startTime).convertTo(timeUnit)
        processingTime = Duration(startTime, endTime).convertTo(timeUnit)
    }

    fun isTimeTaking() = endTime != startTime

    var processingTime = Duration(startTime, endTime).convertTo(timeUnit)
    var waitingTime = Duration(enablementTime, startTime).convertTo(timeUnit)
}

