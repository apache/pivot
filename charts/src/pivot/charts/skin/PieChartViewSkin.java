package pivot.charts.skin;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.util.TableOrder;

import pivot.charts.PieChartView;
import pivot.charts.ChartView;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.wtk.Component;

public class PieChartViewSkin extends ChartViewSkin {
    private Map<String, Number> explodePercentages = new HashMap<String, Number>();

    private boolean threeDimensional = false;
    private boolean darkerSides = false;
    private double depthFactor = 0.10d;

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


        JFreeChart chart;
        if (threeDimensional) {
            if (chartData.getLength() > 1) {
                CategorySeriesDataset dataset = new CategorySeriesDataset(categories,
                    seriesNameKey, chartData);

                chart = ChartFactory.createMultiplePieChart3D(title, dataset, TableOrder.BY_ROW,
                    showLegend, false, false);
            } else {
                PieSeriesDataset dataset = new PieSeriesDataset(categories, chartData.get(0));
                chart = ChartFactory.createPieChart3D(title, dataset, showLegend, false, false);

                PiePlot3D plot = (PiePlot3D)chart.getPlot();
                plot.setDarkerSides(darkerSides);
                plot.setDepthFactor(depthFactor);
            }
        } else {
            if (chartData.getLength() > 1) {
                CategorySeriesDataset dataset = new CategorySeriesDataset(categories,
                    seriesNameKey, chartData);

                chart = ChartFactory.createMultiplePieChart(title, dataset, TableOrder.BY_ROW,
                    showLegend, false, false);
            } else {
                PieSeriesDataset dataset = new PieSeriesDataset(categories, chartData.get(0));
                chart = ChartFactory.createPieChart(title, dataset, showLegend, false, false);

                HashMap<String, String> categoryLabels = new HashMap<String, String>();
                for (int i = 0, n = categories.getLength(); i < n; i++) {
                    ChartView.Category category = categories.get(i);
                    categoryLabels.put(category.getKey(), category.getLabel());
                }

                PiePlot plot = (PiePlot)chart.getPlot();
                for (String categoryKey : explodePercentages) {
                    plot.setExplodePercent(categoryLabels.get(categoryKey),
                        explodePercentages.get(categoryKey).doubleValue());
                }
            }
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

    public Map<String, Number> getExplodePercentages() {
        return explodePercentages;
    }

    public void setExplodePercentages(Map<String, Number> explodePercentages) {
        this.explodePercentages = explodePercentages;
        repaintComponent();
    }

    public boolean getDarkerSides() {
        return darkerSides;
    }

    public void setDarkerSides(boolean darkerSides) {
        this.darkerSides = darkerSides;
        repaintComponent();
    }

    public double getDepthFactor() {
        return depthFactor;
    }

    public void setDepthFactor(double depthFactor) {
        this.depthFactor = depthFactor;
        repaintComponent();
    }
}
