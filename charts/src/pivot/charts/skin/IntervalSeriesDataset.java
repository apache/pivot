package pivot.charts.skin;

import org.jfree.data.xy.IntervalXYDataset;

import pivot.collections.Dictionary;
import pivot.collections.List;

public class IntervalSeriesDataset extends XYSeriesDataset implements IntervalXYDataset {
    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";

    public IntervalSeriesDataset(String seriesNameKey, List<?> chartData) {
        super(seriesNameKey, chartData);
    }

    public Number getStartX(int seriesIndex, int itemIndex) {
        return getX(seriesIndex, itemIndex);
    }

    public double getStartXValue(int seriesIndex, int itemIndex) {
        return getX(seriesIndex, itemIndex).doubleValue();
    }

    public Number getEndX(int seriesIndex, int itemIndex) {
        return getXValue(seriesIndex, itemIndex) + getWidthValue(seriesIndex, itemIndex);
    }

    public double getEndXValue(int seriesIndex, int itemIndex) {
        return getEndX(seriesIndex, itemIndex).doubleValue();
    }

    public Number getStartY(int seriesIndex, int itemIndex) {
        return getY(seriesIndex, itemIndex);
    }

    public double getStartYValue(int seriesIndex, int itemIndex) {
        return getY(seriesIndex, itemIndex).doubleValue();
    }

    public Number getEndY(int seriesIndex, int itemIndex) {
        return getYValue(seriesIndex, itemIndex) + getWidthValue(seriesIndex, itemIndex);
    }

    public double getEndYValue(int seriesIndex, int itemIndex) {
        return getEndY(seriesIndex, itemIndex).doubleValue();
    }

    protected double getWidthValue(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(WIDTH_KEY);
        if (value == null) {
            throw new NullPointerException(WIDTH_KEY + " is null.");
        }

        if (value instanceof String) {
            value = Double.parseDouble((String)value);
        }

        Number width = (Number)value;
        return width.doubleValue();
    }

    protected double getHeightValue(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(HEIGHT_KEY);
        if (value == null) {
            throw new NullPointerException(HEIGHT_KEY + " is null.");
        }

        if (value instanceof String) {
            value = Double.parseDouble((String)value);
        }

        Number height = (Number)value;
        return height.doubleValue();
    }
}
