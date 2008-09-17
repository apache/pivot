package pivot.wtk;

/**
 * <p>Action listener interface.</p>
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
