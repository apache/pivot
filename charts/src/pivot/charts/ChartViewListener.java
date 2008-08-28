package pivot.charts;

import pivot.collections.List;

public interface ChartViewListener {
    public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey);
    public void titleChanged(ChartView chartView, String previousTitle);
    public void chartDataChanged(ChartView chartView, List<?> previousChartData);
    public void showLegendChanged(ChartView chartView);
}
