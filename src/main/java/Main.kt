/**
 * Created by Erdem on 28-Sep-17.
 */

import models.Process
import models.Trace
import org.jfree.chart.ChartFactory
import org.jfree.chart.plot.PlotOrientation
import org.joda.time.format.ISODateTimeFormat
import org.nield.kotlinstatistics.Descriptives
import utils.ChartDrawer
import utils.DataAdapter.prepareHistogramData
import utils.LogParser.parseFileToProcess
import java.util.concurrent.TimeUnit

/**
 * Created by Erdem on 27-Sep-17.
 */

fun main(args: Array<String>) {
    val process = parseFileToProcess("BPI Challenge 2017.xes")
    val (normal, deviant) = process.partitionProcessByCycleTime(1206890, TimeUnit.SECONDS)

    interTimesPrint(normal)
    "AAAAAA".println()
    interTimesPrint(deviant)





    val cycleData = prepareHistogramData(normal.stats.cycleTimesDouble, 100)
    val cycleDataDeviant = prepareHistogramData(deviant.stats.cycleTimesDouble, 100)
    val chart = ChartFactory.createHistogram("Normal", "cycleTime", "Number Of Process",  cycleData, PlotOrientation.VERTICAL,  false, false, false)
    val chart2 = ChartFactory.createHistogram("Deviant", "cycleTime", "Number Of Process",  cycleDataDeviant, PlotOrientation.VERTICAL,  false, false, false)


/*
    ChartDrawer("cycletime", chart).prepareAndDraw()
    ChartDrawer("interArrival", chart2).prepareAndDraw()*/


}


fun interTimesPrint(p: Process){
    p.stats.eventBigramStats.forEach { t, u ->
        t.first.print()
        "   -->  ".print()
        t.second.println()
        u.summarise()
        "======".println()
    }
}



fun Descriptives.summarise(){
    println("max: ${this.max} \nmin: ${this.min} \nmean: ${this.mean} \nstd: ${this.standardDeviation}\nsize: ${this.size}")


}



fun List<Any>.println() = this.forEach { println(it)}

fun Any.println(){
    println(this)
}fun Any.print(){
    print(this)
}





