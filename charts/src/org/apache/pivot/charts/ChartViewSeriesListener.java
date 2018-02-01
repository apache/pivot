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

import org.apache.pivot.util.ListenerList;

/**
 * Chart view series listener interface.
 */
public interface ChartViewSeriesListener {
    /**
     * Chart view series listener list.
     */
    public static class Listeners extends ListenerList<ChartViewSeriesListener>
        implements ChartViewSeriesListener {
        @Override
        public void seriesInserted(ChartView chartView, int index) {
            forEach(listener -> listener.seriesInserted(chartView, index));
        }

        @Override
        public void seriesRemoved(ChartView chartView, int index, int count) {
            forEach(listener -> listener.seriesRemoved(chartView, index, count));
        }

        @Override
        public void seriesUpdated(ChartView chartView, int index) {
            forEach(listener -> listener.seriesUpdated(chartView, index));
        }

        @Override
        public void seriesCleared(ChartView chartView) {
            forEach(listener -> listener.seriesCleared(chartView));
        }

        @Override
        public void seriesSorted(ChartView chartView) {
            forEach(listener -> listener.seriesSorted(chartView));
        }
    }

    /**
     * Fired when a series is inserted into a chart view's data set.
     *
     * @param chartView The chart that has changed.
     * @param index Index of the series that was inserted.
     */
    public void seriesInserted(ChartView chartView, int index);

    /**
     * Fired when a series is removed from a chart view's data set.
     *
     * @param chartView The chart that has changed.
     * @param index Starting index of the removed series.
     * @param count Number of series removed.
     */
    public void seriesRemoved(ChartView chartView, int index, int count);

    /**
     * Fired when a chart view's series data is cleared.
     *
     * @param chartView The chart that has changed.
     */
    public void seriesCleared(ChartView chartView);

    /**
     * Fired when a series is updated in a chart view's data set.
     *
     * @param chartView The chart that has changed.
     * @param index Index of the series that was updated.
     */
    public void seriesUpdated(ChartView chartView, int index);

    /**
     * Fired when a chart view's series data is sorted.
     *
     * @param chartView Chart that changed.
     */
    public void seriesSorted(ChartView chartView);
}
