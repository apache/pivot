package pivot.charts;

import pivot.collections.List;

public interface ChartViewListener {
    public void chartDataChanged(ChartView chartView, List<?> previousChartData);
    public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey);
    public void titleChanged(ChartView chartView, String previousTitle);
    public void horizontalAxisLabelChanged(ChartView chartView, String previousHorizontalAxisLabel);
    public void verticalAxisLabelChanged(ChartView chartView, String previousVerticalAxisLabel);
    public void showLegendChanged(ChartView chartView);
}
