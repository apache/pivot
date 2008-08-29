package pivot.charts.test;

import pivot.beans.BeanDictionary;
import pivot.charts.AreaChartView;
import pivot.charts.BarChartView;
import pivot.charts.ChartView;
import pivot.charts.HighLowChartView;
import pivot.charts.LineChartView;
import pivot.charts.PieChartView;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.Mouse;
import pivot.wtkx.WTKXSerializer;

public class ChartsTest implements Application {
    private class ChartViewMouseButtonHandler implements ComponentMouseButtonListener {
        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
        }

        @SuppressWarnings("unchecked")
        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            ChartView chartView = (ChartView)component;
            ChartView.Element element = chartView.getElementAt(x, y);

            if (element != null) {
                int seriesIndex = element.getSeriesIndex();
                int elementIndex = element.getElementIndex();

                String elementLabel;
                ChartView.CategorySequence categories = chartView.getCategories();
                if (categories.getLength() > 0) {
                    elementLabel = "\"" + chartView.getCategories().get(elementIndex).getLabel() + "\"";
                } else {
                    elementLabel = Integer.toString(elementIndex);
                }

                List<?> chartData = chartView.getChartData();
                Object series = chartData.get(seriesIndex);

                Dictionary<String, Object> seriesDictionary;
                if (series instanceof Dictionary<?, ?>) {
                    seriesDictionary = (Dictionary<String, Object>)series;
                } else {
                    seriesDictionary = new BeanDictionary(series);
                }

                String seriesNameKey = chartView.getSeriesNameKey();

                Alert.alert("You clicked element " + elementLabel + " in \""
                    + seriesDictionary.get(seriesNameKey) + "\".", frame);
            }
        }
    }

    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("charts_test.wtkx")));
        frame.setTitle("Charts Test");

        ChartViewMouseButtonHandler chartViewMouseButtonHandler = new ChartViewMouseButtonHandler();

        PieChartView pieChartView = (PieChartView)wtkxSerializer.getObjectByName("pieCharts.pieChartView");
        pieChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        BarChartView categoryBarChartView = (BarChartView)wtkxSerializer.getObjectByName("barCharts.categoryBarChartView");
        categoryBarChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        BarChartView xyBarChartView = (BarChartView)wtkxSerializer.getObjectByName("barCharts.xyBarChartView");
        xyBarChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        LineChartView categoryLineChartView = (LineChartView)wtkxSerializer.getObjectByName("lineCharts.categoryLineChartView");
        categoryLineChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        LineChartView xyLineChartView = (LineChartView)wtkxSerializer.getObjectByName("lineCharts.xyLineChartView");
        xyLineChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        AreaChartView categoryAreaChartView = (AreaChartView)wtkxSerializer.getObjectByName("areaCharts.categoryAreaChartView");
        categoryAreaChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        AreaChartView xyAreaChartView = (AreaChartView)wtkxSerializer.getObjectByName("areaCharts.xyAreaChartView");
        xyAreaChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        HighLowChartView highLowChartView = (HighLowChartView)wtkxSerializer.getObjectByName("highLowCharts.highLowChartView");
        highLowChartView.getComponentMouseButtonListeners().add(chartViewMouseButtonHandler);

        frame.setPreferredSize(640, 480);
        frame.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
