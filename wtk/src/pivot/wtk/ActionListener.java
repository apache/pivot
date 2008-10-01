package pivot.wtk;

/**
 * Action listener interface.
 *
 * @author gbrown
 */
public interface ActionListener {
    /**
     * Called when an action's enabled state has changed.
     *
     * @param action
     */
    public void enabledChanged(Action action);
}
