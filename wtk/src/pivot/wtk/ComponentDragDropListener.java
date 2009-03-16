package pivot.wtk;

/**
 * Component drag/drop listener interface.
 *
 * @author gbrown
 */
public interface ComponentDragDropListener {
    /**
     * Called when a component's drag handler has changed.
     *
     * @param component
     * @param previousDragHandler
     */
    public void dragHandlerChanged(Component component, DragHandler previousDragHandler);

    /**
     * Called when a component's drop handler has changed.
     *
     * @param component
     * @param previousDropHandler
     */
    public void dropHandlerChanged(Component component, DropHandler previousDropHandler);
}
