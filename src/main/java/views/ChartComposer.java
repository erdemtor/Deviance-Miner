package views;

import models.Process;
import utils.ChartPreferences;
import utils.LogParser;
import utils.StatisticalAspect;
import utils.TimeAspect;
import kotlin.Pair;
import org.zkoss.chart.Charts;
import org.zkoss.chart.model.CategoryModel;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class ChartComposer extends SelectorComposer<Window> {

    @Wire
    Charts chart;
    @Wire
    Window win;

    @Wire
    Combobox timeAspectBox;
    @Wire
    Checkbox isPercentage;
    @Wire
    Combobox statisticalAspectBox;

    Process p = LogParser.INSTANCE.getP();
    ChartPreferences chartPreferences = ChartPreferences.INSTANCE;

    public ListModel<String> getTimeAspectModel() {
        return timeAspectModel;
    }

    public void setTimeAspectModel(ListModel<String> timeAspectModel) {
        this.timeAspectModel = timeAspectModel;
    }

    public ListModel<String> getStatisticalAspectModel() {
        return statisticalAspectModel;
    }

    public void setStatisticalAspectModel(ListModel<String> statisticalAspectModel) {
        this.statisticalAspectModel = statisticalAspectModel;
    }

    ListModel<String> timeAspectModel = new ListModelList(Arrays.stream(TimeAspect.values()).map(Object::toString).collect(toList()));
    ListModel<String> statisticalAspectModel = new ListModelList(Arrays.stream(StatisticalAspect.values()).map(Object::toString).collect(toList()));




    public void updatePreferences(Event event) {
        System.out.println(event  +  " is received");

        chartPreferences.setPercentage(isPercentage.isChecked());
        String timeAspect = timeAspectBox.getSelectedItem().getLabel();
        chartPreferences.setTimeAspect(TimeAspect.valueOf(timeAspect));
        String statisticalAspect = statisticalAspectBox.getSelectedItem().getLabel();
        chartPreferences.setStatisticalAspect(StatisticalAspect.valueOf(statisticalAspect));
        Executions.sendRedirect(null);
    }


    private CategoryModel arrangeDataWithRespectToChartPreferences() {
        CategoryModel model = new DefaultCategoryModel();
        Pair<Process, Process> processProcessPair = p.partitionProcessByCycleTime(21, TimeUnit.DAYS);
        Process normal = processProcessPair.getFirst();
        Process deviant = processProcessPair.getSecond();
            normal.getStats()
                    .getActivityTimeMap(chartPreferences)
                    .forEach(
                            (activityName, durationPercentage) -> model.setValue(activityName, "normal", durationPercentage));

            deviant.getStats()
                    .getActivityTimeMap(chartPreferences)
                    .forEach(
                            (activityName, durationPercentage) -> model.setValue(activityName, "deviant", durationPercentage));


        return model;
    }


    private void arrangeInputFields(){
        timeAspectBox.addEventListener("onChange",this::updatePreferences);
        statisticalAspectBox.addEventListener("onChange",this::updatePreferences);
        isPercentage.addEventListener("onCheck", this::updatePreferences);

        ((ListModelList<String>)timeAspectModel).addToSelection(chartPreferences.getTimeAspect().toString());
        ((ListModelList<String>)statisticalAspectModel).addToSelection(chartPreferences.getStatisticalAspect().toString());
        isPercentage.setChecked(chartPreferences.getPercentage());
    }


    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        arrangeInputFields();
        CategoryModel model = arrangeDataWithRespectToChartPreferences();
        chart.setModel(model);
        chart.getYAxis().setMin(0);
        chart.getYAxis().setTitle(chartPreferences.getPercentage()? "Percentage" : "Stacking"   + " of Total Time Spent");
        chart.getLegend().setReversed(true);
        chart.getPlotOptions().getSeries().setStacking(chartPreferences.getPercentage()? "percent":"normal");
        chart.getCredits().setEnabled(false);

    }
}