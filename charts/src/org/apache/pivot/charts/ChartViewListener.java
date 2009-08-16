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

/**
 * Chart view listener interface.
 */
public interface ChartViewListener {
    /**
     * Fired when a chart view's data changes.
     *
     * @param chartView
     * @param previousChartData
     */
    public void chartDataChanged(ChartView chartView, List<?> previousChartData);

    /**
     * Fired when a chart view's series name key changes.
     *
     * @param chartView
     * @param previousSeriesNameKey
     */
    public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey);

    /**
     * Fired when a chart view's title changes.
     *
     * @param chartView
     * @param previousTitle
     */
    public void titleChanged(ChartView chartView, String previousTitle);

    /**
     * Fired when a chart view's horizontal axis label changes.
     *
     * @param chartView
     * @param previousHorizontalAxisLabel
     */
    public void horizontalAxisLabelChanged(ChartView chartView, String previousHorizontalAxisLabel);

    /**
     * Fired when a chart view's vertical axis label changes.
     *
     * @param chartView
     * @param previousVerticalAxisLabel
     */
    public void verticalAxisLabelChanged(ChartView chartView, String previousVerticalAxisLabel);

    /**
     * Fired when a chart view's "show legend" flag changes.
     *
     * @param chartView
     */
    public void showLegendChanged(ChartView chartView);
}
