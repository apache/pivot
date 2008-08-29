package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.util.TableOrder;

import pivot.charts.PieChartView;
import pivot.charts.ChartView;
import pivot.collections.List;
import pivot.wtk.Component;

public class PieChartViewSkin extends ChartViewSkin {
    private boolean threeDimensional = false;

    @Override
    public void install(Component component) {
        validateComponentType(component, PieChartView.class);
        super.install(component);
    }

    public ChartView.Element getElementAt(int x, int y) {
        ChartView.Element element = null;

        ChartEntity chartEntity = getChartEntityAt(x, y);
        if (chartEntity instanceof PieSectionEntity) {
            PieSectionEntity pieSectionEntity = (PieSectionEntity)chartEntity;
            int sectionIndex = pieSectionEntity.getSectionIndex();
            int seriesIndex = pieSectionEntity.getPieIndex();

            element = new ChartView.Element(seriesIndex, sectionIndex);
        }

        return element;
    }

    protected JFreeChart createChart() {
        PieChartView chartView = (PieChartView)getComponent();

        String title = chartView.getTitle();
        boolean showLegend = chartView.getShowLegend();

        ChartView.CategorySequence categories = chartView.getCategories();
        String seriesNameKey = chartView.getSeriesNameKey();
        List<?> chartData = chartView.getChartData();

        CategorySeriesDataset dataset = new CategorySeriesDataset(categories,
            seriesNameKey, chartData);

        JFreeChart chart;
        if (threeDimensional) {
            chart = ChartFactory.createMultiplePieChart3D(title, dataset, TableOrder.BY_ROW,
                showLegend, false, false);
        } else {
            chart = ChartFactory.createMultiplePieChart(title, dataset, TableOrder.BY_ROW,
                showLegend, false, false);
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
