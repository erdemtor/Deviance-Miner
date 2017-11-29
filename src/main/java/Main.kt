/**
 * Created by Erdem on 28-Sep-17.
 */

import org.nield.kotlinstatistics.Descriptives
import ProcessMiner.utils.LogParser.parseFileToProcess

/**
 * Created by Erdem on 27-Sep-17.
 */

fun main(args: Array<String>) {
    val process = parseFileToProcess("reviewing.xes")






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





