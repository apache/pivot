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

import java.util.Comparator;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Service;
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

        public Category(String key) {
            this(key, key);
        }

        public Category(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public ChartView getChartView() {
            return chartView;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key)  {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

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

        public void setLabel(String label) {
            if (label == null) {
                throw new IllegalArgumentException("label is null.");
            }

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

        public Element(int seriesIndex, int elementIndex) {
            this.seriesIndex = seriesIndex;
            this.elementIndex = elementIndex;
        }

        /**
         * Returns the element's series index.
         *
         * @return
         * The element's series index.
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
         * @return
         * The element index.
         */
        public int getElementIndex() {
            return elementIndex;
        }

        @Override
        public String toString() {
            String string = getClass().getName()
                + seriesIndex + ", " + elementIndex;
            return string;
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
        public int add(Category category) {
            int index = getLength();
            insert(category, index);

            return index;
        }

        @Override
        public void insert(Category category, int index) {
            if (category == null) {
                throw new IllegalArgumentException("category is null.");
            }

            if (category.getChartView() != null) {
                throw new IllegalArgumentException("category is already in use by another chart view.");
            }

            categories.insert(category, index);
            category.chartView = ChartView.this;

            chartViewCategoryListeners.categoryInserted(ChartView.this, index);
        }

        @Override
        public Category update(int index, Category category) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Category category) {
            int index = indexOf(category);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Category> remove(int index, int count) {
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
        public Category get(int index) {
            return categories.get(index);
        }

        @Override
        public int indexOf(Category category) {
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
        public void itemInserted(List<Object> list, int index) {
            chartViewSeriesListeners.seriesInserted(ChartView.this, index);
        }

        @Override
        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            int count = items.getLength();
            chartViewSeriesListeners.seriesRemoved(ChartView.this, index, count);
        }

        @Override
        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            chartViewSeriesListeners.seriesUpdated(ChartView.this, index);
        }

        @Override
        public void listCleared(List<Object> list) {
            chartViewSeriesListeners.seriesCleared(ChartView.this);
        }

        @Override
        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                chartViewSeriesListeners.seriesSorted(ChartView.this);
            }
        }
    }

    /**
     * Chart view listener list.
     */
    private static class ChartViewListenerList extends ListenerList<ChartViewListener>
        implements ChartViewListener {
        @Override
        public void chartDataChanged(ChartView chartView, List<?> previousChartData) {
            for (ChartViewListener listener : this) {
                listener.chartDataChanged(chartView, previousChartData);
            }
        }

        @Override
        public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey) {
            for (ChartViewListener listener : this) {
                listener.seriesNameKeyChanged(chartView, previousSeriesNameKey);
            }
        }

        @Override
        public void titleChanged(ChartView chartView, String previousTitle) {
            for (ChartViewListener listener : this) {
                listener.titleChanged(chartView, previousTitle);
            }
        }

        @Override
        public void horizontalAxisLabelChanged(ChartView chartView, String previousXAxisLabel) {
            for (ChartViewListener listener : this) {
                listener.horizontalAxisLabelChanged(chartView, previousXAxisLabel);
            }
        }

        @Override
        public void verticalAxisLabelChanged(ChartView chartView, String previousYAxisLabel) {
            for (ChartViewListener listener : this) {
                listener.verticalAxisLabelChanged(chartView, previousYAxisLabel);
            }
        }

        @Override
        public void showLegendChanged(ChartView chartView) {
            for (ChartViewListener listener : this) {
                listener.showLegendChanged(chartView);
            }
        }
    }

    /**
     * Chart view category listener list.
     */
    private static class ChartViewCategoryListenerList extends ListenerList<ChartViewCategoryListener>
        implements ChartViewCategoryListener {
        @Override
        public void categoryInserted(ChartView chartView, int index) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryInserted(chartView, index);
            }
        }

        @Override
        public void categoriesRemoved(ChartView chartView, int index, Sequence<ChartView.Category> categories) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoriesRemoved(chartView, index, categories);
            }
        }

        @Override
        public void categoryKeyChanged(ChartView chartView, int index, String previousKey) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryKeyChanged(chartView, index, previousKey);
            }
        }

        @Override
        public void categoryLabelChanged(ChartView chartView, int index, String previousLabel) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryLabelChanged(chartView, index, previousLabel);
            }
        }
    }

    /**
     * Chart view series listener list.
     */
    private static class ChartViewSeriesListenerList extends ListenerList<ChartViewSeriesListener>
        implements ChartViewSeriesListener {
        @Override
        public void seriesInserted(ChartView chartView, int index) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesInserted(chartView, index);
            }
        }

        @Override
        public void seriesRemoved(ChartView chartView, int index, int count) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesRemoved(chartView, index, count);
            }
        }

        @Override
        public void seriesUpdated(ChartView chartView, int index) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesUpdated(chartView, index);
            }
        }

        @Override
        public void seriesCleared(ChartView chartView) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesCleared(chartView);
            }
        }

        @Override
        public void seriesSorted(ChartView chartView) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesSorted(chartView);
            }
        }
    }

    private List<?> chartData;
    private String seriesNameKey;

    private String title = null;
    private String horizontalAxisLabel = null;
    private String verticalAxisLabel = null;
    private boolean showLegend;

    private ArrayList<Category> categories = new ArrayList<Category>();
    private CategorySequence categorySequence = new CategorySequence();

    private ListHandler chartDataHandler = new ListHandler();

    private ChartViewListenerList chartViewListeners = new ChartViewListenerList();
    private ChartViewCategoryListenerList chartViewCategoryListeners = new ChartViewCategoryListenerList();
    private ChartViewSeriesListenerList chartViewSeriesListeners = new ChartViewSeriesListenerList();

    public static final String DEFAULT_SERIES_NAME_KEY = "name";
    public static final String PROVIDER_NAME = Provider.class.getName();

    private static Provider provider = null;

    static {
        provider = (Provider)Service.getProvider(PROVIDER_NAME);

        if (provider == null) {
            throw new ProviderNotFoundException();
        }
    }

    public ChartView() {
        this(DEFAULT_SERIES_NAME_KEY, new ArrayList<Object>());
    }

    public ChartView(String seriesNameKey, List<?> chartData) {
        setSeriesNameKey(seriesNameKey);
        setTitle(title);
        setChartData(chartData);
        setShowLegend(showLegend);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void installSkin(Class<? extends Component> componentClass) {
        Class<? extends org.apache.pivot.wtk.Skin> skinClass =
            provider.getSkinClass((Class<? extends ChartView>)componentClass);

        try {
            setSkin(skinClass.newInstance());
        } catch(InstantiationException exception) {
            throw new IllegalArgumentException(exception);
        } catch(IllegalAccessException exception) {
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
    public void setChartData(List<?> chartData) {
        if (chartData == null) {
            throw new IllegalArgumentException("chartData is null.");
        }

        List<?> previousChartData = this.chartData;

        if (previousChartData != chartData) {
            if (previousChartData != null) {
                ((List<Object>)previousChartData).getListListeners().remove(chartDataHandler);
            }

            ((List<Object>)chartData).getListListeners().add(chartDataHandler);

            this.chartData = chartData;
            chartViewListeners.chartDataChanged(this, previousChartData);
        }
    }

    public String getSeriesNameKey() {
        return seriesNameKey;
    }

    public void setSeriesNameKey(String seriesNameKey) {
        if (seriesNameKey == null) {
            throw new IllegalArgumentException("seriesNameKey is null.");
        }

        String previousSeriesNameKey = this.seriesNameKey;

        if (previousSeriesNameKey != seriesNameKey) {
            this.seriesNameKey = seriesNameKey;
            chartViewListeners.seriesNameKeyChanged(this, previousSeriesNameKey);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String previousTitle = this.title;

        if (previousTitle != title) {
            this.title = title;
            chartViewListeners.titleChanged(this, previousTitle);
        }
    }

    public String getHorizontalAxisLabel() {
        return horizontalAxisLabel;
    }

    public void setHorizontalAxisLabel(String horizontalAxisLabel) {
        String previousHorizontalAxisLabel = this.horizontalAxisLabel;

        if (previousHorizontalAxisLabel != horizontalAxisLabel) {
            this.horizontalAxisLabel = horizontalAxisLabel;
            chartViewListeners.horizontalAxisLabelChanged(this, previousHorizontalAxisLabel);
        }
    }

    public String getVerticalAxisLabel() {
        return verticalAxisLabel;
    }

    public void setVerticalAxisLabel(String verticalAxisLabel) {
        String previousVerticalAxisLabel = this.verticalAxisLabel;

        if (previousVerticalAxisLabel != verticalAxisLabel) {
            this.verticalAxisLabel = verticalAxisLabel;
            chartViewListeners.verticalAxisLabelChanged(this, previousVerticalAxisLabel);
        }
    }

    public boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        if (this.showLegend != showLegend) {
            this.showLegend = showLegend;
            chartViewListeners.showLegendChanged(this);
        }
    }

    public Element getElementAt(int x, int y) {
        return ((Skin)getSkin()).getElementAt(x, y);
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
}

