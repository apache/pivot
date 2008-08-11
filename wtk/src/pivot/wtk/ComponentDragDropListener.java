package pivot.wtk;

public interface ComponentDragDropListener {
    public void dragHandlerChanged(Component component, DragHandler previousDragHandler);
    public void dropHandlerChanged(Component component, DropHandler previousDropHandler);
}
