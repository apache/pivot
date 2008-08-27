package pivot.charts;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;

import pivot.util.ListenerList;
import pivot.wtk.Component;

public class ChartView extends Component {
    public interface Skin extends pivot.wtk.Skin {
        public ChartEntity getChartEntityAt(int x, int y);
    }

    private class ChartViewListenerList extends ListenerList<ChartViewListener>
        implements ChartViewListener {
        public void chartChanged(ChartView chartView, JFreeChart previousChart) {
            for (ChartViewListener listener : this) {
                listener.chartChanged(chartView, previousChart);
            }
        }
    }

    private JFreeChart chart = null;
    private ChartViewListenerList chartViewListeners = new ChartViewListenerList();

    public ChartView() {
        this(null);
    }

    public ChartView(JFreeChart chart) {
        setSkin(new ChartViewSkin());
        setChart(chart);
    }

    public JFreeChart getChart() {
        return chart;
    }

    public void setChart(JFreeChart chart) {
        JFreeChart previousChart = this.chart;

        if (previousChart != chart) {
            this.chart = chart;
            chartViewListeners.chartChanged(this, previousChart);
        }
    }

    public ChartEntity getChartEntityAt(int x, int y) {
        return ((Skin)getSkin()).getChartEntityAt(x, y);
    }

    public ListenerList<ChartViewListener> getChartViewListeners() {
        return chartViewListeners;
    }
}

