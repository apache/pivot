package pivot.charts;

import pivot.collections.List;

public interface ChartViewListener {
    public void chartDataChanged(ChartView chartView, List<?> previousChartData);
}
