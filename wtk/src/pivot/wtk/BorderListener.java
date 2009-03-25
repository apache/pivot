package pivot.wtk;

/**
 * Border listener interface.
 *
 * @author gbrown
 *
 */
public interface BorderListener {
    /**
     * Called when a border's title has changed.
     *
     * @param border
     * @param previousTitle
     */
    public void titleChanged(Border border, String previousTitle);

    /**
     * Called when a border's content component has changed.
     *
     * @param border
     * @param previousContent
     */
    public void contentChanged(Border border, Component previousContent);
}
