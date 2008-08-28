package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.util.TableOrder;

import pivot.charts.PieChartView;
import pivot.charts.ChartView;
import pivot.wtk.Component;

public class PieChartViewSkin extends ChartViewSkin {
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
            String categoryKey = (String)pieSectionEntity.getSectionKey();
            int seriesIndex = pieSectionEntity.getPieIndex();

            element = new ChartView.Element(categoryKey, seriesIndex);
        }

        return element;
    }

    protected JFreeChart createChart() {
        PieChartView chartView = (PieChartView)getComponent();

        String title = chartView.getTitle();
        SeriesDataset dataset = new SeriesDataset(chartView.getCategories(),
            chartView.getSeriesNameKey(), chartView.getChartData());
        boolean showLegend = chartView.getShowLegend();

        return ChartFactory.createMultiplePieChart(title, dataset, TableOrder.BY_ROW,
            showLegend, false, false);
    }
}
