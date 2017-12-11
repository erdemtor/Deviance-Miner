package models.process

import models.chart.TimeUnit
import org.joda.time.DateTimeConstants.*
import org.joda.time.Duration
import java.math.BigDecimal

fun Duration.convertTo(unit: TimeUnit): Double{
    return when(unit){
        TimeUnit.MINUTES -> (this.millis / MILLIS_PER_MINUTE.toDouble())
        TimeUnit.DAYS -> (this.millis / MILLIS_PER_DAY.toDouble())
        TimeUnit.HOURS -> (this.millis / MILLIS_PER_HOUR.toDouble())
        TimeUnit.SECONDS -> (this.millis / MILLIS_PER_SECOND.toDouble())
    }
}

fun Double.roundToLong() = BigDecimal.valueOf(this).setScale(0, BigDecimal.ROUND_HALF_EVEN).toLong()