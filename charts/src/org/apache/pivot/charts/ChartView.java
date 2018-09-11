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

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.charts.content.ValueMarker;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Service;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;

/**
 * Abstract base class for chart views.
 */
@DefaultProperty("chartData")
public abstract class ChartView extends Component {
    /**
     * Represents a chart category.
     */
    public static class Category {
        private ChartView chartView = null;

        private String key;
        private String label;

        public Category() {
            this(null, null);
        }

        public Category(final String key) {
            this(key, key);
        }

        public Category(final String key, final String label) {
            this.key = key;
            this.label = label;
        }

        public ChartView getChartView() {
            return chartView;
        }

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            Utils.checkNull(key, "key");

            String previousKey = this.key;

            if (previousKey != key) {
                this.key = key;

                if (chartView != null) {
                    chartView.chartViewCategoryListeners.categoryKeyChanged(chartView,
                        chartView.categories.indexOf(this), previousKey);
                }
            }
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            Utils.checkNull(label, "label");

            String previousLabel = this.label;

            if (previousLabel != label) {
                this.label = label;

                if (chartView != null) {
                    chartView.chartViewCategoryListeners.categoryLabelChanged(chartView,
                        chartView.categories.indexOf(this), previousLabel);
                }
            }
        }
    }

    /**
     * Represents an element of a chart, such as a bar or a pie wedge.
     */
    public static class Element {
        private int seriesIndex;
        private int elementIndex;

        public Element(final int seriesIndex, final int elementIndex) {
            this.seriesIndex = seriesIndex;
            this.elementIndex = elementIndex;
        }

        /**
         * Returns the element's series index.
         *
         * @return The element's series index.
         */
        public int getSeriesIndex() {
            return seriesIndex;
        }

        /**
         * Returns the element's index within its series. For a category series,
         * the element index represents the index of the category in the
         * category sequence. Otherwise, it represents the index of the item
         * within the series.
         *
         * @return The element index.
         */
        public int getElementIndex() {
            return elementIndex;
        }

        @Override
        public String toString() {
            return getClass().getName() + seriesIndex + ", " + elementIndex;
        }
    }

    /**
     * Chart view skin interface.
     */
    public interface Skin {
        public Element getElementAt(int x, int y);
    }

    /**
     * Internal class for managing the chart's category list.
     */
    public final class CategorySequence implements Sequence<Category> {
        @Override
        public int add(final Category category) {
            int index = getLength();
            insert(category, index);

            return index;
        }

        @Override
        public void insert(final Category category, final int index) {
            Utils.checkNull(category, "category");

            if (category.getChartView() != null) {
                throw new IllegalArgumentException(
                    "Category is already in use by another chart view.");
            }

            categories.insert(category, index);
            category.chartView = ChartView.this;

            chartViewCategoryListeners.categoryInserted(ChartView.this, index);
        }

        @Override
        @UnsupportedOperation
        public Category update(final int index, final Category category) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(final Category category) {
            int index = indexOf(category);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Category> remove(final int index, final int count) {
            Sequence<Category> removed = categories.remove(index, count);

            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    removed.get(i).chartView = null;
                }

                chartViewCategoryListeners.categoriesRemoved(ChartView.this, index, removed);
            }

            return removed;
        }

        @Override
        public Category get(final int index) {
            return categories.get(index);
        }

        @Override
        public int indexOf(final Category category) {
            return categories.indexOf(category);
        }

        @Override
        public int getLength() {
            return categories.getLength();
        }
    }

    /**
     * List event handler.
     */
    private class ListHandler implements ListListener<Object> {
        @Override
        public void itemInserted(final List<Object> list, final int index) {
            chartViewSeriesListeners.seriesInserted(ChartView.this, index);
        }

        @Override
        public void itemsRemoved(final List<Object> list, final int index, final Sequence<Object> items) {
            int count = items.getLength();
            chartViewSeriesListeners.seriesRemoved(ChartView.this, index, count);
        }

        @Override
        public void itemUpdated(final List<Object> list, final int index, final Object previousItem) {
            chartViewSeriesListeners.seriesUpdated(ChartView.this, index);
        }

        @Override
        public void listCleared(final List<Object> list) {
            chartViewSeriesListeners.seriesCleared(ChartView.this);
        }

        @Override
        public void comparatorChanged(final List<Object> list, final Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                chartViewSeriesListeners.seriesSorted(ChartView.this);
            }
        }
    }

    private class ValueMarkersHandler implements ListListener<ValueMarker> {
        @Override
        public void itemInserted(final List<ValueMarker> list, final int index) {
            chartViewListeners.chartDataChanged(ChartView.this, getChartData());
        }

        @Override
        public void itemsRemoved(final List<ValueMarker> list, final int index, final Sequence<ValueMarker> items) {
            chartViewListeners.chartDataChanged(ChartView.this, getChartData());
        }

        @Override
        public void itemUpdated(final List<ValueMarker> list, final int index, final ValueMarker previousItem) {
            chartViewListeners.chartDataChanged(ChartView.this, getChartData());
        }

        @Override
        public void listCleared(final List<ValueMarker> list) {
            chartViewListeners.chartDataChanged(ChartView.this, getChartData());
        }

        @Override
        public void comparatorChanged(final List<ValueMarker> list, final Comparator<ValueMarker> previousComparator) {
            // No-op
        }
    }

    protected List<?> chartData;
    private String seriesNameKey;

    private String title = null;
    private String horizontalAxisLabel = null;
    private String verticalAxisLabel = null;
    private boolean showLegend;

    private List<Category> categories = new ArrayList<>();
    private CategorySequence categorySequence = new CategorySequence();

    private ListHandler chartDataHandler = new ListHandler();
    private ValueMarkersHandler valueMarkersHandler = new ValueMarkersHandler();

    private ChartViewListener.Listeners chartViewListeners = new ChartViewListener.Listeners();
    private ChartViewCategoryListener.Listeners chartViewCategoryListeners = new ChartViewCategoryListener.Listeners();
    private ChartViewSeriesListener.Listeners chartViewSeriesListeners = new ChartViewSeriesListener.Listeners();

    private List<ValueMarker> valueMarkers;

    public static final String DEFAULT_SERIES_NAME_KEY = "name";
    public static final String PROVIDER_NAME = Provider.class.getName();

    private static Provider provider = null;

    static {
        provider = (Provider) Service.getProvider(PROVIDER_NAME);

        if (provider == null) {
            throw new ProviderNotFoundException();
        }
    }

    public ChartView() {
        this(DEFAULT_SERIES_NAME_KEY, new ArrayList<>());
    }

    public ChartView(final String seriesNameKey, final List<?> chartData) {
        setSeriesNameKey(seriesNameKey);
        setTitle(title);
        setChartData(chartData);
        setShowLegend(showLegend);
        setValueMarkers(new ArrayList<ValueMarker>());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void installSkin(final Class<? extends Component> componentClass) {
        Class<? extends org.apache.pivot.wtk.Skin> skinClass =
            provider.getSkinClass((Class<? extends ChartView>) componentClass);

        try {
            setSkin(skinClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
               | InvocationTargetException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public CategorySequence getCategories() {
        return categorySequence;
    }

    public List<?> getChartData() {
        return chartData;
    }

    @SuppressWarnings("unchecked")
    public void setChartData(final List<?> chartData) {
        Utils.checkNull(chartData, "chartData");

        List<?> previousChartData = this.chartData;

        if (previousChartData != chartData) {
            if (previousChartData != null) {
                ((List<Object>) previousChartData).getListListeners().remove(chartDataHandler);
            }

            ((List<Object>) chartData).getListListeners().add(chartDataHandler);

            this.chartData = chartData;
            chartViewListeners.chartDataChanged(this, previousChartData);
        }
    }

    public String getSeriesNameKey() {
        return seriesNameKey;
    }

    public void setSeriesNameKey(final String seriesNameKey) {
        Utils.checkNull(seriesNameKey, "seriesNameKey");

        String previousSeriesNameKey = this.seriesNameKey;

        if (previousSeriesNameKey != seriesNameKey) {
            this.seriesNameKey = seriesNameKey;
            chartViewListeners.seriesNameKeyChanged(this, previousSeriesNameKey);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        String previousTitle = this.title;

        if (previousTitle != title) {
            this.title = title;
            chartViewListeners.titleChanged(this, previousTitle);
        }
    }

    public String getHorizontalAxisLabel() {
        return horizontalAxisLabel;
    }

    public void setHorizontalAxisLabel(final String horizontalAxisLabel) {
        String previousHorizontalAxisLabel = this.horizontalAxisLabel;

        if (previousHorizontalAxisLabel != horizontalAxisLabel) {
            this.horizontalAxisLabel = horizontalAxisLabel;
            chartViewListeners.horizontalAxisLabelChanged(this, previousHorizontalAxisLabel);
        }
    }

    public String getVerticalAxisLabel() {
        return verticalAxisLabel;
    }

    public void setVerticalAxisLabel(final String verticalAxisLabel) {
        String previousVerticalAxisLabel = this.verticalAxisLabel;

        if (previousVerticalAxisLabel != verticalAxisLabel) {
            this.verticalAxisLabel = verticalAxisLabel;
            chartViewListeners.verticalAxisLabelChanged(this, previousVerticalAxisLabel);
        }
    }

    public boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(final boolean showLegend) {
        if (this.showLegend != showLegend) {
            this.showLegend = showLegend;
            chartViewListeners.showLegendChanged(this);
        }
    }

    public Element getElementAt(final int x, final int y) {
        return ((Skin) getSkin()).getElementAt(x, y);
    }

    public ListenerList<ChartViewListener> getChartViewListeners() {
        return chartViewListeners;
    }

    public ListenerList<ChartViewCategoryListener> getChartViewCategoryListeners() {
        return chartViewCategoryListeners;
    }

    public ListenerList<ChartViewSeriesListener> getChartViewSeriesListeners() {
        return chartViewSeriesListeners;
    }

    public List<ValueMarker> getValueMarkers() {
        return valueMarkers;
    }

    public void setValueMarkers(final List<ValueMarker> valueMarkers) {
        List<ValueMarker> previousValueMarkers = this.valueMarkers;

        if (previousValueMarkers != valueMarkers) {
            if (previousValueMarkers != null) {
                previousValueMarkers.getListListeners().remove(valueMarkersHandler);
            }

            this.valueMarkers = valueMarkers;
            if (valueMarkers != null) {
                valueMarkers.getListListeners().add(valueMarkersHandler);
            }
        }
    }

}
