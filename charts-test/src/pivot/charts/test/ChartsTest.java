package pivot.charts.test;

import pivot.charts.ChartView;
import pivot.charts.PieChartView;
import pivot.collections.Dictionary;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Display;
import pivot.wtk.Frame;
import pivot.wtk.Mouse;
import pivot.wtkx.WTKXSerializer;

public class ChartsTest implements Application {
    private Frame frame = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        frame = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("charts_test.wtkx")));
        frame.setTitle("Charts Test");

        PieChartView pieChartView = (PieChartView)wtkxSerializer.getObjectByName("pieChartView");
        pieChartView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public void mouseDown(Component component, Mouse.Button button, int x, int y) {
            }

            public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            }

            public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                PieChartView pieChartView = (PieChartView)component;
                ChartView.Element element = pieChartView.getElementAt(x, y);
                if (element != null) {
                    Alert.alert("You clicked " + element, frame);
                }
            }
        });

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
