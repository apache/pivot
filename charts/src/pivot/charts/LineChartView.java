package pivot.charts;

import pivot.charts.skin.LineChartViewSkin;
import pivot.util.ListenerList;

public class LineChartView extends ChartView {
    private class LineChartViewListenerList extends ListenerList<LineChartViewListener>
        implements LineChartViewListener {
        public void categoryAxisLabelChanged(LineChartView barChartView, String previousCategoryAxisLabel) {
            for (LineChartViewListener listener : this) {
                listener.categoryAxisLabelChanged(barChartView, previousCategoryAxisLabel);
            }
        }

        public void valueAxisLabelChanged(LineChartView barChartView, String previousValueAxisLabel) {
            for (LineChartViewListener listener : this) {
                listener.valueAxisLabelChanged(barChartView, previousValueAxisLabel);
            }
        }
    }

    private String categoryAxisLabel;
    private String valueAxisLabel;

    private LineChartViewListenerList lineChartViewListeners = new LineChartViewListenerList();

    public LineChartView() {
        this(null, null);
    }

    public LineChartView(String categoryAxisLabel, String valueAxisLabel) {
        setCategoryAxisLabel(categoryAxisLabel);
        setValueAxisLabel(valueAxisLabel);

        setSkin(new LineChartViewSkin());
    }

    public String getCategoryAxisLabel() {
        return categoryAxisLabel;
    }

    public void setCategoryAxisLabel(String categoryAxisLabel) {
        String previousCategoryAxisLabel = this.categoryAxisLabel;
        if (previousCategoryAxisLabel != categoryAxisLabel) {
            this.categoryAxisLabel = categoryAxisLabel;
            lineChartViewListeners.categoryAxisLabelChanged(this, previousCategoryAxisLabel);
        }
    }

    public String getValueAxisLabel() {
        return valueAxisLabel;
    }

    public void setValueAxisLabel(String valueAxisLabel) {
        String previousValueAxisLabel = this.valueAxisLabel;
        if (previousValueAxisLabel != valueAxisLabel) {
            this.valueAxisLabel = valueAxisLabel;
            lineChartViewListeners.valueAxisLabelChanged(this, previousValueAxisLabel);
        }
    }

    public ListenerList<LineChartViewListener> getLineChartViewListeners() {
        return lineChartViewListeners;
    }
}
