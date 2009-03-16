package pivot.charts.skin;

import java.util.Date;

import org.jfree.data.xy.OHLCDataset;

import pivot.collections.Dictionary;
import pivot.collections.List;

/**
 * Implementation of JFreeChart OHLCDataset.
 *
 * @author gbrown
 */
public class OHLCSeriesDataset extends XYSeriesDataset implements OHLCDataset {
    public static final String DATE_KEY = "date";
    public static final String OPEN_KEY = "open";
    public static final String HIGH_KEY = "high";
    public static final String LOW_KEY = "low";
    public static final String CLOSE_KEY = "close";
    public static final String VOLUME_KEY = "volume";

    public OHLCSeriesDataset(String seriesNameKey, List<?> chartData) {
        super(seriesNameKey, chartData);
    }

    @Override
    public Number getX(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(X_KEY);
        if (value == null) {
            value = itemDictionary.get(DATE_KEY);

            if (value instanceof Date) {
                value = ((Date)value).getTime();
            }
        }

        if (value == null) {
            throw new NullPointerException(X_KEY + " and " + DATE_KEY + " are null.");
        }

        return (Number)value;
    }

    @Override
    public Number getY(int seriesIndex, int itemIndex) {
        return getClose(seriesIndex, itemIndex);
    }

    public Number getOpen(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(OPEN_KEY);
        if (value == null) {
            value = Double.NaN;
        } else {
            if (value instanceof String) {
                value = Double.parseDouble((String)value);
            }
        }

        return (Number)value;
    }

    public double getOpenValue(int seriesIndex, int itemIndex) {
        return getOpen(seriesIndex, itemIndex).doubleValue();
    }

    public Number getHigh(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(HIGH_KEY);
        if (value == null) {
            value = Double.NaN;
        } else {
            if (value instanceof String) {
                value = Double.parseDouble((String)value);
            }
        }

        return (Number)value;
    }

    public double getHighValue(int seriesIndex, int itemIndex) {
        return getHigh(seriesIndex, itemIndex).doubleValue();
    }

    public Number getLow(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(LOW_KEY);
        if (value == null) {
            value = Double.NaN;
        } else {
            if (value instanceof String) {
                value = Double.parseDouble((String)value);
            }
        }

        return (Number)value;
    }

    public double getLowValue(int seriesIndex, int itemIndex) {
        return getLow(seriesIndex, itemIndex).doubleValue();
    }

    public Number getClose(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(CLOSE_KEY);
        if (value == null) {
            value = Double.NaN;
        } else {
            if (value instanceof String) {
                value = Double.parseDouble((String)value);
            }
        }

        return (Number)value;
    }

    public double getCloseValue(int seriesIndex, int itemIndex) {
        return getClose(seriesIndex, itemIndex).doubleValue();
    }

    public Number getVolume(int seriesIndex, int itemIndex) {
        Dictionary<String, ?> itemDictionary = getItemDictionary(seriesIndex, itemIndex);

        Object value = itemDictionary.get(VOLUME_KEY);
        if (value == null) {
            value = Double.NaN;
        } else {
            if (value instanceof String) {
                value = Double.parseDouble((String)value);
            }
        }

        return (Number)value;
    }

    public double getVolumeValue(int seriesIndex, int itemIndex) {
        return getVolume(seriesIndex, itemIndex).doubleValue();
    }
}
