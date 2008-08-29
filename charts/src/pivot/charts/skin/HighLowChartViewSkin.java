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
import org.jfree.chart.entity.XYItemEntity;

import pivot.charts.ChartView;
import pivot.charts.HighLowChartView;
import pivot.charts.ChartView.Element;
import pivot.collections.List;
import pivot.wtk.Component;

public class HighLowChartViewSkin extends ChartViewSkin {
    private boolean candlestick = false;

    @Override
    public void install(Component component) {
        validateComponentType(component, HighLowChartView.class);
        super.install(component);
    }

    public Element getElementAt(int x, int y) {
        ChartView.Element element = null;

        ChartEntity chartEntity = getChartEntityAt(x, y);
        if (chartEntity instanceof XYItemEntity) {
            XYItemEntity xyItemEntity = (XYItemEntity)chartEntity;
            element = new ChartView.Element(xyItemEntity.getSeriesIndex(),
                xyItemEntity.getItem());
        }

        return element;
    }

    @Override
    protected JFreeChart createChart() {
        HighLowChartView chartView = (HighLowChartView)getComponent();

        String title = chartView.getTitle();
        String horizontalAxisLabel = chartView.getHorizontalAxisLabel();
        String verticalAxisLabel = chartView.getVerticalAxisLabel();
        boolean showLegend = chartView.getShowLegend();

        String seriesNameKey = chartView.getSeriesNameKey();
        List<?> chartData = chartView.getChartData();

        JFreeChart chart;
        OHLCSeriesDataset dataset = new OHLCSeriesDataset(seriesNameKey, chartData);

        if (candlestick) {
            chart = ChartFactory.createCandlestickChart(title,
                horizontalAxisLabel, verticalAxisLabel, dataset, showLegend);
        } else {
            chart = ChartFactory.createHighLowChart(title,
                horizontalAxisLabel, verticalAxisLabel, dataset, showLegend);
        }

        return chart;
    }

    public boolean isCandlestick() {
        return candlestick;
    }

    public void setCandlestick(boolean candlestick) {
        this.candlestick = candlestick;
        repaintComponent();
    }
}
