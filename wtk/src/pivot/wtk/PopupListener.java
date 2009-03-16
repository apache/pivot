package pivot.wtk;

/**
 * Popup listener interface.
 *
 * @author gbrown
 */
public interface PopupListener {
    /**
     * Called when a popup's affiliate has changed.
     *
     * @param popup
     * @param previousAffiliate
     */
    public void affiliateChanged(Popup popup, Component previousAffiliate);
}
