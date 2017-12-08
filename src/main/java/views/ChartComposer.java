package views;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import models.chart.AggregationFunction;
import models.chart.ChartPreferences;
import models.chart.PerformanceMeasure;
import models.process.Process;
import models.process.filtering.ActivityFilterBy;
import models.process.filtering.CycleTimeFilterBy;
import org.zkoss.chart.Charts;
import org.zkoss.chart.model.CategoryModel;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import utils.ProcessUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class ChartComposer extends SelectorComposer<Window> {

    private final String ProcessDetails = "ProcessDetails";



    @Wire
    Charts chartBox;
    @Wire
    Charts chartBar;
    @Wire
    Window win;

    @Wire
    Button variantUploader;

    @Wire
    Button removeVariant;

    @Wire
    Combobox timeAspectBox;
    @Wire
    Checkbox isPercentage;
    @Wire
    Combobox statisticalAspectBox;


    @Wire
    Combobox variantsBox;

    
    ProcessUtils processUtils;

    {

        Session session = Sessions.getCurrent();
        if (session.getAttribute(ProcessDetails) == null){
            session.setAttribute(ProcessDetails, new ProcessUtils());
        }
        this.processUtils = (ProcessUtils) session.getAttribute(ProcessDetails);

    }

 //   List<Process> subProcesses = ProcessUtils.INSTANCE.getSubProcesses();


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
    public ListModel<String> getActiveVariants() {
        return activeVariants;
    }

    public void setActiveVariants(ListModel<String> activeVariants) {
        this.activeVariants = activeVariants;
    }


    ListModel<String> cycleTimeFilters = new ListModelList(Arrays.stream(CycleTimeFilterBy.values()).map(Object::toString).collect(toList()));
    ListModel<String> activityFilters = new ListModelList(Arrays.stream(ActivityFilterBy.values()).map(Object::toString).collect(toList()));
    ListModel<String> cycleTimeUnits = new ListModelList(Arrays.stream(TimeUnit.values()).map(Object::toString).collect(toList()));
    ListModel<String> activityNames = new ListModelList(this.processUtils.getUniqueActivityNames());
    ListModel<String> timeAspectModel = new ListModelList(Arrays.stream(PerformanceMeasure.values()).map(Object::toString).collect(toList()));
    ListModel<String> statisticalAspectModel = new ListModelList(Arrays.stream(AggregationFunction.values()).map(Object::toString).collect(toList()));


    ListModel<String> activeVariants = new ListModelList(this.processUtils.getVariants().stream().map(Process::getName).collect(toList()));


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


    private void addVariant(UploadEvent event) throws IOException {
        InputStream targetStream;
        try{
            Reader reader = event.getMedia().getReaderData();
            targetStream =
                    new ByteArrayInputStream(CharStreams.toString(reader)
                            .getBytes(Charsets.UTF_8));
        }
        catch (IllegalStateException e){
            targetStream = event.getMedia().getStreamData();

        }
        System.out.println(event.getMedia().getName());
        this.processUtils.parseInputStreamToProcess(targetStream, event.getMedia().getName());
        updatePreferences(event);

    }

    private void updateSessionData(){

        Sessions.getCurrent().setAttribute(ProcessDetails, this.processUtils);
    }

    private void removeVariant(Event event) {
        if (variantsBox.getSelectedItem() != null){
            this.processUtils.removeVariant(variantsBox.getSelectedItem().getLabel());
            updatePreferences(event);
        }

    }


    public void updatePreferences(Event event) {
        System.out.println(event + " is received");
        ChartPreferences chartPreferences = processUtils.getChartPreferences();
        chartPreferences.setPercentage(isPercentage.isChecked());
        String timeAspect = timeAspectBox.getSelectedItem().getLabel();
        chartPreferences.setPerformanceMeasure(PerformanceMeasure.valueOf(timeAspect));
        String statisticalAspect = statisticalAspectBox.getSelectedItem().getLabel();
        chartPreferences.setAggregationFunction(AggregationFunction.valueOf(statisticalAspect));
        updateSessionData();

        Executions.sendRedirect(null);
    }


    private CategoryModel arrangeDataWithRespectToChartPreferencesForBarChart() {
        CategoryModel model = new DefaultCategoryModel();
        List<Process> subProcesses = this.processUtils.getVariants();
        ChartPreferences chartPreferences = processUtils.getChartPreferences();

        subProcesses.forEach(process -> {
            Map<String, Double> activityTime = process.getStats().getActivityTimeMap(chartPreferences);
            process.getStats()
                    .getActivityMFOI()
                    .entrySet()
                    .stream()
                    .map(Map.Entry::getKey)
                    .forEach((String activity) -> model.setValue(activity, process.getName(), activityTime.get(activity)));
        });


        return model;
    }



    private void arrangeInputFields() {
        ChartPreferences chartPreferences = processUtils.getChartPreferences();

        timeAspectBox.addEventListener("onChange", this::updatePreferences);
        statisticalAspectBox.addEventListener("onChange", this::updatePreferences);
        isPercentage.addEventListener("onCheck", this::updatePreferences);
        removeVariant.addEventListener("onClick", this::removeVariant);
        variantUploader.addEventListener("onUpload", (EventListener<UploadEvent>) event -> addVariant(event));
        ((ListModelList<String>) timeAspectModel).addToSelection(chartPreferences.getPerformanceMeasure().toString());
        ((ListModelList<String>) statisticalAspectModel).addToSelection(chartPreferences.getAggregationFunction().toString());
        isPercentage.setChecked(chartPreferences.getPercentage());
    }




    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        ChartPreferences chartPreferences = processUtils.getChartPreferences();

        arrangeInputFields();
        CategoryModel model = arrangeDataWithRespectToChartPreferencesForBarChart();
        chartBar.setModel(model);
        chartBar.getYAxis().setMin(0);
        chartBar.getYAxis()
                .setTitle((chartPreferences.getPercentage() ? "Percentage of " : "Stacking of ") +
                        chartPreferences.getAggregationFunction().toString().replace('_', ' ').toLowerCase()
                        + " "
                        + chartPreferences.getPerformanceMeasure().toString().replace('_', ' ').toLowerCase() );
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