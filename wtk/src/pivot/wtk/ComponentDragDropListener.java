package pivot.wtk;

/**
 * Component drag/drop listener interface.
 *
 * @author gbrown
 */
public interface ComponentDragDropListener {
    /**
     * Called when a component's drag source has changed.
     *
     * @param component
     * @param previousDragSource
     */
    public void dragSourceChanged(Component component, DragSource previousDragSource);

    /**
     * Called when a component's drop target has changed.
     *
     * @param component
     * @param previousDropTarget
     */
    public void dropTargetChanged(Component component, DropTarget previousDropTarget);
}
