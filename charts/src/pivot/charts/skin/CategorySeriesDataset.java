package pivot.charts.skin;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

import pivot.beans.BeanDictionary;
import pivot.charts.ChartView;
import pivot.collections.Dictionary;
import pivot.collections.List;

@SuppressWarnings("unchecked")
public class CategorySeriesDataset implements CategoryDataset {
    private ChartView.CategorySequence categories;
    private String seriesNameKey;
    private List<?> chartData;

    private DatasetGroup datasetGroup = null;

    public CategorySeriesDataset(ChartView.CategorySequence categories,
        String seriesNameKey, List<?> chartData) {
        if (categories == null) {
            throw new IllegalArgumentException("categories is null.");
        }

        if (seriesNameKey == null) {
            throw new IllegalArgumentException("seriesNameKey is null.");
        }

        if (chartData == null) {
            throw new IllegalArgumentException("chartData is null.");
        }

        this.categories = categories;
        this.seriesNameKey = seriesNameKey;
        this.chartData = chartData;
    }

    public DatasetGroup getGroup() {
        return datasetGroup;
    }

    public void setGroup(DatasetGroup datasetGroup) {
        this.datasetGroup = datasetGroup;
    }

    public int getColumnCount() {
        return categories.getLength();
    }

    public int getRowCount() {
        return chartData.getLength();
    }

    public int getColumnIndex(Comparable categoryLabel) {
        if (categoryLabel == null) {
            throw new IllegalArgumentException("categoryLabel is null.");
        }

        int columnIndex = -1;
        for (int i = 0, n = categories.getLength(); i < n && columnIndex == -1; i++) {
            ChartView.Category category = categories.get(i);

            if (categoryLabel.compareTo(category.getLabel()) == 0) {
                columnIndex = i;
            }
        }

        return columnIndex;
    }

    public Comparable getColumnKey(int categoryIndex) {
        if (categoryIndex < 0
            || categoryIndex > categories.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        return categories.get(categoryIndex).getLabel();
    }

    public java.util.List getColumnKeys() {
        java.util.ArrayList columnKeys = new java.util.ArrayList(categories.getLength());
        for (int i = 0, n = categories.getLength(); i < n; i++) {
            columnKeys.add(categories.get(i).getLabel());
        }

        return columnKeys;
    }

    public int getRowIndex(Comparable seriesName) {
        if (seriesName == null) {
            throw new IllegalArgumentException("seriesName is null.");
        }

        int rowIndex = -1;
        for (int i = 0, n = chartData.getLength(); i < n && rowIndex == -1; i++) {
            Dictionary<String, ?> seriesDictionary = getSeriesDictionary(i);

            if (seriesName.compareTo(seriesDictionary.get(seriesNameKey)) == 0) {
                rowIndex = i;
            }
        }

        return rowIndex;
    }

    public Comparable getRowKey(int seriesIndex) {
        Dictionary<String, ?> seriesDictionary = getSeriesDictionary(seriesIndex);
        return (String)seriesDictionary.get(seriesNameKey);
    }

    public java.util.List getRowKeys() {
        java.util.ArrayList rowKeys = new java.util.ArrayList(chartData.getLength());
        for (int i = 0, n = chartData.getLength(); i < n; i++) {
            rowKeys.add(getRowKey(i));
        }

        return rowKeys;
    }

    public Number getValue(int seriesIndex, int categoryIndex) {
        Dictionary<String, ?> seriesDictionary = getSeriesDictionary(seriesIndex);

        if (categoryIndex < 0
            || categoryIndex > categories.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        ChartView.Category category = categories.get(categoryIndex);
        String categoryKey = category.getKey();

        Object value = seriesDictionary.get(categoryKey);
        if (value instanceof String) {
            value = Double.parseDouble((String)value);
        }

        return (Number)value;
    }

    public Number getValue(Comparable seriesName, Comparable categoryLabel) {
        return getValue(getRowIndex(seriesName), categoryLabel);
    }

    protected Dictionary<String, ?> getSeriesDictionary(int seriesIndex) {
        if (seriesIndex < 0
            || seriesIndex > chartData.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        Object series = chartData.get(seriesIndex);

        Dictionary<String, ?> seriesDictionary;
        if (series instanceof Dictionary<?, ?>) {
            seriesDictionary = (Dictionary<String, ?>)series;
        } else {
            seriesDictionary = new BeanDictionary(series);
        }

        return seriesDictionary;
    }

    public void addChangeListener(DatasetChangeListener listener) {
        // No-op
    }

    public void removeChangeListener(DatasetChangeListener listener) {
        // No-op
    }
}
