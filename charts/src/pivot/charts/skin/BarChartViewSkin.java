package pivot.charts.skin;

import pivot.charts.BarChartView;
import pivot.charts.ChartView.Element;
import pivot.wtk.Component;

public class BarChartViewSkin extends ChartViewSkin {
    @Override
    public void install(Component component) {
        validateComponentType(component, BarChartView.class);
        super.install(component);
    }

    public Element getElementAt(int x, int y) {
        // TODO Auto-generated method stub
        return null;
    }

}
