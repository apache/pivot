package pivot.jfree;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Rectangle;
import pivot.wtk.skin.ComponentSkin;

public class ChartViewSkin extends ComponentSkin
    implements ChartView.Skin, ChartViewListener, ChartChangeListener {
    private BufferedImage bufferedImage = null;
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

        JFreeChart chart = chartView.getChart();
        if (chart != null) {
            chart.addChangeListener(this);
        }
    }

    @Override
    public void uninstall() {
        ChartView chartView = (ChartView)getComponent();
        chartView.getChartViewListeners().remove(this);

        JFreeChart chart = chartView.getChart();
        if (chart != null) {
            chart.removeChangeListener(this);
        }

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
        ChartView chartView = (ChartView)getComponent();
        JFreeChart chart = chartView.getChart();

        if (chart != null) {
            int width = getWidth();
            int height = getHeight();

            if (bufferedImage == null
                || bufferedImage.getWidth() != width
                || bufferedImage.getHeight() != height) {
                bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImage.getGraphics();

                Rectangle area = new Rectangle(0, 0, width, height);
                chart.draw(bufferedImageGraphics, area, chartRenderingInfo);

                bufferedImageGraphics.dispose();
            }

            graphics.drawImage(bufferedImage, 0, 0, null);
        }
    }

    public ChartEntity getChartEntityAt(int x, int y) {
        ChartEntity result = null;

        // TODO Update this when we add scaling
        if (chartRenderingInfo != null) {
            EntityCollection entities = chartRenderingInfo.getEntityCollection();
            result = (entities != null) ? entities.getEntity(x, y) : null;
        }

        return result;
    }

    public void chartChanged(ChartView chartView, JFreeChart previousChart) {
        // Add/remove listeners
        if (previousChart != null) {
            previousChart.removeChangeListener(this);
        }

        JFreeChart chart = chartView.getChart();
        if (chart != null) {
            chart.addChangeListener(this);
        }

        invalidateComponent();
    }

    public void chartChanged(ChartChangeEvent event) {
        bufferedImage = null;
        repaintComponent();
    }
}
