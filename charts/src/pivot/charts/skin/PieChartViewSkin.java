package pivot.charts.skin;

import pivot.charts.PieChartView;
import pivot.charts.ChartView.Element;
import pivot.wtk.Component;

public class PieChartViewSkin extends ChartViewSkin {
    @Override
    public void install(Component component) {
        validateComponentType(component, PieChartView.class);
        super.install(component);
    }

    public Element getElementAt(int x, int y) {
        // TODO Auto-generated method stub
        return null;
    }

}
