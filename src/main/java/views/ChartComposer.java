package views;

import models.chart.ChartPreferences;
import models.chart.StatisticalAspect;
import models.chart.TimeAspect;
import models.process.Process;
import models.process.filtering.ActivityFilterBy;
import models.process.filtering.CycleTimeFilterBy;
import org.zkoss.chart.Charts;
import org.zkoss.chart.model.CategoryModel;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import utils.ProcessUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class ChartComposer extends SelectorComposer<Window> {

    @Wire
    Charts chartBox;
    @Wire
    Charts chartBar;
    @Wire
    Window win;

    @Wire
    Combobox timeAspectBox;
    @Wire
    Checkbox isPercentage;
    @Wire
    Combobox statisticalAspectBox;
    @Wire
    Combobox timeUnitBox;
    @Wire
    Combobox activityNameBox;
    @Wire
    Combobox cycleTimeFilterBox;
    @Wire
    Combobox activityNameFilterBox;
    @Wire
    Longbox cycleTimeFilteringLongBox;
    @Wire
    Button timeFilterButton;
    @Wire
    Button activityFilterButton;


    List<Process> subProcesses = ProcessUtils.INSTANCE.getSubProcesses();
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


    ListModel<String> cycleTimeFilters = new ListModelList(Arrays.stream(CycleTimeFilterBy.values()).map(Object::toString).collect(toList()));
    ListModel<String> activityFilters = new ListModelList(Arrays.stream(ActivityFilterBy.values()).map(Object::toString).collect(toList()));
    ListModel<String> cycleTimeUnits = new ListModelList(Arrays.stream(TimeUnit.values()).map(Object::toString).collect(toList()));
    ListModel<String> activityNames = new ListModelList(ProcessUtils.INSTANCE.getUniqueActivityNames());
    ListModel<String> timeAspectModel = new ListModelList(Arrays.stream(TimeAspect.values()).map(Object::toString).collect(toList()));
    ListModel<String> statisticalAspectModel = new ListModelList(Arrays.stream(StatisticalAspect.values()).map(Object::toString).collect(toList()));


    public ListModel<String> getCycleTimeFilters() {
        return cycleTimeFilters;
    }

    public void setCycleTimeFilters(ListModel<String> cycleTimeFilters) {
        this.cycleTimeFilters = cycleTimeFilters;
    }

    public ListModel<String> getActivityFilters() {
        return activityFilters;
    }

    public void setActivityFilters(ListModel<String> activityFilters) {
        this.activityFilters = activityFilters;
    }

    public ListModel<String> getCycleTimeUnits() {
        return cycleTimeUnits;
    }

    public void setCycleTimeUnits(ListModel<String> cycleTimeUnits) {
        this.cycleTimeUnits = cycleTimeUnits;
    }

    public ListModel<String> getActivityNames() {
        return activityNames;
    }

    public void setActivityNames(ListModel<String> activityNames) {
        this.activityNames = activityNames;
    }


    public void updatePreferences(Event event) {
        System.out.println(event + " is received");
        chartPreferences.setPercentage(isPercentage.isChecked());
        String timeAspect = timeAspectBox.getSelectedItem().getLabel();
        chartPreferences.setTimeAspect(TimeAspect.valueOf(timeAspect));
        String statisticalAspect = statisticalAspectBox.getSelectedItem().getLabel();
        chartPreferences.setStatisticalAspect(StatisticalAspect.valueOf(statisticalAspect));
        if (event.getTarget().getId().equals("timeFilterButton")) {
            System.out.println("time clicked");

            if (cycleTimeFilterBox.getSelectedItem() != null && timeUnitBox.getSelectedItem() != null && cycleTimeFilteringLongBox.getValue() > 0) {
                ProcessUtils.INSTANCE.addNewSubprocessBasedOnFilter(
                        cycleTimeFilteringLongBox.getValue(),
                        TimeUnit.valueOf(timeUnitBox.getSelectedItem().getLabel()),
                        CycleTimeFilterBy.valueOf(cycleTimeFilterBox.getSelectedItem().getLabel())

                );
            }

        }
        if (event.getTarget().getId().equals("activityFilterButton")) {
            if (activityNameFilterBox.getSelectedItem() != null && activityNameFilterBox.getSelectedItem() != null) {
                ProcessUtils.INSTANCE.addNewSubprocessBasedOnFilter(
                        activityNameBox.getSelectedItem().getLabel(),
                        ActivityFilterBy.valueOf(activityNameFilterBox.getSelectedItem().getLabel())

                );
            }
        }

        Executions.sendRedirect(null);
    }


    private CategoryModel arrangeDataWithRespectToChartPreferencesForBarChart() {
        CategoryModel model = new DefaultCategoryModel();

        subProcesses.forEach(process -> {
            Map<String, Double> activityTime = process.getStats().getActivityTimeMap(chartPreferences);
            process.getStats()
                    .getActivityMFOI().entrySet().stream().map(Map.Entry::getKey).forEach((String activity) -> model.setValue(activity, process.getName(), activityTime.get(activity)));
        });


        return model;
    }

/*
    private BoxPlotModel arrangeDataWithRespectToChartPreferencesForBoxPlot() {
        BoxPlotModel model = new DefaultBoxPlotModel();


        subProcesses.forEach(process -> {
            Map<String, Descriptives> activityDescriptiveMap = process.getStats().getActivityDescriptivesMap(chartPreferences);
            process.getStats()
                    .getActivityMFOI().entrySet().stream().map(Map.Entry::getKey).forEach((String activity) ->{
                        process

                    });


        });
        final Map<String, Descriptives> normalActivityDescriptiveMap = normal.getStats().getActivityDescriptivesMap(chartPreferences);
        final Map<String, Descriptives> deviantActivityDescriptivesMap = deviant.getStats().getActivityDescriptivesMap(chartPreferences);
        deviantActivityDescriptivesMap.keySet().stream().filter(normalActivityDescriptiveMap::containsKey).forEach(
                (activity) -> {
                    Descriptives descriptives = normalActivityDescriptiveMap.get(activity);
                    model.addValue("normal", activity,
                            descriptives.getMin(),
                            descriptives.percentile(25),
                            descriptives.percentile(50),
                            descriptives.percentile(75),
                            descriptives.getMax());
                    descriptives = deviantActivityDescriptivesMap.get(activity);
                    model.addValue("deviant", activity,
                            descriptives.getMin(),
                            descriptives.percentile(25),
                            descriptives.percentile(50),
                            descriptives.percentile(75),
                            descriptives.getMax());

                });
        return model;
    }
*/


    private void arrangeInputFields() {
        timeAspectBox.addEventListener("onChange", this::updatePreferences);
        statisticalAspectBox.addEventListener("onChange", this::updatePreferences);
        isPercentage.addEventListener("onCheck", this::updatePreferences);
        activityFilterButton.addEventListener("onClick", this::updatePreferences);
        timeFilterButton.addEventListener("onClick", this::updatePreferences);
        ((ListModelList<String>) timeAspectModel).addToSelection(chartPreferences.getTimeAspect().toString());
        ((ListModelList<String>) statisticalAspectModel).addToSelection(chartPreferences.getStatisticalAspect().toString());
        isPercentage.setChecked(chartPreferences.getPercentage());
    }


    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        arrangeInputFields();
        CategoryModel model = arrangeDataWithRespectToChartPreferencesForBarChart();
        chartBar.setModel(model);
        chartBar.getYAxis().setMin(0);
        chartBar.getYAxis().setTitle(chartPreferences.getPercentage() ? "Percentage" : "Stacking" + " of Total Time Spent");
        chartBar.getLegend().setReversed(true);
        chartBar.getPlotOptions().getSeries().setStacking(chartPreferences.getPercentage() ? "percent" : "normal");
        chartBar.getCredits().setEnabled(false);
/*
        BoxPlotModel boxPlotData = arrangeDataWithRespectToChartPreferencesForBoxPlot();
        chartBox.getLegend().setEnabled(false);


        int columnCountDeviant = boxPlotData.getDataCount("deviant");
        String[] xLabelNames = IntStream.range(0, columnCountDeviant).mapToObj(i -> boxPlotData.getName("deviant", i)).toArray(String[]::new);
        chartBox.setModel(boxPlotData);
        chartBox.getXAxis().setCategories(xLabelNames);*/

    }
}