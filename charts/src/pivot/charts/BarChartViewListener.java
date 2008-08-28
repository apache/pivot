package pivot.charts;

public interface BarChartViewListener {
    public void categoryAxisLabelChanged(BarChartView barChartView, String previousCategoryAxisLabel);
    public void valueAxisLabelChanged(BarChartView barChartView, String previousValueAxisLabel);
}
