package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import pivot.charts.ChartView;
import pivot.charts.LineChartView;
import pivot.charts.ChartView.Element;
import pivot.collections.List;
import pivot.wtk.Component;

public class LineChartViewSkin extends ChartViewSkin {
    private boolean threeDimensional = false;

    @Override
    public void install(Component component) {
        validateComponentType(component, LineChartView.class);
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
        LineChartView chartView = (LineChartView)getComponent();

        String title = chartView.getTitle();
        String horizontalAxisLabel = chartView.getHorizontalAxisLabel();
        String verticalAxisLabel = chartView.getVerticalAxisLabel();
        boolean showLegend = chartView.getShowLegend();

        String seriesNameKey = chartView.getSeriesNameKey();
        List<?> chartData = chartView.getChartData();

        JFreeChart chart;
        ChartView.CategorySequence categories = chartView.getCategories();
        if (categories.getLength() > 0) {
            CategorySeriesDataset dataset = new CategorySeriesDataset(categories, seriesNameKey, chartData);

            if (threeDimensional) {
                chart = ChartFactory.createLineChart3D(title, horizontalAxisLabel, verticalAxisLabel,
                    dataset, PlotOrientation.VERTICAL, showLegend, false, false);
            } else {
                chart = ChartFactory.createLineChart(title, horizontalAxisLabel, verticalAxisLabel,
                    dataset, PlotOrientation.VERTICAL, showLegend, false, false);
            }
        } else {
            chart = ChartFactory.createXYLineChart(title, horizontalAxisLabel, verticalAxisLabel,
                new XYSeriesDataset(seriesNameKey, chartData),
                PlotOrientation.VERTICAL, showLegend, false, false);
        }

        return chart;
    }

    public boolean isThreeDimensional() {
        return threeDimensional;
    }

    public void setThreeDimensional(boolean threeDimensional) {
        this.threeDimensional = threeDimensional;
        repaintComponent();
    }
}
