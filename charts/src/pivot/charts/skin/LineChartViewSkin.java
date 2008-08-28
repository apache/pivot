package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import pivot.charts.ChartView;
import pivot.charts.LineChartView;
import pivot.charts.LineChartViewListener;
import pivot.charts.ChartView.Element;
import pivot.wtk.Component;

public class LineChartViewSkin extends ChartViewSkin
    implements LineChartViewListener {
    @Override
    public void install(Component component) {
        validateComponentType(component, LineChartView.class);
        super.install(component);

        LineChartView lineChartView = (LineChartView)component;
        lineChartView.getLineChartViewListeners().add(this);
    }

    @Override
    public void uninstall() {
        LineChartView lineChartView = (LineChartView)getComponent();
        lineChartView.getLineChartViewListeners().remove(this);

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
        LineChartView chartView = (LineChartView)getComponent();

        String title = chartView.getTitle();
        String categoryAxisLabel = chartView.getCategoryAxisLabel();
        String valueAxisLabel = chartView.getValueAxisLabel();
        SeriesDataset dataset = new SeriesDataset(chartView.getCategories(),
            chartView.getSeriesNameKey(), chartView.getChartData());
        boolean showLegend = chartView.getShowLegend();

        return ChartFactory.createLineChart(title, categoryAxisLabel, valueAxisLabel, dataset,
            PlotOrientation.VERTICAL, showLegend, false, false);
    }

    public void categoryAxisLabelChanged(LineChartView lineChartView, String previousCategoryAxisLabel) {
        repaintComponent();
    }

    public void valueAxisLabelChanged(LineChartView lineChartView, String previousValueAxisLabel) {
        repaintComponent();
    }
}
