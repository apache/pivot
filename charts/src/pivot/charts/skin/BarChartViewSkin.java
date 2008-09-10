/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import pivot.charts.BarChartView;
import pivot.charts.ChartView;
import pivot.collections.List;
import pivot.wtk.Component;

public class BarChartViewSkin extends ChartViewSkin {
    private boolean stacked = false;
    private boolean threeDimensional = false;

    @Override
    public void install(Component component) {
        validateComponentType(component, BarChartView.class);
        super.install(component);
    }

    public ChartView.Element getElementAt(int x, int y) {
        ChartView.Element element = null;

        ChartEntity chartEntity = getChartEntityAt(x, y);
        if (chartEntity instanceof CategoryItemEntity) {
            CategoryItemEntity categoryItemEntity = (CategoryItemEntity)chartEntity;
            CategoryDataset dataset = categoryItemEntity.getDataset();

            String columnKey = (String)categoryItemEntity.getColumnKey();
            int columnIndex = dataset.getColumnIndex(columnKey);

            String rowKey = (String)categoryItemEntity.getRowKey();
            int rowIndex = dataset.getRowIndex(rowKey);

            element = new ChartView.Element(rowIndex, columnIndex);
        } else if (chartEntity instanceof XYItemEntity) {
            XYItemEntity xyItemEntity = (XYItemEntity)chartEntity;
            element = new ChartView.Element(xyItemEntity.getSeriesIndex(),
                xyItemEntity.getItem());
        }

        return element;
    }

    protected JFreeChart createChart() {
        BarChartView chartView = (BarChartView)getComponent();

        String title = chartView.getTitle();
        String horizontalAxisLabel = chartView.getHorizontalAxisLabel();
        String verticalAxisLabel = chartView.getVerticalAxisLabel();
        boolean showLegend = chartView.getShowLegend();

        String seriesNameKey = chartView.getSeriesNameKey();
        List<?> chartData = chartView.getChartData();

        // TODO Make plot orientation a style property

        JFreeChart chart;
        ChartView.CategorySequence categories = chartView.getCategories();
        if (categories.getLength() > 0) {
            CategorySeriesDataset dataset = new CategorySeriesDataset(categories, seriesNameKey, chartData);

            if (stacked && threeDimensional) {
                chart = ChartFactory.createStackedBarChart3D(title, horizontalAxisLabel, verticalAxisLabel,
                    dataset, PlotOrientation.VERTICAL, showLegend, false, false);
            } else if (stacked) {
                chart = ChartFactory.createStackedBarChart(title, horizontalAxisLabel, verticalAxisLabel,
                    dataset, PlotOrientation.VERTICAL, showLegend, false, false);
            } else if (threeDimensional) {
                chart = ChartFactory.createBarChart3D(title, horizontalAxisLabel, verticalAxisLabel,
                    dataset, PlotOrientation.VERTICAL, showLegend, false, false);
            } else {
                chart = ChartFactory.createBarChart(title, horizontalAxisLabel, verticalAxisLabel,
                    dataset, PlotOrientation.VERTICAL, showLegend, false, false);
            }
        } else {
            // TODO Make the dateAxis argument a style property
            chart = ChartFactory.createXYBarChart(title, horizontalAxisLabel, false, verticalAxisLabel,
                new IntervalSeriesDataset(seriesNameKey, chartData),
                PlotOrientation.VERTICAL, showLegend, false, false);
        }

        return chart;
    }

    public boolean isStacked() {
        return stacked;
    }

    public void setStacked(boolean stacked) {
        this.stacked = stacked;
        repaintComponent();
    }

    public boolean isThreeDimensional() {
        return threeDimensional;
    }

    public void setThreeDimensional(boolean threeDimensional) {
        this.threeDimensional = threeDimensional;
        repaintComponent();
    }
}
