package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import pivot.charts.BarChartView;
import pivot.charts.BarChartViewListener;
import pivot.charts.ChartView;
import pivot.charts.ChartView.Element;
import pivot.wtk.Component;

public class BarChartViewSkin extends ChartViewSkin
    implements BarChartViewListener {
    @Override
    public void install(Component component) {
        validateComponentType(component, BarChartView.class);
        super.install(component);

        BarChartView barChartView = (BarChartView)component;
        barChartView.getBarChartViewListeners().add(this);
    }

    @Override
    public void uninstall() {
        BarChartView barChartView = (BarChartView)getComponent();
        barChartView.getBarChartViewListeners().remove(this);

        super.uninstall();
    }

    public Element getElementAt(int x, int y) {
        ChartView.Element element = null;

        ChartEntity chartEntity = getChartEntityAt(x, y);
        if (chartEntity instanceof CategoryItemEntity) {
            CategoryItemEntity categoryItemEntity = (CategoryItemEntity)chartEntity;
            String categoryKey = (String)categoryItemEntity.getColumnKey();

            CategoryDataset dataset = categoryItemEntity.getDataset();
            int seriesIndex = dataset.getRowIndex(categoryItemEntity.getRowKey());

            element = new ChartView.Element(categoryKey, seriesIndex);
        }

        return element;
    }

    protected JFreeChart createChart() {
        BarChartView chartView = (BarChartView)getComponent();

        String title = chartView.getTitle();
        String categoryAxisLabel = chartView.getCategoryAxisLabel();
        String valueAxisLabel = chartView.getValueAxisLabel();
        SeriesDataset dataset = new SeriesDataset(chartView.getCategories(),
            chartView.getSeriesNameKey(), chartView.getChartData());
        boolean showLegend = chartView.getShowLegend();

        return ChartFactory.createBarChart(title, categoryAxisLabel, valueAxisLabel,
            dataset, PlotOrientation.HORIZONTAL, showLegend, false, false);
    }

    public void categoryAxisLabelChanged(BarChartView barChartView, String previousCategoryAxisLabel) {
        repaintComponent();
    }

    public void valueAxisLabelChanged(BarChartView barChartView, String previousValueAxisLabel) {
        repaintComponent();
    }
}
