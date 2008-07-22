package pivot.wtk;

public interface TablePaneAttributeListener {
    public void rowSpanChanged(TablePane tablePane, Component component,
        int previousRowSpan);
    public void columnSpanChanged(TablePane tablePane, Component component,
        int previousColumnSpan);
}
