package pivot.charts.skin;

import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

import pivot.beans.BeanDictionary;
import pivot.charts.ChartView;
import pivot.collections.Dictionary;
import pivot.collections.List;

@SuppressWarnings("unchecked")
public class SeriesDataset implements CategoryDataset {
    private ChartView.CategorySequence categories;
    private List<?> chartData;

    private DatasetGroup datasetGroup = null;

    public SeriesDataset(ChartView.CategorySequence categories, List<?> chartData) {
        if (categories == null) {
            throw new IllegalArgumentException("categories is null.");
        }

        if (chartData == null) {
            throw new IllegalArgumentException("chartData is null.");
        }

        this.categories = categories;
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

    public int getColumnIndex(Comparable categoryKey) {
        if (categoryKey == null) {
            throw new IllegalArgumentException("categoryKey is null.");
        }

        int i = 0;
        int n = categories.getLength();
        while (i < n
            && categoryKey.compareTo(categories.get(i++).getKey()) != 0);

        return (i == n) ? -1 : i;
    }

    public Comparable getColumnKey(int categoryIndex) {
        if (categoryIndex < 0
            || categoryIndex > categories.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        return categories.get(categoryIndex).getKey();
    }

    public java.util.List getColumnKeys() {
        java.util.ArrayList columnKeys = new java.util.ArrayList(categories.getLength());
        for (int i = 0, n = categories.getLength(); i < n; i++) {
            columnKeys.add(categories.get(i).getKey());
        }

        return columnKeys;
    }

    public int getRowIndex(Comparable seriesKey) {
        if (seriesKey == null) {
            throw new IllegalArgumentException("seriesKey is null.");
        }

        return Integer.parseInt(seriesKey.toString());
    }

    public Comparable getRowKey(int seriesIndex) {
        return Integer.toString(seriesIndex);
    }

    public java.util.List getRowKeys() {
        java.util.ArrayList rowKeys = new java.util.ArrayList(chartData.getLength());
        for (int i = 0, n = chartData.getLength(); i < n; i++) {
            rowKeys.add(getRowKey(i));
        }

        return rowKeys;
    }

    public Number getValue(int seriesIndex, int categoryIndex) {
        return getValue(seriesIndex, getColumnKey(categoryIndex));
    }

    public Number getValue(Comparable seriesKey, Comparable categoryKey) {
        return getValue(getRowIndex(seriesKey), categoryKey);
    }

    public Number getValue(int seriesIndex, Comparable categoryKey) {
        if (seriesIndex < 0
            || seriesIndex > chartData.getLength() - 1) {
            throw new UnknownKeyException("seriesIndex is out of bounds.");
        }

        if (categoryKey == null) {
            throw new IllegalArgumentException("categoryKey is null.");
        }

        Object series = chartData.get(seriesIndex);

        Dictionary<String, Object> seriesDictionary;
        if (series instanceof Dictionary<?, ?>) {
            seriesDictionary = (Dictionary<String, Object>)series;
        } else {
            seriesDictionary = new BeanDictionary(series);
        }

        Object value = seriesDictionary.get(categoryKey.toString());
        if (value == null) {
            throw new UnknownKeyException(categoryKey + " is not a valid key.");
        }

        if (!(value instanceof Number)) {
            value = Double.parseDouble(value.toString());
        }

        return (Number)value;
    }

    public void addChangeListener(DatasetChangeListener listener) {
        // TODO
    }

    public void removeChangeListener(DatasetChangeListener listener) {
        // TODO
    }
}
