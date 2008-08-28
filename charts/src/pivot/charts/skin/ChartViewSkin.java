package pivot.charts.skin;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;

import pivot.charts.ChartView;
import pivot.charts.ChartViewCategoryListener;
import pivot.charts.ChartViewListener;
import pivot.charts.ChartViewSeriesListener;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Rectangle;
import pivot.wtk.skin.ComponentSkin;

public abstract class ChartViewSkin extends ComponentSkin
    implements ChartView.Skin,
        ChartViewListener, ChartViewCategoryListener, ChartViewSeriesListener {
    private BufferedImage bufferedImage = null;

    private JFreeChart chart = null;
    private ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();

    private static final int PREFERRED_WIDTH = 320;
    private static final int PREFERRED_HEIGHT = 240;

    @Override
    public void install(Component component) {
        validateComponentType(component, ChartView.class);
        super.install(component);

        // Add listeners
        ChartView chartView = (ChartView)component;
        chartView.getChartViewListeners().add(this);
        chartView.getChartViewCategoryListeners().add(this);
        chartView.getChartViewSeriesListeners().add(this);
    }

    @Override
    public void uninstall() {
        // Remove listeners
        ChartView chartView = (ChartView)getComponent();
        chartView.getChartViewListeners().remove(this);
        chartView.getChartViewCategoryListeners().remove(this);
        chartView.getChartViewSeriesListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        return PREFERRED_WIDTH;
    }


    public int getPreferredHeight(int width) {
        return PREFERRED_HEIGHT;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        if (bufferedImage == null
            || bufferedImage.getWidth() != width
            || bufferedImage.getHeight() != height) {
            chart = createChart();

            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImage.getGraphics();

            Rectangle area = new Rectangle(0, 0, width, height);
            chart.draw(bufferedImageGraphics, area, chartRenderingInfo);

            bufferedImageGraphics.dispose();
        }

        graphics.drawImage(bufferedImage, 0, 0, null);
    }

    @Override
    public void repaintComponent() {
        super.repaintComponent();
        bufferedImage = null;
    }

    protected abstract JFreeChart createChart();

    protected ChartEntity getChartEntityAt(int x, int y) {
        ChartEntity result = null;

        if (chartRenderingInfo != null) {
            EntityCollection entities = chartRenderingInfo.getEntityCollection();
            result = (entities != null) ? entities.getEntity(x, y) : null;
        }

        return result;
    }

    // Chart view events
    public void seriesNameKeyChanged(ChartView chartView, String previousSeriesNameKey) {
        repaintComponent();
    }

    public void titleChanged(ChartView chartView, String previousTitle) {
        repaintComponent();
    }

    public void chartDataChanged(ChartView chartView, List<?> previousChartData) {
        repaintComponent();
    }

    public void showLegendChanged(ChartView chartView) {
        repaintComponent();
    }

    // Chart view category events
    public void categoryInserted(ChartView chartView, int index) {
        repaintComponent();
    }

    public void categoriesRemoved(ChartView chartView, int index, Sequence<ChartView.Category> categories) {
        repaintComponent();
    }

    public void categoryKeyChanged(ChartView chartView, int index, String previousKey) {
        repaintComponent();
    }

    public void categoryLabelChanged(ChartView chartView, int index, String previousLabel) {
        repaintComponent();
    }

    // Chart view series events
    public void seriesInserted(ChartView chartView, int index) {
        repaintComponent();
    }

    public void seriesRemoved(ChartView chartView, int index, int count) {
        repaintComponent();
    }

    public void seriesUpdated(ChartView chartView, int index) {
        repaintComponent();
    }

    public void seriesSorted(ChartView chartView) {
        repaintComponent();
    }
}
