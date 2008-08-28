package pivot.charts;

import pivot.charts.skin.BarChartViewSkin;
import pivot.util.ListenerList;

public class BarChartView extends ChartView {
    private class BarChartViewListenerList extends ListenerList<BarChartViewListener>
        implements BarChartViewListener {
        public void categoryAxisLabelChanged(BarChartView barChartView, String previousCategoryAxisLabel) {
            for (BarChartViewListener listener : this) {
                listener.categoryAxisLabelChanged(barChartView, previousCategoryAxisLabel);
            }
        }

        public void valueAxisLabelChanged(BarChartView barChartView, String previousValueAxisLabel) {
            for (BarChartViewListener listener : this) {
                listener.valueAxisLabelChanged(barChartView, previousValueAxisLabel);
            }
        }
    }

    private String categoryAxisLabel;
    private String valueAxisLabel;

    private BarChartViewListenerList barChartViewListeners = new BarChartViewListenerList();

    public BarChartView() {
        this(null, null);
    }

    public BarChartView(String categoryAxisLabel, String valueAxisLabel) {
        setCategoryAxisLabel(categoryAxisLabel);
        setValueAxisLabel(valueAxisLabel);

        setSkin(new BarChartViewSkin());
    }

    public String getCategoryAxisLabel() {
        return categoryAxisLabel;
    }

    public void setCategoryAxisLabel(String categoryAxisLabel) {
        String previousCategoryAxisLabel = this.categoryAxisLabel;
        if (previousCategoryAxisLabel != categoryAxisLabel) {
            this.categoryAxisLabel = categoryAxisLabel;
            barChartViewListeners.categoryAxisLabelChanged(this, previousCategoryAxisLabel);
        }
    }

    public String getValueAxisLabel() {
        return valueAxisLabel;
    }

    public void setValueAxisLabel(String valueAxisLabel) {
        String previousValueAxisLabel = this.valueAxisLabel;
        if (previousValueAxisLabel != valueAxisLabel) {
            this.valueAxisLabel = valueAxisLabel;
            barChartViewListeners.valueAxisLabelChanged(this, previousValueAxisLabel);
        }
    }

    public ListenerList<BarChartViewListener> getBarChartViewListeners() {
        return barChartViewListeners;
    }
}
