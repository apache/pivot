package pivot.charts;

public interface ChartViewSeriesListener {
    public void seriesInserted(ChartView chartView, int index);
    public void seriesRemoved(ChartView chartView, int index, int count);
    public void seriesUpdated(ChartView chartView, int index);
    public void seriesSorted(ChartView chartView);
}
