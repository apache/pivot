package pivot.charts;

import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.Component;

/**
 * Abstract base class for chart views.
 *
 * @author gbrown
 */
public abstract class ChartView extends Component {
    /**
     * Represents a chart category.
     */
    public static class Category {
        private ChartView chartView = null;

        private String key;
        private String label;

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

        private void setChartView(ChartView chartView) {
            this.chartView = chartView;
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
     * Represents an element of a chart.
     */
    public static class Element {
        private String categoryKey = null;
        private int seriesIndex = -1;

        public Element(String categoryKey, int seriesIndex) {
            this.categoryKey = categoryKey;
            this.seriesIndex = seriesIndex;
        }

        public String getCategoryKey() {
            return categoryKey;
        }

        public int getSeriesIndex() {
            return seriesIndex;
        }
    }

    /**
     * Chart view skin interface.
     */
    public interface Skin extends pivot.wtk.Skin {
        public Element getElementAt(int x, int y);
    }

    /**
     * Internal class for managing the chart's category list.
     */
    public final class CategorySequence implements Sequence<Category> {
        public int add(Category category) {
            int i = getLength();
            insert(category, i);

            return i;
        }

        public void insert(Category category, int index) {
            if (category == null) {
                throw new IllegalArgumentException("category is null.");
            }

            if (category.getChartView() != null) {
                throw new IllegalArgumentException("category is already in use by another chart view.");
            }

            categories.insert(category, index);
            category.setChartView(ChartView.this);

            chartViewCategoryListeners.categoryInserted(ChartView.this, index);
        }

        public Category update(int index, Category category) {
            throw new UnsupportedOperationException();
        }

        public int remove(Category category) {
            int index = indexOf(category);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Category> remove(int index, int count) {
            Sequence<Category> removed = categories.remove(index, count);

            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    removed.get(i).setChartView(null);
                }

                chartViewCategoryListeners.categoriesRemoved(ChartView.this, index, removed);
            }

            return removed;
        }

        public Category get(int index) {
            return categories.get(index);
        }

        public int indexOf(Category category) {
            return categories.indexOf(category);
        }

        public int getLength() {
            return categories.getLength();
        }
    }

    /**
     * List event handler.
     */
    private class ListHandler implements ListListener<Object> {
        public void itemInserted(List<Object> list, int index) {
            // Notify listeners that items were inserted
            chartViewSeriesListeners.seriesInserted(ChartView.this, index);
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            if (items == null) {
                // All items were removed; clear the selection and notify
                // listeners
                chartViewSeriesListeners.seriesRemoved(ChartView.this, index, -1);
            } else {
                // Notify listeners that items were removed
                int count = items.getLength();
                chartViewSeriesListeners.seriesRemoved(ChartView.this, index, count);
            }
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            chartViewSeriesListeners.seriesUpdated(ChartView.this, index);
        }

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
    private class ChartViewListenerList extends ListenerList<ChartViewListener>
        implements ChartViewListener {
        public void chartDataChanged(ChartView chartView, List<?> previousChartData) {
            for (ChartViewListener listener : this) {
                listener.chartDataChanged(chartView, previousChartData);
            }
        }
    }

    /**
     * Chart view category listener list.
     */
    private class ChartViewCategoryListenerList extends ListenerList<ChartViewCategoryListener>
        implements ChartViewCategoryListener {
        public void categoryInserted(ChartView chartView, int index) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryInserted(chartView, index);
            }
        }

        public void categoriesRemoved(ChartView chartView, int index, Sequence<ChartView.Category> categories) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoriesRemoved(chartView, index, categories);
            }
        }

        public void categoryKeyChanged(ChartView chartView, int index, String previousKey) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryKeyChanged(chartView, index, previousKey);
            }
        }

        public void categoryLabelChanged(ChartView chartView, int index, String previousLabel) {
            for (ChartViewCategoryListener listener : this) {
                listener.categoryLabelChanged(chartView, index, previousLabel);
            }
        }
    }

    /**
     * Chart view series listener list.
     */
    private class ChartViewSeriesListenerList extends ListenerList<ChartViewSeriesListener>
        implements ChartViewSeriesListener {
        public void seriesInserted(ChartView chartView, int index) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesInserted(chartView, index);
            }
        }

        public void seriesRemoved(ChartView chartView, int index, int count) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesRemoved(chartView, index, count);
            }
        }

        public void seriesUpdated(ChartView chartView, int index) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesUpdated(chartView, index);
            }
        }

        public void seriesSorted(ChartView chartView) {
            for (ChartViewSeriesListener listener : this) {
                listener.seriesSorted(chartView);
            }
        }
    }

    private List<?> chartData;

    private ArrayList<Category> categories = new ArrayList<Category>();
    private CategorySequence categorySequence = new CategorySequence();

    private ListHandler chartDataHandler = new ListHandler();

    private ChartViewListenerList chartViewListeners = new ChartViewListenerList();
    private ChartViewCategoryListenerList chartViewCategoryListeners = new ChartViewCategoryListenerList();
    private ChartViewSeriesListenerList chartViewSeriesListeners = new ChartViewSeriesListenerList();

    public static final String DEFAULT_SERIES_NAME_KEY = "id";

    public ChartView() {
        this(DEFAULT_SERIES_NAME_KEY, new ArrayList<Object>());
    }

    public ChartView(String seriesNameKey, List<?> chartData) {
        setChartData(chartData);
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

