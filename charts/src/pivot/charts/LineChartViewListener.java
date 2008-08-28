package pivot.charts;

public interface LineChartViewListener {
    public void categoryAxisLabelChanged(LineChartView lineChartView, String previousCategoryAxisLabel);
    public void valueAxisLabelChanged(LineChartView lineChartView, String previousValueAxisLabel);
}
