package pivot.charts.skin;

import pivot.charts.LineChartView;
import pivot.charts.ChartView.Element;
import pivot.wtk.Component;

public class LineChartViewSkin extends ChartViewSkin {
    @Override
    public void install(Component component) {
        validateComponentType(component, LineChartView.class);
        super.install(component);
    }

    public Element getElementAt(int x, int y) {
        // TODO Auto-generated method stub
        return null;
    }

}
