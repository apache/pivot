package pivot.charts;

import pivot.collections.Sequence;

public interface ChartViewCategoryListener {
    public void categoryInserted(ChartView chartView, int index);
    public void categoriesRemoved(ChartView chartView, int index, Sequence<ChartView.Category> categories);

    public void categoryKeyChanged(ChartView chartView, int index, String previousKey);
    public void categoryLabelChanged(ChartView chartView, int index, String previousLabel);
}
