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
package pivot.charts;

/**
 * <p>Chart view series listener interface.</p>
 *
 * @author gbrown
 */
public interface ChartViewSeriesListener {
    /**
     * Fired when a series is inserted into a chart view's data set.
     *
     * @param chartView
     * @param index
     */
    public void seriesInserted(ChartView chartView, int index);

    /**
     * Fired when a series is removed from a chart view's data set.
     *
     * @param chartView
     * @param index
     * @param count
     */
    public void seriesRemoved(ChartView chartView, int index, int count);

    /**
     * Fired when a series is updated in a chart view's data set.
     * @param chartView
     * @param index
     */
    public void seriesUpdated(ChartView chartView, int index);

    /**
     * Fired when a chart view's series data is sorted.
     *
     * @param chartView
     */
    public void seriesSorted(ChartView chartView);
}
