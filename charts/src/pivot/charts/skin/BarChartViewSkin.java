package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import pivot.charts.BarChartView;
import pivot.charts.ChartView;
import pivot.charts.ChartView.Element;
import pivot.collections.List;
import pivot.wtk.Component;

public class BarChartViewSkin extends ChartViewSkin {
    @Override
    public void install(Component component) {
        validateComponentType(component, BarChartView.class);
        super.install(component);
    }

    public Element getElementAt(int x, int y) {
        ChartView.Element element = null;

        ChartEntity chartEntity = getChartEntityAt(x, y);
        if (chartEntity instanceof CategoryItemEntity) {
            CategoryItemEntity categoryItemEntity = (CategoryItemEntity)chartEntity;
            CategoryDataset dataset = categoryItemEntity.getDataset();

            String columnKey = (String)categoryItemEntity.getColumnKey();
            int columnIndex = dataset.getColumnIndex(columnKey);

            String rowKey = (String)categoryItemEntity.getRowKey();
            int rowIndex = dataset.getRowIndex(rowKey);

            element = new ChartView.Element(rowIndex, columnIndex);
        } else if (chartEntity instanceof XYItemEntity) {
            XYItemEntity xyItemEntity = (XYItemEntity)chartEntity;
            element = new ChartView.Element(xyItemEntity.getSeriesIndex(),
                xyItemEntity.getItem());
        }

        return element;
    }

    protected JFreeChart createChart() {
        BarChartView chartView = (BarChartView)getComponent();

        String title = chartView.getTitle();
        String horizontalAxisLabel = chartView.getHorizontalAxisLabel();
        String verticalAxisLabel = chartView.getVerticalAxisLabel();
        boolean showLegend = chartView.getShowLegend();

        String seriesNameKey = chartView.getSeriesNameKey();
        List<?> chartData = chartView.getChartData();

        // TODO Make plot orientation a style property
        JFreeChart chart;
        ChartView.CategorySequence categories = chartView.getCategories();
        if (categories.getLength() > 0) {
            chart = ChartFactory.createBarChart(title, horizontalAxisLabel, verticalAxisLabel,
                new CategorySeriesDataset(categories, seriesNameKey, chartData),
                PlotOrientation.VERTICAL, showLegend, false, false);
        } else {
            // TODO Make the dateAxis argument a style property
            chart = ChartFactory.createXYBarChart(title, horizontalAxisLabel, false, verticalAxisLabel,
                new IntervalSeriesDataset(seriesNameKey, chartData),
                PlotOrientation.VERTICAL, showLegend, false, false);
        }

        return chart;
    }
}
