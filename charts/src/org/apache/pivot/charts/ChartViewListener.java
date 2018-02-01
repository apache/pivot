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
package org.apache.pivot.charts;

import org.apache.pivot.collections.List;
import org.apache.pivot.util.ListenerList;

/**
 * Chart view listener interface.
 */
public interface ChartViewListener {
    /**
     * Chart view listener list.
     */
    public static class Listeners extends ListenerList<ChartViewListener> implements ChartViewListener {
        @Override
        public void chartDataChanged(ChartView chartView, List<?> previousChartData) {
            forEach(listener -> listener.chartDataChanged(chartView, previousChartData));
        }

        @Override
        public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey) {
            forEach(listener -> listener.seriesNameKeyChanged(chartView, previousSeriesNameKey));
        }

        @Override
        public void titleChanged(ChartView chartView, String previousTitle) {
            forEach(listener -> listener.titleChanged(chartView, previousTitle));
        }

        @Override
        public void horizontalAxisLabelChanged(ChartView chartView, String previousXAxisLabel) {
            forEach(listener -> listener.horizontalAxisLabelChanged(chartView, previousXAxisLabel));
        }

        @Override
        public void verticalAxisLabelChanged(ChartView chartView, String previousYAxisLabel) {
            forEach(listener -> listener.verticalAxisLabelChanged(chartView, previousYAxisLabel));
        }

        @Override
        public void showLegendChanged(ChartView chartView) {
            forEach(listener -> listener.showLegendChanged(chartView));
        }
    }

    /**
     * Fired when a chart view's data changes.
     *
     * @param chartView The chart that is changing.
     * @param previousChartData Previous data for the chart.
     */
    public void chartDataChanged(ChartView chartView, List<?> previousChartData);

    /**
     * Fired when a chart view's series name key changes.
     *
     * @param chartView The chart that is changing.
     * @param previousSeriesNameKey Previous value of the key for the series name.
     */
    public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey);

    /**
     * Fired when a chart view's title changes.
     *
     * @param chartView The chart that changed.
     * @param previousTitle Previous title for this chart.
     */
    public void titleChanged(ChartView chartView, String previousTitle);

    /**
     * Fired when a chart view's horizontal axis label changes.
     *
     * @param chartView The chart that has changed.
     * @param previousHorizontalAxisLabel Previous value of the horizontal axis label.
     */
    public void horizontalAxisLabelChanged(ChartView chartView, String previousHorizontalAxisLabel);

    /**
     * Fired when a chart view's vertical axis label changes.
     *
     * @param chartView The chart that has changed.
     * @param previousVerticalAxisLabel Previous value of the vertical axis label.
     */
    public void verticalAxisLabelChanged(ChartView chartView, String previousVerticalAxisLabel);

    /**
     * Fired when a chart view's "show legend" flag changes.
     *
     * @param chartView The chart that has changed.
     */
    public void showLegendChanged(ChartView chartView);
}
