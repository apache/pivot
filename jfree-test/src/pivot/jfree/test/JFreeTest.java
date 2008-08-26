package pivot.jfree.test;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.data.general.DefaultPieDataset;

import pivot.collections.Dictionary;
import pivot.jfree.ChartView;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.Mouse;

public class JFreeTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Category 1", 43.2);
        dataset.setValue("Category 2", 27.9);
        dataset.setValue("Category 3", 79.5);

        JFreeChart chart = ChartFactory.createPieChart("Sample Pie Chart",
            dataset, true, true, false);

        ChartView chartView = new ChartView(chart);
        chartView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public void mouseDown(Component component, Mouse.Button button, int x, int y) {
            }

            public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            }

            public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                ChartView chartView = (ChartView)component;
                ChartEntity chartEntity = chartView.getChartEntityAt(x, y);
                Alert.alert("You clicked " + chartEntity.getToolTipText(), frame);
            }
        });

        frame = new Frame(chartView);
        frame.setTitle("JFreeChart Test");
        frame.setPreferredSize(320, 240);
        frame.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        frame.close();
        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
