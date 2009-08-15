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
package org.apache.pivot.charts.skin;

import java.awt.Color;

import org.apache.pivot.charts.ChartView;
import org.apache.pivot.charts.ChartViewCategoryListener;
import org.apache.pivot.charts.ChartViewListener;
import org.apache.pivot.charts.ChartViewSeriesListener;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.skin.ComponentSkin;

/**
 * Abstract base class for chart view skins.
 *
 */
public abstract class ChartViewSkin extends ComponentSkin
    implements ChartView.Skin,
        ChartViewListener, ChartViewCategoryListener, ChartViewSeriesListener {
    private Color backgroundColor = Color.WHITE;

    @Override
    public void install(Component component) {
        super.install(component);

        // Add listeners
        ChartView chartView = (ChartView)component;
        chartView.getChartViewListeners().add(this);
        chartView.getChartViewCategoryListeners().add(this);
        chartView.getChartViewSeriesListeners().add(this);
    }

    @Override
    public void uninstall() {
        // Remove listeners
        ChartView chartView = (ChartView)getComponent();
        chartView.getChartViewListeners().remove(this);
        chartView.getChartViewCategoryListeners().remove(this);
        chartView.getChartViewSeriesListeners().remove(this);

        super.uninstall();
    }

    public void layout() {
        // No-op
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    // Chart view events
    public void chartDataChanged(ChartView chartView, List<?> previousChartData) {
        repaintComponent();
    }

    public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey) {
        repaintComponent();
    }

    public void titleChanged(ChartView chartView, String previousTitle) {
        repaintComponent();
    }

    public void horizontalAxisLabelChanged(ChartView chartView, String previousHorizontalAxisLabel) {
        repaintComponent();
    }

    public void verticalAxisLabelChanged(ChartView chartView, String previousVerticalAxisLabel) {
        repaintComponent();
    }

    public void showLegendChanged(ChartView chartView) {
        repaintComponent();
    }

    // Chart view category events
    public void categoryInserted(ChartView chartView, int index) {
        repaintComponent();
    }

    public void categoriesRemoved(ChartView chartView, int index, Sequence<ChartView.Category> categories) {
        repaintComponent();
    }

    public void categoryKeyChanged(ChartView chartView, int index, String previousKey) {
        repaintComponent();
    }

    public void categoryLabelChanged(ChartView chartView, int index, String previousLabel) {
        repaintComponent();
    }

    // Chart view series events
    public void seriesInserted(ChartView chartView, int index) {
        repaintComponent();
    }

    public void seriesRemoved(ChartView chartView, int index, int count) {
        repaintComponent();
    }

    public void seriesUpdated(ChartView chartView, int index) {
        repaintComponent();
    }

    public void seriesCleared(ChartView chartView) {
        repaintComponent();
    }

    public void seriesSorted(ChartView chartView) {
        repaintComponent();
    }
}
