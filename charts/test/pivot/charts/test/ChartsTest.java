/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import pivot.wtk.Mouse;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class ChartsTest implements Application {
    private class ChartViewMouseButtonHandler implements ComponentMouseButtonListener {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        @SuppressWarnings("unchecked")
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
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
                    + seriesDictionary.get(seriesNameKey) + "\".", window);
            }

            return false;
        }
    }

    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("charts_test.wtkx")));
        window.setTitle("Charts Test");

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

        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
