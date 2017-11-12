package utils

import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.ui.ApplicationFrame
import org.jfree.ui.RefineryUtilities
import java.awt.Dimension

/**
 * Created by Erdem on 11-Nov-17.
 */
class ChartDrawer : ApplicationFrame {

    constructor(title: String?, chart: JFreeChart) : super(title) {
        val panel = ChartPanel(chart)
        panel.preferredSize = Dimension(560, 367)
        contentPane = panel

    }



    fun prepareAndDraw(){
        RefineryUtilities.centerFrameOnScreen(this)
        this.pack()
        this.isVisible = true
    }

}
