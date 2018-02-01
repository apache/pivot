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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Chart view category listener interface.
 */
public interface ChartViewCategoryListener {
    /**
     * Chart view category listener list.
     */
    public static class Listeners extends ListenerList<ChartViewCategoryListener>
        implements ChartViewCategoryListener {
        @Override
        public void categoryInserted(ChartView chartView, int index) {
            forEach(listener -> listener.categoryInserted(chartView, index));
        }

        @Override
        public void categoriesRemoved(ChartView chartView, int index,
            Sequence<ChartView.Category> categories) {
            forEach(listener -> listener.categoriesRemoved(chartView, index, categories));
        }

        @Override
        public void categoryKeyChanged(ChartView chartView, int index, String previousKey) {
            forEach(listener -> listener.categoryKeyChanged(chartView, index, previousKey));
        }

        @Override
        public void categoryLabelChanged(ChartView chartView, int index, String previousLabel) {
            forEach(listener -> listener.categoryLabelChanged(chartView, index, previousLabel));
        }
    }

    /**
     * Fired when a category is inserted into a chart view.
     *
     * @param chartView The chart that is changing.
     * @param index The index of the new category that was inserted.
     */
    public void categoryInserted(ChartView chartView, int index);

    /**
     * Fired when a category is removed from a chart view.
     *
     * @param chartView The chart that is changing.
     * @param index The index of the first category that was removed.
     * @param categories The list of removed categories.
     */
    public void categoriesRemoved(ChartView chartView, int index,
        Sequence<ChartView.Category> categories);

    /**
     * Fired when a chart view's category key changes.
     *
     * @param chartView The chart that is changing.
     * @param index The index of the category whose key changed.
     * @param previousKey Previous value of the changed key.
     */
    public void categoryKeyChanged(ChartView chartView, int index, String previousKey);

    /**
     * Fired when a chart view's category label changes.
     *
     * @param chartView The chart that is changing.
     * @param index The index of the category whose label changed.
     * @param previousLabel Previous value of the changed label.
     */
    public void categoryLabelChanged(ChartView chartView, int index, String previousLabel);
}
