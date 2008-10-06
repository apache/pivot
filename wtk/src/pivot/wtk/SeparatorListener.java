package pivot.wtk;

/**
 * Separator listener interface.
 *
 * @author gbrown
 */
public interface SeparatorListener {
    /**
     * Called when a separator's heading has changed.
     *
     * @param separator
     * @param previousHeading
     */
    public void headingChanged(Separator separator, String previousHeading);
}
